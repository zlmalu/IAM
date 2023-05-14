package com.sense.iam.auth.controller;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sense.iam.config.RedisCache;
import com.sense.iam.policy.RedisLoginaPolicy;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sense.core.exception.UserAuthentionException;
import com.sense.core.util.AddressUtils;
import com.sense.core.util.ContextUtil;
import com.sense.core.util.CurrentAccount;
import com.sense.core.util.GatewayHttpUtil;
import com.sense.core.util.HttpUtil;
import com.sense.core.util.JWTUtil;
import com.sense.core.util.StringUtils;
import com.sense.core.util.TimeUtil;
import com.sense.iam.auth.SendMail;
import com.sense.iam.auth.cache.AccessTokenCache;
import com.sense.iam.cache.AuthReamCache;
import com.sense.iam.cache.CompanyCache;
import com.sense.iam.cache.ImageCache;
import com.sense.iam.cache.LoginPolicyCache;
import com.sense.iam.cache.SysCompomentCache;
import com.sense.iam.cache.SysConfigCache;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.AuthInterface;
import com.sense.iam.compoment.FindInterface;
import com.sense.iam.model.am.FindModule;
import com.sense.iam.model.am.LoginModule;
import com.sense.iam.model.am.PwdPolicy;
import com.sense.iam.model.im.Account;
import com.sense.iam.model.im.Org;
import com.sense.iam.model.im.PropelingLogin;
import com.sense.iam.model.im.User;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.AmPwdPolicyService;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.PropelingLoginService;
import com.sense.iam.service.SysCompanyService;
import com.sense.iam.service.UserMultiOrgService;
import com.sense.iam.service.UserService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RestController
public class AuthController extends BaseController{
	@Resource
	private RedisCache redisCache;

	@Resource
	private AuthReamCache authReamCache;

	@Resource
	private SysConfigCache sysConfigCache;

	@Resource
	private ImageCache imageCache;

	@Resource
	private AccountService accountService;

	@Resource
	private SysCompomentCache sysCompomentCache;

	@Resource
	private SysCompanyService sysCompanyService;

	@Resource
	private ApplicationContext applicationContext;

	@Resource
	private AccessTokenCache accessTokenCacke;

	@Resource
	private CompanyCache companyCache;
	@Resource
	private JdbcService jdbcService;
	@Resource
	private UserService userService;

	@Resource
	private StringRedisTemplate stringRedisTemplate;

	@Resource
	private PropelingLoginService propelingLoginService;

	@Resource
	private RedisLoginaPolicy redisLoginaPolicy;

	@Resource
	private LoginPolicyCache loginPolicyCache;



	private static String isRecordAdr;
	@Value("${login.record.address.enable}")
	public void setIsRecordAdr(String isRecordAdr) {
		this.isRecordAdr = isRecordAdr;
	}

	/**
	 *
	 * 输出HTML并且重定向
	 * @param response
	 */
	public void writerHtmlContent(String message,String redirect,HttpServletResponse response){
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=utf-8");
		response.setStatus(200);
		try {
			if(redirect==null){
				response.getWriter().println("<html><body><script>alert('not resources')</script></body></html>");
				response.getWriter().flush();
				response.getWriter().close();
				return;
			}
			if(message==null){
				response.getWriter().println("<html><body><script>location.href='"+redirect+"'</script></body></html>");
			}else{
				response.getWriter().println("<html><body><script>alert('"+message+"');location.href='"+redirect+"'</script></body></html>");
			}
			response.getWriter().flush();
			response.getWriter().close();
			return;
		} catch (IOException e) {
			//ignore
		}
	}

