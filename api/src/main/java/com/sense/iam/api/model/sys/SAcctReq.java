package com.sense.iam.api.model.sys;

import com.sense.iam.model.sys.Acct;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class SAcctReq {
	
	/**登录名*/
	private String loginName;

	/**用户姓名*/
	private String name;
	
	//分级授权时的组织机构标识
	private Long orgId;

	
	
	@ApiModelProperty(value="分级授权时的组织机构标识")
	public Long getOrgId() {
		return orgId;
	}
	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}
	@ApiModelProperty(value="登录名")
	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	@ApiModelProperty(value="用户姓名",required=true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	@ApiModelProperty(hidden=true)
	public Acct getAcct(){
		Acct acct=new Acct();
		acct.setLoginName(this.getLoginName());
		acct.setOrgId(this.getOrgId());
		acct.setName(this.getName());
		return acct;
	}
}
