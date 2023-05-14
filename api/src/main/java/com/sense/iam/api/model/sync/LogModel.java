package com.sense.iam.api.model.sync;








public class LogModel {

	String appSn;
	String filterEndTime;
	String filterStartTime;
	
	String sysEventName;
	
	Integer status;

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getAppSn() {
		return appSn;
	}

	public void setAppSn(String appSn) {
		this.appSn = appSn;
	}

	public String getFilterEndTime() {
		return filterEndTime;
	}

	public void setFilterEndTime(String filterEndTime) {
		this.filterEndTime = filterEndTime;
	}

	public String getFilterStartTime() {
		return filterStartTime;
	}

	public void setFilterStartTime(String filterStartTime) {
		this.filterStartTime = filterStartTime;
	}

	public String getSysEventName() {
		return sysEventName;
	}

	public void setSysEventName(String sysEventName) {
		this.sysEventName = sysEventName;
	}
}
