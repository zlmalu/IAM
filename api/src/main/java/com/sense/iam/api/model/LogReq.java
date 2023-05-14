package com.sense.iam.api.model;

import io.swagger.annotations.ApiModelProperty;





/**
 * 
 * 日志输出
 * 
 * Description:  
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */

public class LogReq {

	
	String remark;
	
	Long time;
	
	String timeFmtString;
	
	String status;

	@ApiModelProperty(value="描述")
	public String getRemark() {
		return remark;
	}

	@ApiModelProperty(value="时间")
	public String getTimeFmtString() {
		return timeFmtString;
	}


	public void setTimeFmtString(String timeFmtString) {
		this.timeFmtString = timeFmtString;
	}
	
 
	public void setRemark(String remark) {
		this.remark = remark;
	}

	@ApiModelProperty(value="毫秒时间")
	public Long getTime() {
		return time;
	}


	public void setTime(Long time) {
		this.time = time;
	}

	@ApiModelProperty(value="操作状态")
	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}
	
	
	
	
}
