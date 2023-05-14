package com.sense.iam.api.model.am;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 用户组-资源
 * @author K3w1n
 *
 */
@ApiModel("用户组-资源")
public class AuthGroupResourceReq {

	/**
	 * 用户组的id字符串，多个id采用英文逗号分隔
	 */
	private String groupIds;
	/**
	 * 资源组的id字符串，多个id采用英文逗号分隔
	 */
	private String resourceIds;
	
	@ApiModelProperty(value="用户组编码字符串",required=true)
	public String getGroupIds() {
		return groupIds;
	}
	public void setGroupIds(String groupIds) {
		this.groupIds = groupIds;
	}
	
	@ApiModelProperty(value="资源组字符串",required=true)
	public String getResourceIds() {
		return resourceIds;
	}
	public void setResourceIds(String resourceIds) {
		this.resourceIds = resourceIds;
	}
	
}
