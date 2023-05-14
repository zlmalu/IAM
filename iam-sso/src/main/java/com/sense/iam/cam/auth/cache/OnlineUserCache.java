package com.sense.iam.cam.auth.cache;

import com.sense.iam.cam.auth.OnlineUser;

/**
 * 
 * 在线用户缓存
 * 
 * Description: 
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public interface OnlineUserCache {

	/**
	 * 存放在线用户
	 * @param oUser
	 */
	public void put(String key,OnlineUser oUser);
	
	/**
	 * 查询在线用户
	 * @param key
	 */
	public OnlineUser get(String key);
	
	/**
	 * 移除缓存中的用户
	 * @param key
	 */
	public void remove(String key);
	
	
}
