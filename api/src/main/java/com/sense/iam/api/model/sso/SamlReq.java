package com.sense.iam.api.model.sso;

import com.sense.core.model.BaseModel;
import com.sense.iam.model.sso.Saml;

import com.sense.iam.model.sso.SamlConfig;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.List;


public class SamlReq extends BaseModel {

	private Long id;

	/**
	 * 证书文件路径
	 */
	private String keyStorePath;

	/**
	 * 证书别名
	 */
	private String keyAlias;

	/**
	 * 证书文件密钥
	 */
	private String keyPwd;

	/**
	 * 有效期
	 */
	private Long validTime;
	/**
	 * 应用默认URL
	 */
	private String defaultUrl;
	/**
	 * 所属应用唯一标识
	 */
	private Long appId;
	/**
	 * 响应属性配置
	 */
	private String config;

	/** IDP ISSUER */
	private String idpIssuer;
	/** SP ISSUER */
	private String spIssuer;
	/** 身份规则 */
	private String nameId;
	/** 请求入口 */
	private String startUrl;
	/** 时区 */
	private String timeZone;
	/** 签名算法 */
	private String signature;
	/** 是否带请求ID */
	private Integer isInResponseTo;
	/** 授权选项 */
	private String authContext;
	/** 签名响应 */
	private Integer isSignResponse;
	/** 签名断言 */
	private Integer isSignAssert;

	private String appSn;

	private String appName;

	private List<SamlConfig> samlConfigList;

	@ApiModelProperty(value="唯一编码",example="0",required=true)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@ApiModelProperty(value="证书文件路径",required=true)
	public String getKeyStorePath() {
		return keyStorePath;
	}
	public void setKeyStorePath(String keyStorePath) {
		this.keyStorePath = keyStorePath;
	}
	@ApiModelProperty(value="证书别名",required=true)
	public String getKeyAlias() {
		return keyAlias;
	}
	public void setKeyAlias(String keyAlias) {
		this.keyAlias = keyAlias;
	}
	@ApiModelProperty(value="证书文件秘钥",required=true)
	public String getKeyPwd() {
		return keyPwd;
	}
	public void setKeyPwd(String keyPwd) {
		this.keyPwd = keyPwd;
	}
	@ApiModelProperty(value="有效期",required=false)
	public Long getValidTime() {
		return validTime;
	}
	public void setValidTime(Long validTime) {
		this.validTime = validTime;
	}
	@ApiModelProperty(value="应用默认地址",required=false)
	public String getDefaultUrl() {
		return defaultUrl;
	}
	public void setDefaultUrl(String defaultUrl) {
		this.defaultUrl = defaultUrl;
	}
	@ApiModelProperty(value="应用唯一标识",required=true)
	public Long getAppId() {
		return appId;
	}
	public void setAppId(Long appId) {
		this.appId = appId;
	}
	@ApiModelProperty(value="响应属性配置",required=false)
	public String getConfig() {
		return config;
	}
	public void setConfig(String config) {
		this.config = config;
	}

	public String getIdpIssuer() {
		return idpIssuer;
	}

	public void setIdpIssuer(String idpIssuer) {
		this.idpIssuer = idpIssuer;
	}

	public String getSpIssuer() {
		return spIssuer;
	}

	public void setSpIssuer(String spIssuer) {
		this.spIssuer = spIssuer;
	}

	public String getNameId() {
		return nameId;
	}

	public void setNameId(String nameId) {
		this.nameId = nameId;
	}

	public String getStartUrl() {
		return startUrl;
	}

	public void setStartUrl(String startUrl) {
		this.startUrl = startUrl;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public Integer getIsInResponseTo() {
		return isInResponseTo;
	}

	public void setIsInResponseTo(Integer isInResponseTo) {
		this.isInResponseTo = isInResponseTo;
	}

	public String getAuthContext() {
		return authContext;
	}

	public void setAuthContext(String authContext) {
		this.authContext = authContext;
	}

	public Integer getIsSignResponse() {
		return isSignResponse;
	}

	public void setIsSignResponse(Integer isSignResponse) {
		this.isSignResponse = isSignResponse;
	}

	public Integer getIsSignAssert() {
		return isSignAssert;
	}

	public void setIsSignAssert(Integer isSignAssert) {
		this.isSignAssert = isSignAssert;
	}

	public String getAppSn() {
		return appSn;
	}
	public void setAppSn(String appSn) {
		this.appSn = appSn;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}

	public List<SamlConfig> getSamlConfigList() {
		return samlConfigList;
	}

	public void setSamlConfigList(List<SamlConfig> samlConfigList) {
		this.samlConfigList = samlConfigList;
	}

	@ApiModelProperty(hidden=true)
	public Saml getSaml(){
		Saml saml=new Saml();
		saml.setId(this.getId());
		saml.setKeyStorePath(this.getKeyStorePath());
		saml.setKeyAlias(this.getKeyAlias());
		saml.setKeyPwd(this.getKeyPwd());
		saml.setValidTime(this.getValidTime());
		saml.setDefaultUrl(this.getDefaultUrl());
		saml.setAppId(this.getAppId());
		saml.setConfig(this.getConfig());
		saml.setCreateTime(new Date());
		saml.setIdpIssuer(this.getIdpIssuer());
		saml.setSpIssuer(this.getSpIssuer());
		saml.setNameId(this.getNameId());
		saml.setStartUrl(this.getStartUrl());
		saml.setTimeZone(this.getTimeZone());
		saml.setSignature(this.getSignature());
		saml.setIsInResponseTo(this.getIsInResponseTo());
		saml.setAuthContext(this.getAuthContext());
		saml.setIsSignResponse(this.getIsSignResponse());
		saml.setIsSignAssert(this.getIsSignAssert());
		saml.setSamlConfigList(this.getSamlConfigList());
		return saml;
	}



}
