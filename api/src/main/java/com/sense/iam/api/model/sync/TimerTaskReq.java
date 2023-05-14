package com.sense.iam.api.model.sync;

import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sense.iam.model.sync.TimerTask;

/**
 * 定时任务Req
 * @author ygd
 *
 */
public class TimerTaskReq {

	/**唯一标识*/
	private Long id;
	/**任务编号*/
	private String sn;
	/**任务名称*/
	private String name;
	/**时间表达式 格式为:秒  分 小时 日期 月份 星期 年      例如：每天12点    0 0 12 * * * *   每周三12点:0 0 12 * * 3 *      */
	private String cronExpression;
	/**上次执行时间,当执行以后更新*/
	private Date preExecuteTime;
	/**下次执行时间,初始化添加时或执行以后更新*/
	private Date nextExecuteTime;
	/**同步组件编码*/
	private Long syncApiId;
	/**运行类*/
	private String runClass;
	/**相关配置 格式为xml  例如:<config><fieldName>fieldValue</fieldName></config>*/
	private String configXml;
	private Integer status;
	/**创建时间*/
	private Date createTime;
	/**同步组件名称*/
	private String syncApiName;
	
	@ApiModelProperty(value="定时任务唯一标识")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@ApiModelProperty(value="任务编号")
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	
	@ApiModelProperty(value="任务名称")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@ApiModelProperty(value="表达式")
	public String getCronExpression() {
		return cronExpression;
	}
	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}
	
	@ApiModelProperty(value="上次执行时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
	public Date getPreExecuteTime() {
		return preExecuteTime;
	}
	public void setPreExecuteTime(Date preExecuteTime) {
		this.preExecuteTime = preExecuteTime;
	}
	
	@ApiModelProperty(value="下次执行时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
	public Date getNextExecuteTime() {
		return nextExecuteTime;
	}
	public void setNextExecuteTime(Date nextExecuteTime) {
		this.nextExecuteTime = nextExecuteTime;
	}
	
	@ApiModelProperty(value="同步接口ID")
	public Long getSyncApiId() {
		return syncApiId;
	}
	public void setSyncApiId(Long syncApiId) {
		this.syncApiId = syncApiId;
	}
	
	@ApiModelProperty(value="同步实例")
	public String getRunClass() {
		return runClass;
	}
	public void setRunClass(String runClass) {
		this.runClass = runClass;
	}
	
	@ApiModelProperty(value="系统参数")
	public String getConfigXml() {
		return configXml;
	}
	public void setConfigXml(String configXml) {
		this.configXml = configXml;
	}
	
	@ApiModelProperty(value="状态：1 启用  2 禁用")
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	
	@ApiModelProperty(value="创建时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	@ApiModelProperty(value="同步接口ID")
	public String getSyncApiName() {
		return syncApiName;
	}
	public void setSyncApiName(String syncApiName) {
		this.syncApiName = syncApiName;
	}
	/**
	 * 添加资源控制关联人员
	 */
	@ApiModelProperty(hidden =  true)
	public TimerTask getTimerTask() {
		TimerTask timerTask = new TimerTask();
		timerTask.setId(this.id);
		timerTask.setSn(this.sn);
		timerTask.setName(this.name);
		timerTask.setCronExpression(this.cronExpression);
		timerTask.setPreExecuteTime(this.preExecuteTime);
		timerTask.setNextExecuteTime(this.nextExecuteTime);
		timerTask.setSyncApiId(this.syncApiId);
		timerTask.setRunClass(this.runClass);
		timerTask.setConfigXml(this.configXml);
		timerTask.setStatus(this.status);
		timerTask.setCreateTime(this.createTime);
		timerTask.setSyncApiName(this.syncApiName);
		return timerTask;
	}
	
	
}
