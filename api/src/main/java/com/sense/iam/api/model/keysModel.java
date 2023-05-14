package com.sense.iam.api.model;

import io.swagger.annotations.ApiModelProperty;


/**
 * 
 *  关键字查询
 * 
 * Description:  
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */
public class keysModel {
	
	long userId;
	String key;

	@ApiModelProperty(value="用户唯一属性",required=true)
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	@ApiModelProperty(value="关键字")
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
}