	/**
	 * 二次认证
	 * @param username
	 * @param password
	 * @param destSrc
	 * @param params
	 * @return
	 */
	@RequestMapping(value="extAuthenticate", method={RequestMethod.GET,RequestMethod.POST}, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public ResultCode extAuthenticate(@RequestParam String username, @RequestParam String password,String destSrc,@RequestParam Map<String, Object> params){
		destSrc=StringEscapeUtils.escapeHtml(destSrc);
		//加载认证域
		String reamlSn = StringUtils.getString(params == null || params.get("reamId") == null ? "1001" : params.get("reamId"));
		CurrentAccount currentAccount=CurrentAccount.getCurrentAccount();
		log.info("currentAccount:"+currentAccount);
		if(currentAccount==null){
			//没有经过第一次认证
			return new ResultCode(Constants.LOGIN_STATUS_UN_FINISHED, "account fist auth not action");
		}
		Account account=null;
		//获取用户查找模块注册用户
		try{
			//查获取查找模块
			ContextUtil.context= applicationContext;
			List<FindModule> findModules=authReamCache.findListFindModuleByReamlSn(reamlSn);
			log.info("findModules size :"+findModules.size());
			if(findModules!=null && findModules.size()>0){
				log.info(CurrentAccount.getCurrentAccount().getCompanySn());
				FindInterface findInterface=(FindInterface)getCompoment(findModules.get(0).getComponentsId(),findModules.get(0).getRunClass(),findModules.get(0).getConfig());
				account=(Account) findInterface.find(username);
				log.info("find account:"+account);
				if (account != null) {// 判断账号是否存在
					username=account.getLoginName();
					//判断当前登录用户和扫码认证的是否一致
					if(currentAccount!=null&&(currentAccount.getId().longValue()!=account.getId())){
						return new ResultCode(Constants.OPERATION_NOT_ALLOW);
					}
				}
			}else{
				return new ResultCode(Constants.LOGIN_STATUS_UNKNOWN, "user find module not exist");
			}

		}catch(Exception e){
			e.printStackTrace();
			return new ResultCode(Constants.LOGIN_STATUS_UNKNOWN, e.getMessage());
		}
		if (account == null) {// 判断账号是否存在
			return new ResultCode(Constants.LOGIN_STATUS_NOT_EXIST, "账号或者密码错误");
		}
		if (account.getStatus().intValue() != Constants.ACCOUNT_ENABLED) {// 判断账号是否禁用
			return new ResultCode(Constants.LOGIN_STATUS_ACCOUNT_DISABLED, "账号被禁用");
		}

		List<LoginModule> loginModules = authReamCache.findListLoginModuleByReamlSn(reamlSn);
		if (loginModules != null && loginModules.size() > 0) {
			LoginModule loginModule = loginModules.get(0);
			try {
				AuthInterface ai = (AuthInterface) getCompoment(loginModule.getComponentsId(),loginModule.getRunClass(),loginModule.getConfig());
				ai.authentication(username, password, params);
				// 移除登陆模块
				loginModules.remove(0);
				// 记录日志
				writerLog(username, "extAuthenticate", Constants.OPT_SUCCESS, loginModule.getName()+"二次认证");

				String sessionId = GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request);
				log.info("sessionId="+sessionId);
				String rediesKey=Constants.CURRENT_REDIS_SESSION_ID+":"+sessionId;
				String senseToken=redisCache.getCacheObject(rediesKey);
				log.info("senseToken:"+senseToken);
				if(senseToken!=null&&senseToken.length()>0){
					//解密
					senseToken = JWTUtil.parseToken(senseToken, Constants.JWT_SECRECTKEY);
					//格式化json数据
					JSONObject pKjson=JSONObject.fromObject(senseToken);
					//添加二次认证资源路径
					if(destSrc!=null&&destSrc.length()>0){
						//判断是否已存在二次认证资源
						if(pKjson.containsKey("destSrc")){
							JSONArray destSrcArray=pKjson.getJSONArray("destSrc");
							destSrcArray.add(destSrc);
							pKjson.put("destSrc", destSrcArray);
						}else{
							JSONArray destSrcArray= new JSONArray();
							destSrcArray.add(destSrc);
							pKjson.put("destSrc", destSrcArray);
						}
					}
					String newpKjson=JWTUtil.generateToken(pKjson.toString(),Constants.JWT_SECRECTKEY,System.currentTimeMillis()+sysConfigCache.SESSION_TIMEOUT*1000);

					redisCache.setCacheObject(Constants.CURRENT_REDIS_SESSION_ID+":"+sessionId,newpKjson,SysConfigCache.SESSION_TIMEOUT.intValue(),TimeUnit.MILLISECONDS);

				}
				return new ResultCode(Constants.LOGIN_STATUS_SUCCESS);
			}catch (UserAuthentionException ex) {
				ex.printStackTrace();
				log.error("user authention fail:"+ex.getMessage());
				writerLog(username, "extAuthenticate", Constants.OPT_FAIL,loginModule.getName() + ":" + ex.getMessage()+"二次认证");
				return new ResultCode(Constants.LOGIN_STATUS_AUTH_ERROR);

			} catch (Exception e) {
				e.printStackTrace();
				log.error("validate error",e);;
				return new ResultCode(Constants.LOGIN_STATUS_UNKNOWN);
			}
		}
		return new ResultCode(Constants.LOGIN_STATUS_UNKNOWN);
	}




