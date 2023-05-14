package com.sense.iam.sso;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sense.iam.config.RedisCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.sense.core.util.CurrentAccount;
import com.sense.core.util.GatewayHttpUtil;
import com.sense.core.util.JWTUtil;
import com.sense.core.util.StringUtils;
import com.sense.iam.cache.CompanyCache;
import com.sense.iam.cache.SysConfigCache1;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.auth.OnlineUser;
import com.sense.iam.model.im.Account;
import com.sense.iam.service.AccountService;
import com.sense.iam.sso.action.BaseAction;
import com.sense.iam.sso.action.BeiSenSSOAction;
import com.sense.iam.sso.action.Cas3Action;
import com.sense.iam.sso.action.FxiaokeSSOAction;
import com.sense.iam.sso.action.LtpaTokenAction;
import com.sense.iam.sso.action.Oauth2Action;
import com.sense.iam.sso.action.Oauth2Action2;
import com.sense.iam.sso.action.Oidc1Action;
import com.sense.iam.sso.action.Oidc1WasAction;
import com.sense.iam.sso.action.SSOAction;
import com.sense.iam.sso.action.SamlAction;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Component
public class LoginInterceptor extends HandlerInterceptorAdapter{

	private Log log=LogFactory.getLog(getClass());

	@Resource
	private CompanyCache companyCache;
	@Resource
	private SysConfigCache1 sysConfigCache1;
	@Resource
	private AccountService accountService;
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
						   ModelAndView modelAndView) throws Exception {
		CurrentAccount.setCurrentAccount(null);
		super.postHandle(request, response, handler, modelAndView);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		/*log.info(request.getRequestURI());
		log.info(request.getRequestURL());*/
		if(!(handler instanceof HandlerMethod) || !((HandlerMethod)handler).getBean().getClass().getName().startsWith("com.sense")){
			return super.preHandle(request, response, handler);
		}

		log.info("sessionId="+GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request));
		CurrentAccount account = getCurrentAccount(request,Constants.CURRENT_SSO_SESSION_ID,response);
		if(account != null){
			CurrentAccount.setCurrentAccount(account);
		}
		HandlerMethod method=(HandlerMethod)handler;
		log.debug("execute method "+method.getBean().getClass()+":"+method.getMethod().getName());

		if(method.getBean().getClass().equals(FxiaokeSSOAction.class))return super.preHandle(request, response, handler);
		if(method.getBean().getClass().equals(SSOAction.class))return super.preHandle(request, response, handler);
		if(method.getBean().getClass().equals(BeiSenSSOAction.class))return super.preHandle(request, response, handler);
		if(method.getBean().getClass().equals(Oidc1Action.class))return super.preHandle(request, response, handler);
		if(method.getBean().getClass().equals(Oauth2Action.class))return super.preHandle(request, response, handler);
		if(method.getBean().getClass().equals(Oauth2Action2.class))return super.preHandle(request, response, handler);
		if(method.getBean().getClass().equals(Cas3Action.class))return super.preHandle(request, response, handler);
		if(method.getBean().getClass().equals(SamlAction.class))return super.preHandle(request, response, handler);
		if(method.getBean().getClass().equals(LtpaTokenAction.class))return super.preHandle(request, response, handler);
		if(method.getBean().getClass().equals(Oidc1WasAction.class))return super.preHandle(request, response, handler);

		if(GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request)==null){
			response.setStatus(403);
			response.getOutputStream().write("{\"code\":\"403\",\"msg\":\"session not exist \"}".getBytes());
			return false;
		}
		if(account==null){
			response.setStatus(403);
			response.getOutputStream().write("{\"code\":\"403\",\"msg\":\"sso account not exist \"}".getBytes());
			return false;
		}
		return super.preHandle(request, response, handler);
	}

	@Resource
	private RedisCache redisCache;

	public CurrentAccount getCurrentAccount(HttpServletRequest request, String sessionId, HttpServletResponse response){
		CurrentAccount account=null;
		String jwtToken = GatewayHttpUtil.getKey("jwtToken", request);
		sessionId = GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request);
		String rediesKey=Constants.CURRENT_REDIS_SESSION_ID+":"+sessionId;
		String senseToken =redisCache.getCacheObject(rediesKey);

		if(senseToken!=null){
			//解密Token
			try{
				senseToken=JWTUtil.parseToken(senseToken, Constants.JWT_SECRECTKEY);
				log.info("parseToken senseToken="+senseToken);
				if(senseToken!=null){
					JSONObject pKjson=JSONObject.fromObject(senseToken);
					//获取企业域
					account = new CurrentAccount(companyCache.getCompanySn(GatewayHttpUtil.getKey("RemoteHost", request)));
					account.setSessionId(sessionId);
					account.setId(Long.valueOf(pKjson.getString("accountId")));
					account.setUserId(Long.valueOf(pKjson.getString("userId")));
					account.setLoginName(pKjson.getString("loginName"));
					if(pKjson.containsKey("destSrc")){
						account.setDestSrc(JSONArray.toList(pKjson.getJSONArray("destSrc")));
					}
					//判断是否需要修改密码
					if(pKjson.containsKey("validataPwd")){
						if(pKjson.getInt("validataPwd")==1){
							account.setValid(true);
						}else{
							account.setValid(false);
						}
					}else{
						account.setValid(false);
					}
					account.setLastLoginTime(System.currentTimeMillis());
					account.setRemoteHost(GatewayHttpUtil.getKey("RemoteHost", request));
				}
			}catch(Exception e){
				e.printStackTrace();
			}

		}else if(!StringUtils.isEmpty(jwtToken)){
			//解密Token
			try{
				jwtToken=JWTUtil.parseToken(jwtToken, Constants.JWT_SECRECTKEY);
				log.info("parseToken jwtToken="+jwtToken);
				if(jwtToken!=null){
					JSONObject pKjson=JSONObject.fromObject(jwtToken);
					//获取企业域
					account = new CurrentAccount(companyCache.getCompanySn(GatewayHttpUtil.getKey("RemoteHost", request)));
					account.setSessionId(sessionId);
					account.setId(Long.valueOf(pKjson.getString("accountId")));
					account.setUserId(Long.valueOf(pKjson.getString("userId")));
					account.setLoginName(pKjson.getString("loginName"));
					if(pKjson.containsKey("destSrc")){
						account.setDestSrc(JSONArray.toList(pKjson.getJSONArray("destSrc")));
					}
					//判断是否需要修改密码
					if(pKjson.containsKey("validataPwd")){
						if(pKjson.getInt("validataPwd")==1){
							account.setValid(true);
						}else{
							account.setValid(false);
						}
					}else{
						account.setValid(false);
					}
					account.setLastLoginTime(System.currentTimeMillis());
					account.setRemoteHost(GatewayHttpUtil.getKey("RemoteHost", request));
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}else {
			//获取header参数，
			String nameField=sysConfigCache1.getValue("header.user.name");
			log.info("获取到的头部KEY:"+nameField);
			if(!StringUtils.isEmpty(nameField)) {
				String headerUserName=GatewayHttpUtil.getKey(nameField, request);
				log.info("获取到的头部值："+headerUserName);
				if(!StringUtils.isEmpty(headerUserName)) {
					String headerAppSn=StringUtils.isEmpty(sysConfigCache1.getValue("header.app.sn"))?"APP001":sysConfigCache1.getValue("header.app.sn");
					//查询账号是否存在
					// 查询用户对应账号并判断用户是否存在多账号
					Account imAccount = new Account();
					imAccount.setAppSn(headerAppSn);
					imAccount.setLoginName(headerUserName);
					imAccount.setStatus(Constants.ACCOUNT_ENABLED);// 进行OAUTH的帐号必须是启用帐号
					imAccount.setIsControl(false);
					List<Account> list = accountService.findList(imAccount);
					if(list!=null&&list.size()>0) {
						//获取企业域
						account = new CurrentAccount(companyCache.getCompanySn(GatewayHttpUtil.getKey("RemoteHost", request)));
						account.setSessionId(sessionId);
						account.setId(list.get(0).getId());
						account.setUserId(list.get(0).getUserId());
						account.setLoginName(headerUserName);
						account.setValid(true);
						account.setLastLoginTime(System.currentTimeMillis());
						account.setRemoteHost(GatewayHttpUtil.getKey("RemoteHost", request));
					}
				}
			}
		}
		//根据session从redis中加载
		return account;
	}

	public boolean isAllowAccess(CurrentAccount account,HandlerMethod method){
		return true;
	}

	public static void main(String[] args) {
		String sm="{\"destSrc\":[\"123321\",\"dddd\"]}";

		System.out.println(JSONArray.toList(JSONObject.fromObject(sm).getJSONArray("destSrc")).contains("123321"));
	}
}
