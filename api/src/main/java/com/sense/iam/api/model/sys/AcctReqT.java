package com.sense.iam.api.model.sys;



import com.sense.iam.model.sys.Acct;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class AcctReqT {
	
	
	/**登录名*/
	private String loginName;
	/**用户姓名*/
	private String name;
	/**联系电话*/
	private String tel;
	/**电子邮件*/
	private String email;
	
	/**帐号状态1启用  2禁用*/
	private Integer status;
	
	private Long appId;

	@ApiModelProperty(value="登录名")
	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	@ApiModelProperty(value="姓名")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	@ApiModelProperty(value="手机号码")
	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}
	@ApiModelProperty(value="邮箱地址")
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	@ApiModelProperty(value="状态 1正常  2禁用")
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	@ApiModelProperty(value="应用唯一标识")
	public Long getAppId() {
		return appId;
	}

	public void setAppId(Long appId) {
		this.appId = appId;
	}
	


	
	@ApiModelProperty(hidden=true)
	public Acct getAcct(){
		Acct acct=new Acct();
		acct.setLoginName(this.getLoginName());
		acct.setStatus(this.getStatus());
		acct.setName(this.getName());
		acct.setTel(this.getTel());
		acct.setEmail(this.getEmail());
		acct.setAppId(this.getAppId());
		return acct;
	}
}
