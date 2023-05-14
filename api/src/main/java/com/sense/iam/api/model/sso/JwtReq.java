package com.sense.iam.api.model.sso;

import io.swagger.annotations.ApiModelProperty;

import com.sense.iam.model.sso.Jwt;

public class JwtReq {

	private Long id;
	
	private String sn;
	
	private String name;
	
	private Long appId;
	
	private String config;
	
	private String appSn;
	
	private String appName;
	
	@ApiModelProperty(value="唯一编码",example="0",required=true)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	@ApiModelProperty(value="编码",required=true)
	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}
	@ApiModelProperty(value="名称",required=true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	@ApiModelProperty(value="应用id",required=true)
	public Long getAppId() {
		return appId;
	}

	public void setAppId(Long appId) {
		this.appId = appId;
	}
	@ApiModelProperty(value="配置",required=true)
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
	public Jwt getJwt(){
		Jwt jwt=new Jwt();
		jwt.setId(this.getId());
		jwt.setSn(this.getSn());
		jwt.setName(this.getName());
		jwt.setAppId(this.getAppId());
		jwt.setConfig(this.getConfig());
		return jwt;
	}
	
}
