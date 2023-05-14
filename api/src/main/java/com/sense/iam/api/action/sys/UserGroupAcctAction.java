package com.sense.iam.api.action.sys;


import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.sys.UserGroupAcct;
import com.sense.iam.service.SysUserGroupAcctService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiSort;
/**
 * 系统用户组与系统用户关联模块
 * 
 * Description: 
 * 
 * @author ygd
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Api(tags = "系统用户组授权用户模块")
@RestController
@Controller
@RequestMapping("sys/userGroupAcct")
@ApiSort(value = 5)
public class UserGroupAcctAction extends AbstractAction<UserGroupAcct,Long>{
	
	@Resource
	private SysUserGroupAcctService sysUserGroupAcctService;

	@ApiOperation(value="新增")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody UserGroupAcct entity) {
		List<Long> acctIdList = entity.getAcctIdList();
		sysUserGroupAcctService.removeByUserGroupId(entity.getUserGroupId());
		if(acctIdList.size()>0){
			for (Long acctId : acctIdList) {
				entity.setAcctId(acctId);
				super.save(entity);
			}
		}
		return new ResultCode(Constants.OPERATION_SUCCESS);
	}
	
	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "id", value = "userGroupById", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findById/{id}", method=RequestMethod.GET)
	@ResponseBody
	public UserGroupAcct findById(@PathVariable Long id) {
		List<Long> acctIdList = sysUserGroupAcctService.findByAcctIds(id);
		UserGroupAcct userGroupAcct = new UserGroupAcct();
		userGroupAcct.setAcctIdList(acctIdList);
		userGroupAcct.setUserGroupId(id);
		return userGroupAcct;
	}
}
