package com.sense.iam.api.model.am;

import io.swagger.annotations.ApiModelProperty;



public class BindAppReq {

	
	
	private Long reamlId;
	
	
	private Long appId;

	
	@ApiModelProperty(value="认证实例ID")
	public Long getReamlId() {
		return reamlId;
	}

	public void setReamlId(Long reamlId) {
		this.reamlId = reamlId;
	}

	
	@ApiModelProperty(value="应用ID")
	public Long getAppId() {
		return appId;
	}

	public void setAppId(Long appId) {
		this.appId = appId;
	}
	
	

	
}
