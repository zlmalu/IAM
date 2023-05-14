package com.sense.iam.portal.action;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;

import com.sense.iam.config.RedisCache;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.impl.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.sense.core.security.UIM;
import com.sense.core.util.CurrentAccount;
import com.sense.core.util.GatewayHttpUtil;
import com.sense.core.util.StringUtils;
import com.sense.iam.cache.CompanyCache;
import com.sense.iam.cam.Constants;
import com.sense.iam.model.am.Email;
import com.sense.iam.model.am.Sms;
import com.sense.iam.model.im.Account;
import com.sense.iam.model.im.App;
import com.sense.iam.model.im.PwdLog;
import com.sense.iam.model.im.User;
import com.sense.iam.portal.util.DESUtil;
import com.sense.iam.portal.util.SMSUtil;
import com.sense.iam.portal.util.SendMail;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.AmPwdPolicyService;
import com.sense.iam.service.AppService;
import com.sense.iam.service.EmailService;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.PwdLogService;
import com.sense.iam.service.SmsService;
import com.sense.iam.service.UserService;


/**
 *
 * 密码找回
 *
 * Description:
 *
 * @author w_jfwen
 *
 *         Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Controller
public class ResetPwdAction extends BaseAction {
	protected Log log = LogFactory.getLog(getClass());
	@Resource
	PwdLogService pwdLogService;
	@Resource
	JdbcService jdbcService;

	@Resource
	SmsService smsService;
	@Resource
	AppService appService;

	@Resource
	UserService userService;

	@Resource
	AccountService accountService;
	@Resource
	private CompanyCache companyCache;
	@Resource
	EmailService emailService;

	@Resource
	AmPwdPolicyService amPwdPolicyService;

	@Resource
	private RedisCache redisCache;

	@RequestMapping("/resetPassword.html")
	public String resetPassword(){
		String sessionId = GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request);
		//0代表进行密码找回
		redisCache.setCacheObject(Constants.PWD_RESET_SETTING_KEY + ":" + sessionId,"0",30 * 60, TimeUnit.SECONDS);
		return "resetPassword/view";
	}

	/**
	 * 第一步验证工号
	 * @return
	 */
	@RequestMapping("/cheackSn.action")
	@ResponseBody
	public String cheackSn(){
		String sn=request.getParameter("sn");
		log.info("sn="+sn);
		JSONObject result=new JSONObject();
		try{
			List list = jdbcService.findList("select * from (select a.user_type_id,a.company_sn,a.sn,a.id,a.email,a.telephone,b.num,ac.id as accountid from im_user a LEFT JOIN im_user_spset b on a.id=b.user_id left join im_account ac on ac.user_id=a.id  where  a.status=1 and ac.app_id in (select id from im_app where sn=?)) a where sn=? and company_sn=?","APP001",StringEscapeUtils.escapeSql(sn),companyCache.getCompanySn(GatewayHttpUtil.getKey("RemoteHost", request)));
			//是否存在记录
			JSONArray data=JSONArray.fromObject(list);
			if(data != null && data.size() > 0){
				//判断APP001的账号是否存在
				if(!data.getJSONObject(0).containsKey("accountid")){
					result.put("code", -100);
				}else{
					if(data.getJSONObject(0).containsKey("email") && !"".equals(data.getJSONObject(0).getString("email"))){
						result.put("email", data.getJSONObject(0).getString("email"));
						result.put("isemail", 1);
					}else{
						result.put("isemail", 0);
					}
					if(data.getJSONObject(0).containsKey("telephone") && !"".equals(data.getJSONObject(0).getString("telephone"))){
						result.put("telephone", data.getJSONObject(0).getString("telephone"));
						result.put("istelephone", 1);
					}else{
						result.put("istelephone", 0);
					}
					if(data.getJSONObject(0).containsKey("num")&&!"".equals(data.getJSONObject(0).getString("num"))){
						List listset = jdbcService.findList("SELECT b.id,b.name,a.num FROM im_user_spset a left join im_user_sp b on a.SP_ID=b.id where user_id=?",data.getJSONObject(0).getLong("id"));
						JSONArray dataset=JSONArray.fromObject(listset);
						if(dataset.size()==0){
							result.put("pwdset", 0);
						}
						else{
							result.put("dataset", dataset);
							result.put("pwdset", 1);
						}
					}else{
						result.put("pwdset", 0);
					}
					if(data.getJSONObject(0).containsKey("status")){
						result.put("status", data.getJSONObject(0).getString("status"));
					}
					result.put("code", 0);
					result.put("userId", data.getJSONObject(0).getLong("id"));
					result.put("userTypeId", data.getJSONObject(0).getLong("user_type_id"));
					String sessionId = GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request);
					//存储redis 找回密码会话与用户ID绑定
					redisCache.setCacheObject(Constants.PWD_RESET_SETTING_KEY+":"+sessionId,data.getJSONObject(0).getLong("id")+"",30*60, TimeUnit.SECONDS);
				}
			}else{
				result.put("code", -1);
			}
		}catch(Exception e){
			e.printStackTrace();
			result.put("code", -5);
		}
		return result.toString();
	}

	/**
	 * 第二步验证密保
	 * @return
	 */
	@RequestMapping("/resetpwdset.action")
	@ResponseBody
	public String resetpwdset(){
		JSONObject result=new JSONObject();
		String sessionId = GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request);
		//存储redis记录会话从1开始  1代表开填写工号第一步已完成
		String userId=redisCache.getCacheObject(Constants.PWD_RESET_SETTING_KEY + ":" + sessionId);
		log.info("resetpwdset stringRedisTemplate userId ="+userId);
		if(StringUtils.isEmpty(userId)){
			result.put("code", 403);
			result.put("msg", "暂无权限");
			return result.toString();
		}else if("0".equals(userId)){
			result.put("code", 403);
			result.put("msg", "暂无权限");
			return result.toString();
		}else{
			String t_num1=request.getParameter("t_num1");
			String t_num2=request.getParameter("t_num2");
			log.info("userId="+userId);
			log.debug("t_num1="+t_num1);
			log.debug("t_num2="+t_num2);
			int flag=0;
			try{
				List<?> list = jdbcService.findList("select value,num from im_user_spset where user_id=? order by num asc",userId);
				//是否存在记录
				JSONArray data=JSONArray.fromObject(list);
				if(data != null && data.size() > 0){
					for(int i=0;i<data.size();i++){
						if(data.getJSONObject(i).getInt("num") == 1){
							if(t_num1.equals(data.getJSONObject(i).getString("value"))){
								flag=1;
							}
						}
						else if(data.getJSONObject(i).getInt("num") == 2){
							if(t_num2.equals(data.getJSONObject(i).getString("value"))){
								if(flag==1){
									flag=3;
								}else{
									flag=2;
								}
							}
						}
					}
					if(flag==1){
						result.put("code", -1);
						result.put("msg", "密保验证失败");
					}else if(flag==2){
						result.put("code", -2);
						result.put("msg", "密保验证失败");
					}else if(flag==3){
						result.put("code", 0);
						result.put("msg", "验证成功");
					}else{
						result.put("code", -3);
						result.put("msg", "密保验证失败");
					}
				}else{
					result.put("code", -5);
					result.put("msg", "不存在密保设置");
				}
			}catch(Exception e){
				e.printStackTrace();
				result.put("code", -10);
				result.put("msg", "系统异常");
			}
		}
		return result.toString();
	}

	/**
	 * 第三步-设置密码
	 * @return
	 */
	@RequestMapping("/setpwd.action")
	@ResponseBody
	public String setpwd(){
		JSONObject result=new JSONObject();
		String sessionId = GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request);
		//存储redis记录会话从1开始  1代表开填写工号第一步已完成
		String userId = redisCache.getCacheObject(Constants.PWD_RESET_SETTING_KEY + ":" + sessionId);
		log.info("setpwd stringRedisTemplate userId ="+userId);
		String newPwd=request.getParameter("newPwd");
		if(StringUtils.isEmpty(userId) || "0".equals(userId)){
			userId=request.getParameter("userId");
			log.info("setpwd getParameter userId ="+userId);
		}
		if(StringUtils.isEmpty(userId) || "0".equals(userId)){
			result.put("code", 403);
			result.put("msg", "暂无权限");
			return result.toString();
		}else{
			String cofnewPwd=request.getParameter("cofnewPwd");
			log.info("userId="+userId);
			log.debug("newPwd="+newPwd);
			log.debug("cofnewPwd="+cofnewPwd);
			try{
				CurrentAccount account=CurrentAccount.getCurrentAccount();
				Account entity=new Account();
				entity.setUserId(Long.valueOf(userId));
				List<Account> list=accountService.findList(entity);
				if(list!=null && list.size()>0){
					for(int i=0;i<list.size();i++){
						//修改门户密码,并且启用账号
						if("APP001".equals(list.get(i).getAppSn())){
							Long[] ids=new Long[1];
							ids[0]=list.get(i).getId();
							account.setLoginName(list.get(i).getLoginName());
							account.setId(list.get(i).getId());
							CurrentAccount.setCurrentAccount(account);
							Account ac=accountService.findById(list.get(i).getId());
							if(ac!=null&&ac.getStatus()!=1){
								ac.setStatus(1);
								accountService.enabled(ids);
							}
							accountService.updatePwd(ids, cofnewPwd);
							log.info("update pwd ok set pwdlog.。。");
							PwdLog entity2=new PwdLog();
							entity2.setAcctId(account.getId());
							entity2.setCreateTime(new Date());
							entity2.setOperate(account.getLoginName());
							entity2.setName(UIM.encode(cofnewPwd));
							pwdLogService.save(entity2);
							log.info("add pwdlog ok");
						}
					}
				}
				result.put("code", 0);
				//重置完成清除redis记录
				redisCache.deleteObject(Constants.PWD_RESET_SETTING_KEY + ":" + sessionId);
			}catch(Exception e){
				e.printStackTrace();
				result.put("code", -10);
				result.put("msg", "系统异常");
			}
		}
		return result.toString();
	}



	/**
	 * 获取验证码
	 * @return
	 */
	@RequestMapping("/sendvCode.action")
	@ResponseBody
	public String sendvCode(){
		JSONObject result=new JSONObject();
		String sessionId = GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request);
		//存储redis记录会话从1开始  1代表开填写工号第一步已完成
		String userId = redisCache.getCacheObject(Constants.PWD_RESET_SETTING_KEY+":"+sessionId);
		log.info("sendvCode stringRedisTemplate userId ="+userId);
		String newPwd=request.getParameter("newPwd");
		if(StringUtils.isEmpty(userId)){
			result.put("code", 403);
			result.put("msg", "暂无权限");
			return result.toString();
		}else if("0".equals(userId)){
			result.put("code", 403);
			result.put("msg", "暂无权限");
			return result.toString();
		}else{
			String mobile=request.getParameter("mobile");
			log.info("userId="+userId);
			log.info("mobile="+mobile);
			try{
				CurrentAccount.setCurrentAccount(new CurrentAccount(companyCache.getCompanySn(GatewayHttpUtil.getKey("RemoteHost", request))));
				String verifyCode = String.valueOf(new Random().nextInt(899999) + 100000);
				//获取统一认证门户应用ID
				App app = new App();
				app.setIsLikeQuery(false);
				app.setSn("APP001");
				Long appId =appService.findByObject(app).getId();

				//获取账号ID
				Account account = new Account();
				account.setUserId(Long.valueOf(userId));
				account.setAppId(appId);
				account.setStatus(1);
				Long accountId=accountService.findByObject(account).getId();

				//保存短信信息
				Sms sms = new Sms();
				sms.setAccountId(accountId);
				sms.setMobile(mobile);
				sms.setValidateCode(verifyCode);
				sms.setStatus(1);
				sms.setCreateTime(new Date());
				sms.setType(2);
				sms.setRemark("密码找回，请求唯一标识："+sessionId);
				SendSmsResponse response=SMSUtil.sendIdentifyingCode1(mobile, verifyCode);
				if(response.getCode().equals("OK")){
					smsService.save(sms);
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					try {
						ObjectOutputStream oos = new ObjectOutputStream(bos);
						oos.writeObject(sms);
						oos.close();
						String value = new String(Base64.encode(bos.toByteArray()));
						//存放在redis中，过期5分钟
						redisCache.setCacheObject(Constants.SMS_SETTING_KEY+":"+sessionId,value,5 * 60, TimeUnit.SECONDS);
					} catch (IOException e) {
						e.printStackTrace();
					}
					result.put("code", 0);
				}else{
					result.put("code", -105);
					result.put("msg", "短信发送失败,原因:"+response.getMessage());
				}
				CurrentAccount.setCurrentAccount(null);

			}catch(Exception e){
				e.printStackTrace();
				result.put("code", -10);
				result.put("msg", "系统异常");
			}
		}
		return result.toString();
	}


	/**
	 * 邮箱验证
	 * @return
	 */
	@RequestMapping("/sendEmail.action")
	@ResponseBody
	public String sendEmail(){
		JSONObject result=new JSONObject();
		String sessionId = GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request);
		//存储redis记录会话从1开始  1代表开填写工号第一步已完成
		String userId=redisCache.getCacheObject(Constants.PWD_RESET_SETTING_KEY+":"+sessionId);
		log.info("sendEmail stringRedisTemplate userId ="+userId);
		String newPwd=request.getParameter("newPwd");
		if(StringUtils.isEmpty(userId)){
			result.put("code", 403);
			result.put("msg", "暂无权限");
			return result.toString();
		}else if("0".equals(userId)){
			result.put("code", 403);
			result.put("msg", "暂无权限");
			return result.toString();
		}else{
			try {
				User user = new User();
				user.setId(Long.parseLong(userId));
				List<User> userList = userService.findList(user);

				//获取加密信息 时间戳 + , + userId
				long time = System.currentTimeMillis() ;
				String tag = time +","+ userId + "," + sessionId;
				tag = UIM.encode(tag);

				long time2 = time + 30*60*1000;
				String dataTime = DESUtil.dataTime(time2);

				//获取邮箱信息
				String email = request.getParameter("email");

				//获取统一认证门户应用ID
				App app = new App();
				app.setIsLikeQuery(false);
				app.setSn("APP001");
				Long appId =appService.findByObject(app).getId();

				//获取账号ID
				Account account = new Account();
				account.setUserId(Long.valueOf(userId));
				account.setAppId(appId);
				account.setStatus(1);
				Long accountId=accountService.findByObject(account).getId();

				Email entity = new Email();
				entity.setAccountId(accountId);
				entity.setEmail(email);
				entity.setStatus(0);
				entity.setCreateTime(new Date());
				entity.setType(1);
				entity.setRemark("邮箱密码找回，请求唯一标识："+sessionId);

				emailService.save(entity);

				//发送邮件
				Message message = SendMail.sendMail();
				message.setRecipient(Message.RecipientType.TO,new InternetAddress(email));
				message.setSubject("重置密码");
				String url =GatewayHttpUtil.getKey("RemoteServer", request)+"/portal/updatePwd.action?tag="+tag;
				message.setContent(
						"<div>尊敬的用户 "+ userList.get(0).getName()
								+"，您好，<p style='margin: 0;margin-left: 26px;'>我们已经收到您重置系统密码的请求，请点击以下链接地址重置您的密码。如非本人操作，请立即登录系统更改密码以确保账户安全。</p> "
								+ "<p style='margin: 0;margin-left: 26px;'><a href='"+url+"' target='_blank'>"+url+"</a></p>"
								+ "<p style='margin: 0;margin-left: 26px;'>(注意：该链接地址 30 分钟内有效。如果您无法点击这个链接，请将此链接复制到浏览器地址栏后访问。如有其它疑问，请及时联系信息管理部处理。)</p>"
								+ "<br/>信息管理部"
								+ "<br/>"+DESUtil.dataTime2(System.currentTimeMillis())+"</div>",

						"text/html;charset=UTF-8");
				Transport.send(message);
				result.put("code", 0);
				result.put("msg", "发送成功");
			}
			catch (Exception e) {
				e.printStackTrace();
				result.put("code", -1);
				result.put("msg", "发送失败");
			}
		}
		log.info(result.toString());
		return result.toString();
	}

	/**
	 * 点击邮箱链接跳转页面,并使链接失效
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/updatePwd.action")
	@ResponseBody
	public void updatePwd(String tag) throws IOException{
		//定义全局错误编码、错误路径
		String code = "";
		String errorUrl=GatewayHttpUtil.getKey("RemoteServer", request)+"/portal/error.html?code=";
		//定义跳转成功路径、token
		String url=GatewayHttpUtil.getKey("RemoteServer", request)+"/portal/pwdUpdate.html?token=";
		try {
			//解密获取时间戳和用户ID
			String decrypt = UIM.decode(tag);
			String[] split = decrypt.split(",");
			Long userId = Long.parseLong(split[1]);
			String global = "邮箱密码找回，请求唯一标识："+split[2];
			//获取邮件AM_Email信息
			Email email = new Email();
			email.setRemark(global);
			List<Email> findList = emailService.findList(email);
			Email entity = findList.get(0);
			if(findList.size()>0){
				//判断地址的状态
				if(entity.getStatus().intValue()==0){
					//判断链接的创建时间是否超过30分钟
					long timer=Long.parseLong(split[0])+(30*60*1000);
					if(timer>System.currentTimeMillis()){
						//获取用户信息
						//修改状态
						entity.setStatus(1);
						emailService.edit(entity);

						String token = userService.findById(userId).getUserTypeId()+","+userId;
						token = UIM.encode(token);
						response.sendRedirect(url+token);
						return;
					}else{
						entity.setStatus(2);
						emailService.edit(entity);
						code = "100001";
					}
				}else if(entity.getStatus().intValue()==1){
					code = "100002";
				}else{
					code = "100001";
				}
			}else{
				code = "-1";
			}
		} catch (Exception e) {
			code = "-1";
		}
		response.sendRedirect(errorUrl+code);
		return;
	}

	@RequestMapping("/pwdUpdate.html")
	public String pwdUpdate(){
		String token=request.getParameter("token");
		if(StringUtils.isEmpty(token)){
			request.setAttribute("code","100003");
			return "resetPassword/error";
		}
		try {
			String decode = UIM.decode(token);
			String[] split = decode.split(",");
			request.setAttribute("userId", split[1]);
			request.setAttribute("userTypeId", split[0]);
		}catch (Exception e){
			e.printStackTrace();
			request.setAttribute("code","500");
			return "resetPassword/error";
		}
		return "resetPassword/pwdUpdate";
	}

	@RequestMapping("/error.html")
	public String error(String code){
		request.setAttribute("code",code);
		return "resetPassword/error";
	}

	private String path;
	@Value("${com.sense.file.upload.tempdir}")
	public void setPath(String path) {
		this.path = path;
	}

	@RequestMapping("/downloadFile/sensesso.html")
	public void downloadFile() {
		log.info("downloadFile...");

        String fileName = "sensesso.zip";
        // 配置文件下载
        response.setHeader("content-type", "application/octet-stream");
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

        String filePath=path+"/sensesso.zip";
        log.info("filePath.:"+filePath);
        // 实现文件下载
        byte[] buffer = new byte[1024];
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
        	InputStream inStream = new FileInputStream(filePath);// 文件的存放路径
            bis = new BufferedInputStream(inStream);
            OutputStream os = response.getOutputStream();
            int i = bis.read(buffer);
            while (i != -1) {
                os.write(buffer, 0, i);
                i = bis.read(buffer);
            }
            log.info("Download successfully! " + fileName);
        } catch (Exception e) {
        	log.error("Download failed! " + fileName);
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
