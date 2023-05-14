package com.sense.iam.cam.auth;

/**
 * 
 * 一次性票据
 * 
 * Description:用户在内存中生成的一次性票据 
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class Token extends CacheModel{

	/**
	 * 票据标识
	 */
	private String id;
	
	/**
	 * 应用标识
	 */
	private String clientId;

	/**
	 * 映射内容
	 */
	private Object content;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Object getContent() {
		return content;
	}

	public void setContent(Object content) {
		this.content = content;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	
}
