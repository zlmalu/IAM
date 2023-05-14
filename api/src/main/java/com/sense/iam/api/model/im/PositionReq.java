package com.sense.iam.api.model.im;

import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sense.core.serializer.JsonDate;
import com.sense.iam.model.im.App;
import com.sense.iam.model.im.AppFunc;
import com.sense.iam.model.im.Position;
import com.sense.iam.model.im.User;
import com.sense.iam.model.sys.Acct;
import com.sense.iam.model.sys.Func;
import com.sense.iam.model.sys.Role;

/**
 * 岗位模型
 * 
 * Description:  岗位信息模型定义
 * 
 * @author j_hy
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class PositionReq{
	/**岗位唯一标识*/
	private Long id;
	/**岗位编码*/
	
	private String sn;
	/**岗位名称*/
	
	private String name;
	/**岗位描述*/
	private String remark;

	/**状态*/
	private Integer status;
	
	private Date createTime;
	
	
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}


	private long[] apps;

	
	private long[] appFuncs;
	
	
	@ApiModelProperty(value="岗位关联应用集合")
	public long[] getApps() {
		return apps;
	}

	public void setApps(long[] apps) {
		this.apps = apps;
	}
	@ApiModelProperty(value="岗位关联应用功能集合")
	public long[] getAppFuncs() {
		return appFuncs;
	}

	public void setAppFuncs(long[] appFuncs) {
		this.appFuncs = appFuncs;
	}

	@ApiModelProperty(value="岗位唯一标识")
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	@ApiModelProperty(value="岗位编码")
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	@ApiModelProperty(value="岗位名称")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@ApiModelProperty(hidden=true)
	@JsonSerialize(using=JsonDate.class)
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	@ApiModelProperty(value="岗位备注")
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	
	@ApiModelProperty(hidden=true)
	public Position getPosition(){
		Position position=new Position();
		position.setId(this.getId());
		position.setName(this.getName());
		position.setRemark(this.getRemark());
		position.setCreateTime(this.getCreateTime());
		position.setSn(this.getSn());
		position.setStatus(this.getStatus());
		List<App> list=new ArrayList<App>();
		List<AppFunc> appFunclist=new ArrayList<AppFunc>();
		if(apps!=null){
			if(apps.length>0){
				for(long id:apps){
					App s=new App();
					s.setId(id);
					list.add(s);
				}
			}
		}
		if(appFuncs!=null){
			if(appFuncs.length>0){
				for(long id:appFuncs){
					AppFunc s=new AppFunc();
					s.setId(id);
					appFunclist.add(s);
				}
			}
		}
		position.setApps(list);;
		position.setAppFuncs(appFunclist);
		return position;
	}
}
