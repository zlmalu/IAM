package com.sense.iam.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.isprint.am.saml.AuthnRequest;
import com.isprint.am.saml.AuthnResponse;
import com.isprint.am.saml.SAMLException;
import com.isprint.am.saml.sp.SP;
import com.isprint.am.saml.sp.SPConfiguration;
import com.sense.core.security.Md5;
import com.sense.core.util.CurrentAccount;
import com.sense.core.util.GatewayHttpUtil;
import com.sense.core.util.StringUtils;
import com.sense.iam.api.action.sys.ResultCodeReq;
import com.sense.iam.cache.PolicyCache;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.sys.Acct;
import com.sense.iam.service.SysAcctService;

/**
 * Servlet implementation class SamlServlet
 */
@WebServlet("/samlServlet")
public class SamlServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected Log log = LogFactory.getLog(getClass());
	private SPConfiguration conf ;
	private SP sp;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		System.out.println("进init方法了");
		log.info("====初始化参数配置====");
		super.init(config);
		//初始化参数配置
		Map<String,String> params=new HashMap<String,String>();
    	///set sp
    	params.put("issuer", "APP050");
    	params.put("sso-acs-url", "http://iamaas.sensesw.com:8882/api/samlServlet");
    	params.put("keystore", "/app/iam-apps/idp.keystore");
    	params.put("key-alias", "idp");
    	params.put("keystore.passphrase", "sense123");
    	params.put("signature.algo", "RSA_SHA256");
    	
    	//set idp
    	params.put("idp.APP050.sso-url", "http://iamaas.sensesw.com:8882/sso/saml/login");
    	params.put("idp.APP050.issuer", "senseIdp");
    	params.put("idp.APP050.keystore", "/app/iam-apps/idp.keystore");
    	params.put("idp.APP050.key-alias", "idp");
    	try{
	    	conf = new SPConfiguration(params);
			sp = SP.newInstance(conf);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
	}
       

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("进service方法了");
		log.info("========进入service========");
		
		//获取统一认证服务器返回的SAML响应
		String samlResponseStr=request.getParameter("SAMLResponse");
		//如果存在SAML响应则解析SAML协议获取用户信息
		if((samlResponseStr!=null && samlResponseStr.trim().length()>0)){
			//并且不是saml响应，则跳转到登陆页面
			try {
				AuthnResponse samlResponse=sp.parseAuthnResponse(samlResponseStr);
				if(samlResponse!=null){
					//获取用户并设置到会话中
					request.getSession().setAttribute("CURRENT_USER", samlResponse.getUserIdentity());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//从会话获取当前用户
		Object user=request.getSession().getAttribute("CURRENT_USER");
		//如果用户已登陆，则跳转到首页
		if(user!=null){
			log.info("========SAML SET USER SUCCESS========");
			String redirectUrl=GatewayHttpUtil.getKey("RemoteServer", request)+"/console/#/ssologin?r="+StringUtils.getSecureRandomnNumber();
			redirectUrl = redirectUrl.replaceAll("\r", "%0D");//Encode \r to url encoded value
			redirectUrl = redirectUrl.replaceAll("\n", "%0A");//Encode \n to url encoded value
			response.sendRedirect(redirectUrl);
			return;
		}else{//用户没有登陆，生成AM的SAML认证请求，并重定向到统一认证
			log.info("========user not login========");
			List<String> authnContexts = new ArrayList<String>();
			authnContexts.add("urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");
			AuthnRequest samlRequest = sp.generateAuthnRequest("APP050",authnContexts);
			String spRelayState = "";
			log.info("---"+samlRequest.getSSOUrl()+"---");
			String url=(samlRequest.getSSOUrl()+"?SAMLRequest="+samlRequest.getUrlEncodedAndDeflatedSAMLRequest()+"&RelayState="+spRelayState+"&r="+StringUtils.getSecureRandomnNumber());
			url = url.replaceAll("\r", "%0D");//Encode \r to url encoded value
			url = url.replaceAll("\n", "%0A");//Encode \n to url encoded value
			response.sendRedirect(url);
			return;
		}
	}
}
