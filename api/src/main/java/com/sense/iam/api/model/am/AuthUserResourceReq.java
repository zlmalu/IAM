package com.sense.iam.api.model.am;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 资源-关联用户
 * @author K3w1n
 *
 */
@ApiModel("资源-关联用户")
public class AuthUserResourceReq {

	/**
	 * 用户的id字符串，多个id采用英文逗号分隔
	 */
	private String userIds;
	
	/**
	 * 资源组id字符串，多个id采用英文逗号分隔
	 */
	private String resourceIds;
	
	@ApiModelProperty(value="用户编码字符串",required=true)
	public String getUserIds() {
		return userIds;
	}
	public void setUserIds(String userIds) {
		this.userIds = userIds;
	}
	
	@ApiModelProperty(value = "资源组编码字符串", required=true)
	public String getResourceIds() {
		return resourceIds;
	}
	public void setResourceIds(String resourceIds) {
		this.resourceIds = resourceIds;
	}
}
