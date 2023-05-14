package com.sense.iam.sso.action;


import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.sense.core.util.CurrentAccount;
import com.sense.core.util.GatewayHttpUtil;
import com.sense.core.util.StringUtils;
import com.sense.iam.cache.CasCache;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.auth.AuthResultCode;
import com.sense.iam.cam.auth.OnlineUser;
import com.sense.iam.cam.auth.Token;
import com.sense.iam.cam.auth.cache.AccessTokenCache;
import com.sense.iam.model.im.Account;
import com.sense.iam.model.sso.Cas;
import com.sense.iam.service.AccountService;

/**
 * 
 * cas3.0认证协议
 * 
 * Description: 调用路径加入前缀：/cas
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Controller
@RequestMapping("cas")
public class CasAction extends BaseAction{
	@Resource
	private AccessTokenCache accessTokenCache;
	@Resource
	private AccountService accountService;
	@Resource
	private CasCache casCache;

	/**
	 * 认证跳转验证
	 */
	@RequestMapping(value="login", method = {RequestMethod.GET,RequestMethod.POST})
	public void login(HttpServletRequest request,HttpServletResponse response){
		String client_id=null;
		String redirect_uri=GatewayHttpUtil.getParameterForHtml("service", request);
		log.info("service : "+redirect_uri);
		Cas cas=casCache.getCas(redirect_uri);
		if(cas==null){
			log.info("service : "+redirect_uri+",未经过注册非法访问!");
			//返回403没有权限
			super.print403(response);
			return ;
		}
		client_id=cas.getAppSn();
		log.info("client_id : "+client_id);
		if(client_id==null){
			//返回403没有权限
			super.print403(response);
			return ;
		}
		if (iSAuthenticated()) {
			
			redirect_uri=URLDecoder.decode(redirect_uri);
			log.info("redirect_uri : "+redirect_uri);
			request.setAttribute("redirectUri", request.getRequestURL()+"?"+request.getQueryString());
			/**如果用户已登陆过**/
			//验证通过跳转到redirect_uri  加入如下参数:
			
		
			CurrentAccount currentAccount = CurrentAccount.getCurrentAccount();
			OnlineUser onlineUser=new OnlineUser();
			onlineUser.setExpried(System.currentTimeMillis()+cas.getSessionValidTime());
			onlineUser.setLoginIp(currentAccount.getRemoteHost());
			onlineUser.setLoginTime(System.currentTimeMillis());
			onlineUser.setUid(currentAccount.getLoginName());
			onlineUser.setAccountId(currentAccount.getId().toString());
			onlineUser.setValid(true);
			onlineUser.setSessionId(currentAccount.getSessionId());
			onlineUser.setAppSn(client_id);
			log.info("onlineUser : "+onlineUser.getAccountId()+",client_id="+onlineUser.getAppSn());
			String accountId = GatewayHttpUtil.getKey("tokenId", request);
			if(accountId==null){
				//是否指定单点登陆账号
				//查询用户对应账号并判断用户是否存在多账号
				Account account=new Account();
				account.setAppSn(client_id);
//				account.setLoginName(onlineUser.getUid());
				account.setUserId(accountService.findById(Long.valueOf(onlineUser.getAccountId())).getUserId());
				account.setStatus(Constants.ACCOUNT_ENABLED);//进行CAS的帐号必须是启用帐号
				account.setIsControl(false);
				List list=accountService.findList(account);
				int count=list.size();
				if(count==0){
					JSONObject result=new JSONObject();
					result.put("error", "-1");
					result.put("error_description","account num:0");
					//返回403没有权限
					super.printERROR(result.toString(),response);
					return ;
				}else if(count>1){
					JSONObject result=new JSONObject();
					result.put("error", "-1");
					result.put("error_description","account num:"+list.size()+"");
					//返回403没有权限
					super.printERROR(result.toString(),response);
					return ;
				}else{
					account=(Account) list.get(0);
					accountId=account.getId()+"";
					onlineUser.setAccountId(accountId);
					onlineUser.setUid(account.getLoginName());
				}
			}else{
				onlineUser.setAccountId(accountId);
			}
		
			//生成ticket
			Token token=accessTokenCache.grantTicketToken(onlineUser);
			token.setClientId(client_id);
			//记录cas单点登陆用户信息
			//记录cas单点登陆日志
			com.sense.iam.model.sso.Log ssoLog=new com.sense.iam.model.sso.Log();
			ssoLog.setUserName(onlineUser.getUid());
			ssoLog.setAccountId(Long.valueOf(accountId));
			ssoLog.setSsoType(Constants.SSO_CAS);
			ssoLog.setCompanySn(CurrentAccount.getCurrentAccount().getCompanySn());
			queueSender.send(ssoLog);
			if(redirect_uri.indexOf("?")!=-1){
				redirect_uri+="&ticket="+token.getId()+"&expried="+token.getExpried()+"&r="+StringUtils.getSecureRandomnNumber();
			}else{
				redirect_uri+="?ticket="+token.getId()+"&expried="+token.getExpried()+"&r="+StringUtils.getSecureRandomnNumber();
			}
			super.sendRedirect(redirect_uri, response);
			return;
		}else{
			String redirectsUrl=GatewayHttpUtil.getKey("RemoteServer", request)+"/portal/login.html?redirectUri="+URLEncoder.encode("/sso/cas/login?service="+redirect_uri)+"&r="+StringUtils.getSecureRandomnNumber();
			super.sendRedirect(redirectsUrl, response);
			return;
		}
	}
	
	/**
	 * 
	 * 用户采用ticket票据获取用户信息
	 * @param ticket
	 * @return 
			1.<cas:serviceResponse xmlns:cas="http://www.yale.edu/tp/cas">    
			2.  <cas:authenticationSuccess>    
			3.    cc    
			4.    <cas:attributes>    
			5.      <cas:FullName>LDAP Guest</cas:FullName>    
			6.      <cas:role>ROLE_USER</cas:role>    
			7.      <cas:LastName>Guest</cas:LastName>    
			8.    </cas:attributes>    
			9.  </cas:authenticationSuccess>    
			10.</cas:serviceResponse>
	 */
	@RequestMapping("serviceValidate")
	@ResponseBody
	public String serviceValidate(HttpServletRequest request,HttpServletResponse response){
		
		String accessToken=request.getParameter("ticket");
		Token token=accessTokenCache.getToken(accessToken);
		StringBuffer resultMsg=new StringBuffer();
		if(token!=null){
			resultMsg.append("<cas:serviceResponse xmlns:cas=\"http://www.yale.edu/tp/cas\">");
			resultMsg.append("<cas:authenticationSuccess>");
			resultMsg.append("<cas:user>"+((OnlineUser)token.getContent()).getUid()+"</cas:user>");
			resultMsg.append("<cas:attributes>");
			resultMsg.append("<cas:FullName>").append("").append("</cas:FullName>");
			resultMsg.append("</cas:attributes>");
			resultMsg.append("</cas:authenticationSuccess>");
			resultMsg.append("</cas:serviceResponse>");
		}else{
			resultMsg.append("<cas:serviceResponse xmlns:cas=\"http://www.yale.edu/tp/cas\">");
			resultMsg.append("<cas:authenticationFailure code='INVALID_TICKET'>");
			resultMsg.append(accessToken);
			resultMsg.append("</cas:authenticationFailure>");
			resultMsg.append("</cas:serviceResponse>");
			return "{\"error\":"+AuthResultCode.ACCESS_CHECK_ERROR+",\"error_description\":\"ticket check failed\"}";
		}
		log.info(resultMsg.toString());
		return resultMsg.toString();
	}
	
	@RequestMapping("validate")
	@ResponseBody
	public String validate(HttpServletRequest request,HttpServletResponse response){
		String accessToken=request.getParameter("ticket");
		Token token=accessTokenCache.getToken(accessToken);
		StringBuffer resultMsg=new StringBuffer();
		if(token!=null){
			resultMsg.append("yes\n");
			resultMsg.append(((OnlineUser)token.getContent()).getUid());
			
		}else{
			resultMsg.append("no\n");
			resultMsg.append("INVALID_TICKET");
		}
		log.info(resultMsg.toString());
		return resultMsg.toString();
	}
	
	private static String getDomain(String url){
        String a=url.substring(url.indexOf("//")+2);
        String b=a.substring(a.indexOf("/")+1);
        String c=b.substring(0,b.indexOf("/"));
        return a.substring(0,a.indexOf("/"))+"/"+c;
   }
}
