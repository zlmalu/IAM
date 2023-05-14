package com.sense.iam.portal;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sense.iam.config.RedisCache;
import com.sense.iam.model.sys.PortalSettingManage;
import com.sense.iam.portal.action.*;
import com.sense.iam.portal.res.model.ThemeElement;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.PortalSettingManageService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.impl.util.Base64;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.sense.core.util.CurrentAccount;
import com.sense.core.util.GatewayHttpUtil;
import com.sense.core.util.JWTUtil;
import com.sense.core.util.StringUtils;
import com.sense.iam.cache.CompanyCache;
import com.sense.iam.cache.SysConfigCache1;
import com.sense.iam.cam.Constants;
import com.sense.iam.model.im.Account;
import com.sense.iam.model.im.User;
import com.sense.iam.service.UserService;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

@Component
public class LoginInterceptor extends HandlerInterceptorAdapter {

	@Resource
	private PortalSettingManageService portalSettingManageService;

	private Log log = LogFactory.getLog(getClass());
	@Resource
	UserService userService;
	@Resource
	private RedisCache redisCache;
	@Resource
	private CompanyCache companyCache;
	@Resource
	private SysConfigCache1 sysConfigCache1;
	@Resource
	private AccountService accountService;
	@Autowired
	private ThemeResolver themeResolver;

