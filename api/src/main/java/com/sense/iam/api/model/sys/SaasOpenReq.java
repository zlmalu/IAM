package com.sense.iam.api.model.sys;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class SaasOpenReq {
	
	String companySn;
	Long appId;
	String appPath;
	
	@ApiModelProperty(value="公司编码",required=true)
	public String getCompanySn() {
		return companySn;
	}
	public void setCompanySn(String companySn) {
		this.companySn = companySn;
	}
	@ApiModelProperty(value="应用ID",required=true)
	public Long getAppId() {
		return appId;
	}
	public void setAppId(Long appId) {
		this.appId = appId;
	}
	@ApiModelProperty(value="应用路径",required=true)
	public String getAppPath() {
		return appPath;
	}
	public void setAppPath(String appPath) {
		this.appPath = appPath;
	}
	
	
}
