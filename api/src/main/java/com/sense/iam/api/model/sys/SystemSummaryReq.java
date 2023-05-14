package com.sense.iam.api.model.sys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class SystemSummaryReq {

	
	int orgTypeCount=0;
	int orgCount=0;
	int orgLength=0;
	
	int appTypeCount=0;
	int appCount=0;
	
	
	int userTypeCount=0;
	int userCount=0;
	int userQuitCount=0;
	int userIncumbencyCount=0;
	
	int accountTypeCount=4;
	int accountCount=0;
	int accountProhibitCount=0;
	int accountIncumbencyCount=0;
	
	int interfaceTypeCount=0;
	int interfaceCount=0;
	int interfaceSyncCount=0;
	//int interfaceTimerCount=0;
	
	//接口管理
	Map<String, List<Map<String, Object>>> interfaceInfo=new HashMap<String, List<Map<String,Object>>>();
	
	@ApiModelProperty(value="数据接口对象，key值是类型，value是接口对象")
	public Map<String, List<Map<String, Object>>> getInterfaceInfo() {
		return interfaceInfo;
	}
	public void setInterfaceInfo(
			Map<String, List<Map<String, Object>>> interfaceInfo) {
		this.interfaceInfo = interfaceInfo;
	}
	
	@ApiModelProperty(value="组织类型总数")
	public int getOrgTypeCount() {
		return orgTypeCount;
	}
	public void setOrgTypeCount(int orgTypeCount) {
		this.orgTypeCount = orgTypeCount;
	}
	@ApiModelProperty(value="组织总数")
	public int getOrgCount() {
		return orgCount;
	}
	public void setOrgCount(int orgCount) {
		this.orgCount = orgCount;
	}
	@ApiModelProperty(value="组织层级数")
	public int getOrgLength() {
		return orgLength;
	}
	public void setOrgLength(int orgLength) {
		this.orgLength = orgLength;
	}
	@ApiModelProperty(value="应用类型总数")
	public int getAppTypeCount() {
		return appTypeCount;
	}
	public void setAppTypeCount(int appTypeCount) {
		this.appTypeCount = appTypeCount;
	}
	@ApiModelProperty(value="应用总数")
	public int getAppCount() {
		return appCount;
	}
	public void setAppCount(int appCount) {
		this.appCount = appCount;
	}
	@ApiModelProperty(value="用户类型总数")
	public int getUserTypeCount() {
		return userTypeCount;
	}
	public void setUserTypeCount(int userTypeCount) {
		this.userTypeCount = userTypeCount;
	}
	@ApiModelProperty(value="用户总数")
	public int getUserCount() {
		return userCount;
	}
	public void setUserCount(int userCount) {
		this.userCount = userCount;
	}
	@ApiModelProperty(value="用户离职总数")
	public int getUserQuitCount() {
		return userQuitCount;
	}
	public void setUserQuitCount(int userQuitCount) {
		this.userQuitCount = userQuitCount;
	}
	@ApiModelProperty(value="用户在职总数")
	public int getUserIncumbencyCount() {
		return userIncumbencyCount;
	}
	public void setUserIncumbencyCount(int userIncumbencyCount) {
		this.userIncumbencyCount = userIncumbencyCount;
	}
	@ApiModelProperty(value="帐号类型总数")
	public int getAccountTypeCount() {
		return accountTypeCount;
	}
	public void setAccountTypeCount(int accountTypeCount) {
		this.accountTypeCount = accountTypeCount;
	}
	@ApiModelProperty(value="帐号总数")
	public int getAccountCount() {
		return accountCount;
	}
	public void setAccountCount(int accountCount) {
		this.accountCount = accountCount;
	}
	@ApiModelProperty(value="禁用账号总数")
	public int getAccountProhibitCount() {
		return accountProhibitCount;
	}
	public void setAccountProhibitCount(int accountProhibitCount) {
		this.accountProhibitCount = accountProhibitCount;
	}
	@ApiModelProperty(value="活跃账号总数")
	public int getAccountIncumbencyCount() {
		return accountIncumbencyCount;
	}
	public void setAccountIncumbencyCount(int accountIncumbencyCount) {
		this.accountIncumbencyCount = accountIncumbencyCount;
	}
	@ApiModelProperty(value="接口类型总数")
	public int getInterfaceTypeCount() {
		return interfaceTypeCount;
	}
	public void setInterfaceTypeCount(int interfaceTypeCount) {
		this.interfaceTypeCount = interfaceTypeCount;
	}
	@ApiModelProperty(value="接口总数")
	public int getInterfaceCount() {
		return interfaceCount;
	}
	public void setInterfaceCount(int interfaceCount) {
		this.interfaceCount = interfaceCount;
	}
	@ApiModelProperty(value="下推接口总数")
	public int getInterfaceSyncCount() {
		return interfaceSyncCount;
	}
	public void setInterfaceSyncCount(int interfaceSyncCount) {
		this.interfaceSyncCount = interfaceSyncCount;
	}
	/*@ApiModelProperty(value="拉取接口总数")
	public int getInterfaceTimerCount() {
		return interfaceTimerCount;
	}
	public void setInterfaceTimerCount(int interfaceTimerCount) {
		this.interfaceTimerCount = interfaceTimerCount;
	}*/

	
		
	
}
