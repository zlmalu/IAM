package com.sense.iam.sso.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sense.am.exception.BlackUserException;
import com.sense.am.exception.IpPolicyException;
import com.sense.am.exception.ResourceAuthUserException;
import com.sense.am.exception.StrongAuthenticationException;
import com.sense.am.exception.TimePolicyException;
import com.sense.am.model.SSORequest;
import com.sense.am.policy.PolicyManager;
import com.sense.core.freemark.StringParse;
import com.sense.core.security.UIM;
import com.sense.core.util.CurrentAccount;
import com.sense.core.util.GatewayHttpUtil;
import com.sense.core.util.StringUtils;
import com.sense.iam.cache.CompanyCache;
import com.sense.iam.cache.SysConfigCache;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.im.Account;
import com.sense.iam.model.im.App;
import com.sense.iam.model.im.User;
import com.sense.iam.model.sso.AnalogInput;
import com.sense.iam.model.sso.Cas;
import com.sense.iam.model.sso.Formbase;
import com.sense.iam.model.sso.Jwt;
import com.sense.iam.model.sso.Ltpa;
import com.sense.iam.model.sso.Oauth;
import com.sense.iam.model.sso.Oidc;
import com.sense.iam.model.sso.Saml;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.AppService;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.SsoAnalogInputService;
import com.sense.iam.service.SsoCasService;
import com.sense.iam.service.SsoFormbaseService;
import com.sense.iam.service.SsoJwtService;
import com.sense.iam.service.SsoLtpaService;
import com.sense.iam.service.SsoOauthService;
import com.sense.iam.service.SsoOidcService;
import com.sense.iam.service.SsoSamlService;
import com.sense.iam.service.UserService;
import com.sense.iam.tld.TldModel;

import net.sf.json.JSONObject;

