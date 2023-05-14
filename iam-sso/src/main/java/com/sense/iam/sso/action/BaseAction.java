package com.sense.iam.sso.action;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.sense.core.freemark.StringParse;
import com.sense.core.queue.QueueSender;
import com.sense.core.util.CurrentAccount;
import com.sense.core.util.ExcelUtils;
import com.sense.core.util.StringUtils;
import com.sense.core.util.XMLUtil;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.auth.OnlineUser;
import com.sense.iam.tld.TldModel;

public class BaseAction {
	



	
	protected Log log=LogFactory.getLog(getClass());
    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;
    
	protected final String ONLINE_USER_SESSION_ID="iam-oline-user";

	@Resource
	private ApplicationContext context;
	
	@Resource
	private TldModel tldModel;
	

	
	/**
	 * 
	 * 输出403错误信息
	 * @param content
	 * @param response
	 */
	protected void print403(HttpServletResponse response){
		response.setCharacterEncoding("UTF-8");  
		response.setContentType("text/html; charset=utf-8");   
		response.setStatus(403);
		try {
			response.getOutputStream().write("Forbidden".getBytes());
		} catch (IOException e) {
			//ignore
		}
	}
	
	/**
	 * 
	 * 输出403错误信息
	 * @param content
	 * @param response
	 */
	protected void printERROR(String msg ,HttpServletResponse response){
		response.setCharacterEncoding("UTF-8");  
		response.setContentType("text/html; charset=utf-8");   
		response.setStatus(200);
		try {
			response.getWriter().println(msg);
			response.getWriter().flush();
			response.getWriter().close();
		} catch (IOException e) {
			//ignore
		}
	}
	
	/**
	 * 输出数据到页面
	 * @param content
	 * @param response
	 *//*
	protected void print(String content,HttpServletResponse response){
		response.setCharacterEncoding("UTF-8");  
		response.setContentType("application/json; charset=utf-8");  
		PrintWriter out = null;  
		try {
			out = response.getWriter();
			out.append(content);
			log.debug(content);
		} catch (IOException e) {
			log.error("response out error",e);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}*/
	
	
	/**
	 * 
	 * 输出重定向
	 * @param content
	 * @param response
	 */
	protected void sendRedirect(String url ,HttpServletResponse response){
		response.setCharacterEncoding("UTF-8");  
		response.setContentType("text/html; charset=utf-8");   
		response.setStatus(200);
		url = url.replaceAll("\r", "%0D");//Encode \r to url encoded value
		url = url.replaceAll("\n", "%0A");//Encode \n to url encoded value
		try {
			response.getWriter().println("<script>location.href='"+url+"'</script>");
			response.getWriter().flush();
			response.getWriter().close();
		} catch (IOException e) {
			//ignore
		}
	}
	
	/**
	 * 
	 * 指定浏览器打开单点登录连接
	 * @param content
	 * @param response
	 */
	protected void sendRedirectBrowser(String ssourl,HttpServletResponse response){
		response.setCharacterEncoding("UTF-8");  
		response.setContentType("text/html; charset=utf-8");   
		response.setStatus(200);
		try {
			response.getWriter().println("<body><span>切勿关闭窗口，浏览器弹出提示消息，点击允许即可。</span><div id='sso' class='display:none'></div></body><script> window.onload = function () {location.href='"+ssourl+"';setTimeout(function(){window.opener=null;window.open('','_self');window.close();}, 5000);}</script>");
			response.getWriter().flush();
			response.getWriter().close();
		} catch (IOException e) {
			//ignore
		}
	}

	
	
	
	/**
	 * 
	 * 输出单点登录日志后重定向到指定连接
	 * @param content
	 * @param response
	 */
	protected void sendRedirectT(String ssourl,String redirect,HttpServletResponse response){
		response.setCharacterEncoding("UTF-8");  
		response.setContentType("text/html; charset=utf-8");   
		response.setStatus(200);
		try {
			response.getWriter().println("<body><span>正在单点,请稍后!</span><div id='sso' class='display:none'></div></body><script> window.onload = function () {var iframe = document.createElement('iframe');iframe.style.display = 'none';iframe.setAttribute('src','"+ssourl+"');document.getElementById('sso').appendChild(iframe);setTimeout(function(){location.href='"+redirect+"'}, 1500);}</script>");
			response.getWriter().flush();
			response.getWriter().close();
		} catch (IOException e) {
			//ignore
		}
	}