	@Autowired
	LocaleResolver localeResolver;

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		CurrentAccount.setCurrentAccount(null);
		super.postHandle(request, response, handler, modelAndView);
	}

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		if (!(handler instanceof HandlerMethod)
				|| !((HandlerMethod) handler).getBean().getClass().getName()
						.startsWith("com.sense")) {
			return super.preHandle(request, response, handler);
		}
		//判断公司标识是否存在
		String companySn = companyCache.getCompanySn(GatewayHttpUtil.getKey("RemoteHost", request));

		if(org.apache.commons.lang3.StringUtils.isEmpty(companySn)){
			response.setStatus(403);
			response.getOutputStream().write("{\"code\":\"403\",\"msg\":\"company not exist \"}".getBytes());
			return false;
		}
		log.debug("sessionId="+GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request));
		HandlerMethod method = (HandlerMethod) handler;
		log.debug("execute method " + method.getBean().getClass() + ":"+ method.getMethod().getName());
		CurrentAccount account = getCurrentAccount(request,Constants.CURRENT_SSO_SESSION_ID);
		loadThemePolic(request,response);
		log.debug("readAccount==="+account);
		if (account != null && account.isValid()) {
			return super.preHandle(request, response, handler);
		} else if (
				method.getBean().getClass() == MainAction.class && (method.getMethod().getName().equals("index"))
						|| method.getBean().getClass() == LoginAction.class
						|| method.getBean().getClass() == ResetPwdAction.class
						|| method.getBean().getClass() == ImageAction.class
						|| method.getBean().getClass() == PwdPolicAction.class
				) {
			return super.preHandle(request, response, handler);
		} else {
			String redirectUrl=GatewayHttpUtil.getKey("RemoteServer", request)+"/portal/login.html?r="+StringUtils.getSecureRandomnNumber();
			redirectUrl = redirectUrl.replaceAll("\r", "%0D");//Encode \r to url encoded value
			redirectUrl = redirectUrl.replaceAll("\n", "%0A");//Encode \n to url encoded value
			response.sendRedirect(redirectUrl);
			return false;
		}
	}


	/**
	 * 判断Cookie是否存在国际化语言
	 * @param request
	 */
	public void isCookieLang(HttpServletRequest request,HttpServletResponse response){
		Cookie[] cookies = request.getCookies();
		boolean isLang=false;
		if(cookies!=null){
			for (Cookie cookie : cookies) {
				if (CookieLocaleResolver.DEFAULT_COOKIE_NAME.equals(cookie.getName())) {
					isLang=true;
				}
			}
		}
		if(!isLang){
			localeResolver.setLocale(request,response,Locale.SIMPLIFIED_CHINESE);
		}
	}

	public void loadThemePolic(HttpServletRequest request,HttpServletResponse response){
		isCookieLang(request,response);
		//门户登录管理配置
		PortalSettingManage portalSettingManage = getPortalSettingManage();
		if(portalSettingManage==null){
			request.setAttribute("isSlideEnable", true);
			request.setAttribute("isUserEnable", true);
			request.setAttribute("TITLE", "统一身份认证平台");
			request.setAttribute("COPYRIGHT", "Copyright © 2016上海申石软件有限公司 All Right Reserved");
		}else{
			request.getSession().setAttribute(Constants.PORTAL_SETTING_MANAGE_KEY,portalSettingManage);
			if(portalSettingManage.getSlideEnable()!=null && portalSettingManage.getSlideEnable().intValue()==1) {
				request.setAttribute("isSlideEnable", true);
			}else{
				request.setAttribute("isSlideEnable", false);
			}
			if(portalSettingManage.getUserEnable()!=null && portalSettingManage.getUserEnable().intValue()==1) {
				request.setAttribute("isUserEnable", true);
				//加载主题可选项
				List<ThemeElement> list=new ArrayList<>();
				if(!StringUtils.isEmpty(portalSettingManage.getThemeConfig())) {
					try{
						JSONArray data=JSONArray.fromObject(portalSettingManage.getThemeConfig());
						for(int i=0;i<data.size();i++){
							ThemeElement element=new ThemeElement();
							element.setId(data.getJSONObject(i).getString("id"));
							element.setName(data.getJSONObject(i).getString("name"));
							list.add(element);
						}
					}catch (Exception e){

					}
				}
				request.setAttribute("THEME_ELEMENT",list);
			}else{
				request.setAttribute("isUserEnable", false);
			}
			//加载主题,如果不允许个性化则强制设置主题，如果允许个性化则不强制加载默认主题
			if(!StringUtils.isEmpty(portalSettingManage.getTheme()) && portalSettingManage.getUserEnable()!=null && portalSettingManage.getUserEnable().intValue()==2){
				themeResolver.setThemeName(request, response, portalSettingManage.getTheme());
			}
			request.setAttribute("TITLE", portalSettingManage.getTitle());
			request.setAttribute("COPYRIGHT", portalSettingManage.getCopyright());
		}
	}
	public CurrentAccount getCurrentAccount(HttpServletRequest request,
			String sessionId) {
		sessionId = GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request);
		String rediesKey=Constants.CURRENT_REDIS_SESSION_ID+":"+sessionId;
		log.debug("rediesKey="+rediesKey);
		//首先从Session里面获取标识
		/*CurrentAccount account = (CurrentAccount) request.getSession().getAttribute(rediesKey);
		//判断账号不为空和允许放行和企业域不为空的情况
		if (account!=null && account.isValid() && !StringUtils.isEmpty(account.getCompanySn())){
			CurrentAccount.setCurrentAccount(account);
			return account;
		}
		*/
		//获取企业域
		CurrentAccount account = new CurrentAccount(companyCache.getCompanySn(GatewayHttpUtil.getKey("RemoteHost", request)));
		account.setSessionId(sessionId);
		account.setValid(false);
		String senseToken =redisCache.getCacheObject(rediesKey);
		String jwtToken = GatewayHttpUtil.getKey("jwtToken", request);
		if (senseToken != null) {
			log.debug(senseToken);
			// 解密Token
			try {
				senseToken = JWTUtil.parseToken(senseToken, Constants.JWT_SECRECTKEY);
				if (senseToken != null) {
					JSONObject pKjson = JSONObject.fromObject(senseToken);
					log.info("current user :::::::::"+pKjson);
					// 获取企业域
					account.setSessionId(sessionId);
					account.setId(Long.valueOf(pKjson.getString("accountId")));
					account.setUserId(Long.valueOf(pKjson.getString("userId")));
					account.setLoginName(pKjson.getString("loginName"));
					//设置放行标识
					account.setValid(true);
					account.setLastLoginTime(System.currentTimeMillis());
					account.setRemoteHost(GatewayHttpUtil.getKey("RemoteHost", request));

					BeanFactory factory = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
					userService = (UserService) factory.getBean(UserService.class);
					User u = userService.findById(account.getUserId());
					account.setName(u.getName());
					//设置会话存放在Session中
					request.getSession().setAttribute(rediesKey,account);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if(!StringUtils.isEmpty(jwtToken)){
			//解密Token
			try{
				jwtToken=JWTUtil.parseToken(jwtToken, Constants.JWT_SECRECTKEY);
				log.info("parseToken jwtToken="+jwtToken);
				if(jwtToken!=null){
					JSONObject pKjson=JSONObject.fromObject(jwtToken);
					//获取企业域
					account = new CurrentAccount(companyCache.getCompanySn(GatewayHttpUtil.getKey("RemoteHost", request)));
					account.setSessionId(sessionId);
					account.setId(Long.valueOf(pKjson.getString("accountId")));
					account.setUserId(Long.valueOf(pKjson.getString("userId")));
					account.setLoginName(pKjson.getString("loginName"));
					if(pKjson.containsKey("destSrc")){
						account.setDestSrc(JSONArray.toList(pKjson.getJSONArray("destSrc")));
					}
					//判断是否需要修改密码
					if(pKjson.containsKey("validataPwd")){
						if(pKjson.getInt("validataPwd")==1){
							account.setValid(true);
						}else{
							account.setValid(false);
						}
					}else{
						account.setValid(false);
					}
					account.setLastLoginTime(System.currentTimeMillis());
					account.setRemoteHost(GatewayHttpUtil.getKey("RemoteHost", request));
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}else {
			//获取header参数，
			String nameField=sysConfigCache1.getValue("header.user.name");
			log.info("获取到的头部KEY:"+nameField);
			if(!StringUtils.isEmpty(nameField)) {
				String headerUserName=GatewayHttpUtil.getKey(nameField, request);
				log.info("获取到的头部值："+headerUserName);
				if(!StringUtils.isEmpty(headerUserName)) {
					String headerAppSn=StringUtils.isEmpty(sysConfigCache1.getValue("header.app.sn"))?"APP001":sysConfigCache1.getValue("header.app.sn");
					//查询账号是否存在
					// 查询用户对应账号并判断用户是否存在多账号
					Account imAccount = new Account();
					imAccount.setAppSn(headerAppSn);
					imAccount.setLoginName(headerUserName);
					imAccount.setStatus(Constants.ACCOUNT_ENABLED);// 进行OAUTH的帐号必须是启用帐号
					imAccount.setIsControl(false);
					List<Account> list = accountService.findList(imAccount);
					if(list!=null&&list.size()>0) {
						//获取企业域
						account = new CurrentAccount(companyCache.getCompanySn(GatewayHttpUtil.getKey("RemoteHost", request)));
						account.setSessionId(sessionId);
						account.setId(list.get(0).getId());
						account.setUserId(list.get(0).getUserId());
						account.setLoginName(headerUserName);
						account.setValid(true);
						account.setLastLoginTime(System.currentTimeMillis());
						account.setRemoteHost(GatewayHttpUtil.getKey("RemoteHost", request));
					}
				}
			}
		}
		CurrentAccount.setCurrentAccount(account);
		return account;
	}


    /**
     * 优先从redis里面获取对象，如果没有则从数据库获取
     * @return
     */
    public PortalSettingManage getPortalSettingManage(){
        PortalSettingManage portalSettingManage=null;
        try {
            //获取根据session 获取认证对象
            String value=redisCache.getCacheObject(Constants.PORTAL_SETTING_MANAGE_KEY);
            log.debug("get PortalSettingManage data:"+value);
            if(value!=null){
                ObjectInputStream oos=new ObjectInputStream(new ByteArrayInputStream(Base64.decode(value.getBytes())));
                portalSettingManage= (PortalSettingManage)oos.readObject();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //优先从redis里面获取对象，如果没有则从数据库获取并且从新在redis生成对象
        if(portalSettingManage == null){
			List<PortalSettingManage> find = portalSettingManageService.findAll();
			if(find!=null&&find.size()>0) {
				portalSettingManage = find.get(0);
			}
            if(portalSettingManage == null)return null;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(portalSettingManage);
                oos.close();
                String value = new String(Base64.encode(bos.toByteArray()));
				redisCache.setCacheObject(Constants.PORTAL_SETTING_MANAGE_KEY,value);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return portalSettingManage;
    }

	public boolean isAllowAccess(CurrentAccount account, HandlerMethod method) {
		return true;
	}

}
