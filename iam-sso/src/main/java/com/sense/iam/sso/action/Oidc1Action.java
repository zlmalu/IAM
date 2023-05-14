package com.sense.iam.sso.action;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.text.StringEscapeUtils;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.keys.RsaKeyUtil;
import org.jose4j.lang.JoseException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.sense.core.util.CurrentAccount;
import com.sense.core.util.GatewayHttpUtil;
import com.sense.core.util.StringUtils;
import com.sense.iam.cache.CompanyCache;
import com.sense.iam.cache.SsoConfigCache;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.auth.AuthResultCode;
import com.sense.iam.cam.auth.OnlineUser;
import com.sense.iam.cam.auth.Token;
import com.sense.iam.cam.auth.cache.AccessTokenCache;
import com.sense.iam.cam.auth.cache.OnlineUserCache;
import com.sense.iam.model.im.Account;
import com.sense.iam.model.sso.Oidc;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.JdbcService;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.URLUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import net.sf.json.JSONObject;

/**
 * 
 * oidc用户认证协议
 * 
 * Description: 调用路径加入前缀：/oidc
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Controller
@RequestMapping("oidc1")
public class Oidc1Action extends BaseAction {
	@Resource
	private AccessTokenCache accessTokenCache;
	@Resource
	private SsoConfigCache  ssoConfigCache;
	@Resource
	private OnlineUserCache onlineUserCache;
	@Resource
	private AccountService accountService;
	@Resource
	private StringRedisTemplate stringRedisTemplate;
	@Resource
	private JdbcService jdbcService;
	@Resource
	private CompanyCache companyCache;
	/**
	 * 认证跳转验证 
	 * @param response_type  采用code代表授权模式
	 * @param client_id 使用的客户端id（应用标识）
	 * @param redirect_uri 回调路径
	 * @param scope openid
	 */
	@RequestMapping(value="{client_id}/authorize", method = {RequestMethod.GET,RequestMethod.POST})
	public void authorize(@PathVariable String client_id,HttpServletRequest request,HttpServletResponse response){
		String redirect_uri = GatewayHttpUtil.getParameterForHtml("redirect_uri",request);
		if (iSAuthenticated()) {
			String response_type = request.getParameter("response_type")==null?"code":request.getParameter("response_type");
			 log.info("oidc-1："+request.getQueryString());
			 if(!"code".equals(response_type)) {
				 log.info("oidc-1返回结果:redirect_ur234234i非法："+response_type);
				// 返回403没有权限
				request.setAttribute("error", "目前仅支持授权码模式");
				super.print403(response);
				return;
			 }
//			String scope = request.getParameter("scope")==null?"openid profile":request.getParameter("scope");//暂时用不到
			if(client_id==null){
				client_id = request.getParameter("client_id");
			}
			String state = request.getParameter("state");
			String nonce=request.getParameter("nonce");
			Oidc oidc = ssoConfigCache.getOidcConfig(client_id);
			if (client_id == null || oidc == null) {
				// 返回403没有权限
				log.info("oidc-1返回结果:redirec3333t_uri非法："+redirect_uri+","+client_id);
				request.setAttribute("error", "应用未注册OIDC认证服务(client_id为空)");
				super.print403(response);
				return;
			}
	
			
			//判断是否有重定向地址，没有的话设置SSO配置中的地址
			if (StringUtils.isEmpty(redirect_uri) || redirect_uri.equals("null")) {
				//获取默认地址
				String defUrl=oidc.getDefaultUrl();
				if(StringUtils.isEmpty(defUrl)||StringUtils.isEmpty(defUrl.trim())){
					// 返回403没有权限
					log.info("oidc-1返回结果:redirect_222uri非法："+redirect_uri);
					request.setAttribute("error", "回调地址未配置");
					super.print403( response);
					return;
				}else{
					redirect_uri = defUrl;
				}
			}else{
				//验证回调url
				//获取默认地址
				String defUrl=oidc.getDefaultUrl();
				if(StringUtils.isEmpty(defUrl)||StringUtils.isEmpty(defUrl.trim())){
					// 返回403没有权限
					log.info("oidc-1返回结果:redirect_uri111非法："+redirect_uri);
					request.setAttribute("error", "回调地址未配置");
					super.print403( response);
					return;
				}
				
				try {
					URL redirectHost = new URL(redirect_uri);
					URL configHost = new URL(defUrl);
					if(!redirectHost.getHost().equals(configHost.getHost())){
						log.info("oidc-1返回结果:redirect_uri非法："+redirect_uri);
						// 返回403没有权限
						request.setAttribute("error", "redirect_uri非法");
						super.print403(response);
						return;
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
	
			CurrentAccount currentAccount = CurrentAccount.getCurrentAccount();
			OnlineUser onlineUser = new OnlineUser();
			onlineUser.setExpried(System.currentTimeMillis() + oidc.getValidTime());
			onlineUser.setLoginIp(currentAccount.getRemoteHost());
			onlineUser.setLoginTime(System.currentTimeMillis());
			onlineUser.setUid(currentAccount.getLoginName());
			onlineUser.setAccountId(currentAccount.getId().toString());
			onlineUser.setValid(true);
			onlineUser.setSessionId(currentAccount.getSessionId());
			onlineUser.setAppSn(client_id);
			String accountId = GatewayHttpUtil.getKey("tokenId", request);
			if (accountId == null) {// 是否指定单点登陆账号
				// 查询用户对应账号并判断用户是否存在多账号
				Account account = new Account();
				account.setAppSn(client_id);
				account.setUserId(currentAccount.getUserId());
				account.setStatus(Constants.ACCOUNT_ENABLED);// 帐号必须是启用帐号
				account.setIsControl(false);
				List<Account> list=accountService.findList(account);
				int count=list.size();
				if (count == 0) {
					log.info("当前应用ID:" + client_id + ",当前用户ID:" + currentAccount.getUserId() + ",账号数量：" + list.size());
					try {
						request.setAttribute("error", "当前用户当前应用无账号：" + client_id + "," + currentAccount.getName());
						// 如果当前应用指定了无账号跳转页面则跳转指定，否则默认403
						super.print403(response);
						return;
					} catch (Exception e) {
						log.error("forward request error", e);
					}
				} else if (count > 1) {
					try {
						JSONObject result=new JSONObject();
						result.put("error", "-1");
						result.put("error_description","account num:"+list.size()+"");
						super.printERROR(result.toString(),response);
						return;
					} catch (Exception e) {
						log.error("forward request error", e);
					}
				} else {
					account = (Account) list.get(0);
					accountId = account.getId() + "";
				}
			}
			onlineUser.setAccountId(accountId);
			onlineUser.setNonce(nonce);
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
			System.out.println("code1:"+token.getId());
			redirect_uri += "code=" + token.getId()+"&state="+state+"&nonce="+nonce+"&r="+StringUtils.getSecureRandomnNumber();
			//记录oidc单点登陆日志
			com.sense.iam.model.sso.Log ssoLog=new com.sense.iam.model.sso.Log();
			ssoLog.setUserName(onlineUser.getUid());
			ssoLog.setAccountId(Long.valueOf(accountId));
			ssoLog.setSsoType(Constants.SSO_OIDC);
			ssoLog.setCompanySn(CurrentAccount.getCurrentAccount().getCompanySn());
			queueSender.send(ssoLog);
		} else {
			//为了登录页面账号密码登录后可以跳转回这个方法里
			String uri=GatewayHttpUtil.getKey("RemoteServer", request)+"/sso/oidc1/"+client_id+"/authorize?"+request.getQueryString()+"&r="+StringUtils.getSecureRandomnNumber();
			log.info("oidc重定向地址："+uri);
			String redirects=GatewayHttpUtil.getKey("RemoteServer", request)+"/portal/login.html?redirectUri="+URLUtil.encodeAll(uri)+"&r="+StringUtils.getSecureRandomnNumber();
			super.sendRedirect(redirects, response);
			return;
		}
		log.info("oidc-1重定向地址："+redirect_uri);
		super.sendRedirect(redirect_uri, response);
		return;
	}
	private Map<String, String> getHeadersInfo(HttpServletRequest request) {
        Map<String, String> map = new HashMap<String, String>();
        Enumeration<?> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }
        return map;
    }
	/**
	 * 根据code获取access_token
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value="{client_id}/token",produces="application/json")
	@ResponseBody
	public Object token(@PathVariable String client_id,HttpServletRequest request, HttpServletResponse response) {
		client_id=StringEscapeUtils.escapeHtml4(client_id);
		
		response.setContentType("application/json");
		log.info("oidc_token-header参数"+getHeadersInfo(request).toString());
		//		log.info("2:"+request.getQueryString());
//		StringBuffer url = request.getRequestURL();
//		String tempContextUrl = url.delete(url.length() - request.getRequestURI().length(), url.length()).append("/").toString();
		String code = request.getParameter("code");
		String client_secret = request.getParameter("client_secret")==null?request.getHeader("client_secret"):request.getParameter("client_secret");
		String grant_type = request.getParameter("grant_type")==null?request.getHeader("grant_type"):request.getParameter("grant_type");
		 
		code=StringEscapeUtils.escapeHtml4(code);
		client_secret=StringEscapeUtils.escapeHtml4(client_secret);
		grant_type=StringEscapeUtils.escapeHtml4(grant_type);
		if("client_credentials".equals(grant_type)) {
			 //这个client_credentials是oauth客户端模式，不支持，直接返回错误信息
			log.info("oidc-token返回结果1");
			return "{\"ret\":-1,\"error\":" + AuthResultCode.ACCESS_CHECK_ERROR + ",\"error_description\":\"client_id or client_secret is error\"}";
		 }
		// String redirectUrl = request.getParameter("redirect_uri");
		log.info("oidc-token:"+request.getQueryString());
		Oidc oidc = ssoConfigCache.getOidcConfig(client_id);
		if (oidc == null || !client_secret.equals(oidc.getClientSecret())) {
			log.info("oidc-token返回结果1");
			return "{\"ret\":-1,\"error\":" + AuthResultCode.ACCESS_CHECK_ERROR + ",\"error_description\":\"client_id or client_secret is error\"}";
		}
		if (StringUtils.isEmpty(code)) {
			log.info("oidc-token返回结果2");
			return "{\"ret\":-1,\"error\":" + AuthResultCode.ACCESS_CHECK_ERROR + ",\"error_description\":\"code error\"}";
		}
		System.out.println("code2:"+code);
		Token codeToken = accessTokenCache.grantAccessToken(code);
		if (codeToken != null) {
			OnlineUser onlineUser=(OnlineUser) codeToken.getContent();
			onlineUserCache.put(codeToken.getId(), onlineUser);
			JSONObject json = new JSONObject();
			json.put("access_token", codeToken.getId());
			
			Long vtime=oidc.getValidTime();
			long exp=DateUtil.offsetSecond(new Date(),vtime.intValue()).getTime();//向后偏移指定秒
			//这个地方uid不一定是登录名，不支持多账号,先这样吧
			String id_token=genrateIdToken(client_id, onlineUser.getUid(), exp, oidc.getJwksAll(),GatewayHttpUtil.getKey("RemoteServer", request)+"/sso/oidc1/"+client_id,onlineUser.getNonce());
			json.put("id_token", id_token);
			json.put("token_type", "bearer");
//			json.put("expires_in", 0);
			json.put("refresh_token", codeToken.getId());
			json.put("scope", "openid profile");
			json.put("jti", client_id);
			log.info("oidc-token返回结果3："+json.toString());
			return json.toString();
		}
		log.info("oidc-token返回结果4");
		return "{\"ret\":-1,\"error\":" + AuthResultCode.ACCESS_CHECK_ERROR + ",\"error_description\":\"code invalid\"}";
	}
	
	/**
	 * 根据access_token获取用户信息
	 * @param request
	 * @param response
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value="{client_id}/userInfo",produces="application/json")
	@ResponseBody
	public String userInfo(@PathVariable String client_id,HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("application/json");
		String isRefresh=request.getParameter("isrefresh");
		String access_token = request.getParameter("access_token");
		access_token=StringEscapeUtils.escapeHtml4(access_token);
		isRefresh=StringEscapeUtils.escapeHtml4(isRefresh);
//		log.info("3:"+request.getQueryString());
//		log.info("header:"+getHeadersInfo(request));
		if(StringUtils.isEmpty(access_token)){
			access_token=request.getHeader("access_token");
		}
		
		if(StringUtils.isEmpty(access_token)){
			String authorization=request.getHeader("Authorization");
			if(!StringUtils.isEmpty(authorization)&&authorization.indexOf("Bearer ")!=-1){
				access_token=authorization.replace("Bearer ", "");
			}
		}
		String result="";
		Map resultMap = new HashMap();
		// 从缓存中获取用户基础信息
		OnlineUser onlineUser = onlineUserCache.get(access_token);
		if (onlineUser == null) {
			resultMap.put("ret", -1);
			resultMap.put("msg", "invalid access_token");
			result=JSONObject.fromObject(resultMap).toString();
			log.info("oidc-userInfo返回结果:"+access_token);
			return result;
		}
		// 从缓存中读取引用密钥，校验密钥是否正确
		Oidc oidc = ssoConfigCache.getOidcConfig(onlineUser.getAppSn());
		if (oidc == null) {
			resultMap.put("ret", -1);
			resultMap.put("msg", "client secretkey is invalid");
			result=JSONObject.fromObject(resultMap).toString();
			log.info("oidc-userInfo返回结果:"+access_token);
			return result;
		}
		if(isRefresh!=null && isRefresh.equals("true")){
			onlineUser.setExpried(System.currentTimeMillis()+oidc.getValidTime());
		}
		resultMap.put("ret", 0);
		resultMap.put("msg", "");
		resultMap.put("uid", onlineUser.getUid());
		resultMap.putAll(super.parseXmlToMap(oidc.getConfig(), onlineUser.getAccountId()));
		result=JSONObject.fromObject(resultMap).toString();
		log.info("oidc-userInfo返回结果:"+access_token);
		return result;
	}
	
	/**
	 * 注销会话
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="logout", method=RequestMethod.GET)
	public void logout(HttpServletRequest request,HttpServletResponse response,HttpSession session){
		List<?> list=jdbcService.findList("select user_name from am_online_user where id='"+session.getId()+"'");
		
		if(list!=null&&list.size()>0){
			net.sf.json.JSONArray data=net.sf.json.JSONArray.fromObject(list);
			String username=data.getJSONObject(0).getString("user_name");
			CurrentAccount.setCurrentAccount(new CurrentAccount(companyCache.getCompanySn(GatewayHttpUtil.getKey("RemoteHost", request))));
			writerLog(username, "logout", Constants.OPT_SUCCESS, "系统退出");
		}
		queueSender.send("delete from am_online_user where id='"+session.getId()+"'");
		stringRedisTemplate.delete(Constants.CURRENT_REDIS_SESSION_ID+":"+request.getSession().getId());
	}
	/**
	 * id_token生成
	 * 
	 * @param appId
	 * @param user
	 * @return
	 */
	private String genrateIdToken(String client_id, String loginName, long exp, String jwks,String iss,String nonce) {
		try {
			// 从jwks中获取秘钥
			JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(jwks);
			PublicJsonWebKey publicJsonWebKey = (RsaJsonWebKey) jsonWebKeySet.findJsonWebKey(client_id, RsaKeyUtil.RSA, Constants.OIDC_USE_SIG, Constants.OIDC_ALG);
			Key key = publicJsonWebKey.getPrivateKey();

			Map<String, Object> headerMap = new HashMap<>();// ID令牌的头部信息
			headerMap.put("typ", "JWT");
			headerMap.put("alg", "RS256");

			Map<String, Object> payloadMap = new HashMap<>();// ID令牌的主体信息
			payloadMap.put("iss", iss);
			payloadMap.put("sub", loginName);
			payloadMap.put("aud", client_id);
			payloadMap.put("exp", exp);//秒
			payloadMap.put("iat", DateUtil.currentSeconds());//秒
			payloadMap.put("jti", publicJsonWebKey.getKeyId());
			payloadMap.put("nonce", nonce);

			return Jwts.builder().setHeaderParams(headerMap).setClaims(payloadMap).signWith(SignatureAlgorithm.RS256, key).compact();
		} catch (JoseException e) {
			e.printStackTrace();
		}
		return null;
	}
	@RequestMapping(value="{client_id}/jwks", method=RequestMethod.GET)
	@ResponseBody
	public Object jwks(@PathVariable String client_id,HttpServletRequest request, HttpServletResponse response) {
		//根据应用ID获取数据库中保存的对应应用的jwks
		client_id=StringEscapeUtils.escapeHtml4(client_id);
		Oidc oidc = ssoConfigCache.getOidcConfig(client_id);
		if(oidc!=null&&!StringUtils.isEmpty(oidc.getJwks())) {
			try{
				return JSONObject.fromObject(oidc.getJwks());
			}catch(Exception e){
				return "{\"ret\":-1,\"error\":" + AuthResultCode.ACCESS_CHECK_ERROR + ",\"error_description\":\"not error\"}";	
			}
		}else {
			return "{\"ret\":-1,\"error\":" + AuthResultCode.ACCESS_CHECK_ERROR + ",\"error_description\":\"not jwks\"}";	
		}
	}
	/**
	 * 配置参数获取
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="{client_id}/.well-known/openid-configuration", method=RequestMethod.GET)
	@ResponseBody
	public JSONObject openidConfiguration(@PathVariable String client_id,HttpServletRequest request, HttpServletResponse response) {
		client_id=StringEscapeUtils.escapeHtml4(client_id);
		JSONObject result =new JSONObject();
		//根据应用ID获取数据库中保存的对应应用的jwks
		Oidc oidc = ssoConfigCache.getOidcConfig(client_id);
		if(oidc.getJwks()==null) {
			result.put("ret", -1);
			result.put("error", AuthResultCode.ACCESS_CHECK_ERROR);
			result.put("error_description", "not jwks");
			return result;	
		}else {
			result.put("issuer", GatewayHttpUtil.getKey("RemoteServer", request)+"/sso/oidc1/"+client_id);//认证前缀
			result.put("authorization_endpoint",GatewayHttpUtil.getKey("RemoteServer", request)+"/sso/oidc1/"+client_id+"/authorize");//认证地址
			result.put("token_endpoint", GatewayHttpUtil.getKey("RemoteServer", request)+"/sso/oidc1/"+client_id+"/token");//获取token地址
			result.put("userinfo_endpoint", GatewayHttpUtil.getKey("RemoteServer", request)+"/sso/oidc1/"+client_id+"/userInfo");//获取用户信息地址
			result.put("jwks_uri", GatewayHttpUtil.getKey("RemoteServer", request)+"/sso/oidc1/"+client_id+"/jwks");//获取加密参数配置地址
			JSONArray rms=new JSONArray();
			rms.add("form_post");
			rms.add("fragment");
			rms.add("query");
			result.put("response_modes_supported",rms);//响应支持的类型，用来指定Authorization Endpoint以何种方式返回数据。
			JSONArray rts=new JSONArray();
			rts.add("code");
	//		rts.add("id_token");
	//		rts.add("id_token token");
	//		rts.add("code id_token");
	//		rts.add("code token");
	//		rts.add("code id_token token");
			result.put("response_types_supported", rts);
			JSONArray gts=new JSONArray();
			gts.add("authorization_code");
			gts.add("implicit");
			gts.add("refresh_token");
	//		gts.add("password");
			result.put("grant_types_supported", gts);
			JSONArray sts=new JSONArray();
			sts.add("public");
			result.put("subject_types_supported", sts);
			JSONArray itsavs=new JSONArray();
			itsavs.add("RS256");
			result.put("id_token_signing_alg_values_supported", itsavs);
			JSONArray ss=new JSONArray();
			ss.add("openid");
			ss.add("profile");
	//		ss.add("email");
	//		ss.add("address");
	//		ss.add("phone");
	//		ss.add("offline_access");
			result.put("scopes_supported", ss);
			JSONArray teams=new JSONArray();
	//		teams.add("client_secret_basic");//oauth客户端模式传参
			teams.add("client_secret_post");//oauth授权码模式
			result.put("token_endpoint_auth_methods_supported", teams);
			JSONArray cs=new JSONArray();
			cs.add("iss");
			cs.add("ver");
			cs.add("sub");
			cs.add("aud");
			cs.add("iat");
			cs.add("exp");
			cs.add("jti");
			//上边七个是必须的
			cs.add("auth_time");//认证时间，可选
	//		cs.add("amr");
	//		cs.add("idp");
			//oidc规范属性
			cs.add("nonce");
			cs.add("name");
	//		cs.add("nickname");
	//		cs.add("preferred_username");
	//		cs.add("given_name");
	//		cs.add("middle_name");
	//		cs.add("family_name");
	//		cs.add("email_verified");
	//		cs.add("profile");
	//		cs.add("zoneinfo");
	//		cs.add("locale");
	//		cs.add("address");
	//		cs.add("phone_number");
	//		cs.add("website");
	//		cs.add("gender");
	//		cs.add("birthdate");
	//		cs.add("updated_at");
	//		cs.add("at_hash");
	//		cs.add("c_hash");
			result.put("claims_supported", cs);
			JSONArray ccms=new JSONArray();
			ccms.add("S256");
			result.put("code_challenge_methods_supported", ccms);
			result.put("introspection_endpoint",  GatewayHttpUtil.getKey("RemoteServer", request)+"/sso/oidc1/"+client_id+"/token/introspect");//获取token地址
			JSONArray ieams=new JSONArray();
			ieams.add("client_secret_basic");
			ieams.add("client_secret_post");
			result.put("introspection_endpoint_auth_methods_supported", ieams);
			result.put("revocation_endpoint",  GatewayHttpUtil.getKey("RemoteServer", request)+"/sso/oidc1/"+client_id+"/token/revoke");//获取token地址
			result.put("revocation_endpoint_auth_methods_supported", ieams);
			result.put("end_session_endpoint",GatewayHttpUtil.getKey("RemoteServer", request)+"/sso/oidc1/"+client_id+"/logout");//获取token地址
			result.put("request_parameter_supported", false);
			JSONArray rosavs=new JSONArray();
	//		rosavs.add("HS256");
	//		rosavs.add("HS384");
	//		rosavs.add("HS512");
			rosavs.add("RS256");
	//		rosavs.add("RS384");
	//		rosavs.add("RS512");
	//		rosavs.add("ES256");
	//		rosavs.add("ES384");
	//		rosavs.add("ES512");
			result.put("request_object_signing_alg_values_supported", rosavs);
			return JSONObject.fromObject(result);
		}
	}
	
}