	@RequestMapping(value="authenticate", method={RequestMethod.GET,RequestMethod.POST}, produces = "application/json;charset=UTF-8")
	@ResponseBody
    public Object authenticate(@RequestParam String username, @RequestParam String password,String destSrc,@RequestParam Map<String, Object> params){
		destSrc=StringEscapeUtils.escapeHtml(destSrc);
		//是否允许跨域请求认证接口
		if(sysConfigCache.AUTH_ALLOW_ORIGIN.intValue()==1){
			response.setHeader("Access-Control-Allow-Origin", "*");
	        response.setHeader("Access-Control-Allow-Methods", "POST,GET,OPTIONS,DELETE,HEAD,PUT,PATCH");
	        response.setHeader("Access-Control-Max-Age", "36000");
	        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept,Authorization,authorization");
	        response.setHeader("Access-Control-Allow-Credentials","true");
		}
		//从head获取sessionId

		String sessionId=GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request);
		log.info("session="+sessionId);
		log.info("RemoteHost: "+GatewayHttpUtil.getKey("RemoteHost",request));
		log.info("senseToken: "+GatewayHttpUtil.getKey("senseToken",request));
		//log.info("params: "+params.toString());
		//查询公司编码
		String companySn=companyCache.getCompanySn(GatewayHttpUtil.getKey("RemoteHost",request));
		if(StringUtils.isEmpty(companySn)){
			return new ResultCode(Constants.COMPANY_SN_NOT_EXIST, "访问域名无权限");
		}
		// 加载认证域
		String reamlSn = StringUtils.getString(params == null || params.get("reamId") == null ? "1001" : params.get("reamId"));
		// 加载认证源设备
		String device = StringUtils.getString(params == null || params.get("device") == null ? "PC" : params.get("device"));
		if(device.equals("1")){
			device="Android";
		}else if(device.equals("2")){
			device="IOS";
		}else{
			device="PC";
		}
		log.info("companySn:"+companySn);
		Account account=null;
		//获取用户查找模块注册用户
		try{
			//查获取查找模块
			CurrentAccount cat = new CurrentAccount(companySn);
			CurrentAccount.setCurrentAccount(cat);
			ContextUtil.context= applicationContext;
			List<FindModule> findModules=authReamCache.findListFindModuleByReamlSn(reamlSn);
			log.info("findModules size :"+findModules.size());
			if(findModules!=null && findModules.size()>0){
				log.info(CurrentAccount.getCurrentAccount().getCompanySn());
				FindInterface findInterface=(FindInterface)getCompoment(findModules.get(0).getComponentsId(),findModules.get(0).getRunClass(),findModules.get(0).getConfig());
				account=(Account) findInterface.find(username);
				log.info("find account:"+account);
			}else{
				return new ResultCode(Constants.LOGIN_STATUS_UNKNOWN, "user find module not exist");
			}
		}catch(Exception e){
			return new ResultCode(Constants.LOGIN_STATUS_UNKNOWN, e.getMessage());
		}
		if (account == null) {// 判断账号是否存在
			return new ResultCode(Constants.LOGIN_STATUS_NOT_EXIST, "account not exist");
		}
		if (account.getStatus() != null && account.getStatus().intValue() != Constants.ACCOUNT_ENABLED) {// 判断账号是否禁用
			return new ResultCode(Constants.LOGIN_STATUS_ACCOUNT_DISABLED, "account is disabled");
		}
		List<LoginModule> loginModules = authReamCache.findListLoginModuleByReamlSn(reamlSn);

