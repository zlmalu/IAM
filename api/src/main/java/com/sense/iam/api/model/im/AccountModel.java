package com.sense.iam.api.model.im;

import io.swagger.annotations.ApiModelProperty;



/**
 * 应用权限账号
 * 
 * Description:  岗位信息模型定义
 * 
 * @author j_hy
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class AccountModel {

	/**账号标识*/
	private Long tokenId;
	
	/**应用编码*/
	private String appSn ;
	/**应用名称*/
	private String appName;
	/**应用单点登录地址*/
	private String ssoLink;
	
	/**应用图标地址*/
	private String imgLink;
	
	
	@ApiModelProperty(value="账号唯一标识")
	public Long getTokenId() {
		return tokenId;
	}
	public void setTokenId(Long tokenId) {
		this.tokenId = tokenId;
	}
	@ApiModelProperty(value="应用编码")
	public String getAppSn() {
		return appSn;
	}
	public void setAppSn(String appSn) {
		this.appSn = appSn;
	}
	
	
	@ApiModelProperty(value="应用名称")
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	
	@ApiModelProperty(value="SSO地址")
	public String getSsoLink() {
		return ssoLink;
	}
	
	public void setSsoLink(String ssoLink) {
		this.ssoLink = ssoLink;
	}
	
	@ApiModelProperty(value="应用图标地址")
	public String getImgLink() {
		return imgLink;
	}
	public void setImgLink(String imgLink) {
		this.imgLink = imgLink;
	}
	
	
}
