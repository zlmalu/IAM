package com.sense.iam.api.model;

import io.swagger.annotations.ApiModelProperty;


/**
 * 
 *  组织绑定应用参数模型
 * 
 * Description:  
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */
public class BindApp {
	
	
	Long orgId;
	Long appId;
	Long isDefault;
	
	@ApiModelProperty(value="组织唯一标识" ,required=true)
	public Long getOrgId() {
		return orgId;
	}
	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}
	@ApiModelProperty(value="应用唯一标识",required=true)
	public Long getAppId() {
		return appId;
	}
	public void setAppId(Long appId) {
		this.appId = appId;
	}
	@ApiModelProperty(value="是否默认应用：1：     关联应用：0，又是关联应用和默认应用传1",required=true)
	public Long getIsDefault() {
		return isDefault;
	}
	public void setIsDefault(Long isDefault) {
		this.isDefault = isDefault;
	}
}

