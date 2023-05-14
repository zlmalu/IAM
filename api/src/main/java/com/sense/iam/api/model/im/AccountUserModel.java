package com.sense.iam.api.model.im;

import io.swagger.annotations.ApiModelProperty;



public class AccountUserModel {

	/**账号标识*/
	private Long acctId;
	
	/**应用编码*/
	private String sn ;
	
	/**应用编码*/
	private String userSn ;

	public String getUserSn() {
		return userSn;
	}

	public void setUserSn(String userSn) {
		this.userSn = userSn;
	}

	@ApiModelProperty(value="账号唯一标识")
	public Long getAcctId() {
		return acctId;
	}

	public void setAcctId(Long acctId) {
		this.acctId = acctId;
	}
	
	@ApiModelProperty(value="用户工号")
	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}
	
	
	
	
}
