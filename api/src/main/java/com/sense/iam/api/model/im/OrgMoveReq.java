package com.sense.iam.api.model.im;

import java.util.Date;

import com.sense.iam.model.im.App;

import io.swagger.annotations.ApiModelProperty;

/**
 * 组织移动
 * 
 * Description:  
 * 
 * @author j_hy
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class OrgMoveReq {

	private Long oldOrgId;
	
	
	private Long newOrgId;


	@ApiModelProperty(value="旧组织ID")
	public Long getOldOrgId() {
		return oldOrgId;
	}


	public void setOldOrgId(Long oldOrgId) {
		this.oldOrgId = oldOrgId;
	}

	@ApiModelProperty(value="新组织ID")
	public Long getNewOrgId() {
		return newOrgId;
	}


	public void setNewOrgId(Long newOrgId) {
		this.newOrgId = newOrgId;
	}
	
	
	
}
