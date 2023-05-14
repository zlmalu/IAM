package com.sense.iam.api.model.im;

import com.sense.iam.model.im.UserType;

import io.swagger.annotations.ApiModelProperty;

public class UserTypeReq {
	
	private Long id;
	
	@ApiModelProperty(value="唯一编码",required=true)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	/**用户类型表名*/
	private String tableName;
	/**用户类型表描述*/
	private String remark;
	
	@ApiModelProperty(value="用户类型表名",required=true, example="INNER_USER")
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	@ApiModelProperty(value="用户类型表描述",required=true)
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	@ApiModelProperty(hidden=true)
	public UserType getUserType(){
		UserType userType=new UserType();
		userType.setId(this.getId());
		userType.setTableName(this.tableName);
		userType.setRemark(this.remark);
		return userType;
	}
	
}