	protected Map parseXmlToMap(String config,String oid){
		Map params=tldModel.getBasicTld();
		params.put("oid", oid);
		try {
			String xml=StringParse.parse(config, params);
			return XMLUtil.simpleXml2Map(xml);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new HashMap();
	}
	
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
	
	/**
	 * 获取spring对象
	 * @param name spring对象名称
	 * @return
	 */
	protected Object getBean(String name){
		return WebApplicationContextUtils.getRequiredWebApplicationContext(request.getSession().getServletContext()).getBean(StringUtils.firstCharToLowerCase(name));
	}
	
	@SuppressWarnings("rawtypes")
	protected void exportXlsx(String fileName,List contentList,List<ExcelUtils.ExcelModel> ems){
		this.exportXlsx(fileName, contentList, ems, "sheet0");
	}
	
	/**
	 * 导出Excel2007数据
	 * @param contentList 导出对象集合
	 * @param ems 导出模型集合
	 */
	@SuppressWarnings("rawtypes")
	protected void exportXlsx(String fileName,List contentList,List<ExcelUtils.ExcelModel> ems,String sheetName){
		OutputStream os=null;
		try{
			response.addHeader("P3P","CP='IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT'");
			response.setContentType("application/vnd.ms-excel;charset=UTF-8");
			response.setHeader("Content-Disposition", "attachment; filename=\""+new String(fileName.getBytes(), "ISO-8859-1")+".xlsx\"");
			ExcelUtils.export(response.getOutputStream(), contentList, ems,sheetName);
		}catch(Exception e){
			//ignore
		}finally{
			if(os!=null){
				try{
					os.flush();
					os.close();
				}catch(IOException ex){
					//ignore
				}
			}
		}
	}
	

	public  int isBrowser(){
		//原理：得到user-agent请求头，判断它的值是否包含了指定的字符串，区分不同的浏览器
        //得到user-agent请求头的值
        String agent = request.getHeader("user-agent");
        if (agent.contains("Chrome")) {
        	 return 2;
        }else if (agent.contains("Firefox")) {
        	 return 3;
        }else {
        	 return 1;
        }
	}

	
	/**
	 * 将权限路径进行改变，主要由于系统中存在业务系统权限判断的路径为cam
	 * @param funcName
	 * @return
	 * description :  
	 * wenjianfeng 2019年10月22日
	 */
	protected String getFuncName(String funcName){
		return funcName.replace(".api.", ".cam.");
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
		sysLog.setCompanySn(CurrentAccount.getCurrentAccount().getCompanySn());
		queueSender.send(sysLog);
		log.debug("writer log to queue:"+sysLog);
	}
	
	
	/**
	 * 判断是否是否认证过
	 * @return
	 */
	protected boolean iSAuthenticated(){
		CurrentAccount currentAccount = CurrentAccount.getCurrentAccount();
		if(currentAccount!=null){
			return true;
		}else{
			return false;
		}
	}
	protected void writeLog(String remark, OnlineUser onlineUser,int authType) {
		com.sense.iam.model.sso.Log ssoLog=new com.sense.iam.model.sso.Log();
		if(onlineUser==null) {
			ssoLog.setUserName("无");
			ssoLog.setAccountId(0L);
			ssoLog.setCompanySn("100001");
		}else {
			ssoLog.setUserName(onlineUser.getUid());
			ssoLog.setAccountId(Long.valueOf(onlineUser.getAccountId()));
			ssoLog.setCompanySn(CurrentAccount.getCurrentAccount().getCompanySn());
		}
		ssoLog.setSsoType(authType);
		
		ssoLog.setRemark(remark);
		queueSender.send(ssoLog);
	}
}
