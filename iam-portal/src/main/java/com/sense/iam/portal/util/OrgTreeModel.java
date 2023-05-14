package com.sense.iam.portal.util;

import java.util.List;


public class OrgTreeModel {
	Long id;
	String title;
	Long parentId;
	List<OrgTreeModel> children;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Long getParentId() {
		return parentId;
	}
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	public List<OrgTreeModel> getChildren() {
		return children;
	}
	public void setChildren(List<OrgTreeModel> children) {
		this.children = children;
	}
	
}
