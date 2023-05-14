package com.sense.iam.api.model.im;

import java.util.HashMap;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;

import com.sense.iam.model.im.Account;



/**
 * 帐号模型
 * 
 * Description:  应用信息模型定义
 * 
 * @author j_hy
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class AccountReq {

	/**
	 * 编号
	 */
	private Long id;
	/**
	 * 登录名
	 */
	private String loginName;
	/**
	 * 登录密码
	 */
	private String loginPwd;
	/**
	 * 应用编号
	 */
	private Long appId;
	
	
	private String appName;
	
	private Integer tagEndType;
	
	@ApiModelProperty(value="终端类型")
	public Integer getTagEndType() {
		return tagEndType;
	}
	public void setTagEndType(Integer tagEndType) {
		this.tagEndType = tagEndType;
	}

	@ApiModelProperty(value="应用名称")
	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}
	/**
	 * 用户编号
	 */
	private Long userId;
	
	/**
	 * account status 1 active ,2 lock
	 */
	private Integer status;
	
	/**开通类型  1 默认开通，2 通过岗位开通*/
	private Integer openType;
	
	
	/**帐号类型  1一般帐号，2 接口帐号 3 公共帐号 4 特权帐号*/
	private Integer acctType;
	
	@ApiModelProperty(value="唯一标识")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	@ApiModelProperty(value="登录帐号")
	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	
	@ApiModelProperty(value="登录密码")
	public String getLoginPwd() {
		return loginPwd;
	}

	public void setLoginPwd(String loginPwd) {
		this.loginPwd = loginPwd;
	}
	@ApiModelProperty(value="状态 1 启用 ,2 禁用")
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	@ApiModelProperty(value="应用唯一标识")
	public Long getAppId() {
		return appId;
	}

	public void setAppId(Long appId) {
		this.appId = appId;
	}

	@ApiModelProperty(value="归属用户ID")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	@ApiModelProperty(value="开通类型  1 默认开通，2 通过岗位开通")
	public Integer getOpenType() {
		return openType;
	}

	public void setOpenType(Integer openType) {
		this.openType = openType;
	}
	
	@ApiModelProperty(value="帐号类型  , 1 一般帐号   2 接口帐号  3 公共帐号   4 特权帐号")
	public Integer getAcctType() {
		return acctType;
	}

	public void setAcctType(Integer acctType) {
		this.acctType = acctType;
	}
	private Map extraAttrs;
	
	@ApiModelProperty(value="扩展字段集合,key-value形式")
	public Map getExtraAttrs() {
		if(extraAttrs==null)extraAttrs=new HashMap();
		return extraAttrs;
	}

	public void setExtraAttrs(Map extraAttrs) {
		this.extraAttrs = extraAttrs;
	}


	@ApiModelProperty(hidden=true)
	public Account getAccount(){
		Account model=new Account();
		model.setAppName(this.getAppName());
		model.setId(this.getId());
		model.setAcctType(this.getAcctType());
		model.setAppId(this.getAppId());
		model.setUserId(this.getUserId());
		model.setLoginName(this.getLoginName());
		model.setLoginPwd(this.getLoginPwd());
		model.setStatus(this.getStatus());
		model.setOpenType(this.getOpenType());
		model.setExtraAttrs(this.getExtraAttrs());
		model.setTagEndType(this.getTagEndType());
		return model;
	}
	
}
