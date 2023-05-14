package com.sense.iam.api.model.im;

import java.util.List;
import io.swagger.annotations.ApiModelProperty;




/**
 * 权限模型数据
 * 
 * Description:  应用信息模型定义
 * 
 * @author j_hy
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class LoadAppfuncInfoModelDataReq {
	

	Long id;
	
	String name;
	
	List<TopHead> tabHead;
	
	List<TopData> data;
	
	Integer authType;
	
	Boolean isRelation;
	
	List<Long> cheack;
	
	
	@ApiModelProperty(value="当前元素是否存在其他元素关联",hidden=true)
	public Boolean getIsRelation() {
		return isRelation;
	}


	public void setIsRelation(Boolean isRelation) {
		this.isRelation = isRelation;
	}


	@ApiModelProperty(value="已勾选权限")
	public List<Long> getCheack() {
		return cheack;
	}
	

	public void setCheack(List<Long> cheack) {
		this.cheack = cheack;
	}

	@ApiModelProperty(value="元素ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	@ApiModelProperty(value="元素名称")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	@ApiModelProperty(value="字典集合")
	public List<TopHead> getTabHead() {
		return tabHead;
	}

	public void setTabHead(List<TopHead> tabHead) {
		this.tabHead = tabHead;
	}
	
	@ApiModelProperty(value="权限数据集合")
	public List<TopData> getData() {
		return data;
	}

	public void setData(List<TopData> data) {
		this.data = data;
	}
	
	@ApiModelProperty(value="授权方式")
	public Integer getAuthType() {
		return authType;
	}

	public void setAuthType(Integer authType) {
		this.authType = authType;
	}

}
