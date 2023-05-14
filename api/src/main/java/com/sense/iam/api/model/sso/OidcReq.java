package com.sense.iam.api.model.sso;

import com.sense.iam.model.sso.Oidc;

import io.swagger.annotations.ApiModelProperty;

public class OidcReq {

	/**唯一标识*/
	private Long id;
	/**安全密钥*/
	private String secretKey;
	/**密钥*/
	private String clientSecret;
	/**回调URL*/
	private String defaultUrl;
	/**会话有效时间*/
	private Long  validTime;
	/**归属应用标识*/
	private Long appId;
	/**响应参数配置*/
	private String config;
	
	private String appSn;
	
	private String appName;
	
	private String serverUrl;
	
	
	@ApiModelProperty(value="唯一编码",example="0",required=true)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@ApiModelProperty(value="安全秘钥",required=true)
	public String getSecretKey() {
		return secretKey;
	}
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	@ApiModelProperty(value="秘钥",required=true)
	public String getClientSecret() {
		return clientSecret;
	}
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
	@ApiModelProperty(value="回调URL",required=false)
	public String getDefaultUrl() {
		return defaultUrl;
	}
	public void setDefaultUrl(String defaultUrl) {
		this.defaultUrl = defaultUrl;
	}
	@ApiModelProperty(value="会话有效时间",required=false)
	public Long getValidTime() {
		return validTime;
	}
	public void setValidTime(Long validTime) {
		this.validTime = validTime;
	}
	@ApiModelProperty(value="归属应用标识",required=false)
	public Long getAppId() {
		return appId;
	}
	public void setAppId(Long appId) {
		this.appId = appId;
	}
	@ApiModelProperty(value="响应参数配置",required=false)
	public String getConfig() {
		return config;
	}
	public void setConfig(String config) {
		this.config = config;
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
	@ApiModelProperty(hidden=true)
	public Oidc getOidc(){
		Oidc oidc=new Oidc();
		oidc.setId(this.getId());
		oidc.setSecretKey(this.getSecretKey());
		oidc.setClientSecret(this.getClientSecret());
		oidc.setDefaultUrl(this.getDefaultUrl());
		oidc.setDefaultUrl(this.getDefaultUrl());
		oidc.setAppId(this.getAppId());
		oidc.setConfig(this.getConfig());
		oidc.setValidTime(this.getValidTime());
		return oidc;
	}
	public String getServerUrl() {
		return serverUrl;
	}
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}
	
}
