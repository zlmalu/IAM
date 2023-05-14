package com.sense.iam.api.model.im;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Map;

import com.sense.iam.cam.Params;
import com.sense.iam.model.im.Account;



/**
 * 权限模型
 * 
 * Description:  权限模型
 * 
 * @author j_hy
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class FuncModelReq {

	Long appId;
	String appName;
	
	List<Map<String,Object>> fModel;
	
	String jsonData;

	@ApiModelProperty(value="应用ID")
	public Long getAppId() {
		return appId;
	}

	public void setAppId(Long appId) {
		this.appId = appId;
	}

	@ApiModelProperty(value="应用名称")
	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}
	@ApiModelProperty(value="权限元素")
	public List<Map<String, Object>> getfModel() {
		return fModel;
	}

	public void setfModel(List<Map<String, Object>> fModel) {
		this.fModel = fModel;
	}
	@ApiModelProperty(value="权限模型数据结构")
	public String getJsonData() {
		return jsonData;
	}

	public void setJsonData(String jsonData) {
		this.jsonData = jsonData;
	}
	
	
}
