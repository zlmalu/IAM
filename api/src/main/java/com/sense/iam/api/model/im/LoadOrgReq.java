package com.sense.iam.api.model.im;


import io.swagger.annotations.ApiModelProperty;

/**
 * 组织加载条件
 * 
 * Description:  岗位信息模型定义
 * 
 * @author j_hy
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class LoadOrgReq {

	private long id;
	
	private long orgTypeId;
	
	private String name;
	

	private int status=0;


	
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	@ApiModelProperty(value="组织ID")
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@ApiModelProperty(value="组织名称")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	@ApiModelProperty(value="组织类型ID")
	public long getOrgTypeId() {
		return orgTypeId;
	}

	public void setOrgTypeId(long orgTypeId) {
		this.orgTypeId = orgTypeId;
	}
	
	
	
	
	
}
