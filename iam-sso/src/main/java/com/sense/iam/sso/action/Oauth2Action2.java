package com.sense.iam.sso.action;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sense.core.util.CurrentAccount;
import com.sense.core.util.GatewayHttpUtil;
import com.sense.core.util.StringUtils;
import com.sense.iam.cache.SsoConfigCache;
import com.sense.iam.cache.SysConfigCache1;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.auth.AuthResultCode;
import com.sense.iam.cam.auth.OnlineUser;
import com.sense.iam.cam.auth.Token;
import com.sense.iam.cam.auth.cache.AccessTokenCache;
import com.sense.iam.cam.auth.cache.OnlineUserCache;
import com.sense.iam.model.im.Account;
import com.sense.iam.model.sso.Oauth;
import com.sense.iam.service.AccountService;

import cn.hutool.core.util.URLUtil;
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
@RequestMapping("oauth2")
public class Oauth2Action2 extends BaseAction{

	@Resource
	private AccessTokenCache accessTokenCache;
	@Resource
	private SsoConfigCache  ssoConfigCache;
	@Resource
	private OnlineUserCache onlineUserCache;
	@Resource
	private AccountService accountService;
	@Resource
	private SysConfigCache1 sysConfigCache1;
	
	/**
	 * 认证跳转验证 
	 * @param response_type  code 代表授权模式；token代表简化模式
	 * @param client_id 使用的客户端id（应用标识）
	 * @param redirect_uri 回调路径
	 * @param scope 用户授权范围   ,统一身份认证系统自己定义，应用不需要传输
	 */
	@RequestMapping(value="authorize", method = {RequestMethod.GET,RequestMethod.POST})
	public String authorize(HttpServletRequest request,HttpServletResponse response){
		String response_type = request.getParameter("response_type")==null?"code":request.getParameter("response_type");
		 String scope = request.getParameter("scope")==null?"read":request.getParameter("scope");
		String client_id = request.getParameter("client_id");
		String redirect_uri = request.getParameter("redirect_uri");
		String state = request.getParameter("state");
		Oauth oauth = ssoConfigCache.getOauthConfig(client_id);
		if (client_id == null || oauth == null) {
			// 返回403没有权限
			//request.setAttribute("error", "应用未注册："+client_id);
			//return "403";
			request.setAttribute("code",403);
			request.setAttribute("msg","clientId参数不能为空或者未配置OAUTH单点协议");
			return "message";
		}

		
		//判断是否有重定向地址，没有的话设置SSO配置中的地址
		if (StringUtils.isEmpty(redirect_uri) || redirect_uri.equals("null")) {
			//获取默认地址
			String defUrl=oauth.getDefaultUrl();
			if(StringUtils.isEmpty(defUrl)||StringUtils.isEmpty(defUrl.trim())){
				// 返回403没有权限
				//request.setAttribute("error", "回调地址不正确");
				//return "403";
				request.setAttribute("code",404);
				request.setAttribute("msg","回调地址为空");
				return "message";
			
			}else{
				redirect_uri = defUrl;
			}
		}else{
			//验证回调url
			//获取默认地址
			String defUrl=oauth.getDefaultUrl();
			if(StringUtils.isEmpty(defUrl)||StringUtils.isEmpty(defUrl.trim())){
				// 返回403没有权限
				log.info("oauth返回结果:redirect_uri111非法："+redirect_uri);
				//request.setAttribute("error", "回调地址不正确");
				//return "403";
				request.setAttribute("code",404);
				request.setAttribute("msg","回调地址为空");
				return "message";
			}
			
			try {
				URL redirectHost = new URL(redirect_uri);
				URL configHost = new URL(defUrl);
				if(!redirectHost.getHost().equals(configHost.getHost())){
					log.info("oauth返回结果:redirect_uri非法："+redirect_uri);
					// 返回403没有权限
					//request.setAttribute("error", "回调地址不正确");
					//return "403";
					request.setAttribute("code",500);
					request.setAttribute("msg","非法的回调地址访问路径");
					return "message";
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		
		/** 如果用户已登陆过 **/
		// 验证通过跳转到redirect_uri 加入如下参数:
		OnlineUser onlineUser = (OnlineUser) request.getSession().getAttribute(super.ONLINE_USER_SESSION_ID);
		CurrentAccount currentAccount = CurrentAccount.getCurrentAccount();
		if (onlineUser == null) {
			if (currentAccount != null) {
				onlineUser = new OnlineUser();
				onlineUser.setExpried(System.currentTimeMillis() + oauth.getSessionValidTime());
				onlineUser.setLoginIp(currentAccount.getRemoteHost());
				onlineUser.setLoginTime(System.currentTimeMillis());
				onlineUser.setUid(currentAccount.getLoginName());
				onlineUser.setAccountId(currentAccount.getId().toString());
				onlineUser.setValid(true);
				onlineUser.setSessionId(currentAccount.getSessionId());
				onlineUser.setAppSn(client_id);
			}
		}
		if (onlineUser != null) {
			String accountId = request.getParameter("tokenId");
			if (accountId == null) {// 是否指定单点登陆账号
				// 查询用户对应账号并判断用户是否存在多账号
				Account account = new Account();
				account.setAppSn(client_id);
				account.setUserId(currentAccount.getUserId());
				account.setStatus(Constants.ACCOUNT_ENABLED);// 进行OAUTH的帐号必须是启用帐号
				account.setIsControl(false);
				List<Account> list = accountService.findList(account);
				if (list == null || list.size() == 0) {
					log.info("当前应用ID:" + client_id + ",当前用户ID:" + currentAccount.getUserId() + ",账号数量：" + list.size());
					try {
						//request.setAttribute("error", "当前用户当前应用无账号：" + client_id + "," + currentAccount.getName());
						request.setAttribute("code",404);
						request.setAttribute("msg","当前用户当前应用无账号：" + client_id + "," + currentAccount.getName());
						return "message";
					} catch (Exception e) {
						log.error("forward request error", e);
					}
				} else if (list.size() > 1) {
					try {
						String uri=GatewayHttpUtil.getKey("RemoteServer", request)+"/sso/oauth2/authorize?"+request.getQueryString();
						request.setAttribute("redirectUri", uri);
						request.setAttribute("accts", list);
						return "acctsel";
					} catch (Exception e) {
						log.error("forward request error", e);
					}
				} else {
					account = (Account) list.get(0);
					accountId = account.getId() + "";
				}
			}
			onlineUser.setAccountId(accountId);
			// 生成AccessToken
			Token token = accessTokenCache.grantToken(onlineUser);
			token.setClientId(client_id);
			if (redirect_uri.indexOf("?") != -1) {
				String str = redirect_uri.substring(redirect_uri.indexOf("?"), redirect_uri.length());
				if (!"?".equals(str)) {
					redirect_uri += "&";
				}
			} else {
				redirect_uri += "?";
			}
			redirect_uri += "code=" + token.getId()+"&client_id="+client_id+"&scope="+scope+"&response_type="+response_type+"&state="+state;
			//记录oauth单点登陆日志
			writeLog("code:"+token.getId()+",重定向地址："+redirect_uri, onlineUser,Constants.SSO_OAUTH);
			return "redirect:"+redirect_uri; 
		} else {
			//为了登录页面账号密码登录后可以跳转回这个方法里
			String uri=GatewayHttpUtil.getKey("RemoteServer", request)+"/sso/oauth2/authorize?"+request.getQueryString();
			log.info("oauth2重定向地址："+uri);
			String redirects=GatewayHttpUtil.getKey("RemoteServer", request)+"/portal/login.html?redirectUri="+URLUtil.encodeAll(uri);
			return "redirect:"+redirects; 
		}
	}

	
	/**
	 * 根据code获取access_token
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("token")
	@ResponseBody
	public String token(HttpServletRequest request, HttpServletResponse response) {

		log.info("2:"+request.getQueryString());
		
		String code = request.getParameter("code");
		String client_id = request.getParameter("client_id")==null?request.getHeader("client_id"):request.getParameter("client_id");
		
		String client_secret = request.getParameter("client_secret")==null?request.getHeader("client_secret"):request.getParameter("client_secret");
		 String grant_type = request.getParameter("grant_type")==null?request.getHeader("grant_type"):request.getParameter("grant_type");
		// String redirectUrl = request.getParameter("redirect_uri");
//		log.info("code:"+code+",client_id:"+client_id+",client_secret:"+client_secret+",grant_type:"+grant_type);
		Oauth oauth = ssoConfigCache.getOauthConfig(client_id);
		
		if (oauth == null || !client_secret.equals(oauth.getSecretKey())) {
			return "{\"error\":" + AuthResultCode.ACCESS_CHECK_ERROR + ",\"error_description\":\"client_id or client_secret is error\"}";
		}
		if (StringUtils.isEmpty(code)) {
			return "{\"ret\":-1,\"error\":" + AuthResultCode.ACCESS_CHECK_ERROR + ",\"error_description\":\"code error\"}";
		}
		Token codeToken = accessTokenCache.getToken(code);
		String ssoDebugStart=sysConfigCache1.getValue("sso.debug.start");
		if (codeToken != null) {
			codeToken.setId(StringUtils.getUuid());// 重置在线用户tokenId
			OnlineUser onlineUser=(OnlineUser) codeToken.getContent();
			onlineUserCache.put(codeToken.getId(), onlineUser);
			JSONObject json = new JSONObject();
			json.put("access_token", codeToken.getId());
			json.put("token_type", "bearer");
			json.put("expires_in", codeToken.getExpried());
			json.put("refresh_token", codeToken.getId());
			json.put("scope", "read");
			if(!StringUtils.isEmpty(ssoDebugStart)&&ssoDebugStart.equals("true")) {
				writeLog("返回结果："+json.toString(), onlineUser,Constants.SSO_OAUTH);
			}
			return json.toString();
		} else {
			if(!StringUtils.isEmpty(ssoDebugStart)&&ssoDebugStart.equals("true")) {
				writeLog("code:"+code+",其他参数："+request.getQueryString(),null,Constants.SSO_OAUTH);
			}
			return "{\"error\":" + AuthResultCode.ACCESS_CHECK_ERROR + ",\"error_description\":\"code is error\"}";
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
		String ssoDebugStart=sysConfigCache1.getValue("sso.debug.start");
		Map resultMap = new HashMap();
		// 从缓存中获取用户基础信息
		OnlineUser onlineUser = onlineUserCache.get(access_token);
		if (onlineUser == null) {
			resultMap.put("ret", -1);
			resultMap.put("msg", "invalid access_token");
			if(!StringUtils.isEmpty(ssoDebugStart)&&ssoDebugStart.equals("true")) {
				writeLog("access_token:"+access_token+",其他参数："+request.getQueryString()+",invalid access_token",null,Constants.SSO_OAUTH);
			}
			return JSONObject.fromObject(resultMap).toString();
		}
		// 从缓存中读取引用密钥，校验密钥是否正确
		Oauth oauth = ssoConfigCache.getOauthConfig(onlineUser.getAppSn());
		if (oauth == null) {
			resultMap.put("ret", -1);
			resultMap.put("msg", "client secretkey is invalid");
			if(!StringUtils.isEmpty(ssoDebugStart)&&ssoDebugStart.equals("true")) {
				writeLog("access_token:"+access_token+",其他参数："+request.getQueryString()+",client secretkey is invalid",onlineUser,Constants.SSO_OAUTH);
			}
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
		if(!StringUtils.isEmpty(ssoDebugStart)&&ssoDebugStart.equals("true")) {
			writeLog("access_token:"+access_token+",返回结果："+resultMap.toString(),onlineUser,Constants.SSO_OAUTH);
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
