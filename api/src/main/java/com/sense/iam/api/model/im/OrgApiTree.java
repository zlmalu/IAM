package com.sense.iam.api.model.im;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class OrgApiTree {

	private long id;
    private String sn;
    private String name;
    private long parentId;
    private String name_path;
    private Integer status;
    private List<OrgApiTree> children;
    
    
    @ApiModelProperty(value="子节点")
	public List<OrgApiTree> getChildren() {
		return children;
	}
	public void setChildren(List<OrgApiTree> children) {
		this.children = children;
	}
	
	@ApiModelProperty(value="组织路径")
	public String getName_path() {
		return name_path;
	}
	public void setName_path(String name_path) {
		this.name_path = name_path;
	}
	@ApiModelProperty(value="组织唯一标识")
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	@ApiModelProperty(value="父组织ID")
	public long getParentId() {
		return parentId;
	}
	public void setParentId(long parentId) {
		this.parentId = parentId;
	}
	@ApiModelProperty(value="组织名称")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@ApiModelProperty(value="组织编码")
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	
	@ApiModelProperty(value="组织状态  1正常 2禁用")
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
    
       

}
