package com.sense.iam.api.model.sys;

import java.util.ArrayList;
import java.util.List;

import com.sense.iam.model.sys.Acct;
import com.sense.iam.model.sys.Event;
import com.sense.iam.model.sys.Role;

import io.swagger.annotations.ApiModelProperty;

public class EventReq {

	private Long id;
	private String name;
	private String clazz;
	private String method;
	/**
	 * 事件类型 1系统日志 2 同步事件  3系统日志和同步事件 
	 */
	private Integer eventType;
	
	
	@ApiModelProperty(value="唯一编码",example="0",required=true)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@ApiModelProperty(value="事件名",required=true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@ApiModelProperty(value="类名",required=true)
	public String getClazz() {
		return clazz;
	}
	public void setClazz(String clazz) {
		this.clazz = clazz;
	}
	@ApiModelProperty(value="操作名",required=true)
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	@ApiModelProperty(value="1 为日志类型,2为数据类型,3为日志与数据",example="1",required=true)
	public Integer getEventType() {
		return eventType;
	}
	public void setEventType(Integer eventType) {
		this.eventType = eventType;
	}
	
	@ApiModelProperty(hidden=true)
	public Event getEvent(){
		Event event=new Event();
		event.setId(this.getId());
		event.setName(this.getName());
		event.setClazz(this.getClazz());
		event.setMethod(this.getMethod());
		event.setEventType(this.getEventType());
		return event;
	}
	
}
