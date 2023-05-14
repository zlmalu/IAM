package com.sense.iam.api.model;

import java.util.HashMap;
import java.util.Map;


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
public class PostionFuncTree {

	/**节点ID*/
	private String id;
	/**节点名称*/
	private String text;
	
	private Long appId;
	
	private Long parentId;
	/**扩展属性*/
	private Map attrMap;
	/**是否允许选中*/
	private Boolean checked;
	
	
	public Long getParentId() {
		return parentId;
	}
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
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
	public Long getAppId() {
		return appId;
	}
	public void setAppId(Long appId) {
		this.appId = appId;
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
	
	
}
