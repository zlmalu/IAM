package com.sense.iam.auth.cache;

import com.sense.iam.auth.Token;

/**
 * 
 * 用户访问令牌缓存
 * 
 * Description: 用户的访问令牌，
 * 令牌存储时间默认为30秒，超过30秒会自动清除，并切为一次性令牌。
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public interface AccessTokenCache{

	public Token grantToken(Object content);
	
	public Token getToken(String id);
	
	public Token grantToken(String tokenId,Object content);
	
	
}
