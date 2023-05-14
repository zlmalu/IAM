package com.sense.iam.api.model;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;


/**
 * 
 *  应用绑定管理员参数模型
 * 
 * Description:  
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */
public class AcctBindApp {
	
	
	List<String> acctIds;
	Long appId;
	
	
	
	@ApiModelProperty(value="系统账户唯一标识ID集合,多数据采用英文逗号分割",required=true)
	public List<String> getAcctIds() {
		return acctIds;
	}
	public void setAcctIds(List<String> acctIds) {
		this.acctIds = acctIds;
	}
	
	
	@ApiModelProperty(value="应用唯一标识",required=true)
	public Long getAppId() {
		return appId;
	}
	public void setAppId(Long appId) {
		this.appId = appId;
	}
	

}

