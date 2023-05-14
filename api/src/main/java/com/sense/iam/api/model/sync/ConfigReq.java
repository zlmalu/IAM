package com.sense.iam.api.model.sync;

import io.swagger.annotations.ApiModelProperty;
import com.sense.iam.model.sync.Config;







public class ConfigReq {

	/**配置编码*/
	private String sn;
	/**目标应用编码*/

	private Long appId;
	/**同步使用的API编码*/
	private Long syncApiId;
	
	/**系统事件ID*/
	private Long sysEventId;
	
	/**同步配置状态 1启用  2禁用*/
	private Integer status;
	

	@ApiModelProperty(value="应用唯一标识")
	public Long getAppId() {
		return appId;
	}
	public void setAppId(Long appId) {
		this.appId = appId;
	}
	@ApiModelProperty(value="同步使用的API标识")
	public Long getSyncApiId() {
		return syncApiId;
	}
	public void setSyncApiId(Long syncApiId) {
		this.syncApiId = syncApiId;
	}
	@ApiModelProperty(value="同步配置状态 1启用  2禁用")
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	@ApiModelProperty(value="系统事件ID")
	public Long getSysEventId() {
		return sysEventId;
	}
	public void setSysEventId(Long sysEventId) {
		this.sysEventId = sysEventId;
	}
	@ApiModelProperty(value="配置编码")
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	@ApiModelProperty(hidden=true)
	public Config getConfig(){
		Config model=new Config();
		model.setId(0L);
		model.setAppId(this.getAppId());
		model.setSn(this.getSn());
		model.setSyncApiId(this.getSyncApiId());
		model.setSysEventId(this.getSysEventId());
		model.setStatus(this.getStatus());
		return model;
	}	
}
