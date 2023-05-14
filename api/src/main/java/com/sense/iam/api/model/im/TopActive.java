package com.sense.iam.api.model.im;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;





/**
 * 已勾选的权限集合
 * 
 * Description:  应用信息模型定义
 * 
 * @author j_hy
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class TopActive {

	
	String funcId;
	
	String zdId;
	
	String radioTypeId;
	

	@ApiModelProperty(value="权限所属类别")
	public String getRadioTypeId() {
		return radioTypeId;
	}

	public void setRadioTypeId(String radioTypeId) {
		this.radioTypeId = radioTypeId;
	}
	
	
	@ApiModelProperty(value="权限ID")
	public String getFuncId() {
		return funcId;
	}

	public void setFuncId(String funcId) {
		this.funcId = funcId;
	}
	@ApiModelProperty(value="字典ID")
	public String getZdId() {
		return zdId;
	}

	public void setZdId(String zdId) {
		this.zdId = zdId;
	}

	
	
}
