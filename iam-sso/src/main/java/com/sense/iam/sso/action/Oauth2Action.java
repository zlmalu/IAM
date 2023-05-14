package com.sense.iam.sso.action;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sense.core.util.CurrentAccount;
import com.sense.core.util.GatewayHttpUtil;
import com.sense.core.util.JWTUtil;
import com.sense.core.util.StringUtils;
import com.sense.iam.cache.SsoConfigCache;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.auth.AuthResultCode;
import com.sense.iam.cam.auth.OnlineUser;
import com.sense.iam.cam.auth.Token;
import com.sense.iam.cam.auth.cache.AccessTokenCache;
import com.sense.iam.cam.auth.cache.OnlineUserCache;
import com.sense.iam.model.im.Account;
import com.sense.iam.model.im.Image;
import com.sense.iam.model.im.Org;
import com.sense.iam.model.im.User;
import com.sense.iam.model.sso.Oauth;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.UserMultiOrgService;
import com.sense.iam.service.UserService;

import net.sf.json.JSONObject;

/**
 * 
 * oauth2.0用户认证协议
 * 
 * Description: 调用路径加入前缀：/oauth
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Controller
@RequestMapping("oauth")
public class Oauth2Action extends BaseAction{

	@Resource
	private AccessTokenCache accessTokenCache;
	@Resource
	private SsoConfigCache  ssoConfigCache;
	@Resource
	private OnlineUserCache onlineUserCache;
	@Resource
	private AccountService accountService;
	
	
	/**
	 * 认证跳转验证 
	 * @param response_type  code 代表授权模式；token代表简化模式
	 * @param client_id 使用的客户端id（应用标识）
	 * @param redirect_uri 回调路径
	 * @param scope 用户授权范围   ,统一身份认证系统自己定义，应用不需要传输
	 */
	@RequestMapping(value="authorize", method = {RequestMethod.GET,RequestMethod.POST})
	public String authorize(HttpServletRequest request,HttpServletResponse response){
		String response_type=request.getParameter("response_type");
		String client_id=request.getParameter("client_id");
		String redirect_uri=request.getParameter("redirect_uri");
		Oauth oauth=ssoConfigCache.getOauthConfig(client_id);
		log.info("client_id : "+client_id);
		if(response_type==null || response_type.trim().length()==0){
			response_type="token";
		}
		if(client_id==null || ssoConfigCache.getOauthConfig(client_id)==null){
			//返回403没有权限
			//super.print403(response);
			request.setAttribute("code",403);
			request.setAttribute("msg","clientId参数不能为空或者未配置OAUTH单点协议");
			return "message";
		}
		if(redirect_uri==null || redirect_uri.trim().length()==0 || redirect_uri.indexOf("accessToken")!=-1){
			redirect_uri=oauth.getDefaultUrl();
		}
		redirect_uri=URLDecoder.decode(redirect_uri);
		log.info("response_type : "+response_type);
		log.info("redirect_uri : "+redirect_uri);
		request.setAttribute("redirectUri", request.getRequestURL()+"?"+request.getQueryString());
		/**如果用户已登陆过**/
		//验证通过跳转到redirect_uri  加入如下参数:
		OnlineUser onlineUser=null;
		if(onlineUser==null){
			CurrentAccount currentAccount=CurrentAccount.getCurrentAccount();
			log.info("currentAccount:"+currentAccount);
			if(currentAccount!=null){
				onlineUser=new OnlineUser();
				onlineUser.setExpried(System.currentTimeMillis()+oauth.getSessionValidTime());
				onlineUser.setLoginIp(currentAccount.getRemoteHost());
				onlineUser.setLoginTime(System.currentTimeMillis());
				onlineUser.setUid(currentAccount.getLoginName());
				onlineUser.setAccountId(currentAccount.getId().toString());
				onlineUser.setValid(true);
				onlineUser.setSessionId(currentAccount.getSessionId());
				onlineUser.setAppSn(client_id);
			}
		}
		log.info("onlineUser : "+onlineUser);
		
		if(onlineUser!=null){
			log.info("onlineUser : "+onlineUser.getAccountId()+",client_id="+onlineUser.getAppSn()+",sessionId="+onlineUser.getSessionId());
			String accountId=request.getParameter("tokenId");
			if(accountId==null){
				//是否指定单点登陆账号
				//查询用户对应账号并判断用户是否存在多账号
				Account account=new Account();
				account.setAppSn(client_id);
//				account.setLoginName(onlineUser.getUid());
				account.setUserId(accountService.findById(Long.valueOf(onlineUser.getAccountId())).getUserId());
				account.setStatus(Constants.ACCOUNT_ENABLED);//进行OAUTH的帐号必须是启用帐号
				account.setIsControl(false);
				List list=accountService.findList(account);
				if(list==null || list.size()==0){
					JSONObject result=new JSONObject();
					result.put("error", "-1");
					result.put("error_description","account num:0");
					//返回403没有权限
					//super.printERROR(result.toString(),response);
					request.setAttribute("code",404);
					request.setAttribute("msg","用户没有该应用权限，请联系管理员进行权限开通");
					return "message";
				}else if(list.size()>1){
					JSONObject result=new JSONObject();
					result.put("error", "-1");
					result.put("error_description","account num:"+list.size()+"");
					//返回多个权限权限
					request.setAttribute("code",500);
					request.setAttribute("msg","用户所在应用有"+ list.size()+"个权限,请联系管理员进行权限调整");
					return "message";

				}else{
					account=(Account) list.get(0);
					accountId=account.getId()+"";
					onlineUser.setAccountId(accountId);
					onlineUser.setUid(account.getLoginName());
				}
			}else{
				onlineUser.setAccountId(accountId);
			}
		
			//生成AccessToken
			Token token=accessTokenCache.grantToken(onlineUser);
			token.setClientId(client_id);
			//记录oauth单点登陆用户信息
			//记录oauth单点登陆日志
			com.sense.iam.model.sso.Log ssoLog=new com.sense.iam.model.sso.Log();
			ssoLog.setUserName(onlineUser.getUid());
			ssoLog.setAccountId(Long.valueOf(accountId));
			ssoLog.setSsoType(Constants.SSO_OAUTH);
			ssoLog.setCompanySn(CurrentAccount.getCurrentAccount().getCompanySn());
			queueSender.send(ssoLog);
			if(response_type!=null && response_type.equals("code")){
				if(redirect_uri.indexOf("#{code}")!=-1){
					redirect_uri=redirect_uri.replace("#{code}", token.getId());
				}else{
					if (redirect_uri.indexOf("?") != -1) {
						redirect_uri += "&code=" + token.getId();
					} else {
						redirect_uri += "?code=" + token.getId();
					}
				}
			}else {
				if(redirect_uri.indexOf("?")!=-1){
					redirect_uri+="&accessToken="+token.getId()+"&expried="+token.getExpried()+"&code=" + token.getId();
				}else{
					redirect_uri+="?accessToken="+token.getId()+"&expried="+token.getExpried()+"&code=" + token.getId();;
				}
			}
			
			log.info("redirecturl========================"+redirect_uri);
			//response.sendRedirect(redirect_uri);
			return "redirect:" + redirect_uri;
			
		}
		String redirectsLogin = GatewayHttpUtil.getKey("RemoteServer", request)+"/portal/login.html?redirectUri="+URLEncoder.encode(GatewayHttpUtil.getKey("RemoteServer", request)+"/sso/oauth/authorize?client_id="+client_id+"&response_type="+response_type+"&redirect_uri="+redirect_uri);
		return "redirect:" + redirectsLogin;
	}
	
	@RequestMapping(value="authorizeGetCode", method = {RequestMethod.GET,RequestMethod.POST})
	@ResponseBody
	public JSONObject authorizeGetCode(HttpServletRequest request,HttpServletResponse response){
		String client_id=request.getParameter("client_id");
		Oauth oauth=ssoConfigCache.getOauthConfig(client_id);
		log.info("client_id : "+client_id);
		JSONObject json = new JSONObject();
		if(client_id==null || ssoConfigCache.getOauthConfig(client_id)==null){
			//返回403没有权限
			json.put("success", "false");
			json.put("msg", "没用认证权限");
			return json;
		}
		request.setAttribute("redirectUri", request.getRequestURL()+"?"+request.getQueryString());
		/**如果用户已登陆过**/
		//验证通过跳转到redirect_uri  加入如下参数:
		OnlineUser onlineUser=null;
		if(onlineUser==null){
			CurrentAccount currentAccount=CurrentAccount.getCurrentAccount();
			log.info("currentAccount:"+currentAccount);
			if(currentAccount!=null){
				onlineUser=new OnlineUser();
				onlineUser.setExpried(System.currentTimeMillis()+oauth.getSessionValidTime());
				onlineUser.setLoginIp(currentAccount.getRemoteHost());
				onlineUser.setLoginTime(System.currentTimeMillis());
				onlineUser.setUid(currentAccount.getLoginName());
				onlineUser.setAccountId(currentAccount.getId().toString());
				onlineUser.setValid(true);
				onlineUser.setSessionId(currentAccount.getSessionId());
				onlineUser.setAppSn(client_id);
			}
		}
		log.info("onlineUser : "+onlineUser);
		
		if(onlineUser!=null){
			log.info("onlineUser : "+onlineUser.getAccountId()+",client_id="+onlineUser.getAppSn());
			String accountId=request.getParameter("tokenId");
			if(accountId==null){
				//是否指定单点登陆账号
				//查询用户对应账号并判断用户是否存在多账号
				Account account=new Account();
				account.setAppSn(client_id);
//				account.setLoginName(onlineUser.getUid());
				account.setUserId(accountService.findById(Long.valueOf(onlineUser.getAccountId())).getUserId());
				account.setStatus(Constants.ACCOUNT_ENABLED);//进行OAUTH的帐号必须是启用帐号
				account.setIsControl(false);
				List list=accountService.findList(account);
				if(list==null || list.size()==0){
					//返回403没有权限
					json.put("success", "false");
					json.put("msg", "没用认证权限");
					json.put("error_description","account num:0");
					return json;
				}else if(list.size()>1){
					//返回403没有权限
					JSONObject result=new JSONObject();
					json.put("success", "false");
					json.put("msg", "没用认证权限");
					json.put("error_description","account num:"+list.size()+"");
					return json;
				}else{
					account=(Account) list.get(0);
					accountId=account.getId()+"";
					onlineUser.setAccountId(accountId);
					onlineUser.setUid(account.getLoginName());
				}
			}else{
				onlineUser.setAccountId(accountId);
			}
		
			//生成AccessToken
			Token token=accessTokenCache.grantToken(onlineUser);
			token.setClientId(client_id);
			//记录oauth单点登陆用户信息
			//记录oauth单点登陆日志
			com.sense.iam.model.sso.Log ssoLog=new com.sense.iam.model.sso.Log();
			ssoLog.setUserName(onlineUser.getUid());
			ssoLog.setAccountId(Long.valueOf(accountId));
			ssoLog.setSsoType(Constants.SSO_OAUTH);
			ssoLog.setCompanySn(CurrentAccount.getCurrentAccount().getCompanySn());
			queueSender.send(ssoLog);
			try {
				json.put("success", "true");
				json.put("msg", token.getId());
				return json;
			} catch (Exception e) {
				log.error("authorize redirect ",e);
			}
		}else{
			json.put("success", "false");
			json.put("msg", "应用超时");
			json.put("error_description","session overtime");
			return json;
		}
		return json;
	}
	
	/**
	 * 根据code获取access_token
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("accessToken")
	@ResponseBody
	public String accessToken(HttpServletRequest request, HttpServletResponse response) {
		JSONObject result = new JSONObject();

		String code = request.getParameter("code");
		String clientId = request.getParameter("client_id");
		String clientSecret = request.getParameter("client_secret");
//		String grant_type = request.getParameter("grant_type"); //authorization_code

		log.info("token [code]:" + code + " [client_id]:" + clientId + " [client_secret]:" + clientSecret);

		Oauth oauth = ssoConfigCache.getOauthConfig(clientId);
		if (oauth == null || !clientSecret.equals(oauth.getSecretKey())) {
			result.put("error", AuthResultCode.ACCESS_CHECK_ERROR);
			result.put("error_description", "client secretkey is invalid");
			log.info(result);
			return result.toString();
		}

		Token token = accessTokenCache.getToken(code);
		if (token != null) {
			token.setId(StringUtils.getUuid());// 重置在线用户tokenId
			OnlineUser user = (OnlineUser) token.getContent();
			onlineUserCache.put(token.getId(), user);
			result.put("access_token", token.getId());
			result.put("token_type", "bearer");
			result.put("expires_in", token.getExpried());
			result.put("refresh_token", token.getId());
			result.put("scope", "read");
			log.info(result);
			return result.toString();
		} else {
			result.put("error", AuthResultCode.ACCESS_CHECK_ERROR);
			result.put("error_description", "access token checked failed");
			log.info(result);
			return result.toString();
		}
	}
	
	/**
	 * 根据access_token获取用户信息
	 * @param request
	 * @param response
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping("userInfo")
	@ResponseBody
	public String userInfo(HttpServletRequest request, HttpServletResponse response) {
		String access_token = request.getParameter("access_token");
		String isRefresh=request.getParameter("isrefresh");
		
		Map resultMap = new HashMap();
		// 从缓存中获取用户基础信息
		OnlineUser onlineUser = onlineUserCache.get(access_token);
		if (onlineUser == null) {
			resultMap.put("ret", -1);
			resultMap.put("msg", "invalid access_token");
			return JSONObject.fromObject(resultMap).toString();
		}
		// 从缓存中读取引用密钥，校验密钥是否正确
		Oauth oauth = ssoConfigCache.getOauthConfig(onlineUser.getAppSn());
		if (oauth == null) {
			resultMap.put("ret", -1);
			resultMap.put("msg", "client secretkey is invalid");
			return JSONObject.fromObject(resultMap).toString();
		}
		if(isRefresh!=null && isRefresh.equals("true")){
			onlineUser.setExpried(System.currentTimeMillis()+oauth.getSessionValidTime());
		}
		resultMap.put("ret", 0);
		resultMap.put("msg", "");
		resultMap.put("uid", onlineUser.getUid());
		if(!StringUtils.isEmpty(oauth.getConfig())) {
			resultMap.putAll(super.parseXmlToMap(oauth.getConfig(), onlineUser.getAccountId()));
		}
		log.info("sssss==="+resultMap);
		return JSONObject.fromObject(resultMap).toString();
	}

	
	
	/**
	 * 获取用户信息根据accessToken 和密钥跳过sessionToken步骤
	 * @param accessToken authorize返回的值
	 * @param oauth_consumer_key oatuh密钥，如果不传oauth_consumer_key参数则返回openId
	 * @return 用户信息
	 */
	@RequestMapping(value="getUserInfoByToken",method = {RequestMethod.GET,RequestMethod.POST} ,produces="application/json; charset=utf-8")
	@ResponseBody
	public String getUserInfoByToken(HttpServletRequest request,HttpServletResponse response){
		JSONObject result=new JSONObject();
		String accessToken=request.getParameter("accessToken");
		String oauth_consumer_key=request.getParameter("oauth_consumer_key");
		Token token=accessTokenCache.getToken(accessToken);
		if(token!=null){
			token.setId(StringUtils.getUuid());//重置在线用户tokenId
			OnlineUser user=(OnlineUser)token.getContent();
			onlineUserCache.put(token.getId(), user);
			result.put("client_id", user.getAppSn());
			result.put("openid", token.getId());
			if(oauth_consumer_key!=null&&oauth_consumer_key.length()>0){
				String openId=token.getId();
				Map resultMap=new HashMap();
				//从缓存中获取用户基础信息
				OnlineUser onlineUser=onlineUserCache.get(openId);
				if(onlineUser==null){
					resultMap.put("ret", -1);
					resultMap.put("msg", "invalid openid");
					return JSONObject.fromObject(resultMap).toString();
				}
				//从缓存中读取引用密钥，校验密钥是否正确
				Oauth oauth=ssoConfigCache.getOauthConfig(onlineUser.getAppSn());
				if(oauth==null || !oauth_consumer_key.equals(oauth.getSecretKey())){
					resultMap.put("ret", -1);
					resultMap.put("msg", "client secretkey are invalid");
					return JSONObject.fromObject(resultMap).toString();
				}
				resultMap.put("ret", 0);
				resultMap.put("msg", "");
				resultMap.put("uid", onlineUser.getUid());
				resultMap.putAll(super.parseXmlToMap(oauth.getConfig(),onlineUser.getAccountId()));
				return JSONObject.fromObject(resultMap).toString();
			}
			return result.toString();
			
		}else{
			result.put("error", AuthResultCode.ACCESS_CHECK_ERROR);
			result.put("error_description","access token check failed");
			return result.toString();
		}
	}
	
	
	
	/**
	 * 获取用户的sessionToken
	 * @param accessToken authorize返回的值
	 * @return {"client_id":"对应的应用编码，主要为防止应用篡改","openid":"用户的会话ID"} 
	 */
	@RequestMapping(value="me",method = {RequestMethod.GET,RequestMethod.POST} ,produces="application/json; charset=utf-8")
	@ResponseBody
	public String me(HttpServletRequest request,HttpServletResponse response){
		JSONObject result=new JSONObject();
		log.info("---------me--------");

		String accessToken=request.getParameter("accessToken");
		log.info("accessToken:"+accessToken);
		Token token=accessTokenCache.getToken(accessToken);
		if(token!=null){
			token.setId(StringUtils.getUuid());//重置在线用户tokenId
			OnlineUser user=(OnlineUser)token.getContent();
			onlineUserCache.put(token.getId(), user);
			result.put("client_id", user.getAppSn());
			result.put("openid", token.getId());
			return result.toString();
			
		}else{
			result.put("error", AuthResultCode.ACCESS_CHECK_ERROR);
			result.put("error_description","access token check failed");
			return result.toString();
		}
	}
	
	
	/**
	 * 获取用户信息
	 * @param oauth_consumer_key 分配给应用的密钥
	 * @param openId 通过me接口获取到的会话标识
	 * @return {"ret":0,"msg":"",用户相关属性",...} 
	 */
	@RequestMapping(value="getUserInfo",method = {RequestMethod.GET,RequestMethod.POST}, produces="application/json; charset=utf-8")
	@ResponseBody
	public String getUserInfo(HttpServletRequest request,HttpServletResponse response){
		log.info("---------getUserInfo--------");
		String oauth_consumer_key=request.getParameter("oauth_consumer_key");
		String openId=request.getParameter("openId");
		String isRefresh=request.getParameter("isrefresh");
		Map resultMap=new HashMap();
		log.info("oauth_consumer_key:"+oauth_consumer_key);
		log.info("openId:"+openId);
		log.info("isrefresh:"+isRefresh);
		//从缓存中获取用户基础信息
		OnlineUser onlineUser=onlineUserCache.get(openId);
		if(onlineUser==null){
			resultMap.put("ret", -1);
			resultMap.put("msg", "invalid openid");
			return JSONObject.fromObject(resultMap).toString();
		}
		//从缓存中读取引用密钥，校验密钥是否正确
		Oauth oauth=ssoConfigCache.getOauthConfig(onlineUser.getAppSn());
		if(oauth==null || !oauth_consumer_key.equals(oauth.getSecretKey())){
			resultMap.put("ret", -1);
			resultMap.put("msg", "client secretkey are invalid");
			return JSONObject.fromObject(resultMap).toString();
		}
		if(isRefresh!=null && isRefresh.equals("true")){
			onlineUser.setExpried(System.currentTimeMillis()+oauth.getSessionValidTime());
		}
		resultMap.put("ret", 0);
		resultMap.put("msg", "");
		resultMap.put("uid", onlineUser.getUid());
		log.info("uid:"+onlineUser.getUid());
		if(!StringUtils.isEmpty(oauth.getConfig())) {
			resultMap.putAll(super.parseXmlToMap(oauth.getConfig(), onlineUser.getAccountId()));
		}
		return JSONObject.fromObject(resultMap).toString();
	}
	
	/**
	 * 注销会话
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="logout", method=RequestMethod.GET)
	@ResponseBody
	public String logout(HttpServletRequest request,HttpServletResponse response){
		String sessionToken=request.getParameter("sessionToken");
		if(sessionToken!=null)onlineUserCache.remove(sessionToken);
		Map resultMap =new HashMap();
		resultMap.put("errorcode", "0000");
		resultMap.put("errormsg", "");
		return JSONObject.fromObject(resultMap).toString();
	}
}
