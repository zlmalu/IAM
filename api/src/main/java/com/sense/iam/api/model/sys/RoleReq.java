package com.sense.iam.api.model.sys;

import java.util.ArrayList;
import java.util.List;

import com.sense.iam.model.sys.Func;
import com.sense.iam.model.sys.Role;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class RoleReq {
private Long id;
	
	@ApiModelProperty(value="唯一编码",required=true)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	private String sn;
	private String name;
	private String remark;
	/**
	 * 角色包含的功能
	 */
	private List<Long> funcIds;
	
	@ApiModelProperty(value="角色编号",required=true)
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	@ApiModelProperty(value="角色名称",required=true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@ApiModelProperty(value="角色描述",required=false)
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	@ApiModelProperty(value="关联系统功能",required=true)
	public List<Long> getFuncIds() {
		return funcIds;
	}
	public void setFuncIds(List<Long> funcIds) {
		this.funcIds = funcIds;
	}
	
	@ApiModelProperty(hidden=true)
	public Role getRole(){
		Role role=new Role();
		role.setId(this.getId());
		role.setSn(sn);
		role.setName(name);
		role.setRemark(remark);
		List<Func> funcs=new ArrayList<Func>();
		if(funcIds!=null)funcIds.forEach(e -> {
			funcs.add(new Func(){{setId(Long.valueOf(e));}});});
		role.setFuncs(funcs);
		return role;
	}
	
}
