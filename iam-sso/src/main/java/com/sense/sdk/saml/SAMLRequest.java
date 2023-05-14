package com.sense.sdk.saml;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameIDPolicy;
import org.w3c.dom.Document;

/**
 * 请求报文
 * 
 * @author LiangRiLu
 * @Time 2019年12月14日下午1:45:41
 */
public class SAMLRequest extends SAML {

	private static SecureRandomIdentifierGenerator generator;

	/**
	 * Any use of this class assures that OpenSAML is bootstrapped. Also
	 * initializes an ID generator.
	 */
	static {
		try {
			DefaultBootstrap.bootstrap();
			generator = new SecureRandomIdentifierGenerator();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 创建authnrequest xml 字符串
	 * 
	 * @param idpSsoUrl
	 * @param acsUrl
	 * @param spEntityId
	 * @return
	 * @throws Exception
	 */
	public String createRequestXmlString(String idpSsoUrl, String acsUrl, String spEntityId, String nameId,
			String timeZone) throws Exception {
		AuthnRequest authnRequest = createRequest(idpSsoUrl, acsUrl, spEntityId, nameId, timeZone);
		Document document = asDOMDocument(authnRequest);
		DOMSource source = new DOMSource(document);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer former = tf.newTransformer();
		former.setOutputProperty(OutputKeys.STANDALONE, "yes");
		StringWriter sw = new StringWriter();
		StreamResult sr = new StreamResult(sw);
		former.transform(source, sr);
		String result = sw.toString();
		return result;
	}

	/**
	 * 创建AutheRequest对象
	 * 
	 * @param idpSsoUrl
	 * @param acsUrl
	 * @param spEntityId
	 * @return
	 */
	public AuthnRequest createRequest(String idpSsoUrl, String acsUrl, String spEntityId, String nameId,
			String timeZone) {
		DateTime nows = new DateTime();
		nows = TimeUtils.transformTimeZone(nows, timeZone);
		AuthnRequest authnRequest = create(AuthnRequest.class, AuthnRequest.DEFAULT_ELEMENT_NAME);
		authnRequest.setIssueInstant(nows);
		authnRequest.setDestination(idpSsoUrl);
		authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
		authnRequest.setID(generator.generateIdentifier());
		authnRequest.setAssertionConsumerServiceURL(acsUrl);

		Issuer issuer = create(Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
		issuer.setValue(spEntityId);
		authnRequest.setIssuer(issuer);

		NameIDPolicy nameIDPolicy = create(NameIDPolicy.class, NameIDPolicy.DEFAULT_ELEMENT_NAME);
		nameIDPolicy.setAllowCreate(true);
		nameIDPolicy.setFormat(nameId);
		authnRequest.setNameIDPolicy(nameIDPolicy);
		return authnRequest;
	}

}
