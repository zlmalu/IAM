package com.sense.sdk.saml;

import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;

import com.sense.core.util.StringUtils;
import com.sense.iam.model.sso.Saml;

/**
 * Simple examples of coding to the OpenSAML API. Methods here can write and
 * parse each of the three main assertion types: authentication, authorization
 * decision, and attributes.
 * 
 * @author LiangRiLu
 * @Time 2019年12月14日下午1:46:09
 */
public class SAMLAssertion extends SAML {

	/**
	 * Creates a file whose contents are a SAML authentication assertion.
	 */
	public Assertion createStockAuthnAssertion(Map<String, String> attributes, Saml samlConfig, String assertionId,
			String spEntityId) {
		DateTime now = new DateTime();
		now = TimeUtils.transformTimeZone(now, samlConfig.getTimeZone());

		Issuer issuer = create(Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
		issuer.setValue(samlConfig.getIdpIssuer());

		Conditions conditions = create(Conditions.class, Conditions.DEFAULT_ELEMENT_NAME);

		Long validTime = samlConfig.getValidTime() == null ? 300 : samlConfig.getValidTime();

		conditions.setNotBefore(now.minusSeconds(validTime.intValue()));
		conditions.setNotOnOrAfter(now.plusSeconds(validTime.intValue()));

		AudienceRestriction audienceRestriction = create(AudienceRestriction.class,
				AudienceRestriction.DEFAULT_ELEMENT_NAME);
		Audience audience = create(Audience.class, Audience.DEFAULT_ELEMENT_NAME);
		audience.setAudienceURI(spEntityId);
		audienceRestriction.getAudiences().add(audience);
		conditions.getAudienceRestrictions().add(audienceRestriction);

		// attr
		AttributeStatement statement = create(AttributeStatement.class, AttributeStatement.DEFAULT_ELEMENT_NAME);

		if (attributes != null)
			for (Map.Entry<String, String> entry : attributes.entrySet())
				addAttribute(statement, entry.getKey(), entry.getValue());

		String authContext = AuthnContext.PASSWORD_AUTHN_CTX;
		if (!StringUtils.isTrimEmpty(samlConfig.getAuthContext())) {
			authContext = samlConfig.getAuthContext();
		}
		AuthnContextClassRef ref = create(AuthnContextClassRef.class, AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
		ref.setAuthnContextClassRef(authContext);

		AuthnContext authnContext = create(AuthnContext.class, AuthnContext.DEFAULT_ELEMENT_NAME);
		authnContext.setAuthnContextClassRef(ref);

		AuthnStatement authnStatement = create(AuthnStatement.class, AuthnStatement.DEFAULT_ELEMENT_NAME);
		authnStatement.setAuthnContext(authnContext);
		authnStatement.setAuthnInstant(now);
		authnStatement.setSessionNotOnOrAfter(now.plusSeconds(validTime.intValue()));
		authnStatement.setSessionIndex(UUID.randomUUID().toString());
		Assertion assertion = create(Assertion.class, Assertion.DEFAULT_ELEMENT_NAME);
		assertion.setID(assertionId);
		assertion.setIssueInstant(now);
		assertion.setIssuer(issuer);
		assertion.getStatements().add(authnStatement);
		assertion.setConditions(conditions);
		assertion.getAttributeStatements().add(statement);
		return assertion;
	}

}