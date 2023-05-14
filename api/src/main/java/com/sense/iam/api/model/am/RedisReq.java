package com.sense.iam.api.model.am;

import io.swagger.annotations.ApiModelProperty;


/**
 * 用户会话
 * @author sbl
 *
 */
public class RedisReq {

	private String sessionId;
	
	private String username;
	
	private String ip;
	
	private String sn;
	
	private long userId;
	
	private long accountId;
	
	private long timeOut;

	private String data;
	
	private String name;
	
	private String enTrdata;
	
	private String remark;
	
	private String createTime;
	
	
	private String device;
	
	
	
	@ApiModelProperty(value="设备")
	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	@ApiModelProperty(value="认证时间")
	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	@ApiModelProperty(value="备注")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@ApiModelProperty(value="姓名")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ApiModelProperty(value="会话唯一标识")
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	@ApiModelProperty(value="认证账号")
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	@ApiModelProperty(value="IP地址")
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	@ApiModelProperty(value="用户ID")
	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}
	@ApiModelProperty(value="账号ID")
	public long getAccountId() {
		return accountId;
	}

	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}
	@ApiModelProperty(value="redis剩余过期时长（单位毫秒）")
	public long getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(long timeOut) {
		this.timeOut = timeOut;
	}
	@ApiModelProperty(value="解密串")
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
	@ApiModelProperty(value="加密串")
	public String getEnTrdata() {
		return enTrdata;
	}

	public void setEnTrdata(String enTrdata) {
		this.enTrdata = enTrdata;
	}

	@ApiModelProperty(value="用户工号")
	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}
	
	
	
	
	
	
}
