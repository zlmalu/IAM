package com.sense.iam.api.model;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


/**
 *
 *  用户变更组织参数模型
 *
 * Description:
 *
 * @author w_jfwen
 *
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */

@ApiModel
public class UserMoveOrg {


	Long oldOrgId;
	Long newOrgId;

	List<String> userIds;

	@ApiModelProperty(value="旧组织唯一标识" ,required=true)
	public Long getOldOrgId() {
		return oldOrgId;
	}
	public void setOldOrgId(Long oldOrgId) {
		this.oldOrgId = oldOrgId;
	}

	@ApiModelProperty(value="新组织唯一标识" ,required=true)
	public Long getNewOrgId() {
		return newOrgId;
	}


	public void setNewOrgId(Long newOrgId) {
		this.newOrgId = newOrgId;
	}
	@ApiModelProperty(value="用户唯一标识ID集合,多数据采用英文逗号分割",required=true)
	public List<String> getUserIds() {
		return userIds;
	}
	public void setUserIds(List<String> userIds) {
		this.userIds = userIds;
	}



}

