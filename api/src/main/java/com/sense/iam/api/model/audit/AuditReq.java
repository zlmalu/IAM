package com.sense.iam.api.model.audit;

import java.util.Map;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 日志审计请求对象
 * 
 * Description:  
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */
@ApiModel("日志请求对象")
public class AuditReq {

	/**
	 * 日志配置对象编码
	 */
	private Long logConfigId;
	
	private Map paramMap;

	@ApiModelProperty(name="日志配置编码")
	public Long getLogConfigId() {
		return logConfigId;
	}

	public void setLogConfigId(Long logConfigId) {
		this.logConfigId = logConfigId;
	}

	@ApiModelProperty(name="扩展查询参数")
	public Map getParamMap() {
		return paramMap;
	}

	public void setParamMap(Map paramMap) {
		this.paramMap = paramMap;
	}
	
	
	
}
