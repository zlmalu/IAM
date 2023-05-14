package com.sense.iam.api.model;

import io.swagger.annotations.ApiModelProperty;


/**
 * 
 *  组织绑定岗位参数模型
 * 
 * Description:  
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */
public class BindPosition {
	
	
	Long orgId;
	Long positionId;
	Long isDefault;
	
	@ApiModelProperty(value="组织唯一标识" ,required=true)
	public Long getOrgId() {
		return orgId;
	}
	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}
	@ApiModelProperty(value="岗位唯一标识",required=true)
	public Long getPositionId() {
		return positionId;
	}
	public void setPositionId(Long positionId) {
		this.positionId = positionId;
	}
	@ApiModelProperty(value="是否默认岗位：1：关联岗位：0，又是关联岗位和默认岗位传1",required=true)
	public Long getIsDefault() {
		return isDefault;
	}
	public void setIsDefault(Long isDefault) {
		this.isDefault = isDefault;
	}
}

