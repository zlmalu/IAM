package com.sense.iam.api.model;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

/**
 * 组织授权用户兼职岗位
 * @author K3w1n
 *
 */
public class EnterAuthUserModel {

	Long orgId;
	List<String> ids;
	
	
	@ApiModelProperty(value="组织ID", required=true)
	public Long getOrgId() {
		return orgId;
	}
	
	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}
	
	@ApiModelProperty(value="授权用户ID集合", required=true)
	public List<String> getIds() {
		return ids;
	}
	
	public void setIds(List<String> ids) {
		this.ids = ids;
	}
}
