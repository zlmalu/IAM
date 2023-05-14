package com.sense.iam.api.model.im;

import com.sense.iam.model.im.OrgType;

import io.swagger.annotations.ApiModelProperty;

public class OrgTypeReq {

	private Long id;
	
	@ApiModelProperty(value="机构类型唯一编码",required=true)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	/**机构类型编码*/
	private String sn;
	/**机构类型名称*/
	private String name;
	/**机构类型描述，代表根节点的组织机构名称*/
	private String remark;
	
	
	@ApiModelProperty(value="机构类型编码",required=true)
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	@ApiModelProperty(value="机构类型名称",required=true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@ApiModelProperty(value="机构类型描述")
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	@ApiModelProperty(hidden=true)
	public OrgType getOrgType(){
		OrgType orgType=new OrgType();
		orgType.setId(this.getId());
		orgType.setSn(this.sn);
		orgType.setName(this.name);
		orgType.setRemark(this.remark);
		return orgType;
	}
	
	
}
