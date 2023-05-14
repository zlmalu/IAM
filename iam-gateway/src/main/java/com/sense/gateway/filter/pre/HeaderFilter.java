package com.sense.gateway.filter.pre;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jcifs.UniAddress;
import jcifs.http.NtlmSsp;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.sense.core.security.UIM;
import com.sense.core.util.GatewayHttpUtil;
import com.sense.gateway.filter.HttpPreFilter;
import com.sense.gateway.util.TokenManage;
import com.sense.iam.cache.SessionJoinCache;
import com.sense.iam.cache.SysConfigCache;
import com.sense.iam.cam.Constants;

/**
 * 用户请求头信息过滤
 *
 * Description:  主要过滤用户请求的真实IP和token
 *
 * @author w_jfwen
 *
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */
@Component("headerPreFilter")
public class HeaderFilter  extends HttpPreFilter{

	private static Log log=LogFactory.getLog(HeaderFilter.class);



	private String getUserSessionFlag(HttpServletRequest request,String id){
		String newSession=request.getSession().getId();
		String path=request.getRequestURL().toString();

		if(path.contains("/sso/")){
			Cookie[] cookies = request.getCookies();
			if(cookies != null && cookies.length > 0){
				for (Cookie cookie : cookies){
					if(cookie.getName().equals(Constants.CURRENT_SSO_SESSION_ID) && !newSession.equals(cookie.getValue())){
						newSession = cookie.getValue();
						request.getSession().setAttribute("id",newSession);
					}
				}
			}
		}


		String ssoToken=GatewayHttpUtil.getParameterForHtml("ssoToken", request);
		if(ssoToken!=null&&ssoToken.trim().length()>0){
			ssoToken=UIM.decode(ssoToken);
			String oidsessionId=ssoToken.split("_")[2];
			SessionJoinCache.update(oidsessionId, newSession);
		}
		if(SessionJoinCache.getSession(request.getSession().getId())!=null){
			return SessionJoinCache.getSession(request.getSession().getId());
		}

		if(request.getParameter(id)==null){
			if(request.getHeader(id)==null){
				return request.getSession().getId();
			}else{
				//两个会话不相等情况下，做会话转移
				if(newSession!=request.getHeader(id)){
					TokenManage.buiIs(newSession, request.getHeader(id), path);
				}
				return request.getHeader(id);
			}
		}else{
			//两个会话不相等情况下，做会话转移
			if(newSession!=request.getParameter(id)){
				TokenManage.buiIs(newSession, request.getParameter(id), path);
			}
			return request.getParameter(id);
		}
	}

	public static String sessionId;

