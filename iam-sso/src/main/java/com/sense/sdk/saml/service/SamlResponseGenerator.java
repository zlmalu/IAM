package com.sense.sdk.saml.service;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


import com.sense.iam.model.sso.SamlConfig;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Subject;
import org.w3c.dom.Document;

import com.sense.core.util.StringUtils;
import com.sense.iam.model.sso.Saml;
import com.sense.sdk.saml.SAML;
import com.sense.sdk.saml.SAMLAssertion;
import com.sense.sdk.saml.SAMLSignature;
import com.sense.sdk.saml.pojo.AuthnRequestField;

/**
 * 生成SAMLResponse字符串
 *
 * @author LiangRiLu
 */
public class SamlResponseGenerator {

	private String spEntityId;
	private String acsUrl;
	private String inResponseTo;
	private String FORMAT = "bearer";

	private static SecureRandomIdentifierGenerator generator;

	static {
		try {
			DefaultBootstrap.bootstrap();
			generator = new SecureRandomIdentifierGenerator();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void init(AuthnRequestField requestField, String spEntityId, String acsUrl) {
		if (requestField == null) {
			this.spEntityId = spEntityId;
			this.acsUrl = acsUrl;
		} else {
			this.spEntityId = requestField.getSpIssuer();
			this.acsUrl = requestField.getAssertionConsumerServiceUrl();
			this.inResponseTo = requestField.getRequestId();
		}
		if (StringUtils.isTrimEmpty(this.acsUrl)) {
			this.acsUrl = acsUrl;
		}
	}

	/**
	 * 生成response字符串
	 */
	public String generateSamlResponse(String id, Saml samlConfig, AuthnRequestField requestField) throws Exception {
		// 初始化配置
		init(requestField, samlConfig.getSpIssuer(), samlConfig.getDefaultUrl());
		// 获取SAML
		SAML saml = new SAML(samlConfig.getIdpIssuer());
		// 创建Subject
		Subject subject = saml.createSubject(id, requestField, samlConfig, FORMAT, this.acsUrl);

		// 创建断言Assertion
		String assertionId = generator.generateIdentifier();
		SAMLAssertion samlAssertion = new SAMLAssertion();

		// TODO 需要处理成可扩展的
		Map<String, String> attributes = new HashMap<String, String>();
		if(samlConfig.getIsSignResponse()==1){
			List<SamlConfig> samlConfigList = samlConfig.getSamlConfigList();
			if(samlConfigList!=null&&samlConfigList.size()>0){
				for(SamlConfig sConfig : samlConfigList){
					attributes.put(sConfig.getResponseKey(),sConfig.getResponseValue());
				}
			}
		}
		attributes.put("username", id);
		attributes.put("id",id);

		Assertion assertion = samlAssertion.createStockAuthnAssertion(attributes, samlConfig, assertionId, spEntityId);
		assertion.setSubject(subject);

		// 创建response
		Response response = saml.createResponse(requestField, assertion, inResponseTo, samlConfig);
		// 签名
		SAMLSignature samlSignature = new SAMLSignature(samlConfig);
		Document document = saml.asDOMDocument(response);

		boolean isSignResponse = true;
		boolean isSignAssert = true;

		if (samlConfig.getIsSignResponse() != null && samlConfig.getIsSignResponse() == 2) {
			isSignResponse = false;
		}

		if (samlConfig.getIsSignAssert() != null && samlConfig.getIsSignAssert() == 2) {
			isSignAssert = false;
		}

		samlSignature.signSAMLObject(document, response.getID(), assertionId, samlConfig.getSignature(), isSignResponse,
				isSignAssert);

		DOMSource source = new DOMSource(document);
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
		StringWriter stringWriter = new StringWriter();
		StreamResult streamResult = new StreamResult(stringWriter);
		transformer.transform(source, streamResult);
		return stringWriter.toString();
	}

}
