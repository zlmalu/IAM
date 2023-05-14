package com.sense.iam.api.model.im;


import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

import com.sense.core.db.DBField;
import com.sense.iam.model.im.AppFunc;

import io.swagger.annotations.ApiModelProperty;

public class AppFuncReq {

	/**应用功能唯一标识*/
	private Long id;
	/**应用功能编号*/
	private String sn;
	/**应用功能名称*/
	private String name;
	/**上级编号*/
	private Long parentId;
	/**应用唯一标识*/
	private Long appId;
	/**是否默认功能 1是默认  2是非默认*/
	private Integer isDefault;
	/**应用帐号唯一标识*/
	private Long acctId;
	/**权限类型  当值为1时代表此数据为权限类型定义数据，其他则人具体功能数据*/
	private Long funcType;
	
	/**权限类型ID*/
	private Long rootFuncType;
	@ApiModelProperty(value="权限类型ID")
	public Long getRootFuncType() {
		return rootFuncType;
	}
	public void setRootFuncType(Long rootFuncType) {
		this.rootFuncType = rootFuncType;
	}
	/**状态 1正常   2禁用*/
	private Integer status;
	/**授权类型 1当前节点授权 , 2基于关联功能授权，3基于所有授权，4不授权*/
	private Integer authType;
	/**信息*/
	private String info;
	
	
	@ApiModelProperty(value="应用功能唯一标识")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@ApiModelProperty(value="应用功能编码")
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	@ApiModelProperty(value="应用功能名称")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getParentId() {
		return parentId;
	}
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	public Long getAppId() {
		return appId;
	}
	public void setAppId(Long appId) {
		this.appId = appId;
	}
	public Long getAcctId() {
		return acctId;
	}
	public void setAcctId(Long acctId) {
		this.acctId = acctId;
	}
	public Long getFuncType() {
		return funcType;
	}
	public void setFuncType(Long funcType) {
		this.funcType = funcType;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getAuthType() {
		return authType;
	}
	public void setAuthType(Integer authType) {
		this.authType = authType;
	}
	public Integer getIsDefault() {
		return isDefault;
	}
	public void setIsDefault(Integer isDefault) {
		this.isDefault = isDefault;
	}
	
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	/**扩展属性*/
	private Map extraAttrs=new HashMap();
	public Map getExtraAttrs() {
		
		return extraAttrs;
	}
	public void setExtraAttrs(Map extraAttrs) {
		this.extraAttrs = extraAttrs;
	}
	
	public AppFunc toAppFunc(){
		AppFunc appFunc=new AppFunc();
		try {
			BeanUtils.copyProperties(appFunc, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		appFunc.setExtraAttrs(this.getExtraAttrs());
		return appFunc;
	}
}
