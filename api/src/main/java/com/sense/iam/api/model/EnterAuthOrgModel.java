package com.sense.iam.api.model;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


/**
 * 
 *  用户授权兼职岗位
 * 
 * Description:  
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */

@ApiModel
public class EnterAuthOrgModel {

	
	Long userId;
	List<String> ids;
	
	
	
	@ApiModelProperty(value="用户ID" ,required=true)
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	@ApiModelProperty(value="授权组织ID集合" ,required=true)
	public List<String> getIds() {
		return ids;
	}
	public void setIds(List<String> ids) {
		this.ids = ids;
	}
	
	
	
	
}
