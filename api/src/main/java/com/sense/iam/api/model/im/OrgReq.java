package com.sense.iam.api.model.im;

import java.util.Date;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sense.iam.model.im.Org;

import io.swagger.annotations.ApiModelProperty;

/**
 * 组织模型
 * 
 * Description:  岗位信息模型定义
 * 
 * @author j_hy
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class OrgReq {

	/**唯一编码*/
	private Long id;
	/**组织机构代码*/
	private String sn;
	/**组织机构名称*/
	private String name;
	/**组织机构名称*/
	private Long parentId;
	/**父节点编码*/
	private String parentSn;

	private Integer sortNum;
	/**组织机构类型标识*/

	private Long orgTypeId;
	/**组织机构编码路径*/

	private String snPath;
	/**组织机构唯一标识路径*/

	private String idPath;
	/**组织机构名称路径*/

	private String namePath;
	/**创建时间*/
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date createTime;

	/**扩展属性*/
	private Map extraAttrs;
	
	private Integer status;
	
	@ApiModelProperty(value="组织状态 1启动，2失效 ")
	public Integer getStatus() {
		return status;
	}
	
	
	public void setStatus(Integer status) {
		this.status = status;
	}
	
	

	@ApiModelProperty(value="组织唯一标识")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ApiModelProperty(value="组织编号")
	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}

	@ApiModelProperty(value="组织名称")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@ApiModelProperty(value="组织父节点标识")
	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	@ApiModelProperty(value="组织父节点编码")
	public String getParentSn() {
		return parentSn;
	}
	public void setParentSn(String parentSn) {
		this.parentSn = parentSn;
	}


	@ApiModelProperty(value="组织排序")
	public Integer getSortNum() {
		return sortNum;
	}

	public void setSortNum(Integer sortNum) {
		this.sortNum = sortNum;
	}
	@ApiModelProperty(value="组织类型")
	public Long getOrgTypeId() {
		return orgTypeId;
	}

	public void setOrgTypeId(Long orgTypeId) {
		this.orgTypeId = orgTypeId;
	}
	@ApiModelProperty(value="编码路径")
	public String getSnPath() {
		return snPath;
	}

	public void setSnPath(String snPath) {
		this.snPath = snPath;
	}
	@ApiModelProperty(value="编码标识路径")
	public String getIdPath() {
		return idPath;
	}

	public void setIdPath(String idPath) {
		this.idPath = idPath;
	}
	@ApiModelProperty(value="组织名称路径")
	public String getNamePath() {
		return namePath;
	}

	public void setNamePath(String namePath) {
		this.namePath = namePath;
	}
	@ApiModelProperty(value="组织创建时间")
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	@ApiModelProperty(value="组织扩展属性")
	public Map getExtraAttrs() {
		return extraAttrs;
	}

	public void setExtraAttrs(Map extraAttrs) {
		this.extraAttrs = extraAttrs;
	}
	
	@ApiModelProperty(hidden=true)
	public Org getOrg(){
		Org model=new Org();
		model.setId(this.getId());
		model.setName(this.getName());
		model.setSn(this.getSn());
		model.setParentId(this.getParentId());
		model.setSortNum(this.getSortNum());
		model.setOrgTypeId(this.getOrgTypeId());
		model.setSnPath(this.getSnPath());
		model.setIdPath(this.getIdPath());
		model.setNamePath(this.getNamePath());
		model.setExtraAttrs(this.getExtraAttrs());
		model.setCreateTime(this.getCreateTime());
		if(this.getStatus()==null){
			model.setStatus(1);
		}else{
			model.setStatus(this.getStatus());
		}
		
		return model;
	}
	
}