		//登录安全策略--判断用户是被锁住
		if(redisLoginaPolicy.isLock("auth",account.getLoginName())){
			return new ResultCode(Constants.LOGIN_REDIS_AUTH_LOCK, "超过最大错误次数，帐号被锁定");
		}

		CurrentAccount ca = CurrentAccount.getCurrentAccount();
		Account saccount=accountService.findById(account.getId());
		ca.setId(account.getId());
		ca.setLoginName(account.getLoginName());
		ca.setUserId(saccount.getUserId());
		ca.setName(account.getUserName());
		ca.setRemoteHost(getClientIp());
		ca.setSessionId(sessionId);
		ca.setValid(false);
		account.setUserId(saccount.getUserId());
		return valiedate(account, loginModules, account.getLoginName(), password, params,sessionId,reamlSn,destSrc,device);
	}





	/**
	 * 组件获取方法
	 * @param id
	 * @param runClass
	 * @param config
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private Object getCompoment(Long id,String runClass,String config) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		Object obj=sysCompomentCache.getModel(id, runClass, config);
		return obj;
	}

	/**
	 * 进行帐号密码校验
	 * @param account
	 * @param loginModules
	 * @param username
	 * @param password
	 * @param params
	 * @return
	 */
	private Object valiedate(Account account, List<LoginModule> loginModules, String username, String password, Map<String, Object> params,String sessionId,String reamlSn,String destSrc,String device) {
		CurrentAccount currentAccount=CurrentAccount.getCurrentAccount();
		if (loginModules != null && loginModules.size() > 0) {
			LoginModule loginModule = loginModules.get(0);
			try {

				AuthInterface ai = (AuthInterface) getCompoment(loginModule.getComponentsId(),loginModule.getRunClass(),loginModule.getConfig());
				ai.authentication(username, password, params);
				// 移除登陆模块
				loginModules.remove(0);
				// 记录日志
				writerLog(username, "authenticate", Constants.OPT_SUCCESS, loginModule.getName());

				if (loginModules.size() > 0) {
					currentAccount.setNextLoginModules(loginModules);
					return new ResultCode(Constants.LOGIN_STATUS_UN_FINISHED, sessionId);
				} else {
					currentAccount.setValid(true);
				}
				//判断登录安全策略 是否允许多会话功能
				if (!loginPolicyCache.IS_ALLOW_MUILT_SESSION()) {
					//如果不允许多会话
					//根据账号ID获取已登录的账号集合
					List<String> onlineList = redisCache.getCacheList(Constants.ONLINE_USER_PRIX + ":" + account.getCompanySn() + ":" + account.getId());
					for(String session : onlineList){
						//删除凭证
						redisCache.deleteObject(Constants.CURRENT_REDIS_SESSION_ID+":"+session);
					}
					//删除集合
					redisCache.deleteObject(Constants.ONLINE_USER_PRIX + ":" + account.getCompanySn() + ":" + account.getId());
				}
				return createBuildSuccess(loginModule,account,sessionId,reamlSn,destSrc,device);
			} catch (UserAuthentionException ex) {
				log.error("user authention fail:"+ex.getMessage());
				writerLog(username, "authenticate", Constants.OPT_FAIL,
						loginModule.getName() + ":" + ex.getMessage());
				// 校验登陆策略
				return validateLoginPolicy(account);
			} catch (Exception e) {
				log.error("validate error",e);
				return new ResultCode(Constants.LOGIN_STATUS_UNKNOWN, e.getMessage());
			}
		}

		return new ResultCode(Constants.LOGIN_STATUS_NOEXIST_LOGINMODULE, "account not login Module");
	}

	/**
	 * 创建成功认证凭证
	 * @param loginModule
	 * @param account
	 * @param sessionId
	 * @param reamlSn
	 * @param destSrc
	 * @param device
	 * @return
	 */
	public Object createBuildSuccess(LoginModule loginModule,Account account,String sessionId,String reamlSn,String destSrc,String device){
		User u = userService.findById(account.getUserId());
		JSONObject payloadM = new JSONObject();
		payloadM.put("accountId", account.getId());
		payloadM.put("loginName", account.getLoginName());
		payloadM.put("companySn",u.getCompanySn());//将当前公司编码放入，以便在日志打印中进行打印
		payloadM.put("sn",u.getSn());
		payloadM.put("name", u.getName());
		payloadM.put("userId", account.getUserId());
		payloadM.put("userName", account.getUserName());
		payloadM.put("device", device);
		payloadM.put("ip", GatewayHttpUtil.getKey("RemoteIp", request));
		if(isRecordAdr.equals("true"))payloadM.put("address", AddressUtils.toAddress(GatewayHttpUtil.getKey("RemoteIp", request)));
		payloadM.put("remark", loginModule.getName());
		payloadM.put("createTime",  TimeUtil.getHmsTime(new Date()));
		payloadM.put("isValid",  true);
		if(destSrc!=null&&destSrc.length()>0){
			JSONArray destSrcArray= new JSONArray();
			destSrcArray.add(destSrc);
			payloadM.put("destSrc", destSrcArray);
		}else{
			payloadM.put("destSrc", new JSONArray());
		}
		JSONObject payloads=JSONObject.fromObject(payloadM);
		payloads.put("validataPwd", 0);
		payloads.put("validataPwdMsg", "正常");
		log.info("payloads:"+payloads.toString());
		String jwtToken=JWTUtil.generateToken(payloads.toString(), Constants.JWT_SECRECTKEY,System.currentTimeMillis() + sysConfigCache.SESSION_TIMEOUT * 1000);
		log.info("oid sessionId="+sessionId);
		//sessionId = request.getSession(true).getId();
		log.info("new sessionId="+sessionId);

		//判断是否是移动端，如果是则保存唯一标识token，用于移动端账号唯一登录标识
		if(device.equals("Android")||device.equals("IOS")&&reamlSn.equals("1002")){
			PropelingLogin entity = new PropelingLogin();
			entity.setSn(account.getLoginName());
			List<PropelingLogin> findList = propelingLoginService.findList(entity);
			entity.setToken(UUID.randomUUID().toString());
			entity.setStatus(0);
			if(findList.size()>0){
				propelingLoginService.editStatus(entity);
			}
			else{
				propelingLoginService.save(entity);
			}
			sessionId += "," +entity.getToken();
		}
		//登录成功删除失败记录
		redisLoginaPolicy.delete("auth",account.getLoginName());
		putOnlineUser(account,sessionId);
		redisCache.setCacheObject(Constants.CURRENT_REDIS_SESSION_ID+":"+sessionId,jwtToken,SysConfigCache.SESSION_TIMEOUT.intValue(),TimeUnit.MILLISECONDS);

		return new ResultCodeReq(Constants.LOGIN_STATUS_SUCCESS,jwtToken,sessionId);
	}
	/**
	 * 用户凭证添加
	 */
	private void putOnlineUser(Account account,String sessionId) {
		List list = new ArrayList();
		list.add(sessionId);
		redisCache.setCacheList(Constants.ONLINE_USER_PRIX+":"+account.getCompanySn()+":"+account.getId(),list);
		redisCache.expire(Constants.ONLINE_USER_PRIX+":"+account.getCompanySn()+":"+account.getId(),sysConfigCache.SESSION_TIMEOUT,TimeUnit.MILLISECONDS);
	}

	/**
	 * 用户凭证移除
	 */
	private void removeOnlineUser(String sessionId) {
		//判断当前账号是否存在
		CurrentAccount currentAccount = CurrentAccount.getCurrentAccount();
		if(currentAccount != null && !StringUtils.isEmpty(currentAccount.getSessionId())){
			//移除redis凭证
			redisCache.deleteObject(Constants.CURRENT_REDIS_SESSION_ID+":"+currentAccount.getSessionId());
			//是否开启多会话功能
			if (!loginPolicyCache.IS_ALLOW_MUILT_SESSION()) {
				//如果不允许多会话
				//移除redis在线记录
				redisCache.deleteObject(Constants.ONLINE_USER_PRIX + ":" + currentAccount.getCompanySn() + ":" + currentAccount.getId());
			}else{
				//允许多会话
				//根据账号ID获取已登录的账号集合
				List<String> onlineList = redisCache.getCacheList(Constants.ONLINE_USER_PRIX + ":" + currentAccount.getCompanySn() + ":" + currentAccount.getId());
				List<String> newonlineList = new ArrayList<>();
				for(String session : onlineList){
					//如果会话ID一致，则进行移除，保留其他会话
					if(!session.equals(currentAccount.getSessionId())){
						newonlineList.add(session);
					}
				}
				//重新设置账号的在线用户
				redisCache.deleteObject(Constants.ONLINE_USER_PRIX + ":" + currentAccount.getCompanySn() + ":" + currentAccount.getId());
				redisCache.setCacheList(Constants.ONLINE_USER_PRIX + ":" + currentAccount.getCompanySn() + ":" + currentAccount.getId() , newonlineList);
			}
		}else{
			if(!StringUtils.isEmpty(sessionId)) {
				//移除redis凭证
				redisCache.deleteObject(Constants.CURRENT_REDIS_SESSION_ID + ":" + sessionId);
			}
		}
	}

	/**
	 * 校验登陆策略
	 */
	private ResultCode validateLoginPolicy(Account account) {
		String returnMsg="";
		if (account == null)returnMsg="账号或者密码错误";
		int errorCount = redisLoginaPolicy.getLockCount("auth",account.getLoginName());
		// 如果大于最大失败次数，锁定帐号
		if (errorCount==-1 || errorCount >= loginPolicyCache.LOGIN_ERROR_NUM()) {
			//accountService.disabled(new Long[] { account.getId() });
			returnMsg = "超过最大错误次数，帐号被锁定";
			redisLoginaPolicy.lock("auth",account.getLoginName());
		}else{
			redisLoginaPolicy.set("auth",account.getLoginName());
			errorCount=errorCount+1;
			returnMsg = "登录错误次数:" + errorCount;
		}
		return new ResultCode(Constants.LOGIN_STATUS_AUTH_ERROR, returnMsg);
	}

	@RequestMapping(value="logout.html", method={RequestMethod.GET,RequestMethod.POST}, produces = "application/json;charset=UTF-8")
	@ResponseBody
    public void logout(){
		String sessionId=request.getParameter("tokenId");
		if(sessionId!=null){
			log.info("logout sessionId:"+sessionId);
			List list=jdbcService.findList("select user_name from am_online_user where id=?",StringEscapeUtils.escapeSql(sessionId));
			if(list!=null&&list.size()>0){
				JSONArray data=JSONArray.fromObject(list);
				String username=data.getJSONObject(0).getString("user_name");
				CurrentAccount.setCurrentAccount(new CurrentAccount(companyCache.getCompanySn(GatewayHttpUtil.getKey("RemoteHost", request))));
				writerLog(username, "logout", Constants.OPT_SUCCESS, "系统退出");
			}
		}else{
			sessionId = request.getSession().getId();
		}
		removeOnlineUser(sessionId);
	}


	@RequestMapping(value="qrMobile.action", method={RequestMethod.GET,RequestMethod.POST}, produces = "application/json;charset=UTF-8")
	@ResponseBody
    public ResultCode qrMobile(){
		String uid=request.getParameter("uid");
		String uuid=request.getParameter("uuid");
		if(uid!=null && uuid!=null){
			accessTokenCacke.grantToken(uuid,uid);
		}
		return new ResultCode(Constants.LOGIN_STATUS_SUCCESS);
	}
}
