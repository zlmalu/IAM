package com.sense.am.policy;

import com.sense.am.exception.BlackUserException;
import com.sense.am.model.SSORequest;

/**
 * 
 * 黑名单用户过滤
 * 
 * Description: 
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class BlackUserFilter extends BaseFilter{


	/**
	 * 过滤黑名单数据
	 */
	@Override
	public void doFilter(SSORequest request, String resource) {
		if(PolicyManager.blackUserCache.contains(request.getUsername())){
			throw new BlackUserException("用户存在于黑名单中!");
		}
	}
}
