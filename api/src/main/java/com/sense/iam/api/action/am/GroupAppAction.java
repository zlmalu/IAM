package com.sense.iam.api.action.am;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sense.core.util.PageList;
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.api.model.PostionFuncTree;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.am.GroupApp;
import com.sense.iam.model.am.GroupFuncModel;
import com.sense.iam.model.am.UserGroup;
import com.sense.iam.model.im.Account;
import com.sense.iam.model.im.App;
import com.sense.iam.model.im.AppFunc;
import com.sense.iam.model.im.AppType;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.AmGroupAppFuncService;
import com.sense.iam.service.AmGroupAppService;
import com.sense.iam.service.AmGroupService;
import com.sense.iam.service.AppFuncService;
import com.sense.iam.service.AppService;
import com.sense.iam.service.AppTypeService;
import com.sense.iam.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(tags = "用户组权限模块")
@RestController
@Controller
@RequestMapping("am/groupApp")
public class GroupAppAction extends AbstractAction<GroupApp,Long>{
	
	@Resource
	private AmGroupAppService amGroupAppService;
	
	@Resource
	private AmGroupAppFuncService amGroupAppFuncService;
	
	@Resource
	private AppTypeService appTypeService;
	
	@Resource
	private AppService appService;
	
	@Resource
	private AppFuncService appFuncService;
	
	@Resource
	private AccountService accountService;
	
	
	/**
	 * 获取用户组绑定的权限对象列表-目前支持三个层级权限，应用类型-应用-应用第一层权限
	 * 
	 * @return
	 */
	@ApiOperation(value="获取用户组关联权限列表")
	@SuppressWarnings("unchecked")
	@RequestMapping(value="getNodeByGroupIdList/{groupId}",method=RequestMethod.GET)
	@ResponseBody
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "groupId", value = "用户组权限标识", required = true, paramType="path", dataType = "Long")
	})
	public List<PostionFuncTree> loadNodeByGroupIdList(@PathVariable Long groupId){
		List<PostionFuncTree> list=new ArrayList<PostionFuncTree>();
		//如果岗位为0则直接返回空数组
		if(groupId==0)return list;
		PostionFuncTree treeNode;
		//第一层  应用类型
		List<AppType> appTypeList=appTypeService.findAll();
		for (AppType appType : appTypeList) {
			App sApp=new App();
			sApp.setAppTypeId(appType.getId());
			List<App> appList=appService.findList(sApp);
			if(appList.size()>0){
				//第二层  应用
				Set<Long> authApps=(Set<Long>) amGroupAppService.findAppIdsByGroupId(groupId);
				for (App app : appList) {
					//保存第一层信息
					treeNode=new PostionFuncTree();
					treeNode.setId(appType.getId().toString());
					//路径======应用类型名称/应用名称
					treeNode.setText("/"+appType.getName());
					if(authApps.contains(app.getId())==false){
						continue;
					}
					treeNode.setChecked(true);
					treeNode.getAttrMap().put("type", "1");
					list.add(treeNode);
					
					//保存第二层 
					treeNode=new PostionFuncTree();
					treeNode.setParentId(appType.getId());
					treeNode.setId(app.getId().toString());
					treeNode.setAppId(app.getId());
					//路径======应用类型名称/应用名称
					treeNode.setText("/"+appType.getName()+"/"+app.getName());
					if(authApps.contains(app.getId())==false){
						continue;
					}
					treeNode.setChecked(true);
					treeNode.getAttrMap().put("type", "2");
					list.add(treeNode);
					AppFunc entity = new AppFunc();
					entity.setAppId(app.getId());
					List<AppFunc> appFuncList = appFuncService.findList(entity);
					if(appFuncList.size()>0){
						//第三层 应用权限
						Set<Long> authAppFuncs=amGroupAppFuncService.findAppFuncIdsByGroupId(groupId);
						for (Long appFunc : authAppFuncs) {
							treeNode=new PostionFuncTree();
							treeNode.setId(appFunc.toString());
							//路径======应用类型名称/应用名称
							treeNode.setChecked(true);
							treeNode.getAttrMap().put("type", "3");
							list.add(treeNode);
						}
					}
				}
			}
		}
		return list;
	}
	
	@Resource
	private AmGroupService amgroupservice;
	@Resource
	private UserService userService;
	
	@ApiOperation(value="保存用户组和应用关系")
    @RequestMapping(value="saveGroupApp",method = RequestMethod.POST)
	@ResponseBody
	@SuppressWarnings("rawtypes")
    public ResultCode saveGroupApp(@RequestBody GroupFuncModel entity) {
		List<Long> apps = entity.getApps();
		//获取原先权限编辑的数据
		Set<Long> oldAppIds = amGroupAppService.findAppIdsByGroupId(entity.getGroupId());
		try{
			if(entity.getGroupId().longValue()==0){
				return new ResultCode(Constants.OPERATION_FAIL);
			}
			Set appSet = new HashSet();
			appSet.addAll(entity.getApps());
			ArrayList<Long> arrayList2 = new ArrayList<Long>();
			arrayList2.addAll(appSet);
			entity.setApps(arrayList2);
			
			Set appFuncSet = new HashSet();
			appFuncSet.addAll(entity.getAppfuns());
			ArrayList<Long> arrayList = new ArrayList<Long>();
			arrayList.addAll(appFuncSet);
			entity.setAppfuns(arrayList);
			
			//保存用户组信息，返回新增组
			Set<Long> authApps=(Set<Long>) amGroupAppService.findAppIdsByGroupId(entity.getGroupId());
			List<Long> addAppList=new ArrayList<Long>();
			List<Long> addUserList=new ArrayList<Long>();
			//对比新增app
			for (Long appId : entity.getApps()) {
				if(!authApps.contains(appId)){
					addAppList.add(appId);
				}
			}
			//获取用户组关联的用户集合
			UserGroup userGroup=new UserGroup();
			userGroup.setGroupId(entity.getGroupId());
			PageList<Map> pageList=amgroupservice.removeusergroupid(userGroup, 0, 10000);
			for (Map map : pageList.getDataList()) {
				Long id=Long.valueOf(map.get("ID").toString());
				addUserList.add(id);
			}
			log.info("addUserList==="+addUserList.size()+"&&&=="+addAppList.size());
			System.out.println("addUserList==="+addUserList.size()+"&&&=="+addAppList.size());
			if(addUserList.size()>0){
				//根据用户组id获取用户ids
				for (Long userId : addUserList) {
					List<Long> list = new ArrayList<Long>();
					//获取用户其他用户组、岗位之间不冲突的appIds
					Set<Long> appIds = amGroupAppService.judgePowerIds(userId);
					//去重复
					oldAppIds.removeAll(apps);
					oldAppIds.removeAll(appIds);
					
					for (Long appId : oldAppIds) {
						Account account = new Account();
						account.setAppId(appId);
						account.setUserId(userId);
						List<Account> findList = accountService.findList(account);
						if(findList.size()>0){
							list.add(findList.get(0).getId());
						}					
					}
					//删除权限
					accountService.removeByIds(list.toArray(new Long[list.size()]));
					list.clear();
				}
				userService.authApp(addUserList.toArray(new Long[addUserList.size()]), addAppList.toArray(new Long[addAppList.size()]), Constants.ACCOUNT_OPEN_TYPE_GROUP);
			}
			amGroupAppFuncService.saveGroupApp(entity);
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			e.printStackTrace();
			return new ResultCode(Constants.OPERATION_FAIL);
		}
    }
	
}
