package com.sense.iam.api.model.im;

import java.util.Date;

import com.sense.iam.model.im.App;

import io.swagger.annotations.ApiModelProperty;

/**
 * 应用模型
 * 
 * Description:  应用信息模型定义
 * 
 * @author j_hy
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class AppReq {

	private Long id;
	
	private Long userId;
	
	private String sn;
	
	private String name;
	
	private Long appTypeId;
	
	
	private Integer ssoType;
	
	private String remark;
	
	private Integer isView;
	
	private Date createTime;
	
	/**应用临时图片*/
	private String tempImageId;
	
	/** 终端类型：1.PC 2.H5 3.Android/IOS*/
	private Integer tagEndType;
	
	@ApiModelProperty(value="用户ID")
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}


	private Integer browserType=0;
	
	
	/**
	 * @return the tagEndType
	 */
	@ApiModelProperty(value="终端类型：1.PC 2.H5 3.Android/IOS")
	public Integer getTagEndType() {
		return tagEndType;
	}
	public void setTagEndType(Integer tagEndType) {
		this.tagEndType = tagEndType;
	}
	
	@ApiModelProperty(value="指定浏览器类型  0默认，1IE，2谷歌  3火狐")
	public Integer getBrowserType() {
		return browserType;
	}
	public void setBrowserType(Integer browserType) {
		this.browserType = browserType;
	}

	@ApiModelProperty(value="应用临时图片")
	public String getTempImageId() {
		return tempImageId;
	}

	public void setTempImageId(String tempImageId) {
		this.tempImageId = tempImageId;
	}
	
	@ApiModelProperty(value="应用唯一标识")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	@ApiModelProperty(value="应用编码")
	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}
	
	@ApiModelProperty(value="应用名称")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	@ApiModelProperty(value="应用类型ID")
	public Long getAppTypeId() {
		return appTypeId;
	}

	public void setAppTypeId(Long appTypeId) {
		this.appTypeId = appTypeId;
	}
	@ApiModelProperty(value="单点登录类型：1 SAML  2 OAUTH  3 FORMBASE  4 JWT  5 OIDC  6 OIDC")
	public Integer getSsoType() {
		return ssoType;
	}

	public void setSsoType(Integer ssoType) {
		this.ssoType = ssoType;
	}
	@ApiModelProperty(value="应用备注")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	@ApiModelProperty(value="应用是否隐藏，1显示   2隐藏")
	public Integer getIsView() {
		return isView;
	}

	public void setIsView(Integer isView) {
		this.isView = isView;
	}
	
	@ApiModelProperty(value="应用创建时间")
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	
	Integer sortNum;
	
	@ApiModelProperty(value="排序号")
	public Integer getSortNum() {
		return sortNum;
	}

	public void setSortNum(Integer sortNum) {
		this.sortNum = sortNum;
	}

	@ApiModelProperty(hidden=true)
	public App getApp(){
		App model=new App();
		model.setId(this.getId());
		if(this.getName()!=null&&this.getName().length()==0){
			model.setName(null);
		}else{
			model.setName(this.getName());
		}
		if(this.getSn()!=null&&this.getSn().length()==0){
			model.setSn(null);
		}else{
			model.setSn(this.getSn());
		}
		model.setBrowserType(browserType);
		model.setSsoType(ssoType);
		model.setAppTypeId(appTypeId);
		model.setIsView(isView);
		model.setRemark(remark);
		model.setSortNum(this.getSortNum());
		model.setCreateTime(this.getCreateTime());
		model.setTagEndType(this.getTagEndType());
		return model;
	}
	
}