/**
 *
 * 单点登录的操作类，提供对外的单点登录服务
 *
 * Description:
 *
 * @author w_jfwen
 *
 *         Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Controller
public class SSOAction extends BaseAction {

	public static String SSO_USER_SESSION_ID = "sso_user_id";

	@Resource
	private AppService appService;
	@Resource
	private AccountService accountService;
	@Resource
	private UserService userService;
	@Resource
	private SsoFormbaseService ssoFormbaseService;
	@Resource
	private SsoJwtService ssoJwtService;
	@Resource
	private SsoSamlService SsoSamlService;
	@Resource
	private SsoOidcService ssoOidcService;
	@Resource
	private SsoCasService ssoCasService;
	@Resource
	private SsoAnalogInputService ssoAnalogInputService;

	@Resource
	private TldModel tldModel;
	@Resource
	private SsoOauthService ssoOauthService;
	@Resource
	private SsoSamlService ssoSamlService;
	@Resource
	private SsoLtpaService ssoLtpaService;
	@Resource
	private JdbcService jdbcService;

	@Resource
	private PolicyManager policyManager;

	//定义ssoToken超时时长默认8小时
	private final Long SSO_TIME_OUT = 8 * 60 * 60 * 1000L;

	@Resource
	private CompanyCache companyCache;

	@Resource
	private SysConfigCache sysConfigCache;

	/**
	 * 兼容老的OAuth协议应用
	 */
	private static String oldOauth2Apps;
	@Value("${sso.old.oauth2.apps}")
	public void setOldOauth2Apps(String oldOauth2Apps) {
		this.oldOauth2Apps = oldOauth2Apps;
	}

	/**
	 * SSO加入ssoToken单点登录,用于跨浏览器根据ssoToken完成单点登录认证
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping("request")
	public String sso() throws Exception {
		String tokenId= GatewayHttpUtil.getParameterForHtml("tokenId", request);
		String ssoToken= GatewayHttpUtil.getParameterForHtml("ssoToken", request);
		String client_id= GatewayHttpUtil.getParameterForHtml("client_id", request);
		String redirectUrl=GatewayHttpUtil.getParameterForHtml("redirectUrl", request);
		log.info("tokenId="+tokenId);
		log.info("redirectUrl="+redirectUrl);
		log.info("client_id="+client_id);
		log.info("ssoToken="+ssoToken);
		if(ssoToken != null){
			CurrentAccount.setCurrentAccount(null);
			//解密
			ssoToken=UIM.decode(ssoToken);
			tokenId=ssoToken.split("_")[0];
			String time=ssoToken.split("_")[1];
			long timeT=Long.valueOf(time);
			log.info("sso decode timeT="+timeT);
			log.info("sso decode tokenId="+tokenId);
			//如果时间小于10秒钟，则放行
			if(System.currentTimeMillis()-timeT < SSO_TIME_OUT){
				//正常时间范围内，放行SSO
				log.info("time ok action sso...");
			}else{
				log.info("timeout...");
				Map resultMap =new HashMap();
				resultMap.put("code", "403");
				resultMap.put("errormsg", "token timeout");
				super.printERROR(resultMap.toString(), response);
				return null;
			}

			CurrentAccount account=new CurrentAccount(companyCache.getCompanySn(GatewayHttpUtil.getKey("RemoteHost", request)));
			List<Map<String, Object>> list= jdbcService.findList("select USER_ID,LOGIN_NAME from im_account where id=?",tokenId);
			account.setUserId(Long.valueOf(list.get(0).get("USER_ID").toString()));
			account.setAuthLeavel(1);
			account.setDestSrc(new ArrayList<String>());
			account.setLoginName(list.get(0).get("LOGIN_NAME").toString());
			account.setRemoteHost(GatewayHttpUtil.getKey("RemoteHost", request));
			account.setId(Long.valueOf(tokenId));
			account.setSessionId(GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request));
			CurrentAccount.setCurrentAccount(account);

		}
		log.info("tokenId="+tokenId+",client_id:"+client_id);
		//追加判断，如果单点登录ID==null的话，则根据client_id+已认证用户获取应用ID,
		if(tokenId==null&&client_id != null){
			//没有的话重定向到认证页面，并且添加重定向URL
			if(CurrentAccount.getCurrentAccount() == null){
				String redirects=GatewayHttpUtil.getKey("RemoteServer", request)+"/portal/login.html?redirectUri="+GatewayHttpUtil.getKey("RemoteServer", request)+"/sso/request?client_id="+client_id;
				log.info("redirects="+redirects);
				super.sendRedirect(redirects, response);
				return null;
			}else{
				/*if(CurrentAccount.getCurrentAccount().isValid()){
					super.sendRedirect(GatewayHttpUtil.getKey("RemoteServer", request)+"/portal/force/pwd.html?client_id="+client_id, response);
					return null;
				}*/
				try{
					//查询账户
					List<Map<String, Object>> accMap=jdbcService.findList("select id from im_account where app_id in(select id from im_app where sn=?) and user_id=?",client_id,CurrentAccount.getCurrentAccount().getUserId());
					if(accMap!=null&&accMap.size()==1){
						tokenId=accMap.get(0).get("id").toString();
						if(redirectUrl != null){
							super.sendRedirectT(GatewayHttpUtil.getKey("RemoteServer", request)+"/sso/request?tokenId="+tokenId, redirectUrl, response);
							return null;
						}
					}else{
						Map resultMap =new HashMap();
						resultMap.put("code", "500");
						resultMap.put("errormsg", "account size :"+accMap.size());
						super.printERROR(resultMap.toString(), response);
						return null;
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}

		log.info("sso token " + tokenId);
		if (tokenId != null) {
			try{
				CurrentAccount currentAccount = CurrentAccount.getCurrentAccount();
				// 获取账号信息
				Account account = accountService.findById(Long.valueOf(tokenId));
				log.info(currentAccount.getUserId() +"==="+account.getUserId());
				if(currentAccount.getUserId() !=+account.getUserId()){
					request.setAttribute("error", "token timeout");
					super.print403(response);
					return null;
				}
				// 获取应用信息
				App app = appService.findById(account.getAppId());
				//如果是跨浏览器单点登录，则直接放行二次认证
				if(ssoToken != null){
					List<String> destSrc=new ArrayList<String>();
					destSrc.add("/"+app.getSn());
					CurrentAccount.getCurrentAccount().setDestSrc(destSrc);
				}
				if(account.getStatus().intValue() != Constants.ACCOUNT_ENABLED){
					Map resultMap =new HashMap();
					resultMap.put("code", "401");
					resultMap.put("errormsg", "account disabled");
					super.printERROR(resultMap.toString(), response);
					return null;
				}

				//判断认证场景
				SSORequest ssoRequest=new SSORequest();
				ssoRequest.setCurrentLevel(CurrentAccount.getCurrentAccount().getAuthLeavel());
				ssoRequest.setLoginIp(GatewayHttpUtil.getKey("RemoteIp", request));
				ssoRequest.setAllowRes(CurrentAccount.getCurrentAccount().getDestSrc());
				//使用用户工号
				User u=userService.findById(CurrentAccount.getCurrentAccount().getUserId());
				ssoRequest.setUsername(u.getSn());
				try{
					policyManager.doFilter(ssoRequest, "/"+app.getSn());
				}catch(StrongAuthenticationException e){
					request.setAttribute("destSrc", "/"+app.getSn());
					request.setAttribute("redirectUri", getRedirectUrl());
					request.setAttribute("username", ssoRequest.getUsername());
					request.setAttribute("telephone", u.getTelephone());
					request.setAttribute("QYWX_APPID", sysConfigCache.QYWX_APPID);
					request.setAttribute("QYWX_SCA_APP_AGENTID", sysConfigCache.QYWX_SCA_APP_AGENTID);
					request.setAttribute("QYWX_SCA_APP_KEY", sysConfigCache.QYWX_SCA_APP_KEY);
					request.setAttribute("QYWX_SCA_REDIRECT_URI", sysConfigCache.QYWX_SCA_REDIRECT_URI.replaceAll("/portal/qywxlogin.action", "/sso/scanQYWX"));
					request.setAttribute("DINGDING_APPID", sysConfigCache.DINGDING_APPID);
					request.setAttribute("DINGDING_SCA_REDIRECT_URI", sysConfigCache.DINGDING_SCA_REDIRECT_URI.replaceAll("/portal/dingdinglogin.action", "/sso/scanDD"));
					return "sso/extauth/"+e.getMessage();
				}catch(BlackUserException e){
					return "sso/error/black";
				}catch(ResourceAuthUserException e){
					return "sso/error/noaccess";
				}catch(TimePolicyException e){
					return "sso/error/time";
				}catch(IpPolicyException e){
					return "sso/error/ip";
				}catch(Exception e){
					log.error("sso exception",e);
					return "sso/error/exception";
				}
				int isBrowser=isBrowser();
				if(app.getBrowserType()!=0){
					if(app.getBrowserType().intValue()!=isBrowser){
						String type="";
						String ssoBrowseURL="";
						if(app.getBrowserType().intValue()==1){
							type="ie:";
						}else if(app.getBrowserType().intValue()==2){
							type="schrome:";
						}else if(app.getBrowserType().intValue()==3){
							type="firefox:";
						}

						ssoBrowseURL=type+GatewayHttpUtil.getKey("RemoteServer", request)+"/sso/request?ssoToken="+UIM.encode(tokenId+"_"+System.currentTimeMillis()+"_"+GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request));
						log.info("ssoBrowseURL:"+ssoBrowseURL);
						super.sendRedirectBrowser(ssoBrowseURL, response);
						return null;
					}
				}


				// 加载用户信息
				if (app != null && app.getSsoType() != null) {

					if (app.getSsoType() == Constants.SSO_FORMBASE) {// 一次性穿透
						log.info("======FORMBASE SSO======");
						Formbase formbase = new Formbase();
						formbase.setAppId(account.getAppId());
						formbase = ssoFormbaseService.findByObject(formbase);
						Map content = tldModel.getBasicTld();
						User user=userService.findById(account.getUserId());
						content.put("user", user);
						content.put("account", account);
						content.put("redirectUrl", StringUtils.getString(request.getParameter("redirectUrl")));
						content.put("ssoToken", UIM.encode(tokenId+"_"+System.currentTimeMillis()+"_"+GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request)));
						request.setAttribute("account", account);
						request.setAttribute("user", user);
						request.setAttribute("app", app);
						request.setAttribute("ssoParam", StringParse.parse(formbase.getConfig(), content));
						com.sense.iam.model.sso.Log log=new com.sense.iam.model.sso.Log();
						log.setUserName(CurrentAccount.getCurrentAccount().getLoginName());
						log.setAccountId(Long.valueOf(tokenId));
						log.setSsoType(Constants.SSO_FORMBASE);
						log.setCompanySn(CurrentAccount.getCurrentAccount().getCompanySn());
						queueSender.send(log);
						return "sso/formbase/submit";
					}
					if (app.getSsoType() == Constants.SSO_JWT) {
						log.info("======JWT SSO======");
						Jwt jwt = new Jwt();
						jwt.setAppId(account.getAppId());
						jwt = ssoJwtService.findByObject(jwt);
						Map content = tldModel.getBasicTld();
						User user=userService.findById(account.getUserId());
						content.put("user", user);
						content.put("account", account);
						content.put("redirectUrl", StringUtils.getString(request.getParameter("redirectUrl")));
						content.put("ssoToken", UIM.encode(tokenId+"_"+System.currentTimeMillis()+"_"+GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request)));
						request.setAttribute("account", account);
						request.setAttribute("user", user);
						request.setAttribute("app", app);
						request.setAttribute("ssoParam", StringParse.parse(jwt.getConfig(), content));
						com.sense.iam.model.sso.Log log=new com.sense.iam.model.sso.Log();
						log.setUserName(CurrentAccount.getCurrentAccount().getLoginName());
						log.setAccountId(Long.valueOf(tokenId));
						log.setSsoType(Constants.SSO_JWT);
						log.setCompanySn(CurrentAccount.getCurrentAccount().getCompanySn());
						queueSender.send(log);
						return "sso/jwt/submit";
					}
					if (app.getSsoType() == Constants.SSO_SAML) {// 一次性穿透
						log.info("======SAML SSO======");
						Saml saml = new Saml();
						saml.setAppId(account.getAppId());
						saml = ssoSamlService.findByObject(saml);
						String appUrl = "";
						if (StringUtils.isEmpty(saml.getStartUrl())) {
							appUrl = GatewayHttpUtil.getKey("RemoteServer", request) + "/sso/saml/"
									+ app.getSn() + "/login";
						} else {
							appUrl = saml.getStartUrl();
						}
						request.setAttribute("appUrl", appUrl);
						return "sso/saml/submit";
					}
					if (app.getSsoType() == Constants.SSO_OAUTH) {// 需要提供token回查接口
						log.info("======OAUTH SSO======");
						Oauth oauth=new Oauth();
						oauth.setAppId(account.getAppId());
						oauth = ssoOauthService.findByObject(oauth);
						request.setAttribute("tokenId", tokenId);
						request.setAttribute("appUrl", oauth.getDefaultUrl());
						request.setAttribute("client_id", app.getSn());
						if(oldOauth2Apps.contains(app.getSn())){
							request.setAttribute("response_type", "token");
						}else{
							request.setAttribute("response_type", "code");
						}
						return "sso/oauth/submit";
					}
					if (app.getSsoType() == Constants.SSO_OIDC) {// 需要提供token回查接口
						log.info("======OIDC SSO======");
						Oidc oidc =new Oidc();
						oidc.setAppId(account.getAppId());
						oidc=ssoOidcService.findByObject(oidc);
						request.setAttribute("tokenId", tokenId);
						request.setAttribute("appUrl", oidc.getDefaultUrl());
						request.setAttribute("client_id", app.getSn());
						return "sso/oidc/submit";
					}
					if (app.getSsoType() == Constants.SSO_CAS) {
						log.info("======CAS SSO======");
						Cas cas =new Cas();
						cas.setAppId(account.getAppId());
						cas=ssoCasService.findByObject(cas);
						request.setAttribute("tokenId", tokenId);
						request.setAttribute("appUrl", cas.getDefaultUrl());
						request.setAttribute("client_id", app.getSn());
						return "sso/cas/submit";
					}
					if (app.getSsoType() == Constants.SSO_LTPA) {
						log.info("======SSO_LTPA SSO======");
						Ltpa ltpa =new Ltpa();
						ltpa.setAppId(account.getAppId());
						ltpa=ssoLtpaService.findByObject(ltpa);
						request.setAttribute("tokenId", tokenId);
						request.setAttribute("appUrl", ltpa.getDefaultUrl());
						request.setAttribute("client_id", app.getSn());
						return "sso/ltpa/submit";
					}
				}

				return "sso/401";
			}catch (Exception e) {
				e.printStackTrace();
				super.sendRedirect(GatewayHttpUtil.getKey("RemoteServer", request)+"/500.html", response);
				return null;
			}
		}else{
			Map resultMap =new HashMap();
			resultMap.put("code",Constants.OPERATION_NOT_EXIST);
			resultMap.put("errormsg", "SSO ACCOUNT NOT EXIST");
			super.printERROR(resultMap.toString(), response);
			return null;
		}

	}

	private String getRedirectUrl() {

		Map<String, String[]> map = request.getParameterMap();
		StringBuffer strBuf = new StringBuffer();
		if (map != null) {
			int length = 1;
			for (Map.Entry<String, String[]> entry : map.entrySet()) {
				if(length == map.entrySet().size()){
					strBuf.append(entry.getKey()).append("=").append(request.getParameter(entry.getKey()));
				}else{
					strBuf.append(entry.getKey()).append("=").append(request.getParameter(entry.getKey())).append("&");
				}
				length++;
			}
		}
		return GatewayHttpUtil.getKey("RemoteServer", request) + "/sso/request?" + strBuf.toString();
	}



		@RequestMapping("updatePersonsPwd")
	@ResponseBody
	public ResultCode updatePwd(Long id,String oldPwd,String newPwd){
		ResultCode rc=new ResultCode();
		if(CurrentAccount.getCurrentAccount()==null || id==null || id.longValue()!=CurrentAccount.getCurrentAccount().getId().longValue()){//判断当前用户是否还能正常使用
			rc.setCode(Constants.OPERATION_NOT_ALLOW);
			return rc;
		}
		Account account = accountService.findById(id);
		if(account==null){
			rc.setCode(Constants.OPERATION_NOT_EXIST);
			return rc;
		}
		if(oldPwd==null || !oldPwd.equals(UIM.decode(account.getLoginPwd()))){
			rc.setCode(Constants.FORM_VALIDATOR_FAIL);//历史密码错误
			return rc;
		}
		accountService.updatePwd(new Long[]{id}, newPwd);
		rc.setCode(Constants.OPERATION_SUCCESS);
		return rc;
	}


	/**
	 * 企业微信扫码二次认证重定向接口
	 * @return
	 */
	@RequestMapping(value ="scanQYWX")
	public String scanQYWX() {
		String baseData=cn.hutool.core.codec.Base64.decodeStr(request.getParameter("baseData"));
		log.info("baseData:"+baseData);
		if(baseData!=null&&baseData.indexOf("||")>0){
			request.setAttribute("destSrc", baseData.split("\\|\\|")[1]);
			request.setAttribute("redirectUri", baseData.split("\\|\\|")[0]);
		}
		request.setAttribute("code", request.getParameter("code"));
		return "sso/extauth/scan/qywxlogin";
	}


	/**
	 * 钉钉扫码二次认证重定向接口
	 * @return
	 */
	@RequestMapping(value ="scanDD/{baseData}")
	public String scanDindDind(@PathVariable String baseData) {
		baseData=cn.hutool.core.codec.Base64.decodeStr(baseData);
		log.info("baseData:"+baseData);
		if(baseData!=null&&baseData.indexOf("||")>0){
			request.setAttribute("destSrc", baseData.split("\\|\\|")[1]);
			request.setAttribute("redirectUri", baseData.split("\\|\\|")[0]);
		}
		request.setAttribute("code", request.getParameter("code"));
		return "sso/extauth/scan/dingdinglogin";
	}


	@RequestMapping(value ="getAnalogInputInfo",produces = "application/json;charset=utf-8")
	@ResponseBody
	public String getAnalogInputInfo(Long accountId) {
		Account account = accountService.findById(accountId);
		AnalogInput analogInput = new AnalogInput();
		analogInput.setAppId(account.getAppId());
		analogInput = ssoAnalogInputService.findByObject(analogInput);
		JSONObject json = new JSONObject();
		json.put("loginName", account.getLoginName());
		json.put("password", UIM.decode(account.getLoginPwd()));
		json.put("config", analogInput.getConfig());
		return json.toString();
	}




	@RequestMapping("bindAcctPwd")
	@ResponseBody
	public ResultCode bindAcctPwd(Long id,String loginName,String loginPwd){
		ResultCode rc=new ResultCode();
		if(CurrentAccount.getCurrentAccount()==null){

			if(id==null||id==0){
				rc.setCode(Constants.OPERATION_NOT_ALLOW);
				return rc;
			}

			CurrentAccount account=new CurrentAccount(companyCache.getCompanySn(GatewayHttpUtil.getKey("RemoteHost", request)));
			CurrentAccount.setCurrentAccount(account);
		}
		Account account = accountService.findById(id);
		if(account==null || account.getUserId().longValue()!=(accountService.findById(CurrentAccount.getCurrentAccount().getId()).getUserId().longValue())){
			rc.setCode(Constants.OPERATION_NOT_EXIST);
			return rc;
		}
		account.setLoginName(loginName);
		account.setLoginPwd(loginPwd);
		accountService.edit(account);
		accountService.updatePwd(new Long[]{id}, loginPwd);
		rc.setCode(Constants.OPERATION_SUCCESS);
		return rc;
	}

}
