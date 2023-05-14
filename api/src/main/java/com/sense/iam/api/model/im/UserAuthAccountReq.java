package com.sense.iam.api.model.im;

import io.swagger.annotations.ApiModelProperty;




/**
 * 用户获取关联帐号搜索条件
 * 
 * Description:  岗位信息模型定义
 * 
 * @author j_hy
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class UserAuthAccountReq{
	
	/**用户唯一标识*/
	private Long userId;
	/**用户工号*/

	private String appName;



	@ApiModelProperty(value="应用名称")
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	@ApiModelProperty(value="用户唯一标识")
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}


}
