package com.sense.iam.api.model.im;

import io.swagger.annotations.ApiModelProperty;





/**
 * 响应实体
 * 
 * 
 * @author j_hy
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class UserResp {
	
	
	
	//用户相关
	long userId;
	String sn;
	String name;
	int sex;
	String email;
	String telephone;
	int status;
	
	//组织相关
	long orgId;
	String orgName;
	String orgSn;
	
	
	@ApiModelProperty(value="用户ID")
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	@ApiModelProperty(value="用户编码")
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	@ApiModelProperty(value="姓名")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@ApiModelProperty(value="性别 1男  2女")
	public int getSex() {
		return sex;
	}
	public void setSex(int sex) {
		this.sex = sex;
	}
	@ApiModelProperty(value="邮箱")
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	@ApiModelProperty(value="手机号")
	public String getTelephone() {
		return telephone;
	}
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	public int getStatus() {
		return status;
	}
	@ApiModelProperty(value="状态 1正常  2禁用")
	public void setStatus(int status) {
		this.status = status;
	}
	@ApiModelProperty(value="所属组织ID")
	public long getOrgId() {
		return orgId;
	}
	public void setOrgId(long orgId) {
		this.orgId = orgId;
	}
	@ApiModelProperty(value="所属组织名称")
	public String getOrgName() {
		return orgName;
	}
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	@ApiModelProperty(value="所属组织编码")
	public String getOrgSn() {
		return orgSn;
	}
	public void setOrgSn(String orgSn) {
		this.orgSn = orgSn;
	}
	
	
	
		
}
