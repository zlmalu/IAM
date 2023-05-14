package com.sense.am.policy;

import com.sense.am.exception.IpPolicyException;
import com.sense.am.model.IpPolicy;
import com.sense.am.model.SSORequest;

/**
 * 
 * IP地址过滤ַ
 * 
 * Description: 
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class IpPolicyFilter extends BaseFilter{

	
	@Override
	public void doFilter(SSORequest request, String resource) {
		for (IpPolicy ipPolicy : PolicyManager.ipPolicyCache.values()) {
			//配置允许访问，并且在排名的之内
			if(ipPolicy.isAllow() && !ipPolicy.isMatch(request.getLoginIp())){
				throw new IpPolicyException("IP不允许访问!");
			}
		}

		
	}
	

}
