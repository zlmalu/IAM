package com.sense.iam.api.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 树节点对象
 * 
 * Description: 定义进行树形结构的数据查询时，返回的数据结构 
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@SuppressWarnings("rawtypes")
public class PosTreeNode {

	/**节点ID*/
	private String id;
	/**节点名称*/
	private String text;
	
	private Long pid;
	
	private int type;
	
	
	public Long getPid() {
		return pid;
	}
	public void setPid(Long pid) {
		this.pid = pid;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	/**是否最终级别*/
	private Boolean leaf;
	/**扩展属性*/
	private Map attrMap;
	/**是否允许选中*/
	private Boolean checked;
	/**节点样式*/
	private String iconCls;
	/**子集*/
	private List<PosTreeNode> children;
	
	private boolean expanded=false;
	
	private String parentId;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Boolean getLeaf() {
		return leaf;
	}
	public void setLeaf(Boolean leaf) {
		this.leaf = leaf;
	}
	public List<PosTreeNode> getChildren() {
		return children;
	}
	public void setChildren(List<PosTreeNode> children) {
		this.children = children;
	}
	public Map getAttrMap() {
		if(attrMap==null){
			attrMap=new HashMap();
		}
		return attrMap;
	}
	public void setAttrMap(Map attrMap) {
		this.attrMap = attrMap;
	}
	public Boolean getChecked() {
		return checked;
	}
	public void setChecked(Boolean checked) {
		this.checked = checked;
	}
	public String getIconCls() {
		return iconCls;
	}
	public void setIconCls(String iconCls) {
		this.iconCls = iconCls;
	}
	public boolean isExpanded() {
		return expanded;
	}
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}
	
	@JsonIgnore
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	
	
}
