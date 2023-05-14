package com.sense.iam.auth.controller;

import nl.bitwalker.useragentutils.Browser;
import nl.bitwalker.useragentutils.OperatingSystem;
import nl.bitwalker.useragentutils.UserAgent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.sense.core.queue.QueueSender;
import com.sense.core.util.CurrentAccount;

import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BaseController {
	
	protected Log log=LogFactory.getLog(getClass());
    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;
    
    public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}
	
	/**
	 * 增加基于F5的ip获取配置
	 * @return
	 */
	public String getClientIp(){
	    return getClientIp(request);
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
	
	public static String getClientNetworkInfo(HttpServletRequest request){
		 String ua = request.getHeader("User-Agent");
	        System.out.println("******************************");
	        //转成UserAgent对象
	        UserAgent userAgent = UserAgent.parseUserAgentString(ua);
	        //获取浏览器信息
	        Browser browser = userAgent.getBrowser();
	        //获取系统信息
	        OperatingSystem os = userAgent.getOperatingSystem();
	        //系统名称
	        String system = os.getName();
	        if(system.equals("Windows")){
	        	system = "Windows 10";
	        }
	       
	        System.out.println("系统名称："+system);
	        //浏览器名称
	        String browserName = browser.getName();
	        if(browserName.equals("Mozilla")){
	        	browserName = "Internet Explorer 11";
	        }
	        System.out.println("浏览器名称："+browserName);
	        System.out.println("******************************");
	        String networkInfo = "系统:"+system + ",浏览器:"+browserName;
			return networkInfo;
	}
	
	@Resource
	protected QueueSender queueSender;
	
	protected void writerLog(String username,String method,Integer status,String remark){
		com.sense.iam.model.sys.Log sysLog=new com.sense.iam.model.sys.Log();
		sysLog.setUserName(username);
		sysLog.setClazz(getClass().getName());
		sysLog.setMethod(method);
		sysLog.setStatus(status);
		sysLog.setRemark(remark);
		sysLog.setCreateTime(new Date());
		sysLog.setIp(getClientIp());
		sysLog.setClientInfo(getClientNetworkInfo(request));
		sysLog.setCompanySn(CurrentAccount.getCurrentAccount().getCompanySn());
		queueSender.send(sysLog);
		log.debug("writer log to queue:"+sysLog);
	}
}
