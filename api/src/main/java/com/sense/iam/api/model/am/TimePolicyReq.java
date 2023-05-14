package com.sense.iam.api.model.am;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sense.iam.model.am.TimePolicy;

import io.swagger.annotations.ApiModelProperty;


/**
 * 时间控制管理 - ModelReq
 * @author K3w1n
 *
 */
public class TimePolicyReq {

	/**唯一标识*/
	private Long id;
	
	/**认证域编码*/
	private String sn;
	
	/**名称*/
	private String name;
	
	/**每月访问*/
	private Long month;
	
	/**每月哪一天*/
	private Long dayMonth;
	
	/**每周哪一天*/
	private Long dayWeek;
	
	/**开始时间*/
	private Date startTime;
	
	/**结束时间*/
	private Date endTime;
	
	/**是否允许访问*/
	private Long isAllow;
	
	/**
	 *获取： 唯一标识 
	 *@return the id 唯一标识
	 */
	@ApiModelProperty(value="唯一标识", required=true)
	public Long getId() {
		return id;
	}
	/**
	 * 设置： 唯一标识
	 * @param id 唯一标识
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 *获取： 认证域编码 
	 *@return the sn 认证域编码
	 */
	@ApiModelProperty(value="认证域编码", required=true)
	public String getSn() {
		return sn;
	}
	/**
	 * 设置： 认证域编码
	 * @param sn 认证域编码
	 */
	public void setSn(String sn) {
		this.sn = sn;
	}
	/**
	 *获取： 名称 
	 *@return the name 名称
	 */
	@ApiModelProperty(value="名称", required=true)
	public String getName() {
		return name;
	}
	/**
	 * 设置： 名称
	 * @param name 名称
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 *获取： 每月访问 
	 *@return the month 每月访问
	 */
	@ApiModelProperty(value="每月访问", required=true)
	public Long getMonth() {
		return month;
	}
	/**
	 * 设置： 每月访问
	 * @param month 每月访问
	 */
	public void setMonth(Long month) {
		this.month = month;
	}
	/**
	 *获取： 每月哪一天 
	 *@return the dayMonth 每月哪一天
	 */
	@ApiModelProperty(value="每月哪天", required=true)
	public Long getDayMonth() {
		return dayMonth;
	}
	/**
	 * 设置： 每月哪一天
	 * @param dayMonth 每月哪一天
	 */
	public void setDayMonth(Long dayMonth) {
		this.dayMonth = dayMonth;
	}
	/**
	 *获取： 每周哪一天 
	 *@return the dayWeek 每周哪一天
	 */
	@ApiModelProperty(value="每周哪天", required=true)
	public Long getDayWeek() {
		return dayWeek;
	}
	/**
	 * 设置： 每周哪一天
	 * @param dayWeek 每周哪一天
	 */
	public void setDayWeek(Long dayWeek) {
		this.dayWeek = dayWeek;
	}
	/**
	 *获取： 开始时间 
	 *@return the startTime 开始时间
	 */
	@ApiModelProperty(value="开始时间", required=true)
	@DateTimeFormat(pattern = "HH:mm:ss")
    @JsonFormat(pattern = "HH:mm:ss",timezone="GMT+8")
	public Date getStartTime() {
		return startTime;
	}
	/**
	 * 设置： 开始时间
	 * @param startTime 开始时间
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	/**
	 *获取： 结束时间 
	 *@return the endTime 结束时间
	 */
	@ApiModelProperty(value="结束时间", required=true)
	@DateTimeFormat(pattern = "HH:mm:ss")
    @JsonFormat(pattern = "HH:mm:ss",timezone="GMT+8")
	public Date getEndTime() {
		return endTime;
	}
	/**
	 * 设置： 结束时间
	 * @param endTime 结束时间
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	/**
	 *获取： 是否允许访问 
	 *@return the isAllow 是否允许访问
	 */
	@ApiModelProperty(value="是否允许访问", required=true)
	public Long getIsAllow() {
		return isAllow;
	}
	/**
	 * 设置： 是否允许访问
	 * @param isAllow 是否允许访问
	 */
	public void setIsAllow(Long isAllow) {
		this.isAllow = isAllow;
	}
	
	@ApiModelProperty(hidden = true)
	public TimePolicy getTimePolicy() {
		TimePolicy timePolicy = new TimePolicy();
		timePolicy.setId(this.getId());
		timePolicy.setSn(this.getSn());
		timePolicy.setName(this.getName());
		timePolicy.setMonth(this.getMonth());
		timePolicy.setDayMonth(this.getDayMonth());
		timePolicy.setDayWeek(this.getDayWeek());
		timePolicy.setStartTime(this.getStartTime());
		timePolicy.setEndTime(this.getEndTime());
		timePolicy.setIsAllow(this.getIsAllow());
		return timePolicy;
	}
	
	
}
