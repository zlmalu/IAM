package com.sense.iam.api.model.sys;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;

/**
 * 授权用户组请求对象
 * 
 * Description:  
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */
@ApiModel("用户授权组模型")
public class AuthUserGroupReq {

	/**
	 * 用户的id字符串，多个id采用英文逗号分割
	 */
	private String userIds;
	/**
	 * 用户组的id字符串，多个id采用英文逗号分割
	 */
	private String groupIds;
	
	@ApiModelProperty(value="用户编码字符串",example="11001,10002",required=true)
	public String getUserIds() {
		return userIds;
	}
	public void setUserIds(String userIds) {
		this.userIds = userIds;
	}
	@ApiModelProperty(value="组编码字符串",example="11001,10002",required=true)
	public String getGroupIds() {
		return groupIds;
	}
	public void setGroupIds(String groupIds) {
		this.groupIds = groupIds;
	}
	
	
	
}
