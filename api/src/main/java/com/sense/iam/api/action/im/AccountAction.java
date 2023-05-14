package com.sense.iam.api.action.im;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiSort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sense.core.freemark.StringParse;
import com.sense.core.util.ArrayUtils;
import com.sense.core.util.CurrentAccount;
import com.sense.core.util.ExcelUtils;
import com.sense.core.util.PageList;
import com.sense.core.util.StringUtils;
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.api.model.AccountEditPwd;
import com.sense.iam.api.model.im.AccountAppFuncReq;
import com.sense.iam.api.model.im.AccountReq;
import com.sense.iam.api.model.im.AccountUserModel;
import com.sense.iam.api.model.im.AccountUserReq;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.im.Account;
import com.sense.iam.model.im.AccountUser;
import com.sense.iam.model.im.App;
import com.sense.iam.model.im.User;
import com.sense.iam.model.im.UserRelation;
import com.sense.iam.model.sys.Field;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.AppService;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.SysFieldService;
import com.sense.iam.service.UserService;
import com.sense.iam.tld.TldModel;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;



@Api(tags = "帐号管理")
@Controller
@RestController
@RequestMapping("im/account")
@ApiSort(value = 8)
public class AccountAction extends  AbstractAction<Account,Long>{
	
	@Resource
	private AccountService accountService;
	
	@Resource
	private SysFieldService sysFieldService;
	
	@Resource
	private UserService userService;
	
	@Resource
	private TldModel tldModel;

	@Resource
	private AppService appService;
	
	@Resource
	private JdbcService jdbcService;
	
	
	@ApiOperation(value="重新推送")
    @RequestMapping(value="restSync",method = RequestMethod.POST)
	@ResponseBody
    public ResultCode restSync(@RequestBody @ApiParam(name="唯一标识集合",value="多数据采用英文逗号分割",required=true)List<String> ids) {
		try{
			if(ids!=null&&ids.size()>0){
				Params params=new Params();
				params.setIds(ids);
				return super.resetSyncs(params);
			}else{
				return new ResultCode(Constants.OPERATION_FAIL);
			}
		}catch(Exception e){
			return new ResultCode(Constants.OPERATION_FAIL);
		}
    }
	
	
	
