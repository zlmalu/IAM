package com.sense.iam.api.model.am;

import com.sense.core.db.DBField;
import com.sense.iam.model.am.LoginPolicy;

import io.swagger.annotations.ApiModelProperty;

/**
 * 密码强度 - LoginPolicyReq
 * @author ygd
 *
 */
public class LoginPolicyReq {

	private Long id;
	@ApiModelProperty(value="唯一标识", example="0", required=true)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	/**密码策略名称*/
	private String name;
	/**密码策略值*/
	private String  value;
	
	@ApiModelProperty(value="密码策略名称", required=true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@ApiModelProperty(value="密码策略值", required=true)
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	@ApiModelProperty(hidden = true)
	public LoginPolicy getLoginPolicy() {
		LoginPolicy loginPolicy = new LoginPolicy();
		loginPolicy.setId(this.getId());
		loginPolicy.setName(this.getName());
		loginPolicy.setValue(this.getValue());
		return loginPolicy;
	}
	
}
