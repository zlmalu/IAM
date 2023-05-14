package com.sense.iam.api.model.im;

import java.util.List;

import com.sense.iam.model.im.AppDictionary;


import com.sense.iam.model.im.AppElement;

import io.swagger.annotations.ApiModelProperty;

/**
 * 响应参数
 * 
 * Description:  岗位信息模型定义
 * 
 * @author j_hy
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class AppDictionaryReq {
	
	AppElement node;
	
	@ApiModelProperty(value="元素对象")
	public AppElement getNode() {
		return node;
	}
	public void setNode(AppElement node) {
		this.node = node;
	}
	List<AppDictionary> allList;
	List<Long> cheackList;
	
	@ApiModelProperty(value="全部字典")
	public List<AppDictionary> getAllList() {
		return allList;
	}
	public void setAllList(List<AppDictionary> allList) {
		this.allList = allList;
	}
	
	@ApiModelProperty(value="已选字典")
	public List<Long> getCheackList() {
		return cheackList;
	}
	public void setCheackList(List<Long> cheackList) {
		this.cheackList = cheackList;
	}
	
	
	
}
