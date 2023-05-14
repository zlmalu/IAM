package com.sense.iam.api.model.im;


import io.swagger.annotations.ApiModelProperty;




/**
 * 权限模型数据
 * 
 * Description:  应用信息模型定义
 * 
 * @author j_hy
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class TopHead {
	
	Long id;
	String label;
	String property;
	String isOne;
	
	String radioVal;
	
	String radioName;
	
	
	@ApiModelProperty(value="单选name")
	public String getRadioName() {
		return radioName;
	}
	public void setRadioName(String radioName) {
		this.radioName = radioName;
	}
	
	@ApiModelProperty(value="单选选中值")
	public String getRadioVal() {
		return radioVal;
	}
	public void setRadioVal(String radioVal) {
		this.radioVal = radioVal;
	}
	
	@ApiModelProperty(value="是否单选 1单选 2多选")
	public String getIsOne() {
		return isOne;
	}
	public void setIsOne(String isOne) {
		this.isOne = isOne;
	}
	
	
	@ApiModelProperty(value="字典ID")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@ApiModelProperty(value="字典名称")
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	@ApiModelProperty(value="字典标识")
	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}
	
}
