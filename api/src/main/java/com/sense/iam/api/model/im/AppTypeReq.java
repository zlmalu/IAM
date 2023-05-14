package com.sense.iam.api.model.im;

import com.sense.iam.model.im.AppType;

import io.swagger.annotations.ApiModelProperty;





public class AppTypeReq {

	private Long id;
	
	@ApiModelProperty(value="应用类型唯一编码",required=true)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	/**应用类型编码*/
	private String sn;
	/**应用类型名称*/
	private String name;

	@ApiModelProperty(value="应用类型编码",required=true)
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	@ApiModelProperty(value="应用类型名称",required=true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
	@ApiModelProperty(hidden=true)
	public AppType getAppType(){
		AppType orgType=new AppType();
		orgType.setId(this.getId());
		orgType.setSn(this.getSn());
		orgType.setName(this.getName());
		return orgType;
	}
	
	
}
