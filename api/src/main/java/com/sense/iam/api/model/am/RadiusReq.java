package com.sense.iam.api.model.am;

import com.sense.iam.model.am.Radius;

import io.swagger.annotations.ApiModelProperty;

public class RadiusReq {
	
	private Long id;
	@ApiModelProperty(value="唯一标识", example="0", required=true)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	/** 应用工号 */
	public String sn;
	
	/**Radius白名单客户端IP*/
	public String ip;
	
	/**管理密码*/
	public String password;
	
	/**管理账号*/
	public String manager;
	
	/**双方规定秘钥*/
	public String sharedSecret;
	
	/**应用名称*/
	public String appName;
	
	/**应用编号*/
	public Long appId;
	/**
	 *获取： 应用工号 
	 *@return the sn 应用工号
	 */
	@ApiModelProperty(value="应用工号")
	public String getSn() {
		return sn;
	}
	/**
	 * 设置： 应用工号
	 * @param sn 应用工号
	 */
	public void setSn(String sn) {
		this.sn = sn;
	}
	/**
	 *获取： Radius白名单客户端IP 
	 *@return the ip Radius白名单客户端IP
	 */
	@ApiModelProperty(value="Radius白名单客户端IP")
	public String getIp() {
		return ip;
	}
	/**
	 * 设置： Radius白名单客户端IP
	 * @param ip Radius白名单客户端IP
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}
	/**
	 *获取： 管理密码 
	 *@return the password 管理密码
	 */
	@ApiModelProperty(value="管理密码")
	public String getPassword() {
		return password;
	}
	/**
	 * 设置： 管理密码
	 * @param password 管理密码
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 *获取： 管理账号 
	 *@return the manager 管理账号
	 */
	@ApiModelProperty(value="管理账号")
	public String getManager() {
		return manager;
	}
	/**
	 * 设置： 管理账号
	 * @param manager 管理账号
	 */
	public void setManager(String manager) {
		this.manager = manager;
	}
	/**
	 *获取： 双方规定秘钥 
	 *@return the sharedSecret 双方规定秘钥
	 */
	@ApiModelProperty(value="双方规定密钥")
	public String getSharedSecret() {
		return sharedSecret;
	}
	/**
	 * 设置： 双方规定秘钥
	 * @param sharedSecret 双方规定秘钥
	 */
	public void setSharedSecret(String sharedSecret) {
		this.sharedSecret = sharedSecret;
	}
	/**
	 *获取： 应用名称 
	 *@return the appName 应用名称
	 */
	@ApiModelProperty(value="应用名称")
	public String getAppName() {
		return appName;
	}
	/**
	 * 设置： 应用名称
	 * @param appName 应用名称
	 */
	public void setAppName(String appName) {
		this.appName = appName;
	}
	/**
	 *获取： 应用编号 
	 *@return the appId 应用编号
	 */
	@ApiModelProperty(value="应用编号")
	public Long getAppId() {
		return appId;
	}
	/**
	 * 设置： 应用编号
	 * @param appId 应用编号
	 */
	public void setAppId(Long appId) {
		this.appId = appId;
	}
	
	@ApiModelProperty(hidden = true)
	public Radius getRadius() {
		Radius radius = new Radius();
		radius.setId(this.getId());
		radius.setSn(this.getSn());
		radius.setIp(this.getIp());
		radius.setPassword(this.getPassword());
		radius.setManager(this.getManager());
		radius.setSharedSecret(this.getSharedSecret());
		radius.setAppName(this.getAppName());
		radius.setAppId(this.getAppId());
		return radius;
	}
	
	
	
	
	
}
