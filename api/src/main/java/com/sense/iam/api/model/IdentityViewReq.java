package com.sense.iam.api.model;

import java.util.List;

import com.sense.iam.model.im.App;
import com.sense.iam.model.im.Org;
import com.sense.iam.model.im.Position;
import com.sense.iam.model.im.User;

import io.swagger.annotations.ApiModelProperty;





/**
 * 
 * 用户身份视图实体对象
 * 
 * Description:  
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */

public class IdentityViewReq {

	
	User user;
	
	Org org;
	
	List<Position> positionList;
	
	List<App> appList;
	
	List<LogReq> logList;
	
	
	int code;
	
	List<Org> multiorgs;
	
	@ApiModelProperty(value="兼职组织对象集合")
	public List<Org> getMultiorgs() {
		return multiorgs;
	}


	public void setMultiorgs(List<Org> multiorgs) {
		this.multiorgs = multiorgs;
	}

	@ApiModelProperty(value="用户对象")
	public User getUser() {
		return user;
	}


	public void setUser(User user) {
		this.user = user;
	}

	@ApiModelProperty(value="组织对象")
	public Org getOrg() {
		return org;
	}


	public void setOrg(Org org) {
		this.org = org;
	}

	@ApiModelProperty(value="岗位集合")
	public List<Position> getPositionList() {
		return positionList;
	}


	public void setPositionList(List<Position> positionList) {
		this.positionList = positionList;
	}

	@ApiModelProperty(value="应用集合")
	public List<App> getAppList() {
		return appList;
	}


	public void setAppList(List<App> appList) {
		this.appList = appList;
	}

	@ApiModelProperty(value="行为追随对象")
	public List<LogReq> getLogList() {
		return logList;
	}


	public void setLogList(List<LogReq> logList) {
		this.logList = logList;
	}

	@ApiModelProperty(value="返回状态")
	public int getCode() {
		return code;
	}


	public void setCode(int code) {
		this.code = code;
	}
	
	
	
	
	
	
	
}
