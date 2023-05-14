package com.sense.iam.api.model.im;

import java.util.List;
import java.util.Map;

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
public class TopData {

	Long id;
	
	String sn;
	
	String name;
	
	Long parentId;
	
	Boolean cheack=false;
	
	Map<String, Object> tabHeadValue;
	
	Map<String, Object> radioNames;
	
	List<TopData> children;
	
	List<TopData> relationFunc;
	
	@ApiModelProperty(value="关联节点")
	public List<TopData> getRelationFunc() {
		return relationFunc;
	}

	public void setRelationFunc(List<TopData> relationFunc) {
		this.relationFunc = relationFunc;
	}
	
	
	@ApiModelProperty(value="RadioNamesg分组")
	public Map<String, Object> getRadioNames() {
		return radioNames;
	}

	public void setRadioNames(Map<String, Object> radioNames) {
		this.radioNames = radioNames;
	}

	@ApiModelProperty(value="权限编号")
	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}
	
	@ApiModelProperty(value="是否勾选")
	public Boolean getCheack() {
		return cheack;
	}

	public void setCheack(Boolean cheack) {
		this.cheack = cheack;
	}
	
	
	@ApiModelProperty(value="权限ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	@ApiModelProperty(value="权限名称")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	@ApiModelProperty(value="父级ID")
	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	@ApiModelProperty(value="字典选中集合")
	public Map<String, Object> getTabHeadValue() {
		return tabHeadValue;
	}

	public void setTabHeadValue(Map<String, Object> tabHeadValue) {
		this.tabHeadValue = tabHeadValue;
	}
	@ApiModelProperty(value="下级节点")
	public List<TopData> getChildren() {
		return children;
	}

	public void setChildren(List<TopData> children) {
		this.children = children;
	}
	
}
