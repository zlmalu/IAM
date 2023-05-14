package com.sense.iam.open.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

























import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.sense.core.freemark.StringParse;
import com.sense.core.util.CurrentAccount;
import com.sense.core.util.PageList;
import com.sense.core.util.StringUtils;
import com.sense.iam.api.action.BaseAction;
import com.sense.iam.api.model.BPMEnterAuthModel;
import com.sense.iam.api.model.EnterAuthModel;
import com.sense.iam.api.model.keysModel;
import com.sense.iam.api.model.im.AppFuncTree;
import com.sense.iam.api.util.AppFuncUtil;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.im.Account;
import com.sense.iam.model.im.App;
import com.sense.iam.model.im.AppFunc;
import com.sense.iam.model.im.Position;
import com.sense.iam.model.im.User;
import com.sense.iam.model.im.UserPosition;
import com.sense.iam.model.sys.Field;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.AppFuncService;
import com.sense.iam.service.AppService;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.PositionService;
import com.sense.iam.service.SysFieldService;
import com.sense.iam.service.UserPositionService;
import com.sense.iam.service.UserService;
import com.sense.iam.tld.TldModel;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiSort;

@Api(value = "API - 开通应用权限接口", tags = "开通应用权限接口")
@Controller
@RestController
@RequestMapping("open/task")
@ApiSort(value = 1)
public class OpenAccountAction extends BaseAction{

	@Resource
	private AppFuncService appFuncService;
	
	@Resource
	private JdbcService jdbcService;
	
	@Resource
	private AccountService accountService;
	
	@Resource
	private AppService appService;
	
	@Resource
	private UserService userService;
	
	@Resource
	private SysFieldService sysFieldService;
	
	@Resource
	private UserPositionService userPositionService;
	
