package com.sense.iam.sso.action;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sense.core.util.CurrentAccount;
import com.sense.core.util.GatewayHttpUtil;
import com.sense.core.util.StringUtils;
import com.sense.iam.cache.CompanyCache;
import com.sense.iam.cache.SsoConfigCache;
import com.sense.iam.cache.SysConfigCache;
import com.sense.iam.cache.SysConfigCache1;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.auth.OnlineUser;
import com.sense.iam.cam.auth.Token;
import com.sense.iam.cam.auth.cache.AccessTokenCache;
import com.sense.iam.cam.auth.cache.OnlineUserCache;
import com.sense.iam.model.im.Account;
import com.sense.iam.model.im.User;
import com.sense.iam.model.sso.Cas;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.AppService;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.SsoCasService;
import com.sense.iam.service.UserService;

import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import net.sf.json.JSONArray;

/**
 *
 * cas3.0认证协议
 *
 * Description: 调用路径加入前缀：/cas
 *
 * @author w_jfwen
 *
 *         Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Controller
@RequestMapping("cas3")
public class Cas3Action extends BaseAction {
	@Resource
	private AccessTokenCache accessTokenCache;
	@Resource
	private SsoConfigCache ssoConfigCache;
	@Resource
	private OnlineUserCache onlineUserCache;
	@Resource
	private AccountService accountService;
	@Resource
	private SsoCasService ssoCasService;
	@Resource
	private AppService appService;
	@Resource
	private StringRedisTemplate stringRedisTemplate;
	@Resource
	private JdbcService jdbcService;
	@Resource
	private CompanyCache companyCache;
	@Resource
	private SysConfigCache1 sysConfigCache1;
	@Resource
	private UserService userService;

	/**
	 * 认证跳转验证
	 *
	 * @param client_id
	 *            使用的客户端id（应用标识）
	 * @param service
	 *            回调路径
	 */
	@RequestMapping(value = "{client_id}/login", method = { RequestMethod.GET, RequestMethod.POST })
	public String login(@PathVariable String client_id, HttpServletRequest request, HttpServletResponse response) {
		String service = GatewayHttpUtil.getParameterForHtml("service", request);
		if (iSAuthenticated()) {
			client_id = GatewayHttpUtil.getParameterForHtml(client_id);
			Cas cas = ssoConfigCache.getCasConfig(client_id);
			if (client_id == null || cas == null) {
				// 返回403没有权限
				try {
					request.setAttribute("error", "应用未注册CAS服务(client_id为空)");
					return "403";
				} catch (Exception e) {
					e.printStackTrace();
					request.setAttribute("error", "应用未注册CAS服务(client_id为空)异常：" + e.getMessage());
					return "403";
				}
			}

			if (StringUtils.isEmpty(service) || service.equals("null")) {
				// 获取默认地址
				String defUrl = cas.getDefaultUrl();
				if (StringUtils.isEmpty(defUrl) || StringUtils.isEmpty(defUrl.trim())) {
					request.setAttribute("error", "应用未配置回调地址");
					return "403";
				} else {
					service = defUrl;
				}
			} else {
				// 验证回调url
				// 获取默认地址
				String defUrl = cas.getDefaultUrl();
				if (StringUtils.isEmpty(defUrl) || StringUtils.isEmpty(defUrl.trim())) {
					// 返回403没有权限
					log.info("oauth返回结果:servicei111非法：" + defUrl);
					request.setAttribute("error", "回调地址不正确");
					return "403";
				}

				try {
					URL redirectHost = new URL(service);
					URL configHost = new URL(defUrl);
					if (!redirectHost.getHost().equals(configHost.getHost())) {
						log.info("oauth返回结果:redirect_uri非法：" + service);
						// 返回403没有权限
						request.setAttribute("error", "回调地址不正确");
						return "403";
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
					log.info("oauth返回结果:redirect_uri非法：" + service);
					// 返回403没有权限
					request.setAttribute("error", "回调地址不正确");
					return "403";
				}
			}
			CurrentAccount currentAccount = CurrentAccount.getCurrentAccount();
			String uri = GatewayHttpUtil.getKey("RemoteServer", request) + "/sso/cas3/" + client_id + "/login?service="
					+ URLUtil.encodeAll(service);
			if (currentAccount == null) {
				// 未登录，检查是否来自移动端,如果requestSource是1，来自企业微信移动端，重定向到移动端模块
				// 判断 是否是微信浏览器
				String userAgent = request.getHeader("user-agent").toLowerCase();
				String qywxEnable = sysConfigCache1.getValue("QYWX_SSO_ENABLE");// 1-启用
				if (userAgent.indexOf("micromessenger") != -1 && !StringUtils.isEmpty(qywxEnable)
						&& "1".equals(qywxEnable)) { // 微信客户端
					String corpid = SysConfigCache.QYWX_APPID;
					// 重定向地址
					String redirectUri = GatewayHttpUtil.getKey("RemoteServer", request) + "/sso/weixin/" + client_id
							+ "/login?redirect_uri=" + URLUtil.encodeAll(uri);
					String redirect = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + corpid
							+ "&redirect_uri=" + URLUtil.encodeAll(redirectUri)
							+ "&response_type=code&scope=SCOPE&state=STATE#wechat_redirect";
					log.info("oauth中企业微信重定向地址：" + redirect);
					return "redirect:" + redirect;
				}
			}
			OnlineUser onlineUser = new OnlineUser();
			onlineUser.setExpried(System.currentTimeMillis() + 30000);
			onlineUser.setLoginIp(currentAccount.getRemoteHost());
			onlineUser.setLoginTime(System.currentTimeMillis());
			onlineUser.setUid(currentAccount.getLoginName());
			onlineUser.setValid(true);
			onlineUser.setSessionId(currentAccount.getSessionId());
			onlineUser.setAppSn(client_id);
			String accountId = GatewayHttpUtil.getKey("tokenId", request);
			log.info("cas3中的tokenId-accountId:" + accountId);
			if (accountId == null) {// 是否指定单点登陆账号
				// 查询用户对应账号并判断用户是否存在多账号
				String sql = "select iaa.*,iu.NAME,app.NAME as APP_NAME from (select ia.ID,ia.LOGIN_NAME,ia.APP_ID,ia.USER_ID from im_account ia where id in "
						+ "(select ID as ACCT_ID from im_account where user_id=" + currentAccount.getUserId() + " "
						+ "union " + "select ACCT_ID from IM_ACCOUNT_USER where user_id=" + currentAccount.getUserId()
						+ ") and ia.status=1 ) iaa " + "left join im_user iu on iu.id=iaa.USER_ID "
						+ "left join im_app app on app.id=iaa.APP_ID where app.sn='" + client_id + "'";
				List<Map<String, Object>> list = jdbcService.findList(sql);
				if (list == null || list.size() == 0) {
					log.info("当前应用ID:" + client_id + ",当前用户ID:" + currentAccount.getUserId() + ",账号数量：" + list.size());
					try {
						request.setAttribute("error", "当前用户当前应用无账号：" + client_id + "," + currentAccount.getName());
						// 如果当前应用指定了无账号跳转页面则跳转指定，否则默认403
						return "403";
					} catch (Exception e) {
						log.error("forward request error", e);
					}
				} else if (list.size() > 1) {
					// 判断回调地址是否需要多账号选择
					String SSO_ACCTS_CHOOSE = sysConfigCache1.getValue("SSO_ACCTS_CHOOSE");// 1-启用,2-禁用
					boolean choose = true;
					if (!service.equals(cas.getDefaultUrl()) && !StringUtils.isEmpty(SSO_ACCTS_CHOOSE)
							&& SSO_ACCTS_CHOOSE.equals("2")) {
						choose = false;
					}
					if (choose) {
						// 查询用户扩展属性REMARK,固定的字段
						Map<Long, Object> remarks = new HashMap<Long, Object>();
						for (Map<String, Object> map : list) {
							Long userId = Long.valueOf(map.get("USER_ID").toString());
							Object remark = remarks.get(userId);
							if (remark == null) {
								// 根据userId查询
								User user = userService.findById(userId);
								remark = user.getExtraAttrs().get("REMARK");
								remarks.put(userId, remark);
							}
							map.put("REMARK", remark);
							Long acctId = Long.valueOf(map.get("ID").toString());
							Account acct = accountService.findById(acctId);
							map.put("REMARKACCT", acct.getExtraAttrs().get("REMARK"));
						}
						try {

							request.setAttribute("redirectUri", uri);
							request.setAttribute("accts", list);
							request.setAttribute("appName", list.get(0).get("APP_NAME").toString() + "-多账号选择界面");
							request.setAttribute("appName1", list.get(0).get("APP_NAME").toString());

							return "acctselsso";

						} catch (Exception e) {
							log.error("forward request error", e);
						}
					} else {
						for (Map<String, Object> map : list) {
							String loginName = map.get("LOGIN_NAME").toString();
							if (onlineUser.getUid().equals(loginName)) {
								accountId = map.get("ID").toString();
								onlineUser.setUid(loginName);
								break;
							}
						}
					}
				} else {
					accountId = list.get(0).get("ID").toString();
					onlineUser.setUid(list.get(0).get("LOGIN_NAME").toString());
				}
			}
			onlineUser.setAccountId(accountId);
			// 生成AccessToken
			Token token = accessTokenCache.grantTicketToken(onlineUser);
			token.setClientId(client_id);
			if (service.indexOf("?") != -1) {
				service += "&ticket=" + token.getId() + "&sessionId="
						+ GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request);
			} else {
				service += "?ticket=" + token.getId() + "&sessionId="
						+ GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request);
			}
			// 记录单点登陆日志
			writeLog("1:ticket:" + token.getId() + ",重定向地址：" + service, onlineUser, Constants.SSO_CAS);
			return "redirect:" + service;
		} else {
			// 跳转到登陆授权页面,并携带应用请求参数发送到登陆页面
			String redirectsUrl = GatewayHttpUtil.getKey("RemoteServer", request) + "/portal/login.html?redirectUri="
					+ URLUtil.encodeAll(GatewayHttpUtil.getKey("RemoteServer", request) + "/sso/cas3/" + client_id
					+ "/login?" + request.getQueryString())
					+ "&r=" + StringUtils.getSecureRandomnNumber();
			return "redirect:" + redirectsUrl;
		}
	}

	/**
	 *
	 * 用户采用ticket票据获取用户信息cas3.0
	 *
	 * @param ticket
	 * @return 1.<cas:serviceResponse xmlns:cas="http://www.yale.edu/tp/cas"> 2.
	 *         <cas:authenticationSuccess> 3. cc 4. <cas:attributes> 5.
	 *         <cas:FullName>LDAP Guest</cas:FullName> 6.
	 *         <cas:role>ROLE_USER</cas:role> 7.
	 *         <cas:LastName>Guest</cas:LastName> 8. </cas:attributes>
	 *         9. </cas:authenticationSuccess> 10.</cas:serviceResponse>
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("{client_id}/p3/serviceValidate")
	@ResponseBody
	public Object p3serviceValidate(@PathVariable String client_id, HttpServletRequest request,
									HttpServletResponse response) {
		String ticket = GatewayHttpUtil.getParameterForHtml("ticket", request);
		log.info("ticket: " + ticket);
		Token token = accessTokenCache.getToken(ticket);
		StringBuffer resultMsg = new StringBuffer();
		OnlineUser onlineUser = null;
		if (token != null) {
			onlineUser = (OnlineUser) token.getContent();
			log.info("cas在线用户存在: " + token);
			Cas cas = ssoConfigCache.getCasConfig(onlineUser.getAppSn());
			Map<String, String> map = new HashMap<>();
			String loginName = onlineUser.getUid();
			log.info("cas在线用户原始登录名: " + loginName);
			map.put("uid", loginName);
			if (!StringUtils.isEmpty(cas.getConfig())) {
				map.putAll(super.parseXmlToMap(cas.getConfig(), onlineUser.getAccountId()));
				loginName = map.get("uid") == null ? loginName : map.get("uid");
			}
			log.info("cas在线用户新登录名: " + loginName);
			resultMsg.append("<cas:serviceResponse xmlns:cas=\"http://www.yale.edu/tp/cas\">");
			resultMsg.append("<cas:authenticationSuccess>");
			resultMsg.append("<cas:user>" + loginName + "</cas:user>");
			resultMsg.append("<cas:attributes>");
			Set<String> keys = map.keySet();
			for (String key : keys) {
				if (!key.equals("uid")) {
					resultMsg.append("<cas:" + key + ">").append(map.get(key)).append("</cas:" + key + ">");
				}
			}
			resultMsg.append("</cas:attributes>");
			resultMsg.append("</cas:authenticationSuccess>");
			resultMsg.append("</cas:serviceResponse>");
		} else {
			resultMsg.append("<cas:serviceResponse xmlns:cas=\"http://www.yale.edu/tp/cas\">");
			resultMsg.append("<cas:authenticationFailure code='INVALID_TICKET'>");
			resultMsg.append("未识别出目标 '" + ticket + "'票根");
			resultMsg.append("</cas:authenticationFailure>");
			resultMsg.append("</cas:serviceResponse>");
		}
		return resultMsg;
	}

	/**
	 *
	 * 用户采用ticket票据获取用户信息
	 *
	 * @param ticket
	 * @return 1.<cas:serviceResponse xmlns:cas="http://www.yale.edu/tp/cas"> 2.
	 *         <cas:authenticationSuccess> 3. cc 4. <cas:attributes> 5.
	 *         <cas:FullName>LDAP Guest</cas:FullName> 6.
	 *         <cas:role>ROLE_USER</cas:role> 7.
	 *         <cas:LastName>Guest</cas:LastName> 8. </cas:attributes>
	 *         9. </cas:authenticationSuccess> 10.</cas:serviceResponse>
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("{client_id}/p3/serviceValidate/serviceValidate")
	@ResponseBody
	public Object serviceValidateserviceValidate(@PathVariable String client_id, HttpServletRequest request,
												 HttpServletResponse response) {
		String ticket = GatewayHttpUtil.getParameterForHtml("ticket", request);
		log.info("ticket: " + ticket);
		Token token = accessTokenCache.getToken(ticket);
		StringBuffer resultMsg = new StringBuffer();
		OnlineUser onlineUser = null;
		if (token != null) {
			onlineUser = (OnlineUser) token.getContent();
			log.info("cas在线用户存在: " + token);
			Cas cas = ssoConfigCache.getCasConfig(onlineUser.getAppSn());
			Map<String, String> map = new HashMap<>();
			String loginName = onlineUser.getUid();
			log.info("cas在线用户原始登录名: " + loginName);
			map.put("uid", loginName);
			if (!StringUtils.isEmpty(cas.getConfig())) {
				map.putAll(super.parseXmlToMap(cas.getConfig(), onlineUser.getAccountId()));
				loginName = map.get("uid") == null ? loginName : map.get("uid");
			}
			// log.info("cas配置："+cas.getConfig());
			// log.info("账号ID:"+onlineUser.getAccountId());
			// log.info("结果："+map);
			log.info("cas在线用户新登录名: " + loginName);
			resultMsg.append("<cas:serviceResponse xmlns:cas=\"http://www.yale.edu/tp/cas\">");
			resultMsg.append("<cas:authenticationSuccess>");
			resultMsg.append("<cas:user>" + loginName + "</cas:user>");
			resultMsg.append("<cas:attributes>");
			Set<String> keys = map.keySet();
			for (String key : keys) {
				if (!key.equals("uid")) {
					resultMsg.append("<cas:" + key + ">").append(map.get(key)).append("</cas:" + key + ">");
				}
			}
			resultMsg.append("</cas:attributes>");
			resultMsg.append("</cas:authenticationSuccess>");
			resultMsg.append("</cas:serviceResponse>");
		} else {
			resultMsg.append("<cas:serviceResponse xmlns:cas=\"http://www.yale.edu/tp/cas\">");
			resultMsg.append("<cas:authenticationFailure code='INVALID_TICKET'>");
			resultMsg.append("未识别出目标 '" + ticket + "'票根");
			resultMsg.append("</cas:authenticationFailure>");
			resultMsg.append("</cas:serviceResponse>");
		}
		return resultMsg;
	}

	/**
	 *
	 * 用户采用ticket票据获取用户信息，cas2.0
	 *
	 * @param ticket
	 * @return 1.<cas:serviceResponse xmlns:cas="http://www.yale.edu/tp/cas"> 2.
	 *         <cas:authenticationSuccess> 3. cc 4. <cas:attributes> 5.
	 *         <cas:FullName>LDAP Guest</cas:FullName> 6.
	 *         <cas:role>ROLE_USER</cas:role> 7.
	 *         <cas:LastName>Guest</cas:LastName> 8. </cas:attributes>
	 *         9. </cas:authenticationSuccess> 10.</cas:serviceResponse>
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("{client_id}/serviceValidate")
	@ResponseBody
	public Object serviceValidate(@PathVariable String client_id, HttpServletRequest request,
								  HttpServletResponse response) {
		String ticket = GatewayHttpUtil.getParameterForHtml("ticket", request);
		log.info("ticket: " + ticket);
		Token token = accessTokenCache.getToken(ticket);
		StringBuffer resultMsg = new StringBuffer();
		OnlineUser onlineUser = null;
		if (token != null) {
			onlineUser = (OnlineUser) token.getContent();
			log.info("cas在线用户存在: " + token);
			Cas cas = ssoConfigCache.getCasConfig(onlineUser.getAppSn());
			Map<String, String> map = new HashMap<>();
			String loginName = onlineUser.getUid();
			log.info("cas在线用户原始登录名: " + loginName);
			map.put("uid", loginName);
			if (!StringUtils.isEmpty(cas.getConfig())) {
				map.putAll(super.parseXmlToMap(cas.getConfig(), onlineUser.getAccountId()));
				loginName = map.get("uid") == null ? loginName : map.get("uid");
			}
			log.info("cas在线用户新登录名: " + loginName);
			resultMsg.append("<cas:serviceResponse xmlns:cas=\"http://www.yale.edu/tp/cas\">");
			resultMsg.append("<cas:authenticationSuccess>");
			resultMsg.append("<cas:user>" + loginName + "</cas:user>");
			resultMsg.append("<cas:attributes>");
			Set<String> keys = map.keySet();
			for (String key : keys) {
				if (!key.equals("uid")) {
					resultMsg.append("<cas:" + key + ">").append(map.get(key)).append("</cas:" + key + ">");
				}
			}
			resultMsg.append("</cas:attributes>");
			resultMsg.append("</cas:authenticationSuccess>");
			resultMsg.append("</cas:serviceResponse>");
		} else {
			resultMsg.append("<cas:serviceResponse xmlns:cas=\"http://www.yale.edu/tp/cas\">");
			resultMsg.append("<cas:authenticationFailure code='INVALID_TICKET'>");
			resultMsg.append("未识别出目标 '" + ticket + "'票根");
			resultMsg.append("</cas:authenticationFailure>");
			resultMsg.append("</cas:serviceResponse>");
		}
		return resultMsg;
	}

	/**
	 *
	 * 用户采用ticket票据获取用户信息，cas1.0
	 *
	 * @param ticket
	 * @return 1.<cas:serviceResponse xmlns:cas="http://www.yale.edu/tp/cas"> 2.
	 *         <cas:authenticationSuccess> 3. cc 4. <cas:attributes> 5.
	 *         <cas:FullName>LDAP Guest</cas:FullName> 6.
	 *         <cas:role>ROLE_USER</cas:role> 7.
	 *         <cas:LastName>Guest</cas:LastName> 8. </cas:attributes>
	 *         9. </cas:authenticationSuccess> 10.</cas:serviceResponse>
	 */
	@RequestMapping("{client_id}/validate")
	@ResponseBody
	public Object validate(@PathVariable String client_id, HttpServletRequest request, HttpServletResponse response) {
		String ticket = GatewayHttpUtil.getParameterForHtml("ticket", request);
		log.info("ticket: " + ticket);
		Token token = accessTokenCache.getToken(ticket);
		StringBuffer resultMsg = new StringBuffer();
		if (token != null) {
			OnlineUser onlineUser = (OnlineUser)token.getContent();
			resultMsg.append("yes\n");
			Map<String, String> map = new HashMap<>();
			String loginName = onlineUser.getUid();
			log.info("cas在线用户原始登录名: " + loginName);
			map.put("uid", loginName);
			Cas cas = ssoConfigCache.getCasConfig(onlineUser.getAppSn());
			if (!StringUtils.isEmpty(cas.getConfig())) {
				map.putAll(super.parseXmlToMap(cas.getConfig(), onlineUser.getAccountId()));
				loginName = map.get("uid") == null ? loginName : map.get("uid");
			}
			log.info("cas返回的登录名------ " + loginName);
			resultMsg.append(loginName);
		} else {
			resultMsg.append("no\n");
			resultMsg.append("INVALID_TICKET");
		}
		// 记录单点登陆日志

		return resultMsg;
	}

	/**
	 * 注销会话
	 *
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "logout", method = RequestMethod.GET)
	public void logout(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
		log.info("=====================进入cas3退出方法================");
		String redirectUrl = request.getParameter("redirectUrl");
		if (redirectUrl == null || redirectUrl.length() == 0) {
			redirectUrl = GatewayHttpUtil.getKey("RemoteServer", request) + "/portal/login.html";
		}
		String sessionId = GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request);
		request.setAttribute("redirectUrl", redirectUrl);
		request.setAttribute("token", sessionId);
		try {
			Enumeration em = request.getSession().getAttributeNames();
			while (em.hasMoreElements()) {
				log.info("退出清除的session值：" + em.nextElement().toString());
				request.getSession().removeAttribute(em.nextElement().toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("logout sessionId:" + sessionId);
		List<?> list = jdbcService.findList(
				"select user_name from am_online_user where id='" + StringEscapeUtils.escapeSql(sessionId) + "'");

		if (list != null && list.size() > 0) {
			JSONArray data = JSONArray.fromObject(list);
			String username = data.getJSONObject(0).getString("user_name");
			CurrentAccount.setCurrentAccount(
					new CurrentAccount(companyCache.getCompanySn(GatewayHttpUtil.getKey("RemoteHost", request))));
			writerLog(username, "logout", Constants.OPT_SUCCESS, "系统退出");
		}
		queueSender.send("delete from am_online_user where id='" + sessionId + "'");
		stringRedisTemplate.delete(Constants.CURRENT_REDIS_SESSION_ID + ":" + sessionId);
		// 获取header参数，
		super.sendRedirect(redirectUrl, response);
		return;
	}

}
