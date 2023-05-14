package com.sense.iam.cam.auth.cache.memery;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sense.core.util.StringUtils;
import com.sense.iam.cam.auth.Token;
import com.sense.iam.cam.auth.cache.AccessTokenCache;
import com.sense.iam.ticket.registry.UniqueTicketIdGenerator;

/**
 * 
 * accessToken处理器的内存模式实现
 * 
 * Description: 令牌存储时间默认为30秒，超过30秒会自动清除
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Component("accessTokenCache")
public class AccessTokenCacheImpl extends BaseCacheImpl implements AccessTokenCache{

	private long defaultExpriedTime=30*1000;//默认过期时间定义
	
	@Resource
	private UniqueTicketIdGenerator  uniqueTicketIdGenerator;
	
	@Override
	public Token grantToken(String tokenId,Object content) {
		Token token=new Token();
		token.setId(tokenId);
		token.setExpried(System.currentTimeMillis()+defaultExpriedTime);
		token.setContent(content);
		cache.put(token.getId(), token);
		log.debug("grant token:"+token);
		return token;
	}

	@Override
	public Token grantToken(Object content) {
		Token token=new Token();
		//创建code票据ID  OC前缀 代表 oauth code凭据ID
		token.setId(uniqueTicketIdGenerator.getNewTicketId("OC"));
		token.setExpried(System.currentTimeMillis()+defaultExpriedTime);
		token.setContent(content);
		cache.put(token.getId(), token);
		log.debug("grant token:"+token);
		return token;
	}

	@Override
	public Token getToken(String key) {
		return (Token) cache.remove(key);
	}

	@Override
	public Token grantAccessToken(String code) {
		Token token=(Token) cache.remove(code);
		if(token==null){
			return null;
		}
		//重新创建accessToken票据ID  AT前缀 
		token.setId(uniqueTicketIdGenerator.getNewTicketId("AT"));
		// TODO Auto-generated method stub
		return token;
	}

	@Override
	public Token grantTicketToken(Object content) {
		Token token=new Token();
		//创建code票据ID  OC前缀 代表 oauth code凭据ID
		token.setId(uniqueTicketIdGenerator.getNewTicketId("ST"));
		token.setExpried(System.currentTimeMillis()+defaultExpriedTime);
		token.setContent(content);
		cache.put(token.getId(), token);
		log.debug("grant token:"+token);
		return token;
	}

	

}
