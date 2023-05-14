package com.sense.iam.sso.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sense.core.security.Base64;
import com.sense.core.util.CurrentAccount;
import com.sense.core.util.GatewayHttpUtil;
import com.sense.core.util.StringUtils;
import com.sense.iam.cache.SsoConfigCache;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.auth.OnlineUser;
import com.sense.iam.model.im.Account;
import com.sense.iam.model.im.App;
import com.sense.iam.model.sso.Saml;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.AppService;
import com.sense.iam.service.SsoSamlService;
import com.sense.sdk.saml.MetadataFactory;
import com.sense.sdk.saml.SAMLRequestFactory;
import com.sense.sdk.saml.pojo.AuthnRequestField;
import com.sense.sdk.saml.service.AuthnRequestHandler;
import com.sense.sdk.saml.service.SamlResponseGenerator;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.URLUtil;

/**
 *
 * saml2.0认证协议
 *
 * Description: 调用前缀加入/saml
 *
 * @author w_jfwen
 *
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Controller
@RequestMapping("saml")
public class SamlAction extends BaseAction {

	private static AuthnRequestHandler authnRequestHandler = new AuthnRequestHandler();

	private static SamlResponseGenerator samlResponseGenerator = new SamlResponseGenerator();
	@Resource
	private SsoConfigCache ssoConfigCache;
	@Resource
	private AccountService accountService;
	@Resource
	private AppService appService;
	@Resource
	private SsoSamlService ssoSamlService;

	/**
	 * 根据用户票据跳转到制定页面 saml/APP002/login?SAMLRequest=xxxx
	 */
	@RequestMapping(value = "{client_id}/login", method = { RequestMethod.GET, RequestMethod.POST })
	public String login(@PathVariable String client_id, HttpServletRequest request) {
		Saml saml = ssoConfigCache.getSamlConfig(client_id);
		if (client_id == null || saml == null) {
			// 返回403没有权限
			try {
				log.info("应用未注册SAML服务(client_id为空)");
				request.setAttribute("error", "应用未注册SAML服务(client_id为空)");
				return "403";
			} catch (Exception e) {
				e.printStackTrace();
				request.setAttribute("error", e.getMessage());
				return "403";
			}
		}

		/*
		  saml请求内容
		 */
		String samlRequest = request.getParameter("SAMLRequest");
		String RelayState = request.getParameter("RelayState");

		log.info("传入的SAMLRequest-------------：" + samlRequest);
		log.info("传入的RelayState-------------：" + RelayState);
		if (StringUtils.isEmpty(samlRequest)) {
			SAMLRequestFactory factory = new SAMLRequestFactory();
			samlRequest = factory.getSAMLRequest(saml.getIdpIssuer(), saml.getDefaultUrl(), saml.getSpIssuer(),
					saml.getNameId(), StringUtils.isEmpty(saml.getTimeZone()) ? "0" : saml.getTimeZone());
			log.info("生成的samlRequest-------------:" + samlRequest);
		}
		// 解析SAMLRequest
		AuthnRequestField authnRequestField = authnRequestHandler.handleAuthnRequest(samlRequest);
		// String spIssuer = authnRequestField.getSpIssuer();
		// AssertionConsumerServiceURL,重定向地址
		String redirect_uri = authnRequestField.getAssertionConsumerServiceUrl();

		// 设置默认请求的url
		request.setAttribute("redirectUri", request.getRequestURL() + "?" + request.getQueryString());

		/* 如果用户已登录过 **/
		// 验证通过跳转到redirect_uri 加入如下参数:
		OnlineUser onlineUser = (OnlineUser) request.getSession().getAttribute(super.ONLINE_USER_SESSION_ID);
		CurrentAccount currentAccount = CurrentAccount.getCurrentAccount();
		if (onlineUser == null) {
			if (currentAccount != null) {
				onlineUser = new OnlineUser();
				onlineUser.setExpried(System.currentTimeMillis() + 30 * 60 * 1000);
				onlineUser.setLoginIp(currentAccount.getRemoteHost());
				onlineUser.setLoginTime(System.currentTimeMillis());
				onlineUser.setValid(true);
				onlineUser.setAccountId(currentAccount.getId().toString());
				onlineUser.setUid(currentAccount.getUsername());
				onlineUser.setAccountId(currentAccount.getId().toString());
				onlineUser.setSessionId(currentAccount.getSessionId());
				onlineUser.setAppSn(client_id);
			}
		}
		if (onlineUser != null) {

			if (redirect_uri == null || redirect_uri.trim().equals("null") || redirect_uri.trim().equals("")) {
				redirect_uri = saml.getDefaultUrl();
			}

			String accountId = request.getParameter("tokenId");
			if (accountId == null) {// 是否指定单点登录账号
				// 查询用户对应账号并判断用户是否存在多账号
				Account account = new Account();
				account.setAppSn(client_id);
				account.setUserId(currentAccount.getUserId());
				account.setStatus(Constants.ACCOUNT_ENABLED);// 必须是启用帐号
				account.setIsControl(false);
				List<Account> list = accountService.findList(account);
				if (list == null || list.size() == 0) {
					try {
						request.setAttribute("error", "当前应用无权限（无账号）");
						return "403";
					} catch (Exception e) {
						log.error("forward request error", e);
					}
				} else if (list.size() > 1) {
					try {
						request.setAttribute("accts", list);
						return "acctsel";
					} catch (Exception e) {
						log.error("forward request error", e);
					}
				} else {
					account = list.get(0);
					accountId = account.getId() + "";
					onlineUser.setUid(account.getLoginName());
				}
			} else {
				Account account = accountService.findById(Long.valueOf(accountId));
				accountId = account.getId() + "";
				onlineUser.setUid(account.getLoginName());
			}
			onlineUser.setAccountId(accountId);
			// 记录saml单点登录日志
			com.sense.iam.model.sso.Log ssoLog = new com.sense.iam.model.sso.Log();
			ssoLog.setUserName(onlineUser.getUid());
			ssoLog.setAccountId(Long.valueOf(accountId));
			ssoLog.setSsoType(Constants.SSO_SAML);
			ssoLog.setCompanySn(CurrentAccount.getCurrentAccount().getCompanySn());
			queueSender.send(ssoLog);
			// 生成AccessToken
			try {
				log.info("响应登录名:" + onlineUser.getUid());
				String samlResponse = samlResponseGenerator.generateSamlResponse(onlineUser.getUid(), saml,
						authnRequestField);
				log.info("响应SAMLResponse BASE64加密前:" + samlResponse);
				log.info("响应SAMLResponse BASE64加密后:" + new String(Base64.encode(samlResponse.getBytes())));
				log.info("响应redirect_uri:" + redirect_uri);

				request.setAttribute("redirectUri", redirect_uri);
				request.setAttribute("SAMLResponse", new String(Base64.encode(samlResponse.getBytes())));
				request.setAttribute("RelayState", RelayState);

				return "sso/saml/samlsso";
			} catch (Exception e) {
				log.error("forward request error", e);
				e.printStackTrace();
				request.setAttribute("error", e.getMessage());
				return "403";
			}
		} else {// 跳转到登录授权页面,并携带应用请求参数发送到登录页面
			try {
				String uri = GatewayHttpUtil.getKey("RemoteServer", request) + "/sso/saml/" + client_id
						+ "/login?SAMLRequest=" + URLEncoder.encode(samlRequest,"UTF-8")+"&RelayState="+URLEncoder.encode(RelayState,"UTF-8");
				String redirects = GatewayHttpUtil.getKey("RemoteServer", request) + "/portal/login.html?redirectUri="
						+ URLUtil.encodeAll(uri);
				return "redirect:" + redirects;
			} catch (Exception e) {
				log.error("forward request error", e);
				e.printStackTrace();
				request.setAttribute("error", e.getMessage());
				return "403";
			}
		}
	}

	/**
	 * metadata url like. http://{ip:port}/{context}/saml/{appSn}/metadata/idp
	 *
	 */
	@RequestMapping(value = "{client_id}/idp", method = { RequestMethod.GET, RequestMethod.POST })
	public void idp(@PathVariable String client_id, HttpServletResponse response) {
		App app = new App();
		app.setSn(client_id);
		//设置应用下载权限为最大
		List<App> appList = appService.findAppList(app);
		if (appList != null && appList.size()>0) {
			Saml saml = new Saml();
			saml.setAppId(appList.get(0).getId());
			saml = ssoSamlService.findByObject(saml);
			if (saml != null) {
				MetadataFactory metadataFactory = new MetadataFactory();
				String xml = metadataFactory.createXML(saml);
				if(null==xml){
					printXML("The certificate information is incorrect!", response,"application/json; charset=utf-8");
					return;
				}
				response.setCharacterEncoding("utf-8");
				response.setContentType("application/octet-stream");

				PrintWriter os = null;
				try {
					// 解决 以文件形式下载 而不会被浏览器打开 以及中文文件名需要编码
					response.setHeader("Content-Disposition",
							"attachment;filename="
									+ URLUtil.encode(DateUtil.format(new Date(), "yyyyMMdd_HHmmss") + "_IAM_metadata")
									+ ".xml");
					os = response.getWriter();
					os.print(xml);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (os != null) {
						os.close();
					}
				}
			}
		}
		else{
			printXML("undefined app!", response,"application/xml; charset=utf-8");
		}
	}

	/**
	 * 输出数据到页面
	 */
	protected void printXML(String content, HttpServletResponse response,String contentType) {
		response.setCharacterEncoding("UTF-8");
		response.setContentType(contentType);
		PrintWriter out = null;
		try {
			out = response.getWriter();
			out.append(content);
			log.debug(content);
		} catch (IOException e) {
			e.printStackTrace();
			log.error("response out error", e);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

}