	@ApiOperation(value="获取账号扩展字段")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "appId", value = "应用ID", required = true, paramType="path", dataType = "Long"),
		 @ApiImplicitParam(name = "userId", value = "用户ID", required = true, paramType="path", dataType = "Long"),
	})
	@RequestMapping(value="field/{appId}/{userId}", method=RequestMethod.GET)
	@ResponseBody
	public List<Field> geField(@PathVariable Long appId,@PathVariable Long userId) {
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
				User u=userService.findById(userId);
				for (Field field : fields) {
					if(field.getDefaultValue()!=null){
						params.put("user",u);
						try {
							field.setDefaultValue(StringParse.parse(field.getDefaultValue(), params));
							if(field.getName().equals("LOGIN_NAME")){
								if(StringUtils.isEmpty(field.getDefaultValue())){
									field.setDefaultValue(u.getSn());
								}
								loginName=field.getDefaultValue();
							}
						} catch (Exception e) {
							log.error("open account set default value fail",e);
						}
					}
				}
				int i=1;
				String tempLoginName=loginName;
				while (true) {
					//查重，重复后追加1,2,3,4,5等第，依次类推
					List list=jdbcService.findList("SELECT * FROM IM_ACCOUNT WHERE APP_ID="+appId+" and LOGIN_NAME='"+tempLoginName+"'");
					if(list!=null&&list.size()>0){
						//如果是邮箱，则@前面追加1，如果是普通账号则最后面追加1
						if(loginName.indexOf("@")!=-1){
							tempLoginName=loginName.split("@")[0]+""+i+loginName.split("@")[1];
						}else{
							tempLoginName=loginName+""+i;
						}
						i++;
					}else{
						loginName=tempLoginName;
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
	
	
	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findById/{id}", method=RequestMethod.GET)
	@ResponseBody
	public Account findById(@PathVariable Long id) {
		try{
			return super.findById(id);
		}catch(Exception e){
			log.error("save error:",e);
			return new Account();
		}
	}
	
	
	
	@ApiOperation(value="新增帐号")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody AccountReq entity) {
		try{
			return super.save(entity.getAccount());
		}catch(Exception e){
			log.error("save error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
	
	@ApiOperation(value="更新帐号")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	public ResultCode edit(@RequestBody AccountReq entity) {
		try{
			return super.edit(entity.getAccount());
		}catch(Exception e){
			log.error("edit error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
	
	/**
	 * 分页查询帐号列表
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	@ApiOperation(value="分页查询帐号列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findListByUser", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 4)
	protected PageList<Account> findList(@RequestBody AccountReq entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			Account p=entity.getAccount();
			p.setIsLikeQuery(true);
			if(p.getAppId()==null){
				p.setAppId(-1L);
			}
			if(entity.getUserId()!=null){
				p.setAppId(null);
			}
			return getBaseService().findPage(p,page,limit);
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<Account>();
		}
	}
	
	
	@ApiOperation(value="移除帐号")
    @RequestMapping(value="remove",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 3)
    public ResultCode remove(@RequestBody @ApiParam(name="唯一标识集合",value="多数据采用英文逗号分割",required=true)List<String> ids) {
		try{
			Params params=new Params();
			params.setIds(ids);
			return super.remove(params);
		}catch(Exception e){
			return new ResultCode(Constants.OPERATION_FAIL);
		}
    }
	
	
	@ApiOperation(value="启用帐号")
    @RequestMapping(value="enabled",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 5)
    public ResultCode enabled(@RequestBody @ApiParam(name="唯一标识集合",value="多数据采用英文逗号分割",required=true)Params params) {
		try{
			accountService.enabled(ArrayUtils.stringArrayToLongArray(params.getIds().toArray(new String[params.getIds().size()])));
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			return new ResultCode(Constants.OPERATION_FAIL);
		}
    }
	
	@ApiOperation(value="禁用帐号")
    @RequestMapping(value="disabled",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 6)
    public ResultCode disabled(@RequestBody @ApiParam(name="唯一标识集合",value="多数据采用英文逗号分割",required=true)Params params) {
		try{
			accountService.disabled(ArrayUtils.stringArrayToLongArray(params.getIds().toArray(new String[params.getIds().size()])));
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			return new ResultCode(Constants.OPERATION_FAIL);
		}
    }
	
	@ApiOperation(value="强制更新密码")
    @RequestMapping(value="updatePwd",method = RequestMethod.POST)
	@ResponseBody
	public ResultCode updatePwd(@RequestBody AccountEditPwd entity){
		try{
			accountService.updatePwd(entity.getIds(),entity.getPwd());
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			e.printStackTrace();
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	

	@ApiOperation(value="获取帐号已关联用户")
    @RequestMapping(value="findAccountUserList",method = RequestMethod.POST)
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认10页",example="20")
	})
	@ResponseBody
	public PageList findAccountUserList(@RequestBody AccountUserModel model,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="10") Integer limit){
		AccountUser au=new AccountUser();
		au.setAcctId(model.getAcctId());
		au.setUserSn(StringEscapeUtils.escapeHtml4(model.getUserSn()));
		au.setIsLikeQuery(true);
		PageList pageList=accountService.findAccountUserList(au,page,limit);
		return pageList;
	}
	
	
	/**
	 * 分页查询关联用户列表
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	@ApiOperation(value="分页查询关联用户列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认10页",example="10")
	})
	@RequestMapping(value="findAccountUserPage", method=RequestMethod.POST)
	@ResponseBody
	protected PageList findAccountUserPage(@RequestBody UserRelation entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="10") Integer limit){
		try{
			if(entity.ok().getSuccess()){
				entity.setIsLikeQuery(true);
				return userService.findListByRelationUser(entity, page, limit);
			}else{
				PageList list=new PageList<>();
				return list;
			}
		}catch(Exception e){
			log.error("findAccountUserPage error:",e);
			return new PageList<>();
		}
	}
	
	@ApiOperation(value="帐号关联用户-累加")
    @RequestMapping(value="saveAccountUserList",method = RequestMethod.POST)
	@ResponseBody
	public ResultCode saveAccountUserList(@RequestBody AccountUserReq accountUserReq){
		try{
			accountService.saveAccountUsers(accountUserReq.getAcctIds(),accountUserReq.getUserIds());
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			e.printStackTrace();
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
	
	@ApiOperation(value="取消帐号关联用户")
    @RequestMapping(value="removeAccountUserList",method = RequestMethod.POST)
	@ResponseBody
	public ResultCode removeAccountUserList(@RequestBody AccountUserReq accountUserReq){
		try{
			accountService.removeAccountUsers(accountUserReq.getAcctIds(),accountUserReq.getUserIds());
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			e.printStackTrace();
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
	
	@ApiOperation(value="保存帐号权限")
    @RequestMapping(value="saveAccountFuncs",method = RequestMethod.POST)
	@ResponseBody
	public Object saveAccountFuncs(@RequestBody AccountAppFuncReq accountAppFuncReq){
		try{
			accountService.saveAccountAppFunc(accountAppFuncReq.toAccount());
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			e.printStackTrace();
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
	@SuppressWarnings("rawtypes")
	@ApiOperation(value="获取帐号权限")
    @RequestMapping(value="findAccountFuncs/{acctId}",method = RequestMethod.POST)
	@ResponseBody
	public Object findAccountFuncs(@PathVariable Long acctId){
		try{
			return accountService.findAppFunc(acctId);
		}catch(Exception e){
			e.printStackTrace();
		}
		return new ArrayList();
	}

	@ApiOperation(value="帐号导入")
    @RequestMapping(value="upLoad/{appId}",method = RequestMethod.POST)
	@ResponseBody
	public ResultCode upLoad(@PathVariable Long appId,@ApiParam(name="file",value="导入内容",required=false) @RequestParam(required=false,name="file",value="file") MultipartFile file){
		try {
			Field entity=new Field();
			entity.setObjId(appId);
			List<Field> appFields=sysFieldService.findList(entity);
			Map<String,String> colM=new HashMap<String,String>();
			for (Field field : appFields) {
				colM.put(field.getRemark(),field.getName());
			}
			List<Map<String,String>> acctList=ExcelUtils.parseToMap(file.getInputStream(),appService.findById(appId).getSn(),colM);
			if(acctList==null){
				return new ResultCode(Constants.OPERATION_UNKNOWN,"导入应用有误");
			}
			return accountService.importAcct(acctList, appId);
		} catch (Exception e) {
			log.error("import acct error",e);
			return new ResultCode(Constants.OPERATION_FAIL,"导入失败");
		}
	}
	

	@ApiOperation(value = "账号导出")
	@RequestMapping(value = "export", method = RequestMethod.POST)
	@ResponseBody
	public void export(@RequestBody AccountReq entity){
		try {
			entity.getAccount().setIsLikeQuery(true);
			super.exportXlsx("account", accountService.findPage(entity.getAccount(),1,Constants.EXPORT_MAX_NUM).getDataList(), Arrays.asList(new ExcelUtils.ExcelModel[]{
				new ExcelUtils.ExcelModel("登录账号","loginName",5000),
				new ExcelUtils.ExcelModel("登录密码","",5000),
				new ExcelUtils.ExcelModel("归属用户","userSn",3000),
				new ExcelUtils.ExcelModel("账号类型","acctType"),
				new ExcelUtils.ExcelModel("状态","status")
			}));
		} catch (Exception e) {
			log.error("import acct error",e);
		}
	}
	/**
	 * 根据组织ID和应用ID批量更新账号
	 * @return
	 */
	@RequestMapping(value="updateAccts")
	@ResponseBody
	public Object updateAccts(Long orgId,Long appId,String triggerRemark){
		accountService.updateAccts(appId,orgId);
		return new ResultCode(Constants.OPERATION_SUCCESS);
	}
}
