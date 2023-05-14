package com.sense.iam.api.model.im;

import java.util.HashMap;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;

import com.sense.iam.model.im.Account;



/**
 * 组织重新同步对象
 * 
 * Description:  组织重新同步对象
 * 
 * @author j_hy
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class OrgSyncReq {

	/**
	 * 组织编号
	 */
	private Long orgId;

	
	/**
	 * 应用编号
	 */
	private Long[] appIds;
	
	@ApiModelProperty(value="应用唯一标识集合")
	public Long[] getAppIds() {
		return appIds;
	}

	public void setAppIds(Long[] appIds) {
		this.appIds = appIds;
	}

	@ApiModelProperty(value="组织唯一标识")
	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

	
}
