package com.sense.iam.api.model.am;



import com.sense.iam.model.am.Black;

import io.swagger.annotations.ApiModelProperty;
/**
 * 黑名单管理 - ModelReq
 * @author K3w1n
 *
 */
public class BlackReq {

	private Long id;
	
	@ApiModelProperty(value="唯一编码",example="0",required=true)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	/** 用户唯一标识 */
	private String userId;
	@ApiModelProperty(value = "用户编号",required=true)
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	@ApiModelProperty(hidden = true)
	public Black getBlack() {
		Black black = new Black();
		black.setId(this.getId());
		black.setUserId(this.getUserId());
		return black;
	}
	
}
