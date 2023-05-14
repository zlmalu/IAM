package com.sense.iam.api.model.im;


import java.util.List;

import com.sense.iam.model.im.Account;
import com.sense.iam.model.im.User;

import io.swagger.annotations.ApiModelProperty;



/**
 * 响应结果
 * 
 * 
 * @author j_hy
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class ResponseData {
	
	
	
	public ResponseData(int code) {
		super();
		this.code = code;
	}
	public ResponseData(int code,User u,List<Account> acct) {
		super();
		this.code = code;
		this.userinfo=u;
		this.acct=acct;
	}
	int code;
	
	User userinfo;
	List<Account> acct;
	
	@ApiModelProperty(value="响应状态码 0成功，其他失败")
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	@ApiModelProperty(value="用户信息")
	public User getUserinfo() {
		return userinfo;
	}
	public void setUserinfo(User userinfo) {
		this.userinfo = userinfo;
	}
	@ApiModelProperty(value="帐号信息")
	public List<Account> getAcct() {
		return acct;
	}
	public void setAcct(List<Account> acct) {
		this.acct = acct;
	}
	
	
}
