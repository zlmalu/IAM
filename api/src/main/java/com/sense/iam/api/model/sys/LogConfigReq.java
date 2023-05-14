package com.sense.iam.api.model.sys;

import com.sense.iam.model.sys.LogConfig;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class LogConfigReq {

	/**唯一标识*/
	private Long id;
	/**登录名*/
	private String sn;
	/**日志名*/
	private String name;
	/**sql配置*/
	private String findConfig;
	/**查询组件*/
	private String gridConfig;
	/**
	 * 审计类型 1位日志审计 2 为安全审计
	 */
	private Integer type ;
	

	@ApiModelProperty(value="唯一编码",example="0",required=true)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	@ApiModelProperty(value="编号",required=true)
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
	public String getGridConfig() {
		return gridConfig;
	}

	public void setGridConfig(String gridConfig) {
		this.gridConfig = gridConfig;
	}
	@ApiModelProperty(value="审计类型:1为日志审计 2 为安全审计",required=true)
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}
	
	@ApiModelProperty(hidden=true)
	public LogConfig getLogConfig(){
		LogConfig logConfig=new LogConfig();
		logConfig.setId(this.getId());
		logConfig.setSn(this.getSn());
		logConfig.setName(this.getName());
		logConfig.setType(this.getType());
		logConfig.setFindConfig(this.getFindConfig());
		logConfig.setGridConfig(this.getGridConfig());
		return logConfig;
	}
	
}
