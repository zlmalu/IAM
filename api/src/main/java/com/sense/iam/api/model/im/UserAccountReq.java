package com.sense.iam.api.model.im;

import io.swagger.annotations.ApiModelProperty;




/**
 * 用户获取关联帐号响应对象
 * 
 * Description:  岗位信息模型定义
 * 
 * @author j_hy
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class UserAccountReq{
	

	private Long id;
	
	private String usersn;
	
	private String username;
	
	private String appsn;
	
	private String appname;
	
	private String loginname;
	
	private int accstatus;

	
	@ApiModelProperty(value="帐号唯一标识")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ApiModelProperty(value="关联用户工号")
	public String getUsersn() {
		return usersn;
	}

	public void setUsersn(String usersn) {
		this.usersn = usersn;
	}
	@ApiModelProperty(value="关联用户姓名")
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	@ApiModelProperty(value="应用编码")
	public String getAppsn() {
		return appsn;
	}

	public void setAppsn(String appsn) {
		this.appsn = appsn;
	}
	@ApiModelProperty(value="应用名称")
	public String getAppname() {
		return appname;
	}

	public void setAppname(String appname) {
		this.appname = appname;
	}
	@ApiModelProperty(value="帐号名")
	public String getLoginname() {
		return loginname;
	}

	public void setLoginname(String loginname) {
		this.loginname = loginname;
	}
	@ApiModelProperty(value="帐号状态 1正常  2禁用 3异常")
	public int getAccstatus() {
		return accstatus;
	}

	public void setAccstatus(int accstatus) {
		this.accstatus = accstatus;
	}

	
	
	


}
