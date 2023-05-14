package com.sense.iam.api.model;

import io.swagger.annotations.ApiModelProperty;


public class AccountEditPwd {
	
	
	Long[] ids;
	
	
	String pwd;


	@ApiModelProperty(value="帐号唯一标识集合")
	public Long[] getIds() {
		return ids;
	}


	public void setIds(Long[] ids) {
		this.ids = ids;
	}


	@ApiModelProperty(value="新密码")
	public String getPwd() {
		return pwd;
	}


	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	
	
	
	

}
