package com.sense.iam.api.model.sys;


import com.sense.iam.model.sys.Compoment;
import com.sense.iam.model.sys.Event;
import io.swagger.annotations.ApiModelProperty;



/**
 *
 * 系统组件表
 *
 * Description:
 *
 * @author w_jfwen
 *
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */

public class CompomentReq  {


	private Long id;

	/**
	 * 组件编号
	 */
	private String sn;

	/**
	 * 组件名称
	 * 1 同步组件 2 计划任务组件 3认证组件
	 */
	private String name;
	/**
	 * 组件类型
	 */
	private String type;
	/**
	 * 组件备注
	 */
	private String remark;


	@ApiModelProperty(value="组件ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ApiModelProperty(value="组件编号",required=true)
	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}
	@ApiModelProperty(value="组件名称",required=true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ApiModelProperty(value="组件类型 1 同步组件 2 计划任务组件 3认证组件",required=true)
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@ApiModelProperty(value="组件描述")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@ApiModelProperty(hidden=true)
	public Compoment getCompoment(){
		Compoment compoment=new Compoment();
		compoment.setId(this.getId());
		compoment.setName(this.getName());
		compoment.setSn(this.getSn());
		compoment.setType(this.getType());
		compoment.setRemark(this.getRemark());
		return compoment;
	}
}
