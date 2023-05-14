package com.sense.iam.api.model.sso;

import com.sense.iam.model.sso.Cas;
import com.sense.iam.model.sso.Oidc;

import io.swagger.annotations.ApiModelProperty;

public class CasReq {

	/**唯一标识*/
	private Long id;
	/**回调URL*/
	private String defaultUrl;
	/**会话有效时间*/
	private Long  sessionValidTime;
	/**归属应用标识*/
	private Long appId;
	/**响应参数配置*/
	private String config;
	
	private String appSn;
	
	private String appName;
	/**正则表达式*/
	private String regularReg;
	
	@ApiModelProperty(value="安全域名正则表达式",required=false)
	public String getRegularReg() {
		return regularReg;
	}
	public void setRegularReg(String regularReg) {
		this.regularReg = regularReg;
	}
	
	
	
	@ApiModelProperty(value="唯一编码",example="0",required=true)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@ApiModelProperty(value="默认回调URL",required=false)
	public String getDefaultUrl() {
		return defaultUrl;
	}
	public void setDefaultUrl(String defaultUrl) {
		this.defaultUrl = defaultUrl;
	}
	@ApiModelProperty(value="会话有效时间",required=false)
	public Long getSessionValidTime() {
		return sessionValidTime;
	}
	public void setSessionValidTime(Long sessionValidTime) {
		this.sessionValidTime = sessionValidTime;
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
	public Cas getCas(){
		Cas cas=new Cas();
		cas.setId(this.getId());
		cas.setRegularReg(this.getRegularReg());
		cas.setDefaultUrl(this.getDefaultUrl());
		cas.setAppId(this.getAppId());
		cas.setConfig(this.getConfig());
		cas.setSessionValidTime(this.getSessionValidTime());
		return cas;
	}
	
	
	
	
}
