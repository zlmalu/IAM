package com.sense.iam.api.model.sys;

import java.util.ArrayList;
import java.util.List;

import com.sense.iam.model.sys.Acct;
import com.sense.iam.model.sys.Role;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class AcctReq {
	
	private Long id;
	
	@ApiModelProperty(value="唯一编码",example="0",required=true)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	/**登录名*/
	private String loginName;
	/**登录密码*/
	private String loginPwd;
	/**用户姓名*/
	private String name;
	/**联系电话*/
	private String tel;
	/**电子邮件*/
	private String email;
	/**帐号状态1启用  2禁用*/
	private Integer status;
	
	private List<Long> roleIds;
	
	//分级授权时的组织机构标识
	private Long orgId;

	@ApiModelProperty(value="分级授权时的组织机构标识")
	public Long getOrgId() {
		return orgId;
	}
	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}
	@ApiModelProperty(value="登录名",example="example",required=true)
	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	@ApiModelProperty(value="登陆密码",example="example",required=true)
	public String getLoginPwd() {
		return loginPwd;
	}

	public void setLoginPwd(String loginPwd) {
		this.loginPwd = loginPwd;
	}

	@ApiModelProperty(value="用户姓名",example="example",required=true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ApiModelProperty("联系电话")
	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	@ApiModelProperty("电子邮件")
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@ApiModelProperty(value="状态 1 为正常,2 禁用",example="1",required=true)
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@ApiModelProperty("拥有角色ID列表")
	public List<Long> getRoleIds() {
		return roleIds;
	}

	public void setRoleIds(List<Long> roleIds) {
		this.roleIds = roleIds;
	}
	
	@ApiModelProperty(hidden=true)
	public Acct getAcct(){
		Acct acct=new Acct();
		acct.setId(this.getId());
		acct.setLoginName(this.getLoginName());
		acct.setLoginPwd(this.getLoginPwd());
		acct.setStatus(this.getStatus());
		acct.setName(this.getName());
		acct.setTel(this.getTel());
		acct.setEmail(this.getEmail());
		List<Role> roles=new ArrayList<Role>();
		if(roleIds!=null)roleIds.forEach(e -> {roles.add(new Role(){{setId(Long.valueOf(e));}});});
		acct.setRoles(roles);
		return acct;
	}
}
