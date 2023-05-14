package com.sense.sdk.saml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.joda.time.DateTime;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.ArtifactResolutionService;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml2.metadata.NameIDFormat;
import org.opensaml.saml2.metadata.SingleLogoutService;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml2.metadata.impl.ArtifactResolutionServiceBuilder;
import org.opensaml.saml2.metadata.impl.EntityDescriptorBuilder;
import org.opensaml.saml2.metadata.impl.IDPSSODescriptorBuilder;
import org.opensaml.saml2.metadata.impl.KeyDescriptorBuilder;
import org.opensaml.saml2.metadata.impl.NameIDFormatBuilder;
import org.opensaml.saml2.metadata.impl.SingleLogoutServiceBuilder;
import org.opensaml.saml2.metadata.impl.SingleSignOnServiceBuilder;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.X509Data;
import org.opensaml.xml.signature.X509IssuerName;
import org.opensaml.xml.signature.X509IssuerSerial;
import org.opensaml.xml.signature.X509SerialNumber;
import org.opensaml.xml.signature.X509SubjectName;
import org.opensaml.xml.signature.impl.KeyInfoBuilder;
import org.opensaml.xml.signature.impl.X509CertificateBuilder;
import org.opensaml.xml.signature.impl.X509DataBuilder;
import org.opensaml.xml.signature.impl.X509IssuerNameBuilder;
import org.opensaml.xml.signature.impl.X509IssuerSerialBuilder;
import org.opensaml.xml.signature.impl.X509SerialNumberBuilder;
import org.opensaml.xml.signature.impl.X509SubjectNameBuilder;
import org.w3c.dom.Document;

import com.sense.iam.model.sso.Saml;
import com.sense.sdk.saml.security.KeyStoreUtil;

import cn.hutool.core.codec.Base64;

public class MetadataFactory extends SAML {

	protected static int TEN_YEAR_LATER_TIMES = 10;

