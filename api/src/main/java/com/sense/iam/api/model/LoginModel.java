package com.sense.iam.api.model;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;

/**
 *
 * 登陆参数模型对象
 *
 * Description:
 *
 * @author w_jfwen
 *
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */
public class LoginModel implements Serializable{


	private String username;

	private String password;
	@ApiModelProperty(hidden = true)
	private  String verificationCode;
	@ApiModelProperty(hidden = true)
	private  String tokenid;

	public String getTokenid() {
		return tokenid;
	}

	public void setTokenid(String tokenid) {
		this.tokenid = tokenid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	public String getVerificationCode() {
		return verificationCode;
	}

	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}




}
