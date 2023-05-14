package com.sense.iam.api.model.sys;

import com.sense.iam.model.sys.Message;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class MessageReq {

	private Long id;
	private String title;
	private String content;
	private Integer type;
	private Integer status;
	
	
	@ApiModelProperty(value="唯一编码",example="0",required=true)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@ApiModelProperty(value="消息标题", required=true)
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	@ApiModelProperty(value="消息内容", required=true)
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	@ApiModelProperty(value="消息类型 1 通知   2 告警  3 错误", example="1", required=true)
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	@ApiModelProperty(value="状态 1 已读 2未读", example="2", required=true)
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	
	@ApiModelProperty(hidden=true)
	public Message getMessage(){
		Message message=new Message();
		message.setId(this.getId());
		message.setTitle(this.getTitle());
		message.setContent(this.getContent());
		message.setType(this.getType());
		message.setStatus(this.getStatus());
		return message;
	}
	
	
	
	
}