	public String createXML(Saml samlConfig) {
		try {
			EntityDescriptorBuilder entityDescriptorBuilder = new EntityDescriptorBuilder();
			EntityDescriptor entityDescriptor = entityDescriptorBuilder.buildObject();
			entityDescriptor.setEntityID(samlConfig.getIdpIssuer());
			DateTime validUnitl = new DateTime().plusYears(TEN_YEAR_LATER_TIMES);
			entityDescriptor.setValidUntil(validUnitl);

			IDPSSODescriptorBuilder iDPSSODescriptorBuilder = new IDPSSODescriptorBuilder();
			IDPSSODescriptor iDPSSODescriptor = iDPSSODescriptorBuilder.buildObject();
			iDPSSODescriptor.setWantAuthnRequestsSigned(false);
			iDPSSODescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);
			entityDescriptor.getRoleDescriptors().add(iDPSSODescriptor);

			NameIDFormatBuilder nameIDFormatBuilder = new NameIDFormatBuilder();
			NameIDFormat nameIDFormat = nameIDFormatBuilder.buildObject();
			nameIDFormat.setFormat(samlConfig.getNameId());
			iDPSSODescriptor.getNameIDFormats().add(nameIDFormat);

			// ArtifactResolutionService
			ArtifactResolutionService artifactResolutionService = new ArtifactResolutionServiceBuilder().buildObject();
			artifactResolutionService.setIndex(0);
			artifactResolutionService.setIsDefault(true);
			artifactResolutionService.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
			artifactResolutionService.setLocation(samlConfig.getLoginUrl());
			iDPSSODescriptor.getArtifactResolutionServices().add(artifactResolutionService);

			// SingleLogoutService
			SingleLogoutServiceBuilder singleLogoutServiceBuilder = new SingleLogoutServiceBuilder();
			SingleLogoutService singleLogoutService = singleLogoutServiceBuilder.buildObject();
			singleLogoutService.setLocation(samlConfig.getLogoutUrl());
			singleLogoutService.setResponseLocation(samlConfig.getLogoutUrl());
			iDPSSODescriptor.getSingleLogoutServices().add(singleLogoutService);

			// HTTP-POST
			SingleSignOnServiceBuilder postSingleSignOnServiceBuilder = new SingleSignOnServiceBuilder();
			SingleSignOnService postSingleSignOnService = postSingleSignOnServiceBuilder.buildObject();
			postSingleSignOnService.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
			postSingleSignOnService.setLocation(samlConfig.getLoginUrl());
			iDPSSODescriptor.getSingleSignOnServices().add(postSingleSignOnService);

			// HTTP-Redirect
			SingleSignOnServiceBuilder redirectSingleSignOnServiceBuilder = new SingleSignOnServiceBuilder();
			SingleSignOnService redirectSingleSignOnService = redirectSingleSignOnServiceBuilder.buildObject();
			redirectSingleSignOnService.setBinding(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
			redirectSingleSignOnService.setLocation(samlConfig.getLoginUrl());
			iDPSSODescriptor.getSingleSignOnServices().add(redirectSingleSignOnService);

			// HTTP-Artifact
			SingleSignOnServiceBuilder artifactSingleSignOnServiceBuilder = new SingleSignOnServiceBuilder();
			SingleSignOnService artifactSingleSignOnService = artifactSingleSignOnServiceBuilder.buildObject();
			artifactSingleSignOnService.setBinding(SAMLConstants.SAML2_ARTIFACT_BINDING_URI);
			artifactSingleSignOnService.setLocation(samlConfig.getLoginUrl());
			iDPSSODescriptor.getSingleSignOnServices().add(artifactSingleSignOnService);


			// signing key
			KeyDescriptorBuilder signingKeyDescriptorBuilder = new KeyDescriptorBuilder();
			KeyDescriptor signingKeyDescriptor = signingKeyDescriptorBuilder.buildObject();
			signingKeyDescriptor.setUse(UsageType.SIGNING);

			iDPSSODescriptor.getKeyDescriptors().add(signingKeyDescriptor);

			KeyStore signingKeyStore = new KeyStoreUtil().getKeyStore(samlConfig.getKeyStorePath(), samlConfig.getKeyPwd());
			X509Certificate signingCert = (X509Certificate) signingKeyStore.getCertificate(samlConfig.getKeyAlias());
			String singningKeyStr = Base64.encode(signingCert.getEncoded());

			KeyInfoBuilder signingKeyInfoBuilder = new KeyInfoBuilder();
			KeyInfo signingKeyInfo = signingKeyInfoBuilder.buildObject();

			signingKeyDescriptor.setKeyInfo(signingKeyInfo);

			X509DataBuilder signingX509DataBuilder = new X509DataBuilder();
			X509Data signingX509Data = signingX509DataBuilder.buildObject();

			signingKeyInfo.getX509Datas().add(signingX509Data);

			X509CertificateBuilder signingX509CertificateBuilder = new X509CertificateBuilder();
			org.opensaml.xml.signature.X509Certificate signingX509Certificate = signingX509CertificateBuilder
					.buildObject();
			signingX509Certificate.setValue(singningKeyStr);
			signingX509Data.getX509Certificates().add(signingX509Certificate);

			// signing issuerSerial
			X509IssuerSerial signingX509IssuerSerial = new X509IssuerSerialBuilder().buildObject();
			X509IssuerName signingX509IssuerName = new X509IssuerNameBuilder().buildObject();
			signingX509IssuerName.setValue(signingCert.getIssuerDN().getName());
			X509SerialNumber signingX509SerialNumber = new X509SerialNumberBuilder().buildObject();
			signingX509SerialNumber.setValue(signingCert.getSerialNumber());
			signingX509IssuerSerial.setX509IssuerName(signingX509IssuerName);
			signingX509IssuerSerial.setX509SerialNumber(signingX509SerialNumber);
			signingX509Data.getX509IssuerSerials().add(signingX509IssuerSerial);

			// signing subName
			X509SubjectName signingSubjectName = new X509SubjectNameBuilder().buildObject();
			signingSubjectName.setValue(signingCert.getSubjectDN().getName());
			signingX509Data.getX509SubjectNames().add(signingSubjectName);

			// encryption key
			KeyDescriptorBuilder encryptionKeyDescriptorBuilder = new KeyDescriptorBuilder();
			KeyDescriptor encryptionKeyDescriptor = encryptionKeyDescriptorBuilder.buildObject();
			encryptionKeyDescriptor.setUse(UsageType.ENCRYPTION);

			iDPSSODescriptor.getKeyDescriptors().add(encryptionKeyDescriptor);

			KeyStore encryptionKeyStore = new KeyStoreUtil().getKeyStore(samlConfig.getKeyStorePath(),
					samlConfig.getKeyPwd());

			X509Certificate encryptionCert = (X509Certificate) encryptionKeyStore
					.getCertificate(samlConfig.getKeyAlias());

			String encryptionKeyStr = Base64.encode(encryptionCert.getEncoded());

			KeyInfoBuilder encryptionKeyInfoBuilder = new KeyInfoBuilder();
			KeyInfo encryptionKeyInfo = encryptionKeyInfoBuilder.buildObject();

			encryptionKeyDescriptor.setKeyInfo(encryptionKeyInfo);

			X509DataBuilder encryptionX509DataBuilder = new X509DataBuilder();
			X509Data encryptionX509Data = encryptionX509DataBuilder.buildObject();

			encryptionKeyInfo.getX509Datas().add(encryptionX509Data);

			X509CertificateBuilder encryptionX509CertificateBuilder = new X509CertificateBuilder();
			org.opensaml.xml.signature.X509Certificate encryptionX509Certificate = encryptionX509CertificateBuilder
					.buildObject();
			encryptionX509Certificate.setValue(encryptionKeyStr);
			encryptionX509Data.getX509Certificates().add(encryptionX509Certificate);

			// encryption issuerSerial
			X509IssuerSerial encryptionX509IssuerSerial = new X509IssuerSerialBuilder().buildObject();
			X509IssuerName encryptionX509IssuerName = new X509IssuerNameBuilder().buildObject();
			encryptionX509IssuerName.setValue(encryptionCert.getIssuerDN().getName());
			X509SerialNumber encryptionX509SerialNumber = new X509SerialNumberBuilder().buildObject();
			encryptionX509SerialNumber.setValue(encryptionCert.getSerialNumber());
			encryptionX509IssuerSerial.setX509IssuerName(encryptionX509IssuerName);
			encryptionX509IssuerSerial.setX509SerialNumber(encryptionX509SerialNumber);
			encryptionX509Data.getX509IssuerSerials().add(encryptionX509IssuerSerial);

			// encryption subName
			X509SubjectName encryptionSubjectName = new X509SubjectNameBuilder().buildObject();
			encryptionSubjectName.setValue(encryptionCert.getSubjectDN().getName());
			encryptionX509Data.getX509SubjectNames().add(encryptionSubjectName);

			// document
			Document document = asDOMDocument(entityDescriptor);

			DOMSource source = new DOMSource(document);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer former = tf.newTransformer();
			StringWriter sw = new StringWriter();
			StreamResult sr = new StreamResult(sw);
			former.transform(source, sr);
			String result = sw.toString();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) throws CertificateException, FileNotFoundException {
		MetadataFactory metadata = new MetadataFactory();
		Saml saml = new Saml();
		saml.setKeyStorePath("D:\\sense.crt");
		saml.setIdpIssuer("sense");
		metadata.createXML(saml);
	}
}
