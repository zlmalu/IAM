package com.sense.iam.portal.action;

import java.util.Enumeration;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sense.core.util.GatewayHttpUtil;
import com.sense.core.util.StringUtils;
import com.sense.iam.cache.SysConfigCache1;
import com.sense.iam.cam.Constants;


@Controller
@RequestMapping("sso")
public class SSOlogoutAction extends BaseAction {
	
	@Resource
	private SysConfigCache1 sysConfigCache1;
	
	@RequestMapping("/logout.html")
    public String logout(){
		String redirectUrl=request.getParameter("redirectUrl");
		if(redirectUrl==null||redirectUrl.length()==0){
			redirectUrl=GatewayHttpUtil.getKey("RemoteServer", request)+"/portal/login.html";
		}
		String sessionId = GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request);
		request.setAttribute("redirectUrl", redirectUrl);
		request.setAttribute("token", sessionId);
		try {
			Enumeration em = request.getSession().getAttributeNames();
			while (em.hasMoreElements()) {
				request.getSession().removeAttribute(em.nextElement().toString());
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		//获取header参数，
		String extLogoutUrl=sysConfigCache1.getValue("ext.logout.url");
		log.info("外部退出url:"+extLogoutUrl);
		if(!StringUtils.isEmpty(extLogoutUrl)) {
			return "redirect:"+extLogoutUrl;
		}else {
			return "logout";
		}
    }	
}
