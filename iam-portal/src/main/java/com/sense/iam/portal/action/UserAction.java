package com.sense.iam.portal.action;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sense.core.security.UIM;
import com.sense.core.util.CurrentAccount;
import com.sense.core.util.PageList;
import com.sense.core.util.StringUtils;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.am.PwdPolicy;
import com.sense.iam.model.im.Account;
import com.sense.iam.model.im.AccountUser;
import com.sense.iam.model.im.Org;
import com.sense.iam.model.im.PwdLog;
import com.sense.iam.model.im.User;
import com.sense.iam.model.sys.Field;
import com.sense.iam.portal.res.model.AccountCheack;
import com.sense.iam.portal.util.Validate;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.AmPwdPolicyService;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.PwdLogService;
import com.sense.iam.service.SysFieldService;
import com.sense.iam.service.UserService;



/**
 * 
 * 个人中心
 * 
 * Description:
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Controller
@RequestMapping("user")
public class UserAction extends BaseAction {
	protected Log log = LogFactory.getLog(getClass());

	@Resource
	JdbcService jdbcService;
	
	@Resource
	UserService userService;
	
	@Resource
	AccountService accountService;
	
	@Resource
	SysFieldService sysFieldService;
	
	@Resource
	AmPwdPolicyService amPwdPolicyService;

	@RequestMapping("/main.html")
	public String appCenter(){
		CurrentAccount account=CurrentAccount.getCurrentAccount();
		List orgList=jdbcService.findList("select p.NAME from im_position p left join im_user_position o on o.position_id=p.id where o.user_id="+account.getUserId());
		JSONArray data=JSONArray.fromObject(orgList);
		String positionName="";
		for(int i=0;i<data.size();i++){
			if(i+1==data.size()){
				positionName+=data.getJSONObject(i).getString("NAME");
			}else{
				positionName+=data.getJSONObject(i).getString("NAME")+",";
			}
		}
		User u=userService.findById(account.getUserId());
		request.setAttribute("positionName", positionName);
		request.setAttribute("user", u);

		//判断用户类型，并给出具体的输入字段
		Field conditionField=new Field();
		conditionField.setObjId(u.getUserTypeId());
		List<Field> fields=sysFieldService.findList(conditionField);
		request.setAttribute("userextAttr", fields);
		request.setAttribute("clickTagNulber", request.getParameter("clickTagNulber")==null?0:request.getParameter("clickTagNulber"));

		return "user/main";
	}

	/**
	 * 强制密码修改
	 * @return
	 */
	@RequestMapping("/forceUpdatePwd.html")
	public String forceUpdatePwd(){
		CurrentAccount account=CurrentAccount.getCurrentAccount();
		User u=userService.findById(account.getUserId());
		request.setAttribute("user", u);
		return "user/forceUpdatePwd";
	}



	@RequestMapping("/saveUserInfo.action")
	@ResponseBody
	public ResultCode saveUserInfo(){
		Field conditionField=new Field();
		CurrentAccount account=CurrentAccount.getCurrentAccount();
		User ac=userService.findById(account.getUserId());
		conditionField.setObjId(ac.getUserTypeId());
		List<Field> fields=sysFieldService.findList(conditionField);
		if(fields!=null&&fields.size()>0){
			for(int i=0;i<fields.size();i++){
				//过滤不可输入的字段
				//if(fields.get(i).getIsPortalEdit()==2)continue;
				//动态匹配值
				String obj=request.getParameter(fields.get(i).getName());
				//基础字段
				if(obj!=null&&obj.length()>0){
					//log.info("基础字段："+fields.get(i).getName()+",值="+obj);
					//判断SN是否正确饿匹配
					if("SN".equals(fields.get(i).getName())&&!obj.equals(ac.getSn())){
						log.info("字段值变更1："+fields.get(i).getName()+",值="+obj);
						ac.setSn(obj);;
					}
					else if("NAME".equals(fields.get(i).getName())&&!obj.equals(ac.getName())){
						log.info("字段值变更2："+fields.get(i).getName()+",值="+obj);
						ac.setName(obj);;
					}
					else if("SEX".equals(fields.get(i).getName())&&!(Integer.valueOf(obj)==ac.getSex())){
						log.info("字段值变更3："+fields.get(i).getName()+",值="+obj);
						ac.setSex(Integer.valueOf(obj));;
					}
					else if("EMAIL".equals(fields.get(i).getName())&&!obj.equals(ac.getEmail())){
						log.info("字段值变更4："+fields.get(i).getName()+",值="+obj);
						ac.setEmail(obj);;
					}
					else if("TELEPHONE".equals(fields.get(i).getName())&&!obj.equals(ac.getTelephone())){
						log.info("字段值变更5："+fields.get(i).getName()+",值="+obj);
						ac.setTelephone(obj);;
					}
				}else{
					//扩展字段
					obj=request.getParameter("ext_"+fields.get(i).getName());
					if(obj==null){
						continue;
					}else if(ac.getExtraAttrs().containsKey(fields.get(i).getName())){
						ac.getExtraAttrs().remove(fields.get(i).getName());
						ac.getExtraAttrs().put(fields.get(i).getName(), obj);
						log.info("扩展字段值变更："+fields.get(i).getName()+",值="+obj);
					}else{
						ac.getExtraAttrs().put(fields.get(i).getName(), obj);
					}

				}
			}
		}
		return userService.edit(ac);
	}


	@RequestMapping("/pwdSecurityLoad.action")
	@ResponseBody
	public String pwdSecurityLoad(){
		JSONObject result=new JSONObject();
		CurrentAccount account=CurrentAccount.getCurrentAccount();
		List list = jdbcService.findList("select id,name from im_user_sp");
		List list2 = jdbcService.findList("select num,sp_id,value from im_user_spset where user_id="+account.getUserId());
		JSONArray data=JSONArray.fromObject(list);
		JSONArray data2=JSONArray.fromObject(list2);
		result.put("list", data);
		result.put("userlist", data2);
		return result.toString();
	}


	@RequestMapping("/pwdSecuritySave.action")
	@ResponseBody
	public ResultCode pwdSecuritySave(){
		String sp_num1=request.getParameter("sp_num1");
		String sp_num2=request.getParameter("sp_num2");
		String t_num1=request.getParameter("t_num1");
		String t_num2=request.getParameter("t_num2");
		CurrentAccount account=CurrentAccount.getCurrentAccount();
		log.info("sp_num1="+sp_num1);
		log.info("sp_num2="+sp_num2);
		log.info("t_num1="+t_num1);
		log.info("t_num2="+t_num2);
		log.info("id="+account.getId());
		try{
			List list = jdbcService.findList("SELECT num FROM im_user_spset where user_id="+account.getUserId());
			//是否存在记录
			if(list != null && list.size() > 0){
				//修改
				jdbcService.executeSql("update im_user_spset set sp_id="+sp_num1+",value='"+t_num1+"' where user_id="+account.getUserId()+" and num=1");
				jdbcService.executeSql("update im_user_spset set sp_id="+sp_num2+",value='"+t_num2+"' where user_id="+account.getUserId()+" and num=2");
			}else{
				//添加
				jdbcService.executeSql("insert into im_user_spset(id,num,sp_id,value,user_id) values ('"+account.getUserId()+"_1',1,"+sp_num1+",'"+t_num1+"',"+account.getUserId()+")");
				jdbcService.executeSql("insert into im_user_spset(id,num,sp_id,value,user_id) values ('"+account.getUserId()+"_2',2,"+sp_num2+",'"+t_num2+"',"+account.getUserId()+")");
			}
		}catch(Exception e){
			e.printStackTrace();
			return new ResultCode(Constants.LOGIN_STATUS_AUTH_ERROR);
		}
		return new ResultCode(Constants.LOGIN_STATUS_SUCCESS);
	}


	@RequestMapping("/getLogs.action")
	@ResponseBody
	public PageList getLogs(Integer page,Integer limit){
		//查询sys_log/sso_log
		CurrentAccount account=CurrentAccount.getCurrentAccount();
		User u=userService.findById(account.getUserId());
		String sql="select * from (select user_name,DATE_FORMAT(create_time,'%Y-%m%-%d% %H%:%i%:%s') as create_time ,clazz,method,remark,ip,'统一门户' as appname from sys_log where user_name !='admin' and clazz='com.sense.iam.auth.controller.AuthController' union all select o.user_name,DATE_FORMAT(o.create_time,'%Y-%m%-%d% %H%:%i%:%s') as create_time,'com.sense.iam.auth.controller.SSOController' as 'clazz','sso' as 'method','单点登录' as remark,'' as ip,ap.name as appname from sso_log o LEFT JOIN im_account a on a.id=o.ACCOUNT_ID LEFT JOIN im_app ap on ap.id=a.app_id) a";
		sql+=" where user_name in(select login_name from im_account where user_id="+u.getId()+")";
		AccountUser model=new AccountUser();
		model.setSort("[{\"property\":\"create_time\",\"direction\":\"DESC\"}]");
		PageList pageList=jdbcService.findPage(sql, model, page, limit);
		return pageList;
	}


}
