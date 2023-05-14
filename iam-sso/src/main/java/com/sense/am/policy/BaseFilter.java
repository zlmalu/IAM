package com.sense.am.policy;

import com.sense.am.model.SSORequest;

/**
 * 
 * 基础过滤器
 * 
 * Description:抽象过滤器
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public abstract class BaseFilter {

	
	public abstract void doFilter(SSORequest request,String resource);
	
}
