package com.sense.iam.api.model.im;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;



public class OrgPositionTree {
	
	private long id;
	
    private long parent_id;
    
    private String name;
    
    private String sn;
    
    private List<PositionReq> positionList;
    
    @ApiModelProperty(value="机构类型唯一编码")
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	@ApiModelProperty(value="组织父节点ID")
	public long getParent_id() {
		return parent_id;
	}
	public void setParent_id(long parent_id) {
		this.parent_id = parent_id;
	}
	@ApiModelProperty(value="组织名称")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@ApiModelProperty(value="组织SN")
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	@ApiModelProperty(value="岗位对象集合")
	public List<PositionReq> getPositionList() {
		return positionList;
	}
	public void setPositionList(List<PositionReq> positionList) {
		this.positionList = positionList;
	}
	

}
