package com.sense.iam.api.model.sync;


import io.swagger.annotations.ApiModelProperty;


import com.sense.iam.model.sync.Config;


/**
 * 同步配置模型
 * 
 * Description: 定义同步配置模型
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class ConfigSync {

	private Long id;
	
	private String sn;

	private Long appId;

	private Long syncApiId;
	
	private Long sysEventId;

	private String attrXml;
	
	private String contentXml;
	
	private String runClass;
	
	private Integer status;

	
	
	@ApiModelProperty(value="唯一标识")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@ApiModelProperty(value="应用唯一标识")
	public Long getAppId() {
		return appId;
	}
	public void setAppId(Long appId) {
		this.appId = appId;
	}
	
	@ApiModelProperty(value="同步唯一标识")
	public Long getSyncApiId() {
		return syncApiId;
	}
	public void setSyncApiId(Long syncApiId) {
		this.syncApiId = syncApiId;
	}
	@ApiModelProperty(value="状态 1禁用 2启用")
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	@ApiModelProperty(value="同步参数")
	public String getAttrXml() {
		return attrXml;
	}
	public void setAttrXml(String attrXml) {
		this.attrXml = attrXml;
	}
	@ApiModelProperty(value="同步内容")
	public String getContentXml() {
		return contentXml;
	}
	public void setContentXml(String contentXml) {
		this.contentXml = contentXml;
	}
	@ApiModelProperty(value="事件唯一标识")
	public Long getSysEventId() {
		return sysEventId;
	}
	public void setSysEventId(Long sysEventId) {
		this.sysEventId = sysEventId;
	}
	@ApiModelProperty(value="同步编码")
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	@ApiModelProperty(value="同步类路径")
	public String getRunClass() {
		return runClass;
	}
	public void setRunClass(String runClass) {
		this.runClass = runClass;
	}
	
	
	@ApiModelProperty(hidden=true)
	public Config getConfig(){
		Config model=new Config();
		model.setId(this.getId());
		model.setAppId(this.getAppId());
		model.setSn(this.getSn());
		model.setSyncApiId(this.getSyncApiId());
		model.setSysEventId(this.getSysEventId());
		model.setStatus(this.getStatus());
		model.setAttrXml(this.getAttrXml());
		model.setRunClass(this.getRunClass());
		model.setContentXml(this.getContentXml());
		return model;
	}	
	
	
	
	
	
}
