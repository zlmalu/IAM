package com.sense.iam.portal.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import com.sense.core.util.*;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.sense.iam.cache.ImageCache;
import com.sense.iam.cache.LoginPolicyCache;
import com.sense.iam.cache.PasswordPolicyCache;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.im.Image;
import com.sense.iam.model.im.PwdLog;
import com.sense.iam.model.im.User;
import com.sense.iam.portal.HomeDataUtil;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.AppService;
import com.sense.iam.service.ImageService;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.PwdLogService;
import com.sense.iam.service.UserService;



/**
 *
 * 单点登录的操作类，提供对外的单点登录服务
 *
 * Description:
 *
 * @author w_jfwen
 *
 *         Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Controller
public class MainAction extends BaseAction {

	protected Log log = LogFactory.getLog(getClass());

	@Resource
	AppService appService;

	@Resource
	JdbcService jdbcService;

	@Resource
	UserService userService;

	@Resource
	AccountService accountService;

	@Resource
	ImageCache imageCache;


	public final static String IMAGE="image";

	@Resource
	private ImageService imageService;

	@Resource
	private LoginPolicyCache loginPolicyCache;

	@Resource
	private PwdLogService pwdLogService;



	@RequestMapping("/main.html")
    public String main(){
		int valistatus = 0;
		CurrentAccount account=CurrentAccount.getCurrentAccount();
		List<Map<String, Object>> orgList=jdbcService.findList("select o.ID,o.NAME from im_org_user ou left join im_org o on o.id=ou.ORG_ID where ou.USER_ID=?",account.getUserId());
		String orgName="";
		Long orgId=null;
		for(int i=0;i<orgList.size();i++){
			if(i+1==orgList.size()){
				orgName+=orgList.get(i).get("NAME").toString();
			}else{
				orgName+=orgList.get(i).get("NAME").toString()+",";
			}
			orgId=Long.valueOf(orgList.get(i).get("ID").toString());
		}
		//判断当前用户有没有安装阔浏览器脚本,存在记录则安装，不存在为未安装
		List initSSO=jdbcService.findList("SELECT * FROM AM_SSO_CONFIG WHERE USER_ID=?",account.getUserId());
		if(initSSO!=null&&initSSO.size()>0){
			request.setAttribute("initsso", 1);
		}else{
			request.setAttribute("initsso", 0);
		}
		request.setAttribute("token", GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request));
		User u=userService.findById(account.getUserId());
		request.setAttribute("orgName", orgName);
		request.setAttribute("username", account.getName());
		request.setAttribute("usersn", account.getLoginName());
		loadPwdPolic(valistatus,u.getUserTypeId());
		return "main/main";
    }

	/**
	 * 加载过期策略
	 * @param valistatus
	 */
	public void loadPwdPolic(int valistatus,Long userTypeId){
		int day=0;
		//判断首次是否强制修改密码
		if(loginPolicyCache.FIRST_LOGIN_FORCE_UP()){
			//查询密码修改日志，当前用户密码修改的记录；
			PwdLog entity=new PwdLog();
			entity.setAcctId(CurrentAccount.getCurrentAccount().getId());
			entity.setSort("[{\"property\":\"CREATE_TIME\",\"direction\":\"DESC\"}]");
			List<PwdLog> list=pwdLogService.findList(entity);
			if(list!=null && list.size()>0){
			    //获取最后一条记录密码修改时间
			    long pwdlogtime=list.get(0).getCreateTime().getTime();

			    //获取当前时间
			    long currtime=System.currentTimeMillis();
			    //密码有效天数
			    int pwdvalidday=PasswordPolicyCache.PWD_VALID_DAY(userTypeId);
			    log.info("pwdvalidday:"+pwdvalidday);

			    if(pwdvalidday==0){
			        pwdvalidday=180;
			    }
			    //获取密码策略过期天数
			    int pwdExpiredRemindDay=PasswordPolicyCache.PWD_EXPIRED_REMIND_DAY(userTypeId);
			    log.info("pwdExpiredRemindDay:"+pwdExpiredRemindDay);
							   // System.out.println("pwdExpiredRemindDay="+pwdExpiredRemindDay);
			    if(pwdExpiredRemindDay==0){
			        pwdExpiredRemindDay=10;
			    }
			    //天数转换为时间毫秒
			    long pwdvaliddaytime= Long.valueOf(pwdvalidday) * (24 * 60 * 60 * 1000);
							   // System.out.println("pwdvaliddaytime="+pwdvaliddaytime);
			    //获取密码策略过期天数毫秒
	            long pwdExpiredRemindDaytime=Long.valueOf(pwdExpiredRemindDay) * (24 * 60 * 60 * 1000);
					            //System.out.println("pwdExpiredRemindDaytime="+pwdExpiredRemindDaytime);
	            //判断有效时间
	            if(currtime-pwdlogtime>pwdvaliddaytime){
	                //过期强制修改
	                valistatus=1;
	            }else if((currtime-pwdlogtime>(pwdvaliddaytime-pwdExpiredRemindDaytime)) && (currtime-pwdlogtime<=pwdvaliddaytime)){
	                //需要提醒
	                valistatus=2;
	                long extime=pwdvaliddaytime-(currtime-pwdlogtime);
	                //获取提醒天数
	                day=(int) (extime / (24 *60 * 60 * 1000));
	                if(day<1){
	                    day=0;
	                }

	            }
			}else{
				if(loginPolicyCache.FIRST_LOGIN_FORCE_UP()){
					valistatus = 1;
				}
			    //过期强制修改
			    //valistatus=1,强制修改  =2提醒   0正常
			}
		}
		request.setAttribute("valistatus", valistatus);
		request.setAttribute("expday", day);
	}



	@RequestMapping("/init.action")
	@ResponseBody
    public String init(){
		CurrentAccount account=CurrentAccount.getCurrentAccount();
		JSONObject apps=HomeDataUtil.getAppsByUserId(jdbcService, account);
        return apps.toString();
    }


	@RequestMapping("/accountEntrusted/userAccountInfo.html")
    public String userAccountInfo(){
		 return "accountEntrusted/userAccountInfo";
	}
	@RequestMapping("/accountEntrusted/addWindowUserAccountInfo.html")
    public String accountEntrustedAddWindowUserAccountInfo(){
		 return "accountEntrusted/addWindowUserAccountInfo";
	}


	@RequestMapping("/accountEntrusted/toOrgUserInfo.html")
    public String toOrgUserInfo(){
		 return "accountEntrusted/toOrgUserInfo";
	}


	@RequestMapping("/accountEntrusted/appList.action")
	@ResponseBody
    public Object appList(){
		CurrentAccount account=CurrentAccount.getCurrentAccount();
		List<Map<String, Object>> maps=jdbcService.findList("SELECT a.ID,b.NAME,a.LOGIN_NAME FROM IM_ACCOUNT a left join IM_APP b on a.APP_ID=b.ID where a.USER_ID=? and b.IS_VIEW=? and b.SN!=? and a.STATUS=?",account.getUserId(),1,"APP001",1);
		return maps;
	}

	private static java.text.SimpleDateFormat dateHms=new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@RequestMapping("/accountEntrusted/saveAccountUser.action")
	@ResponseBody
    public ResultCode saveAccountUser(Long accountId,String userIds,String startTime,String endTime){
		CurrentAccount account=CurrentAccount.getCurrentAccount();
		try{
			log.info("accountId:"+accountId);
			log.info("userIds:"+userIds);
			log.info("startTime:"+startTime);
			log.info("endTime:"+endTime);
			Date startTime1=null;
			if(startTime!=null&&startTime.trim().length()>0){
				try{
					startTime1=dateHms.parse(startTime);
				}catch(Exception e){
					startTime1=null;
				}
			}
			Date entTime1=null;
			if(endTime!=null&&endTime.trim().length()>0){
				try{
					entTime1=dateHms.parse(endTime);
				}catch(Exception e){
					entTime1=null;
				}
			}
			if(accountId!=null&&userIds!=null&&userIds.trim().length()>0){
				List<Long> acclist=new ArrayList<Long>();
				List<Long> userIdsArray=new ArrayList<Long>();
				acclist.add(accountId);
				String[] array=userIds.split("_");
				for(String userId:array){
					userIdsArray.add(Long.valueOf(userId));
				}
				accountService.saveAccountUsersValidate(acclist, userIdsArray, startTime1, entTime1);

				return new ResultCode(Constants.OPERATION_SUCCESS);
			}else{
				return new ResultCode(Constants.OPERATION_FAIL,"提交参数格式不正确或者参数缺失");
			}
		}catch(Exception e){
			return new ResultCode(Constants.OPERATION_FAIL,e.getMessage());
		}
	}








}