	@Override
	public Object run() throws ZuulException {
		RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();
        HttpServletResponse response = currentContext.getResponse();
        String path=request.getRequestURL().toString();
	    String httpsd=request.getScheme();
	    if(SysConfigCache.HTTPS_ENABLED.intValue()==1){
		   httpsd="https";
	    }
	    if(path!=null &&(
	    		path.indexOf(".action")!=-1 ||
	    		path.indexOf(".html")!=-1)
	    		){
	    	 log.info("PathInfo="+request.getRequestURI());
	    	 String url=request.getRequestURI();
	         //Shiro框架鉴权绕过,主要针对URL进行特殊字符注入
	         if(url!=null && (url.indexOf(";")!=-1 || url.indexOf("%3")!=-1)){
	        	currentContext.setResponseStatusCode(403);
	        	//不进行路由。
	         	currentContext.setSendZuulResponse(false);
	         	return null;
	         }
	    }

	    String host=request.getHeader("Host");
    	String remoteServer=httpsd+"://"+request.getHeader("Host");
        String sessionId=getUserSessionFlag(request,Constants.CURRENT_SSO_SESSION_ID);
        this.sessionId=sessionId;
	    if(!path.contains("/portal/testing.action")){
	    	//重新设置session会话过期时间
	    	 request.getSession().setMaxInactiveInterval(Integer.valueOf(SysConfigCache.SESSION_TIMEOUT+"")/1000);
	    	 if(path.contains(".action") ||path.contains(".html")){
		        log.info("--------------------------------------");
		        log.info("request Host:"+request.getHeader("Host"));
		        log.info("host:"+host);
		        log.info("RemoteHost:"+remoteServer);
		        log.info("sessionId="+sessionId);;
		        log.info("--------------------------------------");
		        log.info("");
	    	 }
	    }
	    //AD域名单点
        if(path.contains("/portal/")){
        	boolean isTrue= authAd(request, sessionId, response);
        	if(!isTrue){
	        	currentContext.setResponseStatusCode(401);
	        	//不进行路由。
	         	currentContext.setSendZuulResponse(false);
	         	return null;
        	}
        }
        currentContext.addZuulRequestHeader("RemoteServer", remoteServer);
        currentContext.addZuulRequestHeader("RemoteIp", getClientIp(request));
        currentContext.addZuulRequestHeader("RemoteHost", host);
        String senseToken=TokenManage.getToken(sessionId,path);
        if(senseToken!=null){
            currentContext.addZuulRequestHeader("senseToken", senseToken);
        }
        currentContext.addZuulRequestHeader(Constants.CURRENT_SESSION_ID, getUserSessionFlag(request,Constants.CURRENT_SESSION_ID));
        currentContext.addZuulRequestHeader(Constants.CURRENT_SSO_SESSION_ID, getUserSessionFlag(request,Constants.CURRENT_SSO_SESSION_ID));

		return null;
	}

	/**
	 * 构建门户AD自带单点登录会话
	 * @param request
	 * @param sessionId
	 * @param response
	 * @return true认证成功，false认证失败
	 */
	public static boolean authAd(HttpServletRequest request,String sessionId,HttpServletResponse response){
		//判断是否启用AD与域认证
	    if(SysConfigCache.AD_AUTH_ENABLED.equals("1")){
	    	String path=request.getRequestURL().toString();
		    String host=request.getHeader("Host");
	    	if(TokenManage.getToken(sessionId, path)==null){
		    	String remoteAdUser=null;
		    	String msg = request.getHeader("Authorization");
		    	if(msg != null && msg.startsWith("NTLM ")){
		    		log.info(msg);
		    		 try{
			    		 UniAddress dc = UniAddress.getByName(SysConfigCache.AD_AUTH_IP, true);
			    		 byte challenge[] = SmbSession.getChallenge(dc);
			    		 NtlmPasswordAuthentication ntlm=NtlmSsp.authenticate(request, response, challenge);
			    		 remoteAdUser=ntlm.getName();
			    		 if(remoteAdUser.indexOf("\\")>0){
			    			 remoteAdUser=remoteAdUser.substring(remoteAdUser.indexOf("\\")+1,remoteAdUser.length());
			    		 }
		    		 }catch(Exception e){

		    		 }
		    	}
			    log.info("remoteAdUser="+remoteAdUser);
			    if(remoteAdUser!=null){
			    	TokenManage.authADConversationBuild(sessionId, remoteAdUser,getClientIp(request),host);
			    	return true;
			    }
	    	}
	    	//无法登录情况下返回false,让不进行路由，拒绝访问
	    	return false;
	    }else{
	    	return true;
	    }
	}
	public static String getClientIp(HttpServletRequest request){
		String ip = request.getHeader("x-forwarded-for");
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	      ip = request.getHeader("Proxy-Client-IP");
	    }
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	      ip = request.getHeader("WL-Proxy-Client-IP");
	    }
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	      ip = request.getHeader("HTTP_CLIENT_IP");
	    }
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	      ip = request.getHeader("HTTP_X_FORWARDED_FOR");
	    }
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
		      ip = request.getParameter("remoteIP");
		    }
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	      ip = request.getRemoteAddr();
	    }
	    return ip;
	}

}
