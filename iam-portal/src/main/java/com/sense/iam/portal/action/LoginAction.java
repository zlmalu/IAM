package com.sense.iam.portal.action;


import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.google.common.base.Splitter;
import com.sense.core.security.UIM;
import com.sense.iam.cam.Constants;
import com.sense.iam.model.am.Sms;
import com.sense.iam.model.im.Account;
import com.sense.iam.model.im.App;
import com.sense.iam.model.im.User;
import com.sense.iam.portal.util.SMSUtil;
import com.sense.iam.service.*;
import net.sf.json.JSONObject;
import org.apache.xmlbeans.impl.util.Base64;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.sense.core.util.CurrentAccount;
import com.sense.core.util.GatewayHttpUtil;
import com.sense.core.util.StringUtils;
import com.sense.iam.cache.CompanyCache;
import com.sense.iam.cache.SysConfigCache;

@Controller
public class LoginAction extends BaseAction {

	@Resource
	private JdbcService jdbcService;
	@Resource
	private SysConfigCache sysConfigCache;

	@Resource
	private CompanyCache companyCache;
	@Resource
	private StringRedisTemplate stringRedisTemplate;

	@Resource
	private UserService userService;

	@Resource
	private AccountService accountService;
	@Resource
	private AppService appService;

	@Resource
	private SmsService smsService;

	@RequestMapping("/login.html")
	public String login(String redirectUri,String client_id){
		client_id = client_id==null?"APP001":client_id;
		//重新解码重定向URL
		if(CurrentAccount.getCurrentAccount()!=null && CurrentAccount.getCurrentAccount().isValid()){
			log.info("redirectUri="+redirectUri);
			if(StringUtils.isEmpty(redirectUri)){
				redirectUri=GatewayHttpUtil.getKey("RemoteServer", request)+"/portal/main.html?r"+StringUtils.getSecureRandomnNumber();
			}
			//输出重定向到首页
			return "redirect:"+redirectUri;
		}else{
			if(StringUtils.isEmpty(redirectUri)){
				log.info("loadAuthReaml client_id="+client_id);
				loadAuthReaml(client_id);
				redirectUri=GatewayHttpUtil.getKey("RemoteServer", request)+"/portal/login.html";
			}else{
				//判断是否业务系统进行登录从而加载认证方式
				try{
					String params = redirectUri.substring(redirectUri.indexOf("?") + 1, redirectUri.length());
					Map<String, String> split = Splitter.on("&").withKeyValueSeparator("=").split(params);
					String sso_client_id= split.get("client_id");
					if(StringUtils.isEmpty(sso_client_id)){
						sso_client_id=client_id;
					}
					log.info("loadAuthReaml sso_client_id="+sso_client_id);
					loadAuthReaml(sso_client_id);

				}catch(Exception e){
					log.info("loadAuthReaml client_id="+client_id);
					loadAuthReaml(client_id);
					e.printStackTrace();
				}
			}
			log.info("redirectUri="+redirectUri);
			request.setAttribute("redirectUri", redirectUri);
			request.setAttribute("client_id", client_id);
			request.setAttribute("QYWX_APPID", sysConfigCache.QYWX_APPID);
			request.setAttribute("QYWX_SCA_APP_AGENTID", sysConfigCache.QYWX_SCA_APP_AGENTID);
			request.setAttribute("QYWX_SCA_APP_KEY", sysConfigCache.QYWX_SCA_APP_KEY);
			request.setAttribute("QYWX_SCA_REDIRECT_URI", sysConfigCache.QYWX_SCA_REDIRECT_URI);
			request.setAttribute("DINGDING_APPID", sysConfigCache.DINGDING_APPID);
			request.setAttribute("DINGDING_SCA_REDIRECT_URI", sysConfigCache.DINGDING_SCA_REDIRECT_URI);
			return "login/login";
		}
	}

