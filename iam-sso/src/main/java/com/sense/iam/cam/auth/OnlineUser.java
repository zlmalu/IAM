package com.sense.iam.cam.auth;

/**
 * 
 * 在线用户对象
 * 
 * Description: 
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class OnlineUser extends CacheModel{
	
	/**
	 * 用户工号
	 */
	private String uid;
	/**
	 * 会话标识
	 */
	private String sessionId;
	
	/**
	 * 登陆地址
	 */
	private String loginIp;
	
	/**
	 * 登陆时间
	 */
	private long loginTime;
	
	/**
	 * 用户是否有效
	 */
	private boolean isValid;
	
	/**
	 * 登陆的应用编码
	 */
	private String appSn;
	
	/**
	 * 登陆账号ID
	 */
	private String accountId;
	
	/**
	 * nonce
	 */
	private String nonce;
	
	
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getLoginIp() {
		return loginIp;
	}

	public void setLoginIp(String loginIp) {
		this.loginIp = loginIp;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}

	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	public String getAppSn() {
		return appSn;
	}

	public void setAppSn(String appSn) {
		this.appSn = appSn;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getNonce() {
		return nonce;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}
	
}
