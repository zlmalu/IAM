package com.sense.iam.api.model.im;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;




/**
 * 权限模型数据
 * 
 * Description:  应用信息模型定义
 * 
 * @author j_hy
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class LoadAppfuncInfoModelDataObjectReq {
	
	List<LoadAppfuncInfoModelDataReq> model;
	
	
	List<TopActive> authActive;
	
	
	
	
	@ApiModelProperty(value="已授权的权限")
	public List<TopActive> getAuthActive() {
		return authActive;
	}


	public void setAuthActive(List<TopActive> authActive) {
		this.authActive = authActive;
	}

	@ApiModelProperty(value="模型数据")
	public List<LoadAppfuncInfoModelDataReq> getModel() {
		return model;
	}


	public void setModel(List<LoadAppfuncInfoModelDataReq> model) {
		this.model = model;
	}

	
	
	
}
