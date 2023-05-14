package com.sense.iam.api.model;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Set;

import com.sense.iam.cam.TreeNode;




/**
 * 
 * 系统角色权限对象
 * 
 * Description:  
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */

public class FuncTree {

	
	private List<TreeNode> list;
	
	
	private Set<Long> authFuncs;

	@ApiModelProperty(value="所有系统权限对象")
	public List<TreeNode> getList() {
		return list;
	}


	public void setList(List<TreeNode> list) {
		this.list = list;
	}

	@ApiModelProperty(value="已勾选的权限对象")
	public Set<Long> getAuthFuncs() {
		return authFuncs;
	}


	public void setAuthFuncs(Set<Long> authFuncs) {
		this.authFuncs = authFuncs;
	}
	
	
}
