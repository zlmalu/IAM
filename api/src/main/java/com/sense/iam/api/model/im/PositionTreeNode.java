package com.sense.iam.api.model.im;

import io.swagger.annotations.ApiModelProperty;

/**
 * 岗位应用模型
 * 
 * Description:  岗位信息模型定义
 * 
 * @author j_hy
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class PositionTreeNode{
	
	Long id;
	
	Integer type;
	
	Long positionId;
	
	@ApiModelProperty(value="权限ID标识")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@ApiModelProperty(value="加载上级ID")
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	
	@ApiModelProperty(value="当加载应用类型时为-1或null，加载应用时应用类型id，加载应用权限时为应用id")
	public Long getPositionId() {
		return positionId;
	}
	public void setPositionId(Long positionId) {
		this.positionId = positionId;
	}
	
	
	
}
