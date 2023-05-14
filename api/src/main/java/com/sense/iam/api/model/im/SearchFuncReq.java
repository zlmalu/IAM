package com.sense.iam.api.model.im;

import java.util.List;

import com.sense.iam.model.im.AppDictionary;


import com.sense.iam.model.im.AppElement;

import io.swagger.annotations.ApiModelProperty;

/**
 * 查询关联权限树入参请求
 * 
 * Description:  
 * 
 * @author j_hy
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class SearchFuncReq {
	
	Long objId;
	
	Long accountId;
	
	String ids;
	
	@ApiModelProperty(value="帐号ID")
	public Long getAccountId() {
		return accountId;
	}
	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}
	
	@ApiModelProperty(value="权限对象ID")
	public Long getObjId() {
		return objId;
	}
	public void setObjId(Long objId) {
		this.objId = objId;
	}
	 @ApiModelProperty(value="关联元素集合，多个以小写字母逗号隔开")
	public String getIds() {
		return ids;
	}
	public void setIds(String ids) {
		this.ids = ids;
	}
	
	
	
	
	
	
	
}
