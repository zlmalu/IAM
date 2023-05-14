package com.sense.iam.api.model.im;


import io.swagger.annotations.ApiModelProperty;

/**
 * 数据查询对象
 * 
 * Description:  应用信息模型定义
 * 
 * @author j_hy
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class UParameterInfo {

	/**
	 * 应用编码
	 */
	private String appSn;

	private Long userId;
	
	

	@ApiModelProperty(value="应用编码")
	public String getAppSn() {
		return appSn;
	}
	public void setAppSn(String appSn) {
		this.appSn = appSn;
	}
	@ApiModelProperty(value="用户唯一标识")
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
}
