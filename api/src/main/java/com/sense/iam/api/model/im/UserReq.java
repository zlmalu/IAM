package com.sense.iam.api.model.im;

import java.util.Date;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.annotations.ApiModelProperty;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sense.iam.model.im.User;


/**
 * 用户模型
 * 
 * Description:  岗位信息模型定义
 * 
 * @author j_hy
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class UserReq{
	
	/**用户唯一标识*/
	private Long id;
	/**用户工号*/
	
	private String sn;
	/**用户名称*/
	private String name;

	/**用户 归属类型ID*/
	private Long userTypeId;
	
	/**组织归属类型ID*/
	private Long orgTypeId;
	
	/**用户临时图片*/
	private String tempImageId;
	
	public Long getOrgTypeId() {
		return orgTypeId;
	}

	@ApiModelProperty(value="用户临时图片")
	public String getTempImageId() {
		return tempImageId;
	}

	public void setTempImageId(String tempImageId) {
		this.tempImageId = tempImageId;
	}


	public void setOrgTypeId(Long orgTypeId) {
		this.orgTypeId = orgTypeId;
	}





	/**用户归属 组织ID*/
	private Long orgId;
	/**用户归属 组织编码*/
	private String orgSn;
	
	/**用户邮箱*/
	private String email;
	
	/**手机号*/
	private String telephone;
	
	
	/**用户状态 1在职 2离职*/
	private Integer status;
	
	/**用户状态 1在职 2离职*/
	private Integer sex;
	
	
	/**扩展属性*/
	private Map extraAttrs;


	
	
	@ApiModelProperty(value="扩展属性 ，key-value形式")
	public Map getExtraAttrs() {
		return extraAttrs;
	}


	public void setExtraAttrs(Map extraAttrs) {
		this.extraAttrs = extraAttrs;
	}





	/**用户创建时间*/
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date createTime;
	

	@ApiModelProperty(value="用户类型ID")
	public Long getUserTypeId() {
		return userTypeId;
	}


	public void setUserTypeId(Long userTypeId) {
		this.userTypeId = userTypeId;
	}

	@ApiModelProperty(value="用户归属组织Id")
	public Long getOrgId() {
		return orgId;
	}


	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

	@ApiModelProperty(value="性别  1男  2女")
	public Integer getSex() {
		return sex;
	}


	public void setSex(Integer sex) {
		this.sex = sex;
	}
	
	@ApiModelProperty(value="用户唯一标识")
	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}




	@ApiModelProperty(value="工号")
	public String getSn() {
		return sn;
	}





	public void setSn(String sn) {
		this.sn = sn;
	}




	@ApiModelProperty(value="姓名")
	public String getName() {
		return name;
	}





	public void setName(String name) {
		this.name = name;
	}



	@ApiModelProperty(value="邮件地址")
	public String getEmail() {
		return email;
	}





	public void setEmail(String email) {
		this.email = email;
	}




	@ApiModelProperty(value="手机号码")
	public String getTelephone() {
		return telephone;
	}





	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}




	@ApiModelProperty(value="用户状态 1在职  2离职")
	public Integer getStatus() {
		return status;
	}





	public void setStatus(Integer status) {
		this.status = status;
	}




	@ApiModelProperty(value="用户创建时间")
	public Date getCreateTime() {
		return createTime;
	}





	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	



	@ApiModelProperty(value="用户归属组织编码")
	public String getOrgSn() {
		return orgSn;
	}

	public void setOrgSn(String orgSn) {
		this.orgSn = orgSn;
	}

	@ApiModelProperty(hidden=true)
	public User getUser(){
		User model=new User();
		model.setUserTypeId(this.getUserTypeId());
		model.setOrgId(this.getOrgId());
		model.setId(this.getId());
		model.setName(this.getName());
		model.setCreateTime(this.getCreateTime());
		model.setSn(this.getSn());
		model.setStatus(this.getStatus());
		model.setSex(this.getSex());;
		model.setEmail(this.getEmail());
		model.setTelephone(this.getTelephone());
		model.setOrgTypeId(this.getOrgTypeId());;
		if(this.getExtraAttrs()!=null){
			model.setExtraAttrs(this.extraAttrs);
		}
		return model;
	}
}
