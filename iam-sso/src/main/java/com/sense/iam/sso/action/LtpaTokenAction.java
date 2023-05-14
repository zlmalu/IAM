package com.sense.iam.sso.action;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sense.core.util.CurrentAccount;
import com.sense.core.util.GatewayHttpUtil;
import com.sense.core.util.StringUtils;
import com.sense.iam.cache.CompanyCache;
import com.sense.iam.cache.SsoConfigCache;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.auth.OnlineUser;
import com.sense.iam.cam.auth.cache.AccessTokenCache;
import com.sense.iam.cam.auth.cache.OnlineUserCache;
import com.sense.iam.model.im.Account;
import com.sense.iam.model.sso.Ltpa;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.AppService;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.SsoCasService;
import com.sense.iam.sso.ltpa.LtpaToken;

/**
 * 
 * ltpa认证协议
 * 
 * Description: 调用路径加入前缀：/ltpa
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Controller
@RequestMapping("ltpa")
public class LtpaTokenAction extends BaseAction{
	@Resource
	private AccessTokenCache accessTokenCache;
	@Resource
	private SsoConfigCache  ssoConfigCache;
	@Resource
	private OnlineUserCache onlineUserCache;
	@Resource
	private AccountService accountService;
	@Resource
	private SsoCasService ssoCasService;
	@Resource
	private AppService appService;
	@Resource
	private StringRedisTemplate stringRedisTemplate;
	@Resource
	private JdbcService jdbcService;
	@Resource
	private CompanyCache companyCache;
	/**
	 * 认证跳转验证 
	 * @param client_id 使用的客户端id（应用标识）
	 * @param service 回调路径
	 */
	@RequestMapping(value="{client_id}/login", method = {RequestMethod.GET,RequestMethod.POST})
	public String login(@PathVariable String client_id,HttpServletRequest request,HttpServletResponse response){
		String redirect_uri = request.getParameter("redirect_uri");
		String accountId = request.getParameter("tokenId");
		Ltpa ltpa = ssoConfigCache.getLtpaConfig(client_id);
		if (client_id == null || ltpa == null) {
			// 返回403没有权限
			request.setAttribute("error", "应用未注册Ltpa服务(client_id为空)");
			try {
				log.info("应用未注册Ltpa服务(client_id为空)");
				return "403";
			} catch (Exception e) {
				e.printStackTrace();
				return "403";
			}
			
		}
		
		if (StringUtils.isEmpty(redirect_uri) || redirect_uri.equals("null")) {
			//获取默认地址
			String defUrl=ltpa.getDefaultUrl();
			if(StringUtils.isEmpty(defUrl)||StringUtils.isEmpty(defUrl.trim())){
				request.setAttribute("error", "应用未配置回调地址:"+ltpa.getAppName());
				return "403";
			}else{
				redirect_uri = defUrl;
			}
		}
		/** 如果用户已登陆过 **/
		// 验证通过跳转到redirect_uri 加入如下参数:
		CurrentAccount currentAccount = CurrentAccount.getCurrentAccount();
		// 验证通过跳转到redirect_uri 加入如下参数:
		OnlineUser onlineUser = (OnlineUser) request.getSession().getAttribute(super.ONLINE_USER_SESSION_ID);
		if (onlineUser == null) {
			if (currentAccount != null) {
				onlineUser = new OnlineUser();
				onlineUser.setExpried(System.currentTimeMillis() + 30000);
				onlineUser.setLoginIp(currentAccount.getRemoteHost());
				onlineUser.setLoginTime(System.currentTimeMillis());
				onlineUser.setUid(currentAccount.getLoginName());
				onlineUser.setValid(true);
				onlineUser.setSessionId(currentAccount.getSessionId());
				onlineUser.setAppSn(client_id);
			}
		}
		
		if (onlineUser != null) {
			if (accountId == null) {// 是否指定单点登陆账号
				// 查询用户对应账号并判断用户是否存在多账号
				Account account = new Account();
				account.setAppSn(client_id);
				// account.setLoginName(onlineUser.getUid());
				account.setUserId(currentAccount.getUserId());
				account.setStatus(Constants.ACCOUNT_ENABLED);// 进行OAUTH的帐号必须是启用帐号
				account.setIsControl(false);
				List<Account> list = accountService.findList(account);
				if (list == null || list.size() == 0) {
					log.info("当前应用ID:" + client_id + ",当前用户ID:" + currentAccount.getUserId() + ",账号数量：" + list.size());
					try {
						request.setAttribute("error", "当前用户当前应用无账号：" + client_id + "," + currentAccount.getName());
						// 如果当前应用指定了无账号跳转页面则跳转指定，否则默认403
						return "403";
					} catch (Exception e) {
						log.error("forward request error", e);
					}
				} else if (list.size() > 1) {
					try {
						String uri=GatewayHttpUtil.getKey("RemoteServer", request)+"/sso/cas3/"+client_id+"/login?"+request.getQueryString();
						request.setAttribute("redirectUri", uri);
						request.setAttribute("accts", list);
						return "acctsel";
					} catch (Exception e) {
						log.error("forward request error", e);
					}
				} else {
					account = (Account) list.get(0);
					accountId = account.getId() + "";
					onlineUser.setUid(account.getLoginName());
				}
			}
			onlineUser.setAccountId(accountId);
			// 单点起始时间
	          Date tokenCreation = new Date(new Date().getTime() - 60000 * 10);
			// 单点到期时间
	          Long svtime=ltpa.getSessionValidTime()==null?180000:ltpa.getSessionValidTime();
			  Date tokenExpires = new Date(tokenCreation.getTime() + svtime * 60000);
			  
			  String dominoSecret = ltpa.getSecretKey();
//			  String dominoSecret="/Jq/2bPWTMF6EGvcCB4UgggRUfktaQHBOYAPBmhVSc/Tf16yWnEvZE8BPwxq9iDTiTYJ/pIelGsb2VGY9XPg4rgmi0JRHFUpvTD77XDQZlxlZza+ZBirMiZTVZUzLvsv7R4PijrzY2ROVi/9EmZ/nxGP8jR3DNL5YzdgCpUJFDbz4d0anHfk1pHZ4lygKCWZhHdXmiYBMTk7o7hXejqiS/NT0tb5ObnZBziQTiPW5ZngGogv3G4dd/ils6MdZaN1YR0xk4vFHLUxOfad/LWfuMlkuORZVG6MKHArA6BDsiXOInEkBDGDIi25N1xH92DayTDBfWziR2tHskCtmFPFojy2clsiHXo65a3Gae0vXUQ=";
			  String token = LtpaToken.generate(onlineUser.getUid(), tokenCreation, tokenExpires, dominoSecret).getLtpaToken();
			  request.setAttribute("ltpaToken", token);
	          if(StringUtils.isEmpty(token)) {
	        	  request.setAttribute("error", "ltpa生成错误：" + token);
					// 如果当前应用指定了无账号跳转页面则跳转指定，否则默认403
					return "403";
			}
			  request.setAttribute("redirect_uri", redirect_uri);
			  request.setAttribute("domain", ltpa.getDoMain());
			  request.setAttribute("expires", tokenExpires.getTime());
			//记录oidc单点登陆日志
			com.sense.iam.model.sso.Log ssoLog=new com.sense.iam.model.sso.Log();
			ssoLog.setUserName(onlineUser.getUid());
			ssoLog.setAccountId(Long.valueOf(accountId));
			ssoLog.setSsoType(Constants.SSO_LTPA);
			ssoLog.setCompanySn(CurrentAccount.getCurrentAccount().getCompanySn());
			queueSender.send(ssoLog);
			return "sso/ltpa/jump"; 
		} else {// 跳转到登陆授权页面,并携带应用请求参数发送到登陆页面
			String uri=GatewayHttpUtil.getKey("RemoteServer", request)+"/sso/ltpa/"+client_id+"/login?"+request.getQueryString();
			String redirects=GatewayHttpUtil.getKey("RemoteServer", request)+"/portal/login.html?redirectUri="+java.net.URLEncoder.encode(uri);
			return "redirect:"+redirects; 
		}
	}
	public static void main(String[] args) {
		// BASE64解码 ltpa3DESKey 
		byte[] decode3DES = Base64.decodeBase64("pR0pRGfRuin+TswuJFbleaOc2y8EC6Cqp7ji+BJPSDM=".getBytes()); 
		System.out.println(decode3DES);
//		int a=Base64.DONT_BREAK_LINES;
	}
}
