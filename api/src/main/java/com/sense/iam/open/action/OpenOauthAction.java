package com.sense.iam.open.action;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set; 

import javax.annotation.Resource;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.json.JSONObject;

import com.sense.core.security.Md5;
import com.sense.core.util.CurrentAccount;
import com.sense.core.util.GatewayHttpUtil;
import com.sense.core.util.HttpUtil;
import com.sense.core.util.StringUtils;
import com.sense.iam.api.SessionManager;
import com.sense.iam.api.action.BaseAction;
import com.sense.iam.api.action.sys.ResultCodeReq;
import com.sense.iam.api.model.LoginModel;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.sys.Acct;
import com.sense.iam.model.sys.Func;
import com.sense.iam.service.SysAcctService;
import com.sense.iam.service.SysFuncService;

@Api(value = "API - OAUTH单点登录接口", tags = "OAUTH单点登录接口")
@Controller
@RestController
@RequestMapping("open/sso")
public class OpenOauthAction extends BaseAction {

	String client_secret = "9df66fec-811b-4b06-9842-306d6a25";

	String client_id = "APP009";
	
	private static String redirect_uri = "http://localhost:8882/api/ssoAuth/oauth";

	private static String getAccessTokenUrl = "http://localhost:8882/sso/oauth/accessToken";

	private static String getUserInfoUrl = "http://localhost:8882/sso/oauth/userInfo";

	@Resource
	private SysFuncService sysFuncService;
	
	@Resource
	private SysAcctService sysAcctService;

	@ApiOperation(value = "OAUTH单点认证",  notes = "单点登录说明")
	@RequestMapping(value = "/oauth",method = RequestMethod.GET)
	@ResponseBody
	public void oauth(String code) {
		try {
			String params = "&client_id="+client_id+"&client_secret="+client_secret+"&grant_type=authorization_code&redirect_uri="+redirect_uri+"&code="+code;
			String access_token_info = HttpUtil.POST_API(getAccessTokenUrl, params,new HashMap<String, String>());
			JSONObject jsonObject = new JSONObject(access_token_info);
			if(null!=jsonObject.get("access_token")){
				String access_token = jsonObject.get("access_token").toString();
				String params2 = "&access_token="+access_token;
				String userInfo = HttpUtil.POST_API(getUserInfoUrl, params2,new HashMap<String, String>());
				JSONObject jsonObject2 = new JSONObject(userInfo);
				if(null!=jsonObject2.get("name")){
					String loginName = jsonObject2.get("name").toString();
					request.getSession().setAttribute("CURRENT_USER", loginName);
				}
			}
			String redirectUrl=GatewayHttpUtil.getKey("RemoteServer", request)+"/console/#/ssologin?r="+StringUtils.getSecureRandomnNumber();
			redirectUrl = redirectUrl.replaceAll("\r", "%0D");//Encode \r to url encoded value
			redirectUrl = redirectUrl.replaceAll("\n", "%0A");//Encode \n to url encoded value
			response.sendRedirect(redirectUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
