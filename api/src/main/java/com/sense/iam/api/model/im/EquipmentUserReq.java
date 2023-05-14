package com.sense.iam.api.model.im;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

import com.sense.iam.cam.Params;
import com.sense.iam.model.im.Account;



/**
 * 设备关联用户入参模型
 * 
 * Description:  应用信息模型定义
 * 
 * @author ygd
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class EquipmentUserReq {

	private List<Long> equipmentIds;
	
	private List<Long> userIds;
	
	public List<Long> getEquipmentIds() {
		return equipmentIds;
	}
	public void setEquipmentIds(List<Long> equipmentIds) {
		this.equipmentIds = equipmentIds;
	}
	@ApiModelProperty(value="用户ID集合")
	public List<Long> getUserIds() {
		return userIds;
	}
	public void setUserIds(List<Long> userIds) {
		this.userIds = userIds;
	}
	
	
}