	@Resource
	private TldModel tldModel;
	@Resource
	private PositionService positionService;
	/**
	 * 根据应用编码查询应用权限树
	 * @param appSn
	 * @return tree
	 */
	@ApiOperation(value="申请开通应用权限",code=1)
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "appSn", value = "应用编码 集合[]", required = true, paramType="path", dataType = "String"),
		 @ApiImplicitParam(name = "userId", value = "登录帐号名称", required = true, paramType="path", dataType = "String"),
		 @ApiImplicitParam(name = "clientId", value = "BPM应用标识", required = true, paramType="path", dataType = "String")
	})
	@RequestMapping(value="applyApp",method={RequestMethod.POST})
	@ResponseBody
	public ResultCode applyApp(@RequestParam String appSn,@RequestParam String userId,@RequestParam String clientId){
		Account ac=new Account();
		ac.setAppSn(clientId);
		ac.setLoginName(userId);
		log.info("appSn:"+appSn);
		log.info("userId:"+userId);
		log.info("clientId:"+clientId);
		ac=accountService.findByObject(ac);
		if(ac!=null){
			//声明所申请的应用是否存在
			int status=0;
			//声明查询不存在的应用标识集合;
			String notApps="";
			//申请人用户ID
			Long applyUserId=ac.getUserId();
			log.info("get applyUserId:"+applyUserId);
			//移除[]和两边空格
			if(appSn==null||appSn.length()==0)return new ResultCode(Constants.OPERATION_FAIL,"appSn is null");
			appSn=appSn.replace("[", "").replaceAll("]", "").trim();
			String[] appIds=appSn.split(",");
			for (String sn : appIds) {
				App app=new App();
				app.setSn(sn.trim());
				app=appService.findByObject(app);
				if(app==null){
					log.info("app sn not exit:"+sn.trim()+",continue");
					notApps+=sn.trim()+",";
					status=1;
					continue;
				}
				Account account=new Account();
				account.setAppId(app.getId());;
				account.setUserId(applyUserId);
				//判断用户是否已经授权过应用
				List<Account> accts = accountService.findList(account);
				if(accts==null || accts.size()==0){
					//根据账号策略开通账号,如果没有策略则使用默认用户名和密码
					accountService.autoOpenAccount(applyUserId, app.getId(),Constants.ACCOUNT_OPEN_TYPE_BASIC);
				}
			}
			if(status==1){
				return new ResultCode(Constants.OPERATION_SUCCESS,"some apps are not open sn:"+notApps);
			}else{
				return new ResultCode(Constants.OPERATION_SUCCESS);
			}
		}else{
			return new ResultCode(Constants.OPERATION_FAIL,"account not exit clientId="+clientId+",userId="+userId);
		}
	}

	
	/**
	 * 分页查询用户岗位列表（已授权标标记）
	 * @param entity 查询对象
	 * @return
	 */
	@ApiOperation(value="分页查询用户岗位列表（已授权标标记）")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findAuthPositionsList", method=RequestMethod.POST)
	@ResponseBody
	public PageList<?> findAuthPositionsList(@RequestBody keysModel key, @RequestParam(defaultValue="1") Integer page, @RequestParam(defaultValue="20") Integer limit) {
		
		String sql="SELECT b.ID,b.NAME AS POSITION_NAME,b.SN,a.ORG_ID,c.name as ORGNAME,c.NAME_PATH,c.NAME FROM im_org_position a left join im_position b on a.position_id=b.id left join im_org c on a.org_id=c.ID where b.STATUS=1";
		//加入模糊查询
		if(key.getKey()!=null&&key.getKey().length()>0){
			if(!"-ALL".equals(key.getKey())){
				sql=sql+" b.NAME like '%"+key.getKey()+"%' or c.name like '%"+key.getKey()+"%' or b.sn like '%"+key.getKey()+"%' or c.name_path like '%"+key.getKey()+"%' ";
			}
		}
		Position model=new Position();
		model.setSort("[{\"property\":\"SN\",\"direction\":\"DESC\"}]");
		PageList pagdse=jdbcService.findPage(sql,model, page, limit);
		List<Map<String, Object>> map=pagdse.getDataList();
		UserPosition entity = new UserPosition();
		entity.setUserId(key.getUserId());
		PageList list= userPositionService.findAuthPositions(entity, 0, 1000);
		List<Map<String, Object>> list1s=list.getDataList();
		List<String> ara=new ArrayList<String>();
		List<String> ara2=new ArrayList<String>();
		//获取用户已勾选的所有集合
		for(Map<String, Object> ms:list1s){
			int type=Integer.valueOf(ms.get("TYPE").toString());
			if(type==1){
				String dmodel=ms.get("ORG_ID").toString()+"_"+ms.get("ID").toString();
				ara.add(dmodel);
			}else if(type==2){
				String dmode2=ms.get("ORG_ID").toString()+"_"+ms.get("ID").toString();
				ara2.add(dmode2);
			}
		}
		
		List<Map<String, Object>> newObject=new ArrayList<Map<String,Object>>();
		//构建选择标识
		for(Map<String, Object> ms:map){
			String key2=ms.get("ORG_ID").toString()+"_"+ms.get("ID").toString();
			//是否已勾选主岗
			if(ara.indexOf(key2)!=-1){
				ms.put("CHEACK", true);
			}else{
				ms.put("CHEACK", false);
			}
			//是否存在兼岗
			if(ara2.indexOf(key2)!=-1){
				ms.put("CHEACK2", true);
			}else{
				ms.put("CHEACK2", false);
			}
			
			//获取当前岗位的所有互斥岗位
			List<Map<String, Object>> mutex_list=jdbcService.findList("SELECT a.B_ID,b.SN,B.NAME FROM IM_MUTEX a left join im_position b on a.B_ID=b.ID WHERE a.A_ID="+ms.get("ID"));
			ms.put("MUTEX_LIST", mutex_list);
			String name=ms.remove("NAME").toString();
			String namePath=ms.get("NAME_PATH").toString();
			ms.put("NAME", namePath+""+name);
			newObject.add(ms);
		}
		pagdse.setCurrentPage(page);
		pagdse.setPageSize(limit);
		pagdse.setDataList(newObject);
		return pagdse;
	}
	
	
	/**
	 * 授权用户岗位信息
	 * @return
	 */
	@ApiOperation(value="授权用户岗位信息")
	@RequestMapping(value="authPosition", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode authPosition(@RequestBody List<BPMEnterAuthModel> entityList){
		try{
			List<UserPosition> ups=new ArrayList<UserPosition>();
			UserPosition up;
			//循环用户标识
			for (BPMEnterAuthModel entity : entityList) {
				//循环岗位信息
				//Auth_ids[岗位ID_组织ID格式传参]
				up=new UserPosition();
				up.setUserId(entity.getUserId());
				up.setType(entity.getType());
				up.setOrgId(Long.valueOf(entity.getAuth_ids().split("_")[0]));
				up.setPositionId(Long.valueOf(entity.getAuth_ids().split("_")[1]));
				ResultCode code=isMuite(up.getUserId(), up.getPositionId());
				//判断当前授权岗位存不存在互斥，如果存在则返回授权失败信息
				if(!code.getSuccess()){
					return code;
				}
				ups.add(up);
			}
			userService.authAndCancelPosition(ups, new ArrayList<UserPosition>());
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
	
	private ResultCode isMuite(@RequestParam Long userId,@RequestParam Long positionId){
		boolean flag=false;
		String sql="SELECT B_ID FROM IM_MUTEX WHERE A_ID="+positionId;
		List<Map<String, Long>> maps=jdbcService.findList(sql);
		if(maps!=null&&maps.size()>0){
			UserPosition entity2 = new UserPosition();
			entity2.setUserId(userId);
			PageList list= userPositionService.findAuthPositions(entity2, 0, 1000);
			List<Map<String, Object>> list1s=list.getDataList();
			List<String> ara=new ArrayList<String>();
			//获取用户已勾选的所有集合
			for(Map<String, Object> ms:list1s){
				String dmodel=ms.get("ID").toString();
				ara.add(dmodel);
			}
			for(int i=0;i<maps.size();i++){
				for(String id:ara){
					if(maps.get(i).get("B_ID").longValue()==Long.valueOf(id).longValue()){
						//存在用户已勾选的互斥岗位，进行后端返回提示消息
						flag=true;
						break;
					}
				}
			}
		}
		if(flag){
			String name="";
			for(int i=0;i<maps.size();i++){
				Position p=positionService.findById(Long.valueOf(maps.get(i).get("B_ID")));
				if(p!=null){
					name+="["+p.getName()+"],";
				}
			}
			if(name==null||name.length()==0){
				return new ResultCode(Constants.OPERATION_FAIL,"当前岗位与其他岗位存在互斥。");
			}else{
				name=name.substring(0, name.length()-1);
				return new ResultCode(Constants.OPERATION_FAIL,"当前岗位与"+name+"存在互斥。");
			}
		}
		return new ResultCode(Constants.OPERATION_SUCCESS);
	}
	
	/**
	 * 获取可申请的应用集合
	 * @return tree
	 */
	@ApiOperation(value="获取可申请的应用集合",code=1)
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "userId", value = "申请用户ID", required = true, paramType="path", dataType = "Long"),
	})
	@RequestMapping(value="getApps",method={RequestMethod.POST})
	@ResponseBody
	public List<App> getApps(@RequestParam Long userId){
		 List<App> appList=new ArrayList<App>();
		 try{
			String sql="select ID,SN,NAME FROM IM_APP WHERE ID NOT IN(SELECT APP_ID FROM IM_ACCOUNT WHERE USER_ID="+userId+" and COMPANY_SN='"+CurrentAccount.getCurrentAccount().getCompanySn()+"')";
			List<Map<String, Object>> list=jdbcService.findList(sql);
			if(list!=null&&list.size()>0){	
				for(Map<String, Object> map:list){
					App appModel=new App();
					appModel.setId(Long.valueOf(map.get("ID").toString()));
					appModel.setSn(map.get("SN").toString());
					appModel.setName(map.get("NAME").toString());
					appList.add(appModel);
				}
			}
		 }catch (Exception e) {
			e.printStackTrace();
		 }
		 return appList;
	}
	
	
	/**
	 * 获取当前用户的所有账号
	 * @return tree
	 */
	@ApiOperation(value="获取当前用户的所有账号",code=1)
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "userId", value = "用户ID", required = true, paramType="path", dataType = "Long"),
	})
	@RequestMapping(value="getAccount",method={RequestMethod.POST})
	@ResponseBody
	public List<Account> getAccount(@RequestParam Long userId){
		 List<Account> appList=new ArrayList<Account>();
		 try{
			String sql="select a.ID,a.LOGIN_NAME,a.APP_ID,a.STATUS,b.NAME AS APP_NAME,b.SN AS APP_SN FROM IM_ACCOUNT a left join IM_APP b on a.APP_ID=b.ID WHERE a.USER_ID="+userId+" and a.COMPANY_SN='"+CurrentAccount.getCurrentAccount().getCompanySn()+"'";
			List<Map<String, Object>> list=jdbcService.findList(sql);
			if(list!=null&&list.size()>0){	
				for(Map<String, Object> map:list){
					Account appModel=new Account();
					appModel.setId(Long.valueOf(map.get("ID").toString()));
					appModel.setLoginName(map.get("LOGIN_NAME").toString());
					appModel.setAppSn(map.get("APP_SN").toString());
					appModel.setAppName(map.get("APP_NAME").toString());
					appModel.setStatus(Integer.valueOf(map.get("STATUS").toString()));
					appList.add(appModel);
				}
			}
		 }catch (Exception e) {
			e.printStackTrace();
		 }
		 return appList;
	}
	
	/**
	 * 移除用户账号
	 * @return tree
	 */
	@ApiOperation(value="移除用户账号",code=1)
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "userId", value = "用户ID", required = true, paramType="path", dataType = "Long"),
		 @ApiImplicitParam(name = "acctId", value = "账号ID", required = true, paramType="path", dataType = "Long"),
	})
	@RequestMapping(value="cancelUserAccount",method={RequestMethod.POST})
	@ResponseBody
	public ResultCode cancelUserAccount(@RequestParam Long userId,@RequestParam Long acctId){
		 try{
			String sql="SELECT ID FROM IM_ACCOUNT WHERE USER_ID="+userId+" AND ID="+acctId+" AND  COMPANY_SN='"+CurrentAccount.getCurrentAccount().getCompanySn()+"')";
			List<Map<String, Object>> list=jdbcService.findList(sql);
			if(list!=null&&list.size()==1){	
				Long[] ids=new Long[1];
				ids[0]=Long.valueOf(list.get(0).get("ID").toString());
				accountService.removeByIds(ids);
				return new ResultCode(Constants.OPERATION_SUCCESS);
			}else{
				return new ResultCode(Constants.OPERATION_FAIL,"account exit acctId="+acctId+",userId="+userId);
			}
		 }catch (Exception e) {
			e.printStackTrace();
		 }
		 return new ResultCode(Constants.OPERATION_FAIL);
	}

	
	
	/**
	 * 申请开通应用权限-根据输入用户名密码
	 * @return tree
	 */
	@ApiOperation(value="申请开通应用权限-根据输入用户名密码",code=1)
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "appId", value = "申请应用ID", required = true, paramType="path", dataType = "Long"),
		 @ApiImplicitParam(name = "userId", value = "申请用户ID", required = true, paramType="path", dataType = "Long"),
		 @ApiImplicitParam(name = "loginName", value = "输入帐号名", required = true, paramType="path", dataType = "String"),
		 @ApiImplicitParam(name = "password", value = "输入密码", required = true, paramType="path", dataType = "String")
	})
	@RequestMapping(value="applyAppByInput",method={RequestMethod.POST})
	@ResponseBody
	public ResultCode applyAppByInput(@RequestParam Long appId,@RequestParam Long userId,@RequestParam String loginName,@RequestParam String password){
		Account ac=new Account();
		ac.setAppId(Long.valueOf(appId));
		ac.setLoginName(loginName);
		ac.setLoginPwd(password);
		ac.setUserId(Long.valueOf(userId));
		ac.setAcctType(Constants.ACCOUNT_OPEN_TYPE_BASIC);
		ac.setStatus(1);
		ac.setOptUser("sysBpmAdmin");
		ac.setOpenType(Constants.ACCOUNT_OPEN_TYPE_BASIC);
		ac=accountService.findByObject(ac);
		if(ac!=null){
			accountService.save(ac);
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}else{
			return new ResultCode(Constants.OPERATION_FAIL,"account exit appId="+appId+",userId="+userId+",loginName="+loginName);
		}
	}

	
	
	/**
	 * 验证开通账号
	 * @param appSn
	 */
	@ApiOperation(value="验证开通账号",code=1)
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "appId", value = "应用ID", required = true, paramType="path", dataType = "Long"),
		 @ApiImplicitParam(name = "loginName", value = "账号登录名", required = true, paramType="path", dataType = "String"),
	})
	@RequestMapping(value="cheackAccount",method={RequestMethod.POST})
	@ResponseBody
	public ResultCode cheackAccount(@RequestParam Long appId,@RequestParam String loginName){
		try{
			List list=jdbcService.findList("SELECT * FROM IM_ACCOUNT WHERE APP_ID="+appId+" and LOGIN_NAME='"+loginName+"' and COMPANY_SN='"+CurrentAccount.getCurrentAccount().getCompanySn()+"'");
			if(list!=null&&list.size()>0){
				return new ResultCode(Constants.OPERATION_EXIST,"account exit appId="+appId+",loginName="+loginName);
			}else{
				return new ResultCode(Constants.OPERATION_SUCCESS);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return new ResultCode(Constants.FORM_VALIDATOR_FAIL,"account exit appId="+appId+",loginName="+loginName);
	}
	
	
	
	/**
	 * 获取应用的账号策略
	 * @param appSn
	 * @return tree
	 */
	@ApiOperation(value="获取应用的账号策略",code=1)
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "appId", value = "申请应用ID", required = true, paramType="path", dataType = "Long"),
		 @ApiImplicitParam(name = "userId", value = "申请用户ID", required = true, paramType="path", dataType = "Long"),
	})
	@RequestMapping(value="getAppPolicy/{appId}/{userId}",method={RequestMethod.POST})
	@ResponseBody
	public List<Field> getAppPolicy(@PathVariable Long appId,@PathVariable Long userId){
		List<Field> fields=new ArrayList<Field>();
		try{
			//判断用户类型，并给出具体的输入字段
			Field conditionField=new Field();
			conditionField.setObjId(appId);
			fields=sysFieldService.findList(conditionField);
			if(fields!=null&&fields.size()>0){
				//设置初始化时
				String loginName=null;
				Map<String,Object> params=tldModel.getBasicTld();
				for (Field field : fields) {
					if(field.getDefaultValue()!=null){
						User u=userService.findById(userId);
						params.put("user",u);
						try {
							field.setDefaultValue(StringParse.parse(field.getDefaultValue(), params));
							if(field.getName().equals("LOGIN_NAME")){
								if(!StringUtils.isEmpty(field.getDefaultValue()))field.setDefaultValue(u.getSn());
								loginName=field.getDefaultValue();
							}
						} catch (Exception e) {
							log.error("open account set default value fail",e);
						}
					}
				}
				int i=1;
				while (true) {
					//查重，重复后追加1,2,3,4,5等第，依次类推
					List list=jdbcService.findList("SELECT * FROM IM_ACCOUNT WHERE APP_ID="+appId+" and LOGIN_NAME='"+loginName+"' and COMPANY_SN='"+CurrentAccount.getCurrentAccount().getCompanySn()+"'");
					if(list!=null&&list.size()>0){
						//如果是邮箱，则@前面追加1，如果是普通账号则最后面追加1
						if(loginName.indexOf("@")!=-1){
							loginName=loginName.split("@")[0]+i+"@"+loginName.split("@")[1];
						}else{
							loginName=loginName+i;
						}
						i++;
					}else{
						break;
					}
				}
				for (Field field : fields) {
					if(field.getDefaultValue()!=null){
						if(field.getName().equals("LOGIN_NAME")){
							field.setDefaultValue(loginName);
						}
					}
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return fields;
	}
}