	/**
	 * 加载认证方式
	 */
	public void loadAuthReaml(String client_id){


		//获取MFA编码和状态
		List<Map<String, Object>> list = jdbcService.findList("select SN,STATUS FROM AM_AUTH_REAML  WHERE COMPANY_SN=?",companyCache.getCompanySn(GatewayHttpUtil.getKey("RemoteHost", request)));
		Map<String, Integer> newMap = new HashMap<String, Integer>();
		for(Map<String, Object> map:list){
			newMap.put(map.get("SN").toString(), Integer.valueOf(map.get("STATUS").toString()));
		}

		//用户密码登录
		boolean pwdFlag=true;

		//APP扫码
		boolean qrFlag=true;

		//企业微信扫码
		boolean qywxFlag=true;

		//钉钉扫码
		boolean ddFlag=true;

		//短信动态码
		boolean smsFlag=true;

		//OPT动态码
		boolean optFlag=true;

		//用户密码登录是否被禁用
		if(newMap.containsKey("1002")&&newMap.get("1002")==2){
			pwdFlag=false;
		}
		//企业微信扫码是否被禁用
		if(newMap.containsKey("1003")&&newMap.get("1003")==2){
			qywxFlag=false;
		}
		//钉钉扫码是否被禁用
		if(newMap.containsKey("1004")&&newMap.get("1004")==2){
			ddFlag=false;
		}
		//短信动态码是否被禁用
		if(newMap.containsKey("1006")&&newMap.get("1006")==2){
			smsFlag=false;
		}
		//OPT动态码是否被禁用
		if(newMap.containsKey("1008")&&newMap.get("1008")==2){
			optFlag=false;
		}
		//APP扫码是否被禁用
		if(newMap.containsKey("1007")&&newMap.get("1007")==2){
			qrFlag=false;
		}
		if(!"APP001".equals(client_id)){
			//查询应用
			List<Map<String, Object>> apps = jdbcService.findList("select ID FROM IM_APP WHERE SN=? and  COMPANY_SN=?",client_id,companyCache.getCompanySn(GatewayHttpUtil.getKey("RemoteHost", request)));
			//获取应用绑定的认证方式
			if(apps!=null&&apps.size()>0){
				newMap=new HashMap<String, Integer>();
				//设置默认支持用户名密码登录
				newMap.put("1001", 1);
				newMap.put("1002", 1);
				List<Map<String, Object>> appReaMlist = jdbcService.findList("select SN FROM AM_AUTH_REAML WHERE ID IN(SELECT AM_REAML_ID FROM SYS_REAML_APP WHERE IM_APP_ID=?) AND COMPANY_SN=?",apps.get(0).get("ID"),companyCache.getCompanySn(GatewayHttpUtil.getKey("RemoteHost", request)));
				for(Map<String, Object> map:appReaMlist){
					newMap.put(map.get("SN").toString(), 1);
				}
				pwdFlag=true;
				qrFlag=newMap.containsKey("1007");
				qywxFlag=newMap.containsKey("1003");
				ddFlag=newMap.containsKey("1004");
				smsFlag=newMap.containsKey("1006");
				optFlag=newMap.containsKey("1008");

			}
		}
		request.setAttribute("pwdFlag", pwdFlag);
		request.setAttribute("qrFlag", qrFlag);
		request.setAttribute("qywxFlag", qywxFlag);
		request.setAttribute("ddFlag", ddFlag);
		request.setAttribute("smsFlag", smsFlag);
		request.setAttribute("optFlag", optFlag);
	}




