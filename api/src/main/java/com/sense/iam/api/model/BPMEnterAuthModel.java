package com.sense.iam.api.model;

import java.util.List;

import com.sense.core.db.DBField;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


/**
 * 
 *  流程申请用户授权接口入参类
 * 
 * Description:  
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */

@ApiModel
public class BPMEnterAuthModel {

	Long userId;
	String auth_ids;
	Integer type;
	
	@ApiModelProperty(value="授权类型，1主岗位  2兼岗")
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	
	@ApiModelProperty(value="授权用户唯一标识" ,required=true)
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	@ApiModelProperty(value="授权对象集合，岗位授权对象填写orgid_positionid组合")
	public String getAuth_ids() {
		return auth_ids;
	}
	public void setAuth_ids(String auth_ids) {
		this.auth_ids = auth_ids;
	}
	
}
