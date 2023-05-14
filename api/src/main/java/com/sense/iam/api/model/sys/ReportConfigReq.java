package com.sense.iam.api.model.sys;

import com.sense.iam.model.sys.LogConfig;
import com.sense.iam.model.sys.ReportConfig;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class ReportConfigReq {
	/**唯一标识*/
	private Long id;
	/**登录名*/
	private String sn;
	/**日志名*/
	private String name;
	/**sql配置*/
	private String findConfig;
	/**查询组件*/
	private String pageConfig;
	
	
	@ApiModelProperty(value="唯一编码",example="0",required=true)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@ApiModelProperty(value="编码",required=true)
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	@ApiModelProperty(value="名称",required=true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@ApiModelProperty(value="SQL配置",required=true)
	public String getFindConfig() {
		return findConfig;
	}
	public void setFindConfig(String findConfig) {
		this.findConfig = findConfig;
	}
	@ApiModelProperty(value="页面配置",required=true)
	public String getPageConfig() {
		return pageConfig;
	}
	public void setPageConfig(String pageConfig) {
		this.pageConfig = pageConfig;
	}
	@ApiModelProperty(hidden=true)
	public ReportConfig getReportConfig(){
		ReportConfig reportConfig=new ReportConfig();
		reportConfig.setId(this.getId());
		reportConfig.setSn(this.getSn());
		reportConfig.setName(this.getName());
		reportConfig.setFindConfig(this.getFindConfig());
		reportConfig.setPageConfig(this.getPageConfig());
		return reportConfig;
	}
	
	
	
}
