package com.sense.sdk.saml;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;

import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.schema.XSAny;
import org.w3c.dom.Document;

import com.sense.core.util.StringUtils;
import com.sense.iam.model.sso.Saml;
import com.sense.sdk.saml.pojo.AuthnRequestField;

/**
 * SAML
 * 
 * @author LiangRiLu
 * @Time 2019年12月14日下午1:07:44
 */
public class SAML {

	private DocumentBuilder builder;
	private String issuerURL;

	private static SecureRandomIdentifierGenerator generator;
	private static final String CM_PREFIX = "urn:oasis:names:tc:SAML:2.0:cm:";

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
	 * Initialize JAXP DocumentBuilder instance for later use and reuse.
	 */
	public SAML() {
		this(null);
	}

	/**
	 * Initialize JAXP DocumentBuilder instance for later use and reuse, and
	 * establishes an issuer URL.
	 * 
	 * @param issuerURL
	 *            This will be used in all generated assertions
	 */
	public SAML(String issuerURL) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			builder = factory.newDocumentBuilder();

			this.issuerURL = issuerURL;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * <u>Slightly</u> easier way to create objects using OpenSAML's builder
	 * system.
	 */
	// cast to SAMLObjectBuilder<T> is caller's choice
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> T create(Class<T> cls, QName qname) {
		return (T) ((XMLObjectBuilder) Configuration.getBuilderFactory().getBuilder(qname)).buildObject(qname);
	}

	/**
	 * Helper method to get an XMLObject as a DOM Document.
	 */
	public Document asDOMDocument(XMLObject object) throws IOException, MarshallingException, TransformerException {
		Document document = builder.newDocument();
		Marshaller out = Configuration.getMarshallerFactory().getMarshaller(object);
		out.marshall(object, document);
		return document;
	}

	/**
	 * Helper method to spawn a new Issuer element based on our issuer URL.
	 */
	public Issuer spawnIssuer() {
		Issuer result = null;
		if (issuerURL != null) {
			result = create(Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
			result.setValue(issuerURL);
		}

		return result;
	}

	/**
	 * Returns a SAML subject.
	 * 
	 * @param username
	 *            The subject name
	 * @param format
	 *            If non-null, we'll set as the subject name format
	 * @param confirmationMethod
	 *            If non-null, we'll create a SubjectConfirmation element and
	 *            use this as the Method attribute; must be "sender-vouches" or
	 *            "bearer", as HOK would require additional parameters and so is
	 *            NYI
	 */
	public Subject createSubject(String username, AuthnRequestField requestField, Saml samlConfig,
			String confirmationMethod, String acsUrl) {
		NameID nameID = create(NameID.class, NameID.DEFAULT_ELEMENT_NAME);
		nameID.setValue(username);
		if (samlConfig.getNameId() != null) {
			nameID.setFormat(samlConfig.getNameId());
		}
		Subject subject = create(Subject.class, Subject.DEFAULT_ELEMENT_NAME);
		subject.setNameID(nameID);

		DateTime now = new DateTime();
		now = TimeUtils.transformTimeZone(now, samlConfig.getTimeZone());
		Long validateTime;
		validateTime = samlConfig.getValidTime() == null ? 300 : samlConfig.getValidTime();
		if (confirmationMethod != null) {
			SubjectConfirmation confirmation = create(SubjectConfirmation.class,
					SubjectConfirmation.DEFAULT_ELEMENT_NAME);
			confirmation.setMethod(CM_PREFIX + confirmationMethod);
			SubjectConfirmationData confirmationData = create(SubjectConfirmationData.class,
					SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
			confirmationData.setRecipient(acsUrl);
			confirmationData.setNotOnOrAfter(now.plusSeconds(validateTime.intValue()));
			Integer isInResponseTo = samlConfig.getIsInResponseTo();
			if (isInResponseTo != null && isInResponseTo.intValue() == 1) {
				if (!StringUtils.isEmpty(requestField.getRequestId())) {
					confirmationData.setInResponseTo(requestField.getRequestId());
				}
			}
			confirmation.setSubjectConfirmationData(confirmationData);
			subject.getSubjectConfirmations().add(confirmation);
		}
		return subject;
	}

	/**
	 * Helper method to generate a shell response with a given status code,
	 * status message, and query ID.
	 */
	public Response createResponse(AuthnRequestField requestField, String statusCode, String inResponseTo,
			Saml samlConfig) {
		Response response = create(Response.class, Response.DEFAULT_ELEMENT_NAME);
		response.setID(generator.generateIdentifier());
		String consumerServiceUrl = requestField.getAssertionConsumerServiceUrl();
		if (!StringUtils.isTrimEmpty(consumerServiceUrl)) {
			response.setDestination(consumerServiceUrl);
		}
		Integer isInResponseTo = samlConfig.getIsInResponseTo();
		if (isInResponseTo != null && isInResponseTo.intValue() == 1) {
			if (!StringUtils.isTrimEmpty(inResponseTo)) {
				response.setInResponseTo(inResponseTo);
			}
		}
		DateTime now = new DateTime();
		now = TimeUtils.transformTimeZone(now, samlConfig.getTimeZone());
		response.setIssueInstant(now);
		if (issuerURL != null) {
			response.setIssuer(spawnIssuer());
		}
		StatusCode statusCodeElement = create(StatusCode.class, StatusCode.DEFAULT_ELEMENT_NAME);
		statusCodeElement.setValue(statusCode);
		Status status = create(Status.class, Status.DEFAULT_ELEMENT_NAME);
		status.setStatusCode(statusCodeElement);
		response.setStatus(status);
		return response;
	}

	/**
	 * Helper method to generate a response, based on a pre-built assertion and
	 * query ID.
	 */
	public Response createResponse(AuthnRequestField requestField, Assertion assertion, String inResponseTo,
			Saml samlConfig) {
		Response response = createResponse(requestField, StatusCode.SUCCESS_URI, inResponseTo, samlConfig);
		response.getAssertions().add(assertion);
		return response;
	}

	/**
	 * Adds a SAML attribute to an attribute statement.
	 * 
	 * @param statement
	 *            Existing attribute statement
	 * @param name
	 *            Attribute name
	 * @param value
	 *            Attribute value
	 */
	@SuppressWarnings("rawtypes")
	public void addAttribute(AttributeStatement statement, String name, String value) {
		final XMLObjectBuilder builder = Configuration.getBuilderFactory().getBuilder(XSAny.TYPE_NAME);
		XSAny valueElement = (XSAny) builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
		valueElement.setTextContent(value);
		Attribute attribute = create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
		attribute.setNameFormat(Attribute.BASIC);
		attribute.setName(name);
		attribute.getAttributeValues().add(valueElement);
		statement.getAttributes().add(attribute);
	}

}
