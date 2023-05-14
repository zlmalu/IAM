package com.sense.iam.api.model;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;


/**
 * 
 *  组织绑定系统帐号参数模型
 * 
 * Description:  
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */
public class BindAccByOrg {
	
	
	Long orgId;

	List<String> acctIds;
	
	@ApiModelProperty(value="组织唯一标识" ,required=true)
	public Long getOrgId() {
		return orgId;
	}
	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}
	
	@ApiModelProperty(value="系统账户唯一标识ID集合,多数据采用英文逗号分割",required=true)
	public List<String> getAcctIds() {
		return acctIds;
	}
	public void setAcctIds(List<String> acctIds) {
		this.acctIds = acctIds;
	}
	
	
	
}

