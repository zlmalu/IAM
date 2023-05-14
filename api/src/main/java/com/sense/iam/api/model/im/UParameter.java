package com.sense.iam.api.model.im;


import io.swagger.annotations.ApiModelProperty;

/**
 * 帐号模型
 * 
 * Description:  应用信息模型定义
 * 
 * @author j_hy
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class UParameter {

	/**
	 * 应用编码
	 */
	private String appSn;

	private String starttime;
	
	private String endtime;
	
	private int page=1;
	
	private int pageSize=100;

	@ApiModelProperty(value="应用编码")
	public String getAppSn() {
		return appSn;
	}
	public void setAppSn(String appSn) {
		this.appSn = appSn;
	}
	
	@ApiModelProperty(value="查询开始时间范围yyyy-MM-dd HH:mm:ss格式")
	public String getStarttime() {
		return starttime;
	}
	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}
	@ApiModelProperty(value="查询结束时间范围yyyy-MM-dd HH:mm:ss格式")
	public String getEndtime() {
		return endtime;
	}
	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}
	@ApiModelProperty(value="页数,默认1")
	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}
	@ApiModelProperty(value="数量,默认100")
	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	@Override
	public String toString() {
		return "UParameter [appSn=" + appSn + ", starttime=" + starttime
				+ ", endtime=" + endtime + ", page=" + page + ", pageSize="
				+ pageSize + "]";
	}
	
	
	
	
	
	
}
