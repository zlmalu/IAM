package com.sense.iam.portal.action;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sense.iam.cam.Constants;
import com.sense.iam.model.sys.PortalSettingManage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.sense.core.freemark.StringParse;
import com.sense.core.queue.QueueSender;
import com.sense.core.util.CurrentAccount;
import com.sense.core.util.ExcelUtils;
import com.sense.core.util.StringUtils;
import com.sense.core.util.XMLUtil;
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

	public PortalSettingManage getPortalSettingManage(){
		return  (PortalSettingManage) request.getSession().getAttribute(Constants.PORTAL_SETTING_MANAGE_KEY);
	}

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
		response.setStatus(500);
		try {
			response.getWriter().println(msg.toString());
			response.getWriter().flush();
			response.getWriter().close();
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
	protected void sendRedirect(String url ,HttpServletResponse response){
		try {
			if(StringUtils.isEmpty(url))return;
			url=url.trim();
			if(url.indexOf("?")!=-1){
				url=url+"&r="+StringUtils.getSecureRandomnNumber();
			}else{
				url=url+"?r="+StringUtils.getSecureRandomnNumber();
			}
			url = url.replaceAll("\r", "%0D");//Encode \r to url encoded value
			url = url.replaceAll("\n", "%0A");//Encode \n to url encoded value
			//判断是否存在http开头或者https或者www.
			if(url.indexOf("http:")==-1||url.indexOf("https:")==-1||url.indexOf("www.")==-1){
				response.setCharacterEncoding("UTF-8");  
				response.setContentType("text/html; charset=utf-8");   
				response.setStatus(200);
				try {
					response.getWriter().println("<script>location.href='"+url+"'</script>");
					response.getWriter().flush();
					response.getWriter().close();
				} catch (IOException e) {
					//ignore
				}
			}else{
				response.sendRedirect(url);
			}
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
}
