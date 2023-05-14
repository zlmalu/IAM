package com.sense.iam.api.model.am;

import java.util.Date;

import com.sense.core.db.DBField;
import com.sense.iam.model.am.FindModule;
import com.sense.iam.model.sso.Formbase;

import io.swagger.annotations.ApiModelProperty;

public class findModuleReq {

	/**唯一标识*/
	private Long id;
	/**模块编码*/
	private String sn;
	/**模块名称*/
	private String name;
	/**组件id*/
	private Long componentsId;
	/**参数配置*/
	private String config;
	/**实例信息*/
	private String runClass;
	/**表示组件id的名称*/
	private String syscompomentName;
	
	
	@ApiModelProperty(value="唯一编码",example="0",required=true)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@ApiModelProperty(value="模块编码",required=true)
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	@ApiModelProperty(value="模块名称",required=true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@ApiModelProperty(value="组件id",required=true)
	public Long getComponentsId() {
		return componentsId;
	}
	public void setComponentsId(Long componentsId) {
		this.componentsId = componentsId;
	}
	@ApiModelProperty(value="参数配置",required=false)
	public String getConfig() {
		return config;
	}
	public void setConfig(String config) {
		this.config = config;
	}
	@ApiModelProperty(value="组件名称",required=false)
	public String getSyscompomentName() {
		return syscompomentName;
	}
	public void setSyscompomentName(String syscompomentName) {
		this.syscompomentName = syscompomentName;
	}
	@ApiModelProperty(value="实例信息",required=true)
	public String getRunClass() {
		return runClass;
	}
	public void setRunClass(String runClass) {
		this.runClass = runClass;
	}
	
	@ApiModelProperty(hidden=true)
	public FindModule getFindModule(){
		FindModule findModule=new FindModule();
		findModule.setId(this.getId());
		findModule.setSn(this.getSn());
		findModule.setName(this.getName());
		findModule.setComponentsId(this.getComponentsId());
		findModule.setConfig(this.getConfig());
		findModule.setSyscompomentName(this.getSyscompomentName());
		findModule.setRunClass(this.getRunClass());
		return findModule;
	}
	
}
