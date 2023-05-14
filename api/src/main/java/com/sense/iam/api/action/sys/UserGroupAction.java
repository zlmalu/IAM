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

import com.sense.core.util.ArrayUtils;
import com.sense.core.util.PageList;
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.sys.UserGroup;
import com.sense.iam.service.SysUserGroupService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiSort;
/**
 * 系统用户组模块
 * 
 * Description: 
 * 
 * @author ygd
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Api(tags = "系统用户组模块")
@RestController
@Controller
@RequestMapping("sys/userGroup")
@ApiSort(value = 5)
public class UserGroupAction extends AbstractAction<UserGroup,Long>{
	
	@Resource
	private SysUserGroupService sysUserGroupService;

	
	/**
	 * 分页查询用户组列表
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	@ApiOperation(value="分页查询用户组列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findList", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 6)
	protected PageList<UserGroup> findList(@RequestBody UserGroup entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			entity.setIsLikeQuery(true);
			return getBaseService().findPage(entity,page,limit);
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<UserGroup>();
		}
	}
	
	@ApiOperation(value="新增")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody UserGroup entity) {
		if (sysUserGroupService.findBySn(entity.getSn()).size()>0) {
			return new ResultCode(Constants.OPERATION_EXIST,"该账号已存在！");
		}
		return super.save(entity);
	}
	
	@ApiOperation(value="更新")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 3)
	public ResultCode edit(@RequestBody UserGroup entity) {
		return super.edit(entity);
	}
	
	
	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findById/{id}", method=RequestMethod.GET)
	@ResponseBody
	public UserGroup findById(@PathVariable Long id) {
		return super.findById(id);
	}
	
	@ApiOperation(value="移除系统用户组")
    @RequestMapping(value="remove",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 4)
    public ResultCode remove(@RequestBody @ApiParam(name="唯一标识集合",value="多数据采用英文逗号分割",required=true)List<String> ids) {
		Params params=new Params();
		params.setIds(ids);
		return super.remove(params);
    }
	
	@ApiOperation(value="启动")
    @RequestMapping(value="modifyStartUp",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 5)
	public ResultCode modifyStartUp(@RequestBody @ApiParam(name = "唯一标识集合", value="多数据采取英文逗号分割", required = true)List<String> ids){
		try{
			Params params = new Params();
			params.setIds(ids);
			sysUserGroupService.sfyxStartUp(ArrayUtils.stringArrayToLongArray(params.getIds().toArray(new String[params.getIds().size()])));
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
	@ApiOperation(value="禁用")
    @RequestMapping(value="modifyStop",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 6)
	public ResultCode modifyStop(@RequestBody @ApiParam(name = "唯一标识集合", value="多数据采取英文逗号分割", required = true)List<String> ids){
		try{
			Params params = new Params();
			params.setIds(ids);
			sysUserGroupService.sfyxStop(ArrayUtils.stringArrayToLongArray(params.getIds().toArray(new String[params.getIds().size()])));
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
}
