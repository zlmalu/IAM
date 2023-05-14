package com.sense.iam.api.action.am;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.text.StringEscapeUtils;
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
import com.sense.iam.api.model.EnterAuthModel;
import com.sense.iam.api.model.sys.AuthUserGroupReq;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.am.Group;
import com.sense.iam.model.am.GroupApp;
import com.sense.iam.model.am.GroupDynamic;
import com.sense.iam.model.am.UserGroup;
import com.sense.iam.model.im.Account;
import com.sense.iam.model.im.UserPosition;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.AmGroupAppService;
import com.sense.iam.service.AmGroupDynamicService;
import com.sense.iam.service.AmGroupService;
import com.sense.iam.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiSort;
/**
 * 用户组模块
 * 
 * Description: 
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Api(tags = "静态用户组模块")
@RestController
@Controller
@RequestMapping("am/group")
@ApiSort(value = 5)
public class GroupAction extends AbstractAction<Group,Long>{
	
	
	@Resource
	private AmGroupService amgroupservice;
	
	@Resource
	private AmGroupDynamicService amGroupDynamicService;
	
	@Resource
	private UserService userService;
	
	@Resource
	private AmGroupAppService amGroupAppService;

	@Resource
	private AccountService accountService;
	
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
	protected PageList<Group> findList(@RequestBody Group entity1,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			Group entity=new Group();
			entity.setSn(StringEscapeUtils.escapeHtml4(entity1.getSn()));
			entity.setName(StringEscapeUtils.escapeHtml4(entity1.getName()));
			entity.setType(entity1.getType());
			entity.setIsLikeQuery(true);
			return getBaseService().findPage(entity,page,limit);
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<Group>();
		}
	}
	
	
	
	@ApiOperation(value="新增")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody Group entity) {
		return super.save(entity);
	}
	
	@ApiOperation(value="更新")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 3)
	public ResultCode edit(@RequestBody Group entity) {
		return super.edit(entity);
	}
	
	
	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findById/{id}", method=RequestMethod.GET)
	@ResponseBody
	public Group findById(@PathVariable Long id) {
		return super.findById(id);
	}
	
	
	
	
	/**
	 * 添加用户到用户组
	 * @param userIds
	 * @param groupIds
	 * @return
	 */
	@ApiOperation(value="添加用户到用户组")
	@RequestMapping(value="authGroup", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	public ResultCode authGroup(@RequestBody AuthUserGroupReq userGroupReq){
		try {
			String[] ids = userGroupReq.getUserIds().split(",");
			if(ids.length>0){
				for (String id : ids) {
					List<Long> addUserList=new ArrayList<Long>();
					addUserList.add(Long.parseLong(id));
					Set<Long> findAppIdsByGroupId = amGroupAppService.findAppIdsByGroupId(Long.parseLong(userGroupReq.getGroupIds()));
					userService.authApp(addUserList.toArray(new Long[addUserList.size()]),findAppIdsByGroupId.toArray(new Long[findAppIdsByGroupId.size()]), Constants.ACCOUNT_OPEN_TYPE_GROUP);
				}
			}
			else{
				List<Long> addUserList=new ArrayList<Long>();
				addUserList.add(Long.parseLong(userGroupReq.getUserIds()));
				Set<Long> findAppIdsByGroupId = amGroupAppService.findAppIdsByGroupId(Long.parseLong(userGroupReq.getGroupIds()));
				userService.authApp(addUserList.toArray(new Long[addUserList.size()]),findAppIdsByGroupId.toArray(new Long[findAppIdsByGroupId.size()]), Constants.ACCOUNT_OPEN_TYPE_GROUP);
			}
			amgroupservice.addgroupacct(userGroupReq.getUserIds(), userGroupReq.getGroupIds());
			return new ResultCode(Constants.OPERATION_SUCCESS);
		} catch (Exception e) {
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
	/**
	 * 移除用户组中的用户
	 * @param userIds
	 * @param groupIds
	 * @return
	 */
	@ApiOperation(value="移除用户组中的用户")
	@RequestMapping(value="removegroupacct", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 5)
	public ResultCode removegroupacct(@RequestBody AuthUserGroupReq userGroupReq){
		try {
			//查询用户组中绑定的app权限id
			String[] groupIds = userGroupReq.getGroupIds().split(",");
			String[] userIds = userGroupReq.getUserIds().split(",");
			for (String userId : userIds) {
				//遍历用户的用户组
				for (String groupId : groupIds) {
					//查询权限中重复的权限
					List<Long> appIds = judgePowerIds(Long.parseLong(groupId),Long.parseLong(userId));
					//获取对应的账号id
					List<Long> list = new ArrayList<Long>();
					for (Long appId : appIds) {
						Account account = new Account();
						account.setAppId(appId);
						account.setUserId(Long.parseLong(userId));
						List<Account> findList = accountService.findList(account);
						if(findList.size()>0){
							list.add(findList.get(0).getId());
						}					
					//删除权限
					accountService.removeByIds(list.toArray(new Long[list.size()]));
					}
				}
			}
			amgroupservice.removegroupacct(userGroupReq.getUserIds(), userGroupReq.getGroupIds());
			
			return new ResultCode(Constants.OPERATION_SUCCESS);
		} catch (Exception e) {
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
	/**
	 * 获取用户组的用户分页查询
	 * @param id
	 * @param page
	 * @param limit
	 * @return
	 */
	@ApiOperation(value="获取未分配用户分页查询")
	@RequestMapping(value="findusergroupid", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 6)
	public Object findusergroupid(@RequestBody UserGroup entity, @RequestParam(defaultValue="1") Integer page, @RequestParam(defaultValue="20") Integer limit) {
		if(entity.getSn().length()>0||entity.getName().length()>0){
			//开启模糊查询
			entity.setIsLikeQuery(true);
		}
		return amgroupservice.findusergroupid(entity, page, limit);
	}
	
	/**
	 * 获取用户组的用户集合
	 * @param id
	 * @param page
	 * @param limit
	 * @return
	 */
	@ApiOperation(value="获取已分配用户组的用户集合")
	@RequestMapping(value="getUserByGroupId", method=RequestMethod.POST)
	@ResponseBody
	public Object removeusergroupid(@RequestBody UserGroup entity, @RequestParam(defaultValue="1") Integer page, @RequestParam(defaultValue="20") Integer limit) {
		if(entity.getSn().length()>0||entity.getName().length()>0){
			//开启模糊查询
			entity.setIsLikeQuery(true);
		}
		return amgroupservice.removeusergroupid(entity, page, limit);
	}
	
	
	@ApiOperation(value="移除用户组")
    @RequestMapping(value="remove",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 4)
    public ResultCode remove(@RequestBody @ApiParam(name="唯一标识集合",value="多数据采用英文逗号分割",required=true)List<String> ids) {
		for (String id : ids) {
			Group findById = amgroupservice.findById(Long.parseLong(id));
			if(findById.getType().equals(2)){
				GroupDynamic dynamic = new GroupDynamic();
				dynamic.setGroupId(Long.parseLong(id));
				List<GroupDynamic> findList = amGroupDynamicService.findList(dynamic);
				if(findList.size()>0){
					amGroupDynamicService.removeGroupId(Long.parseLong(id));
				}
			}
		}
		Params params=new Params();
		params.setIds(ids);
		return super.remove(params);
    }
	
	@ApiOperation(value="通过用户id分页查询用户组")
	@RequestMapping(value="findByGroupId", method=RequestMethod.POST)
	@ResponseBody
	public Object findByGroupId(@RequestBody UserGroup entity1, @RequestParam(defaultValue="1") Integer page, @RequestParam(defaultValue="20") Integer limit) {
		UserGroup entity=new UserGroup();
		entity.setSn(StringEscapeUtils.escapeHtml4(entity1.getSn()));
		entity.setName(StringEscapeUtils.escapeHtml4(entity1.getName()));
		entity.setType(entity1.getType());
		entity.setUserId(entity1.getUserId());
		entity.setGroupId(entity1.getGroupId());
		entity.setIsLikeQuery(true);
		PageList findByGroupId = amgroupservice.findByGroupId(entity,page,limit);
		return findByGroupId;
	}

	/**
	 * 获取用户组与岗位权限之间不重复数据
	 * @param groupId
	 * @return
	 */
	public List<Long> judgePowerIds(Long groupId,Long userId){
		Set<Long> judgePowerIds = amGroupAppService.judgePowerIds(userId);
		Set<Long> appIds =  amGroupAppService.findAppIdsByGroupId(groupId);
		List<Long> ids = new  ArrayList<Long>();
		//去除appIds中未重复在其他权限中的应用
		appIds.removeAll(judgePowerIds);
		ids.addAll(appIds);
		return ids;
	}
}
