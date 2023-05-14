package com.sense.iam.api;


import java.lang.annotation.Annotation;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import springfox.documentation.annotations.ApiIgnore;

import com.sense.core.util.CurrentAccount;
import com.sense.core.util.GatewayHttpUtil;
import com.sense.iam.api.action.ImageAction;
import com.sense.iam.auth.LoginAction;
import com.sense.iam.cache.CompanyCache;
import com.sense.iam.cam.Constants;

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
		HandlerMethod method=(HandlerMethod)handler;
		//System.out.println(companyCache);
		log.debug("execute method "+method.getBean().getClass()+":"+method.getMethod().getName());
		log.info("sessionId="+GatewayHttpUtil.getKey(Constants.CURRENT_SESSION_ID, request));
		if(method.getBean().getClass().equals(SamlServlet.class))return super.preHandle(request, response, handler);
		CurrentAccount account=getCurrentAccount(request,Constants.CURRENT_SESSION_ID);

		if(GatewayHttpUtil.getKey(Constants.CURRENT_SESSION_ID, request)==null){
			response.setStatus(403);
			response.getOutputStream().write("{\"code\":\"403\",\"msg\":\"token not exist \"}".getBytes());
			return false;
		}
		if(account==null){//设置登陆来源
			//获取企业域
			String company=companyCache.getCompanySn(GatewayHttpUtil.getKey("RemoteHost", request));
			account=new CurrentAccount(company);
			account.setSessionId(GatewayHttpUtil.getKey(Constants.CURRENT_SESSION_ID, request));
		}
		log.debug(GatewayHttpUtil.getKey("RemoteHost", request));
		log.debug("current use domain::::::"+account.getCompanySn());
		if(account.getCompanySn()==null){
			response.setStatus(403);
			response.getOutputStream().write("{\"code\":\"403\",\"msg\":\"company not exist \"}".getBytes());
			return false;
		}
		CurrentAccount.setCurrentAccount(account);
		//设置开放接口过滤当前前缀包路径所有action
		if((method.getBean().getClass().toString().contains("com.sense.iam.open.action"))){
			return super.preHandle(request, response, handler);
		}

		if((method.getBean().getClass()==LoginAction.class && (method.getMethod().getName().equals("login") || method.getMethod().getName().equals("ssoLogin")||method.getMethod().getName().equals("getverificationCode")|| method.getMethod().getName().equals("logout")|| method.getMethod().getName().equals("getToken")))
				|| method.getBean().getClass()==ImageAction.class){
			return super.preHandle(request, response, handler);
		}else{
			if(account!=null  && account.isValid() && isAllowAccess(account,method)){
				return super.preHandle(request, response, handler);
			}else{
				//response.sendRedirect(request.getContextPath()+"/index.html");
				response.setStatus(403);
				response.getOutputStream().write("{\"code\":\"403\",\"msg\":\"no allow access\"}".getBytes());
				return false;
			}
		}
	}


	public CurrentAccount getCurrentAccount(HttpServletRequest request,String sessionId){
		//根据session从redis中加载
		return SessionManager.getSession(GatewayHttpUtil.getKey(sessionId, request),request);
	}

	public boolean isAllowAccess(CurrentAccount account,HandlerMethod method){
		return true;
	}

}
