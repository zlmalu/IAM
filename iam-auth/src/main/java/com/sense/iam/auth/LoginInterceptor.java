package com.sense.iam.auth;


import javax.annotation.Resource;
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
import com.sense.iam.cache.CompanyCache;
import com.sense.iam.cam.Constants;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Component
public class LoginInterceptor extends HandlerInterceptorAdapter{

	private Log log=LogFactory.getLog(getClass());
	
	@Resource
	private CompanyCache companyCache;
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		CurrentAccount.setCurrentAccount(null);
		super.postHandle(request, response, handler, modelAndView);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if(!(handler instanceof HandlerMethod) || !((HandlerMethod)handler).getBean().getClass().getName().startsWith("com.sense")){
			return super.preHandle(request, response, handler);
		}
		log.debug("sessionId="+GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request));
		CurrentAccount account = getCurrentAccount(request,Constants.CURRENT_SSO_SESSION_ID);
		if(account != null){
			CurrentAccount.setCurrentAccount(account);
		}
		HandlerMethod method=(HandlerMethod)handler;
		log.debug("execute method "+method.getBean().getClass()+":"+method.getMethod().getName());
		return super.preHandle(request, response, handler);
	}

	@Resource
	private RedisCache redisCache;
	
	public CurrentAccount getCurrentAccount(HttpServletRequest request,String sessionId){
		CurrentAccount account = null;
		sessionId = GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request);
		log.info("sessionId="+sessionId);
		String rediesKey=Constants.CURRENT_REDIS_SESSION_ID+":"+sessionId;
		String senseToken =redisCache.getCacheObject(rediesKey);
		log.info("senseToken:"+senseToken);
		if (senseToken != null) {
			// 解密Token
			try {
				account=new CurrentAccount(companyCache.getCompanySn(GatewayHttpUtil.getKey("RemoteHost", request)));
				senseToken = JWTUtil.parseToken(senseToken, Constants.JWT_SECRECTKEY);
				if(senseToken!=null){
					JSONObject pKjson=JSONObject.fromObject(senseToken);
					log.info("pKjson:"+pKjson);
					account.setSessionId(sessionId);
					account.setId(Long.valueOf(pKjson.getString("accountId")));
					account.setUserId(Long.valueOf(pKjson.getString("userId")));
					account.setLoginName(pKjson.getString("loginName"));
					if(pKjson.containsKey("destSrc")){
						log.info("destSrc:"+JSONArray.toList(pKjson.getJSONArray("destSrc")));
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
			
		}
		//根据session从redis中加载
		return account;
	}
	
	public boolean isAllowAccess(CurrentAccount account,HandlerMethod method){
		return true;
	}
	
}
