package com.sense.iam.api.model.am;

import com.sense.core.db.DBField;
import com.sense.iam.model.am.AuthReaml;

import io.swagger.annotations.ApiModelProperty;

public class AuthReamlReq {

	/**唯一标识*/
	private Long id;
	/**认证域编码*/
	private String sn;
	/**模块名称*/
	private String name;
	/**模块名称*/
	private Long findId;
	
	/**扩展参数，用于接收页面传入参数*/
	private Long reamlId;
	
	/**认证状态1启用，2禁用*/
	private Integer status;
	/**第一登录模块ID*/
	private Long loginId;
	/**第二登录模块ID*/
	private Long loginId1;
	/**第三登录模块ID*/
	private Long loginId2;
	
	private Long appId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getFindId() {
		return findId;
	}

	public void setFindId(Long findId) {
		this.findId = findId;
	}

	public Long getReamlId() {
		return reamlId;
	}

	public void setReamlId(Long reamlId) {
		this.reamlId = reamlId;
	}

	public Long getLoginId() {
		return loginId;
	}

	public void setLoginId(Long loginId) {
		this.loginId = loginId;
	}

	public Long getLoginId1() {
		return loginId1;
	}

	public void setLoginId1(Long loginId1) {
		this.loginId1 = loginId1;
	}

	public Long getLoginId2() {
		return loginId2;
	}

	public void setLoginId2(Long loginId2) {
		this.loginId2 = loginId2;
	}

	public Long getAppId() {
		return appId;
	}

	public void setAppId(Long appId) {
		this.appId = appId;
	}
	
	
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@ApiModelProperty(hidden=true)
	public AuthReaml getAuthReaml(){
		AuthReaml authReaml=new AuthReaml();
		authReaml.setId(this.getId());
		authReaml.setSn(this.getSn());
		authReaml.setName(this.getName());
		authReaml.setFindId(this.getFindId());
		authReaml.setReamlId(this.getReamlId());
		authReaml.setLoginId(this.getLoginId());
		authReaml.setLoginId1(this.getLoginId1());
		authReaml.setLoginId2(this.getLoginId2());
		authReaml.setAppId(this.getAppId());
		authReaml.setStatus(this.getStatus());
		return authReaml;
	}
	
}
