package com.sense.iam.api.model;

import java.util.List;

import com.sense.core.db.DBField;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


/**
 * 
 *  用户授权接口入参类
 * 
 * Description:  
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */

@ApiModel
public class EnterAuthModel {

	
	List<Long> userIds;
	List<String> auth_ids;
	List<String> cancel_auth_ids;
	Long orgId; 

	private Integer type;
	
	@ApiModelProperty(value="授权类型，1主岗位  2兼岗")
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	
	@ApiModelProperty(value="授权用户唯一标识集合" ,required=true)
	public List<Long> getUserIds() {
		return userIds;
	}
	public void setUserIds(List<Long> userIds) {
		this.userIds = userIds;
	}
	
	@ApiModelProperty(value="授权对象集合，岗位授权对象填写orgid_positionid组合，应用授权填写应用标识")
	public List<String> getAuth_ids() {
		return auth_ids;
	}
	public void setAuth_ids(List<String> auth_ids) {
		this.auth_ids = auth_ids;
	}

	@ApiModelProperty(value="取消授权对象集合，岗位授权对象填写orgid_positionid组合，应用授权填写应用标识")
	public List<String> getCancel_auth_ids() {
		return cancel_auth_ids;
	}
	public void setCancel_auth_ids(List<String> cancel_auth_ids) {
		this.cancel_auth_ids = cancel_auth_ids;
	}
	@ApiModelProperty(value="授权用户所属组织ID" ,required=true)
	public Long getOrgId() {
		return orgId;
	}
	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}
	
	
}
