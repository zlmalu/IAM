package com.sense.am.policy;

import com.sense.am.exception.TimePolicyException;
import com.sense.am.model.SSORequest;
import com.sense.am.model.TimePolicy;

/**
 * 
 * 访问时间过滤
 * 
 * Description: 
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class TimePolicyFilter extends BaseFilter{
	
	@Override
	public void doFilter(SSORequest request, String resource) {
		for (TimePolicy timePolicy : PolicyManager.timePolicyCache.values()) {
			if(!timePolicy.isAllow() && timePolicy.isMatch()){
				throw new TimePolicyException("时间段内不允许访问!");
			}
		}
		
	}

}
