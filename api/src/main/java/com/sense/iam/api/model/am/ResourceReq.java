package com.sense.iam.api.model.am;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sense.iam.model.am.Ip;
import com.sense.iam.model.am.Resource;
import com.sense.iam.model.am.Time;
import com.sense.iam.service.AmResourceService;

import io.swagger.annotations.ApiModelProperty;

/**
 * 资源控制管理 - ModelReq
 * @author K3w1n
 *
 */
public class ResourceReq {

	@javax.annotation.Resource
	private AmResourceService amresourceservice;
	
	
	/** 唯一标识*/
	private Long id;
	/** 认证域编码*/
	private String sn;
	/** 模块名称*/
	private String name;
	/** 资源路径*/
	private String url;
	/** 安全级别*/
	private Long leval;
	/** 认证域表ID*/
	private Long reamlId;
	/** 创建时间*/
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
	private Date createTime;
	/** 认证域名*/
	private String amauthreamlName;
	/** 资源ID*/
	private Long resourceId;
	/** 时间策略ID*/
	private String timeId;
	/** IP策略ID*/
	private String ipId;
	
	private List<Time> listtime;
	private List<Ip> listip;
	/**
	 *获取： 唯一标识 
	 *@return the id 唯一标识
	 */
	@ApiModelProperty(value="唯一标识", required=true)
	public Long getId() {
		return id;
	}
	/**
	 * 设置： 唯一标识
	 * @param id 唯一标识
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 *获取： 认证域编码 
	 *@return the sn 认证域编码
	 */
	@ApiModelProperty(value="认证域编码", required=true)
	public String getSn() {
		return sn;
	}
	/**
	 * 设置： 认证域编码
	 * @param sn 认证域编码
	 */
	public void setSn(String sn) {
		this.sn = sn;
	}
	/**
	 *获取： 模块名称 
	 *@return the name 模块名称
	 */
	@ApiModelProperty(value="模块名称", required=true)
	public String getName() {
		return name;
	}
	/**
	 * 设置： 模块名称
	 * @param name 模块名称
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 *获取： 资源路径 
	 *@return the url 资源路径
	 */
	@ApiModelProperty(value="资源路径", required=true)
	public String getUrl() {
		return url;
	}
	/**
	 * 设置： 资源路径
	 * @param url 资源路径
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 *获取： 安全级别 
	 *@return the leval 安全级别
	 */
	@ApiModelProperty(value="安全级别", required=true)
	public Long getLeval() {
		return leval;
	}
	/**
	 * 设置： 安全级别
	 * @param leval 安全级别
	 */
	public void setLeval(Long leval) {
		this.leval = leval;
	}
	/**
	 *获取： 认证域表ID 
	 *@return the reamlId 认证域表ID
	 */
	@ApiModelProperty(value="认证域表ID", required=true)
	public Long getReamlId() {
		return reamlId;
	}
	/**
	 * 设置： 认证域表ID
	 * @param reamlId 认证域表ID
	 */
	public void setReamlId(Long reamlId) {
		this.reamlId = reamlId;
	}
	/**
	 *获取： 创建时间 
	 *@return the createTime 创建时间
	 */
	public Date getCreateTime() {
		return createTime;
	}
	/**
	 * 设置： 创建时间
	 * @param createTime 创建时间
	 */
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	/**
	 *获取： 认证域名 
	 *@return the amauthreamlName 认证域名
	 */
	public String getAmauthreamlName() {
		return amauthreamlName;
	}
	/**
	 * 设置： 认证域名
	 * @param amauthreamlName 认证域名
	 */
	public void setAmauthreamlName(String amauthreamlName) {
		this.amauthreamlName = amauthreamlName;
	}
	/**
	 *获取：  
	 *@return the resourceId 
	 */
	@ApiModelProperty(value="资源ID", required=true)
	public Long getResourceId() {
		return resourceId;
	}
	/**
	 * 设置： 
	 * @param resourceId 
	 */
	public void setResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}
	/**
	 *获取：  
	 *@return the timeId 
	 */
	@ApiModelProperty(value="时间ID", required=true)
	public String getTimeId() {
		return timeId;
	}
	/**
	 * 设置： 
	 * @param timeId 
	 */
	public void setTimeId(String timeId) {
		this.timeId = timeId;
	}
	/**
	 *获取：  
	 *@return the ipId 
	 */
	@ApiModelProperty(value="IPID", required=true)
	public String getIpId() {
		return ipId;
	}
	/**
	 * 设置： 
	 * @param ipId 
	 */
	public void setIpId(String ipId) {
		this.ipId = ipId;
	}
	/**
	 *获取：  
	 *@return the listtime 
	 */
	public List<Time> getListtime() {
		return listtime;
	}
	/**
	 * 设置： 
	 * @param listtime 
	 */
	public void setListtime(List<Time> listtime) {
		this.listtime = listtime;
	}
	/**
	 *获取：  
	 *@return the listip 
	 */
	public List<Ip> getListip() {
		return listip;
	}
	/**
	 * 设置： 
	 * @param listip 
	 */
	public void setListip(List<Ip> listip) {
		this.listip = listip;
	}
	/**
	 * 添加资源控制关联人员
	 */
	@ApiModelProperty(hidden =  true)
	public Resource getResource() {
		Resource resource = new Resource();
		resource.setId(this.getId());
		resource.setSn(this.getSn());
		resource.setName(this.getName());
		resource.setUrl(this.getUrl());
		resource.setLeval(this.getLeval());
		resource.setReamlId(this.getReamlId());
		resource.setTimeId(this.getTimeId());
		resource.setIpId(this.getIpId());
		return resource;
	}
}
