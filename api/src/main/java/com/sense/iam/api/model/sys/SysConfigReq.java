package com.sense.iam.api.model.sys;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import com.sense.iam.model.sys.Config;


/**
 * 系统配置
 * 
 * Description: 系统配置模型
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@ApiModel
public class SysConfigReq {

	/**唯一标识*/
	
	private Long id;
	
	private String name;
	
	private String remark;
	
	private String value;
	
	@ApiModelProperty(value="唯一编码",example="0",required=true)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@ApiModelProperty(value="值", required=true)
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	@ApiModelProperty(value="系统配置名称")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@ApiModelProperty(value="系统配置备注",required=true)
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	@ApiModelProperty(hidden=true)
	public Config getConfig(){
		Config model=new Config();
		model.setId(this.getId());
		model.setName(this.getName());
		model.setRemark(this.getRemark());
		model.setValue(this.getValue());
		return model;
	}	
	
	
	
}
