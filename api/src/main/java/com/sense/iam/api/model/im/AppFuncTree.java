package com.sense.iam.api.model.im;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

import com.sense.iam.model.im.AppFunc;

public class AppFuncTree {
	
	private long appId;
    private String appName;
    private String appSn;
    
    private List<AppFunc> children;
    
    
    @ApiModelProperty(value="应用唯一标识")
	public long getAppId() {
		return appId;
	}
	public void setAppId(long appId) {
		this.appId = appId;
	}
	@ApiModelProperty(value="应用名称")
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	 @ApiModelProperty(value="应用编码")
	public String getAppSn() {
		return appSn;
	}
	public void setAppSn(String appSn) {
		this.appSn = appSn;
	}
	@ApiModelProperty(value="权限集合")
	public List<AppFunc> getChildren() {
		return children;
	}
	public void setChildren(List<AppFunc> children) {
		this.children = children;
	}
	
   
   

    

}
