package com.sense.am.model;

import java.util.List;

/**
 * 
 * 单点登陆请求
 * 
 * Description: 
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class SSORequest {

	
	private String username;
	
	private String loginIp;
	
	private Integer currentLevel=1;
	
	private List<String> allowRes;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getLoginIp() {
		return loginIp;
	}

	public void setLoginIp(String loginIp) {
		this.loginIp = loginIp;
	}

	public Integer getCurrentLevel() {
		return currentLevel;
	}

	public void setCurrentLevel(Integer currentLevel) {
		this.currentLevel = currentLevel;
	}

	public List<String> getAllowRes() {
		return allowRes;
	}

	public void setAllowRes(List<String> allowRes) {
		this.allowRes = allowRes;
	}
	
	
	
}
