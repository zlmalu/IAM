package com.sense.sdk.saml;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.SignatureConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sense.core.util.StringUtils;
import com.sense.iam.model.sso.Saml;
import com.sense.sdk.saml.security.KeyStoreUtil;

import cn.hutool.core.codec.Base64;

/**
 * 加密部分
 *
 * @author LiangRiLu
 * @Time 2019年12月14日下午1:44:39
 */
public class SAMLSignature {

	private XMLSignatureFactory factory;
	private KeyStore keyStore;
	private KeyPair keyPair;
	private KeyInfo keyInfo;
	private BasicX509Credential credential;

	/**
	 * Loads a keystore and builds a stock key-info structure for use by base
	 * classes.
	 */
	public SAMLSignature(Saml samlConfig) {
		try {
			factory = XMLSignatureFactory.getInstance();

			keyStore = new KeyStoreUtil().getKeyStore(samlConfig.getKeyStorePath(), samlConfig.getKeyPwd());
			KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(samlConfig.getKeyAlias(),
					new KeyStore.PasswordProtection(samlConfig.getKeyPwd().toCharArray()));
			keyPair = new KeyPair(entry.getCertificate().getPublicKey(), entry.getPrivateKey());

			KeyInfoFactory kFactory = factory.getKeyInfoFactory();
			keyInfo = kFactory.newKeyInfo(
					Collections.singletonList(kFactory.newX509Data(Collections.singletonList(entry.getCertificate()))));
			X509Certificate certificate = (X509Certificate) entry.getCertificate();
			credential = new BasicX509Credential();
			credential.setEntityCertificate(certificate);
			credential.setPrivateKey(entry.getPrivateKey());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void signSAMLObject(Document doc, String responseReferenceId, String assertReferenceId, String signatureAlgo,
			boolean isSignResponse, boolean isSignAssert)
			throws GeneralSecurityException, XMLSignatureException, MarshalException {
		// 默认为RSA_SHA1
		if (StringUtils.isTrimEmpty(signatureAlgo)) {
			signatureAlgo = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1;
		}

		// 签名断言
		if (isSignAssert) {
			Node assertSignNode = doc.getElementsByTagName("saml:Assertion").item(0);
			List assertTransforms = new ArrayList(2);
			assertTransforms.add(factory.newTransform(SignatureConstants.TRANSFORM_ENVELOPED_SIGNATURE,
					(TransformParameterSpec) null));
			assertTransforms.add(factory.newTransform(SignatureConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS,
					(TransformParameterSpec) null));
			Reference assertRef = factory.newReference("#" + assertReferenceId,
					factory.newDigestMethod(SignatureConstants.ALGO_ID_DIGEST_SHA1, null), assertTransforms, null,
					null);
			SignedInfo assertSignedInfo = factory.newSignedInfo(
					factory.newCanonicalizationMethod(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS,
							(C14NMethodParameterSpec) null),
					factory.newSignatureMethod(signatureAlgo, null), Collections.singletonList(assertRef));
			XMLSignature assertSignature = factory.newXMLSignature(assertSignedInfo, keyInfo);
			DOMSignContext assertSignContext = new DOMSignContext(keyPair.getPrivate(), assertSignNode);
			assertSignContext.setDefaultNamespacePrefix("ds");
			assertSignature.sign(assertSignContext);
			Node assertSignatureElement = assertSignNode.getLastChild();
			boolean assertFoundIssuer = false;
			Node assertElementAfterIssuer = null;
			NodeList assertChildren = assertSignNode.getChildNodes();
			for (int c = 0; c < assertChildren.getLength(); ++c) {
				Node child = assertChildren.item(c);
				if (assertFoundIssuer) {
					assertElementAfterIssuer = child;
					break;
				}
				if (child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals("Issuer"))
					assertFoundIssuer = true;
			}
			// Place after the Issuer, or as first element if no Issuer:
			if (!assertFoundIssuer || assertElementAfterIssuer != null) {
				assertSignNode.removeChild(assertSignatureElement);
				assertSignNode.insertBefore(assertSignatureElement,
						assertFoundIssuer ? assertElementAfterIssuer : assertSignNode.getFirstChild());
			}
		}
		if (isSignResponse) {
			// 签名响应
			Node responseSignNode = doc.getFirstChild();
			List responseTransforms = new ArrayList(2);
			responseTransforms.add(factory.newTransform(SignatureConstants.TRANSFORM_ENVELOPED_SIGNATURE,
					(TransformParameterSpec) null));
			responseTransforms.add(factory.newTransform(SignatureConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS,
					(TransformParameterSpec) null));
			Reference responseRef = factory.newReference("#" + responseReferenceId,
					factory.newDigestMethod(SignatureConstants.ALGO_ID_DIGEST_SHA1, null), responseTransforms, null,
					null);
			SignedInfo responseSignedInfo = factory.newSignedInfo(
					factory.newCanonicalizationMethod(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS,
							(C14NMethodParameterSpec) null),
					factory.newSignatureMethod(signatureAlgo, null), Collections.singletonList(responseRef));
			XMLSignature responseSignature = factory.newXMLSignature(responseSignedInfo, keyInfo);
			DOMSignContext responseSignContext = new DOMSignContext(keyPair.getPrivate(), responseSignNode);
			responseSignContext.setDefaultNamespacePrefix("ds");
			responseSignature.sign(responseSignContext);
			Node responseSignatureElement = responseSignNode.getLastChild();
			boolean responseFoundIssuer = false;
			Node responseElementAfterIssuer = null;
			NodeList responseChildren = responseSignNode.getChildNodes();
			for (int c = 0; c < responseChildren.getLength(); ++c) {
				Node child = responseChildren.item(c);
				if (responseFoundIssuer) {
					responseElementAfterIssuer = child;
					break;
				}
				if (child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals("Issuer"))
					responseFoundIssuer = true;
			}
			// Place after the Issuer, or as first element if no Issuer:
			if (!responseFoundIssuer || responseElementAfterIssuer != null) {
				responseSignNode.removeChild(responseSignatureElement);
				responseSignNode.insertBefore(responseSignatureElement,
						responseFoundIssuer ? responseElementAfterIssuer : responseSignNode.getFirstChild());
			}
		}
	}

	public static void main(String[] args) throws KeyStoreException, CertificateEncodingException {
		KeyStore keyStore = new KeyStoreUtil().getKeyStore("D:\\20191128_1516_SHA1_证书\\20191128_sense.keystore", "passw0rd");
		Certificate cert = keyStore.getCertificate("sense");
		String keyStr = Base64.encode(cert.getEncoded());
		System.out.println(keyStr);
	}

}
