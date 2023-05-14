package com.sense.iam.api.model;

import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;

import io.swagger.annotations.ApiModelProperty;





/**
 * 
 * 密码修改
 * 
 * Description:  
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */

public class PwdRep {

	
	long userId;
	
	String oldPassword;
	
	String newpassword;
	
	String appSn;
	
	
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
	
	@ApiModelProperty(value="历史密码")
	public String getOldPassword() {
		return oldPassword;
	}
	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}
	
	@ApiModelProperty(value="appSn为指定的应用编码")
	public String getAppSn() {
		return appSn;
	}
	public void setAppSn(String appSn) {
		this.appSn = appSn;
	}
	
	
	public  ResultCode cheack(){
		if(getUserId()==0){
			return new ResultCode(Constants.OPERATION_FAIL,"用户ID不能为空");
		}
		if(getOldPassword()==null||getOldPassword().trim().length()==0){
			return new ResultCode(Constants.OPERATION_FAIL,"原密码不能为空");
		}
		if(getNewpassword()==null||getNewpassword().trim().length()==0){
			return new ResultCode(Constants.OPERATION_FAIL,"新密码不能为空");
		}
		if(getAppSn()==null||getAppSn().trim().length()==0){
			return new ResultCode(Constants.OPERATION_FAIL,"应用标识不能为空");
		}
		if(getOldPassword()==getNewpassword()){
			return new ResultCode(Constants.OPERATION_FAIL,"原密码和新密码不能相同");
		}
		return new ResultCode(Constants.OPERATION_SUCCESS);
	}

}
