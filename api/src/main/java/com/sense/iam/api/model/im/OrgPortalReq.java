package com.sense.iam.api.model.im;


import io.swagger.annotations.ApiModelProperty;





public class OrgPortalReq {

	private Long orgId;
	private Long templateId;
	
	@ApiModelProperty(value="组织唯一标识",required=true)
	public Long getOrgId() {
		return orgId;
	}
	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}
	@ApiModelProperty(value="门户模板唯一标识",required=true)
	public Long getTemplateId() {
		return templateId;
	}
	public void setTemplateId(Long templateId) {
		this.templateId = templateId;
	}
	@Override
	public String toString() {
		return "OrgPortalReq [orgId=" + orgId + ", templateId=" + templateId
				+ "]";
	}
	
	
	
}
