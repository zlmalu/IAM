package com.sense.iam.api.model.im;

import java.util.Map;

import com.sense.iam.model.im.Equipment;

import io.swagger.annotations.ApiModelProperty;

public class EquipmentReq {
	
	private Long id;
	
	@ApiModelProperty(value="唯一编码",required=true)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * 设备编号
	 */
	private String sn;
	/**
	 * 设备标识
	 */
	private String token;
	/**
	 * 设备来源
	 */
	private Integer source;
	/**
	 * 设备状态
	 */
	private Integer status;
	/**
	 * 备注
	 */
	private String remark;
	
	/**设备归属类型ID*/
	private Long equipmentTypeId;
	
	/**管理该设备用户ID*/
	private String userIds;

	/**扩展属性*/
	private Map extraAttrs;
	
	@ApiModelProperty(value="扩展属性 ，key-value形式")
	public Map getExtraAttrs() {
		return extraAttrs;
	}

	public void setExtraAttrs(Map extraAttrs) {
		this.extraAttrs = extraAttrs;
	}
	
	@ApiModelProperty(value="管理该设备用户ID")
	public String getUserIds() {
		return userIds;
	}
	public void setUserIds(String userIds) {
		this.userIds = userIds;
	}
	
	@ApiModelProperty(value="设备类型ID")
	public Long getEquipmentTypeId() {
		return equipmentTypeId;
	}
	public void setEquipmentTypeId(Long equipmentTypeId) {
		this.equipmentTypeId = equipmentTypeId;
	}
	
	@ApiModelProperty(value="设备编号",required=true)
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	
	@ApiModelProperty(value="设备标识",required=true)
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}

	@ApiModelProperty(value="设备来源",required=true)
	public Integer getSource() {
		return source;
	}
	public void setSource(Integer source) {
		this.source = source;
	}
	
	@ApiModelProperty(value="设备状态",required=true)
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	
	@ApiModelProperty(value="设备描述",required=true)
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	@ApiModelProperty(hidden=true)
	public Equipment getEquipment(){
		Equipment equipment=new Equipment();
		equipment.setId(this.getId());
		equipment.setSn(this.sn);
		equipment.setToken(this.token);
		equipment.setSource(this.source);
		equipment.setStatus(this.status);
		equipment.setEquipmentTypeId(this.equipmentTypeId);
		equipment.setUserIds(this.userIds);
		equipment.setRemark(this.remark);
		equipment.setExtraAttrs(this.extraAttrs);
		return equipment;
	}
	
}
