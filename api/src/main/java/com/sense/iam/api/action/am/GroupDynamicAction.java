package com.sense.iam.api.action.am;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.sense.core.util.CurrentAccount;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;





import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.api.model.sys.GroupReq;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.am.Group;
import com.sense.iam.model.am.GroupDynamic;
import com.sense.iam.model.am.UserGroup;
import com.sense.iam.model.sys.Field;
import com.sense.iam.service.AmGroupDynamicService;
import com.sense.iam.service.AmGroupService;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.SysFieldService;




import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 动态用户组模块
 * 
 * Description: 
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Api(tags = "动态用户组模块")
@RestController
@Controller
@RequestMapping("am/groupDynamic")
public class GroupDynamicAction extends AbstractAction<GroupDynamic,Long>{
	
	@Resource
	private AmGroupService amgroupservice;
	
	@Resource
	private AmGroupDynamicService amGroupDynamicService;
	
	@Resource
	private SysFieldService sysFieldService;
	
	@Resource
	private JdbcService jdbcService;
	
	
	/**
	 * @param entity
	 * @return
	 */
	@ApiOperation(value="新增")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode save(@RequestBody GroupReq entity) {
		//通过id判断是否是新增或修改
		ResultCode resultCode = new ResultCode();
		if(entity.getId()!=null){
			resultCode = amgroupservice.edit(entity.getGroup());
		}else{
			resultCode = amgroupservice.save(entity.getGroup());
		}
		List<Group> findList = amgroupservice.findList(entity.getGroup());
		if(resultCode.getSuccess()){
			//通过id判断是否是修改
			if(entity.getId()!=null){
				try{
				//刪除过滤条件及user_group表中动态添加的数据
				amGroupDynamicService.removeGroupId(entity.getId());
				amGroupDynamicService.removeUserGroud(entity.getId().toString());
				}
				catch(Exception e){
				}
			}
			for (GroupDynamic groupDynamic : entity.getDynamicList()) {
				groupDynamic.setGroupId(findList.get(0).getId());
				amGroupDynamicService.save(groupDynamic);
			}
			
			//动态添加用户数据
			String findSql = findSql(entity.getDynamicList());
			List<Map> userList = jdbcService.findList(findSql);
			String ids = "";
			if(userList.size()>0){
				for (int i = 0; i < userList.size(); i++) {
					ids += userList.get(i).get("ID")+",";
					if(userList.size()-1==i){
						ids += userList.get(i).get("ID")+"";
					}
				}
				amgroupservice.addgroupacct(ids, findList.get(0).getId().toString());
			}
			return resultCode;
		}
		return resultCode;
	}
	
	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findByGId/{id}", method=RequestMethod.GET)
	@ResponseBody
	public GroupReq findByGId(@PathVariable Long id) {
		GroupDynamic entity = new GroupDynamic();
		entity.setGroupId(id);
		List<GroupDynamic> findList = amGroupDynamicService.findList(entity);
		Group findById = amgroupservice.findById(id);
		amGroupDynamicService.findList(entity);
		GroupReq req = new GroupReq();
		req.setName(findById.getName());
		req.setRemark(findById.getRemark());
		req.setSn(findById.getSn());
		req.setType(findById.getType());
		req.setDynamicList(findList);
		return req;
	}
	
	/**
	 * 获取用户组的用户分页查询
	 * @param id
	 * @param page
	 * @param limit
	 * @return
	 * @throws Exception 
	 */
	@ApiOperation(value="获取动态组用户分页查询")
	@RequestMapping(value="findusergroupid", method=RequestMethod.POST)
	@ResponseBody
	public Object findusergroupid(@RequestBody UserGroup entity, @RequestParam(defaultValue="1") Integer page, @RequestParam(defaultValue="20") Integer limit) throws Exception {
		if(entity.getSn().length()>0||entity.getName().length()>0){
			//开启模糊查询
			entity.setIsLikeQuery(true);
		}
		return amGroupDynamicService.findusergroupid(entity, page, limit);
		
	}
	
	/**
	 * 用户动态组查询sql生成
	 * @param findList
	 * @return
	 */
	public String findSql(List<GroupDynamic> findList){
		String findusergroupidCountSql = "select * from IM_USER where ID not in (select IM_USER_ID from AM_USER_GROUPE where AM_GROUP_ID = '"+findList.get(0).getId()+"' ) ";
		if(findList.size()>0){
			for (GroupDynamic groupDynamic : findList) {
				Field findById = sysFieldService.findById(groupDynamic.getSysFieldId());
				String type = groupDynamic.getType();
				String value = groupDynamic.getValue();
				if(type.equals("包含")){
					type = "like";
					value = "'%"+value+"%'";
				}
				else if(type.equals("不包含")){
					type = "not like";
					value = "'%"+value+"%'";
				}
				else{
					value = "'"+value+"'";
				}

				findusergroupidCountSql += " and " + findById.getName() +" "+  type + " " + value;
			}
		}
		findusergroupidCountSql += "and COMPANY_SN = '"+ CurrentAccount.getCurrentAccount().getCompanySn() +"' ORDER BY create_time desc";
		return findusergroupidCountSql;
	}
	
}
