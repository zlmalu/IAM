package com.sense.iam.api.model;

import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;

import io.swagger.annotations.ApiModelProperty;





/**
 * 
 * 新密码验证
 * 
 * Description:  
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */

public class ValidateRep {

	
	long userId;
	String newpassword;
	
	@ApiModelProperty(value="用户ID")
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	@ApiModelProperty(value="新密码",required=true)
	public String getNewpassword() {
		return newpassword;
	}
	public void setNewpassword(String newpassword) {
		this.newpassword = newpassword;
	}
	

	public  ResultCode cheack(){
		if(newpassword==null||newpassword.trim().length()==0){
			return new ResultCode(Constants.OPERATION_FAIL,"原密码不能为空");
		}
		return new ResultCode(Constants.OPERATION_SUCCESS);
	}
	public  ResultCode ok(){
		return new ResultCode(Constants.OPERATION_SUCCESS);
	}
}
