package com.sense.iam.api.model.im;


import io.swagger.annotations.ApiModelProperty;

/**
 * 权限模型数据入参对象
 * 
 * Description:  权限模型数据入参对象
 * 
 * @author j_hy
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class SaveModelReq {
	
	Long appId;
	String jsonModelData;
	
	@ApiModelProperty(value="应用ID")
	public Long getAppId() {
		return appId;
	}
	public void setAppId(Long appId) {
		this.appId = appId;
	}
	@ApiModelProperty(value="模型对象数据")
	public String getJsonModelData() {
		return jsonModelData;
	}
	public void setJsonModelData(String jsonModelData) {
		this.jsonModelData = jsonModelData;
	}
	
	
	
}
