package com.sense.iam.api.action.sys;


import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
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
import com.sense.iam.model.sys.UserGroupApp;
import com.sense.iam.model.sys.UserGroupOrg;
import com.sense.iam.service.SysUserGroupOrgService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiSort;
/**
 * 系统用户组与组织关联模块
 * 
 * Description: 
 * 
 * @author ygd
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Api(tags = "系统用户组授权组织模块")
@RestController
@Controller
@RequestMapping("sys/userGroupOrg")
@ApiSort(value = 5)
public class UserGroupOrgAction extends AbstractAction<UserGroupOrg,Long>{
	
	@Resource
	private SysUserGroupOrgService sysUserGroupOrgService;

	@ApiOperation(value="新增")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody UserGroupOrg entity) {
		List<Long> userGroupIdList = entity.getUserGroupIdList();
		sysUserGroupOrgService.removeByOrgId(entity.getOrgId());
		if(userGroupIdList.size()>0){
			for (Long userGroupId : userGroupIdList) {
				entity.setUserGroupId(userGroupId);
				super.save(entity);
			}
		}
		return new ResultCode(Constants.OPERATION_SUCCESS);
	}
	
	@ApiOperation(value="分页查询用户组授权组织列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findByOrgList", method=RequestMethod.POST)
	@ResponseBody
	public PageList<UserGroupOrg> findByOrgList(@RequestBody UserGroupOrg entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit) {
		entity.setIsLikeQuery(true);
		return sysUserGroupOrgService.findByOrgPage(entity,page,limit);
	}
}