	/**
	 * 获取验证码
	 * @return
	 */
	@RequestMapping("/cheackCode.action")
	@ResponseBody
	public String cheackCode(){
		String sessionId = GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request);
		JSONObject result=new JSONObject();
		String code=request.getParameter("code");
		log.info("code="+code);
		try{
			Sms model=getRedisMsg(sessionId);
			if(model==null){
				result.put("code", -1);
				result.put("msg", "短信验证码无效");
				return result.toString();
			}
			//验证码一致
			if(code.equals(model.getValidateCode())){
				result.put("code", 0);
				result.put("msg", UIM.encode(model.getAccountId()+""));
				//验证码成功后删除redis条目
				stringRedisTemplate.delete(Constants.SMS_SETTING_KEY+"_"+sessionId);
			}else{
				result.put("code", -1);
				result.put("msg", "短信验证码无效");
			}
		}catch(Exception e){
			e.printStackTrace();
			result.put("code", -10);
			result.put("msg", "系统异常");
		}
		return result.toString();
	}

	public Sms getRedisMsg(String sessionId){

		try {
			//获取根据session 获取认证对象
			String value = stringRedisTemplate.opsForValue().get(Constants.SMS_SETTING_KEY+"_"+sessionId);
			log.debug("get sms data:" + value);
			if (value != null) {
				ObjectInputStream oos = new ObjectInputStream(new ByteArrayInputStream(Base64.decode(value.getBytes())));
				return  (Sms) oos.readObject();
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 短信登录获取验证码
	 * @return
	 */
	@RequestMapping("/sendvCode2.action")
	@ResponseBody
	public String sendvCode2(){
		String sessionId = GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request);
		JSONObject result=new JSONObject();
		//验证是否非法请求
		String mobile=request.getParameter("mobile");
		log.info("mobile="+mobile);
		try{
			CurrentAccount.setCurrentAccount(new CurrentAccount(companyCache.getCompanySn(GatewayHttpUtil.getKey("RemoteHost", request))));
			User u=new User();
			u.setTelephone(mobile);
			u.setIsLikeQuery(false);
			u.setIsControl(false);
			//查询用户
			List<User> listUser=userService.findList(u);
			if(listUser != null && listUser.size() > 0){
				if(listUser.size() > 1){
					result.put("code", -1);
					result.put("msg", "当前手机号绑定多个用户!");
					return result.toString();
				}
				//获取统一认证门户应用ID
				App app = new App();
				app.setIsControl(false);
				app.setIsLikeQuery(false);
				app.setSn("APP001");
				Long appId =appService.findByObject(app).getId();
				log.info("appId:::"+appId);
				log.info("userId:::"+listUser.get(0).getId());
				//获取帐号ID
				Account account = new Account();
				account.setUserId(listUser.get(0).getId());
				account.setAppId(appId);
				account.setStatus(1);
				account.setIsControl(false);

				account=accountService.findByObject(account);
				if(account != null){
					Long accountId=account.getId();
					log.info("accountId:::"+accountId);
					String verifyCode = String.valueOf(new Random().nextInt(899999) + 100000);
					//保存短信信息
					Sms sms = new Sms();
					sms.setAccountId(accountId);
					sms.setMobile(mobile);
					sms.setValidateCode(verifyCode);
					sms.setStatus(1);
					sms.setCreateTime(new Date());
					sms.setType(1);
					sms.setRemark("短信认证，请求唯一标识："+sessionId);
					SendSmsResponse response= SMSUtil.sendIdentifyingCode1(mobile, verifyCode);

					if(response.getCode().equals("OK")){
						smsService.save(sms);
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						try {
							ObjectOutputStream oos = new ObjectOutputStream(bos);
							oos.writeObject(sms);
							oos.close();
							String value = new String(Base64.encode(bos.toByteArray()));
							//存放在redis中，过期5分钟
							stringRedisTemplate.opsForValue().set(Constants.SMS_SETTING_KEY+"_"+sessionId,value,5*60L, TimeUnit.SECONDS);
						} catch (IOException e) {
							e.printStackTrace();
						}
						result.put("code", 0);
					}else{
						result.put("code", -105);
						result.put("msg", "短信发送失败,原因:"+response.getMessage());
					}
				}else{
					result.put("code", -1);
					result.put("msg", "当前手机号绑定用户不存在门户帐号!");
				}
			}else{
				result.put("code", -1);
				result.put("msg", "手机号未绑定用户!");
			}
			CurrentAccount.setCurrentAccount(null);
		}catch(Exception e){
			e.printStackTrace();
			result.put("code", -10);
			result.put("msg", "系统异常");
		}
		return result.toString();
	}


	/**
	 * 企业微信扫码登录
	 * @return
	 */
	@RequestMapping("/qywxlogin.action")
	public String qywxlogin(){
		request.setAttribute("code", request.getParameter("code"));
		request.setAttribute("redirectUri", request.getParameter("redirectUri"));
		return "sso/qywxlogin";
	}

	/**
	 * 钉钉扫码登录
	 * @return
	 */
	@RequestMapping("/dingdinglogin.action")
	public String dingdinglogin(){
		request.setAttribute("code", request.getParameter("code"));
		request.setAttribute("redirectUri", request.getParameter("redirectUri"));
		return "sso/dingdinglogin";
	}


}
