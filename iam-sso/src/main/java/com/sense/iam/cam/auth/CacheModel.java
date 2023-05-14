package com.sense.iam.cam.auth;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 
 * 缓存模型
 * 
 * Description: 
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@SuppressWarnings("serial")
public class CacheModel implements java.io.Serializable{
	
	/**
	 * 过期时间
	 */
	private Long expried;

	public Long getExpried() {
		return expried;
	}

	public void setExpried(Long expried) {
		this.expried = expried;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
	

}
