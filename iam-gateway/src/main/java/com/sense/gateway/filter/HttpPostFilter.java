package com.sense.gateway.filter;

import com.netflix.zuul.ZuulFilter;

/**
 * 响应消息头处理
 * 
 * Description: 将从各服务模块获取的响应头信息进行处理 
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */
public abstract class HttpPostFilter  extends ZuulFilter{

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public String filterType() {
		return "post";
	}

	@Override
	public int filterOrder() {
		return 0;
	}

}
