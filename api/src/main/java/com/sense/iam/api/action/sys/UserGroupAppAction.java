package com.sense.iam.api.action.sys;


import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sense.core.util.PageList;
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.sys.UserGroup;
import com.sense.iam.model.sys.UserGroupApp;
import com.sense.iam.service.SysUserGroupAppService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiSort;
/**
 * 系统用户组与应用关联模块
 * 
 * Description: 
 * 
 * @author ygd
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Api(tags = "系统用户组授权应用模块")
@RestController
@Controller
@RequestMapping("sys/userGroupApp")
@ApiSort(value = 5)
public class UserGroupAppAction extends AbstractAction<UserGroupApp,Long>{
	
	@Resource
	private SysUserGroupAppService sysUserGroupAppService;

	@ApiOperation(value="新增")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody UserGroupApp entity) {
		List<Long> userGroupIdList = entity.getUserGroupIdList();
		sysUserGroupAppService.removeByAppId(entity.getAppId());
		if(userGroupIdList.size()>0){
			for (Long userGroupId : userGroupIdList) {
				entity.setUserGroupId(userGroupId);
				super.save(entity);
			}
		}
		return new ResultCode(Constants.OPERATION_SUCCESS);
	}
	

	@ApiOperation(value="分页查询用户组授权应用列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findByAppList", method=RequestMethod.POST)
	@ResponseBody
	public PageList<UserGroupApp> findByAppList(@RequestBody UserGroupApp entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit) {
		entity.setIsLikeQuery(true);
		return sysUserGroupAppService.findByAppPage(entity,page,limit);
	}
	
	@ApiOperation(value="分页查询用户组授权应用类型列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findByAppTypeList", method=RequestMethod.POST)
	@ResponseBody
	public PageList<UserGroupApp> findByAppTypeList(@RequestBody UserGroupApp entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit) {
		entity.setIsLikeQuery(true);
		return sysUserGroupAppService.findByAppTypePage(entity,page,limit);
	}
}
