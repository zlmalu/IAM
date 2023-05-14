package com.sense.iam.api.model.am;


import com.sense.iam.model.am.IpPolicy;

import io.swagger.annotations.ApiModelProperty;

/**
 * ip控制管理 - ModelReq
 * @author K3w1n
 *
 */
public class IpPolicyReq {

	private Long id;
	@ApiModelProperty(value="唯一标识", example="0", required=true)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	/** 认证域编码*/
	private String sn;
	/** 名称*/
	private String name;
	/** 起始IP*/
	private String startIp;
	/** 终止IP*/
	private String endIp;
	/** 是否允许访问*/
	private Long isAllow;
	/**
	 *获取： 认证域编码 
	 *@return the sn 认证域编码
	 */
	public String getSn() {
		return sn;
	}
	/**
	 * 设置： 认证域编码
	 * @param sn 认证域编码
	 */
	@ApiModelProperty(value="认证域编码", required=true)
	public void setSn(String sn) {
		this.sn = sn;
	}
	/**
	 *获取： 名称 
	 *@return the name 名称
	 */
	@ApiModelProperty(value="名称", required=true)
	public String getName() {
		return name;
	}
	/**
	 * 设置： 名称
	 * @param name 名称
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 *获取： 起始IP 
	 *@return the startIp 起始IP
	 */
	@ApiModelProperty(value="起始IP", required=true)
	public String getStartIp() {
		return startIp;
	}
	/**
	 * 设置： 起始IP
	 * @param startIp 起始IP
	 */
	public void setStartIp(String startIp) {
		this.startIp = startIp;
	}
	/**
	 *获取： 终止IP 
	 *@return the endIp 终止IP
	 */
	@ApiModelProperty(value="终止IP", required=true)
	public String getEndIp() {
		return endIp;
	}
	/**
	 * 设置： 终止IP
	 * @param endIp 终止IP
	 */
	public void setEndIp(String endIp) {
		this.endIp = endIp;
	}
	/**
	 *获取： 是否允许访问 
	 *@return the isAllow 是否允许访问
	 */
	@ApiModelProperty(value="是否允许访问", required=true)
	public Long getIsAllow() {
		return isAllow;
	}
	/**
	 * 设置： 是否允许访问
	 * @param isAllow 是否允许访问
	 */
	public void setIsAllow(Long isAllow) {
		this.isAllow = isAllow;
	}
	
	@ApiModelProperty(hidden = true)
	public IpPolicy getIpPolicy() {
		IpPolicy ipPolicy = new IpPolicy();
		ipPolicy.setId(this.getId());
		ipPolicy.setSn(this.getSn());
		ipPolicy.setName(this.getName());
		ipPolicy.setStartIp(this.getStartIp());
		ipPolicy.setEndIp(this.getEndIp());
		ipPolicy.setIsAllow(this.getIsAllow());
		return ipPolicy;
		
	}
	
}
