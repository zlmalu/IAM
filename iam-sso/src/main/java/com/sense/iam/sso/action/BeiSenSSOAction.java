package com.sense.iam.sso.action;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/*import com.beisen.provider.BeisenTokenProvider;
import com.beisen.utility.SafeTools;*/
import com.sense.iam.sso.HttpUtil;

import net.sf.json.JSONObject;


/**
 * 
 * Beisen hr buid token sso
 * 
 * Description: 北森HR单点登录URL生成器
 * 
 * @author shibanglin
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Controller
@RequestMapping("beisen/build")
public class BeiSenSSOAction extends BaseAction{

	/*
	*//**
	 * 获取token单点登录
	 * @param request
	 * @param response
	 * @return
	 *//*
	@RequestMapping(value="getOpenSSOURL",method = {RequestMethod.GET,RequestMethod.POST} ,produces="application/json; charset=utf-8")
	@ResponseBody
	public String getToken(HttpServletRequest request,HttpServletResponse response){
		JSONObject result=new JSONObject();
		String tenant_id=request.getParameter("tenant_id");
		String secret=request.getParameter("secret");
		String jobNumber=request.getParameter("jobNumber");
		if(tenant_id==null){
			result.put("error",-1);
			result.put("error_description","tenant_id not");
			return result.toString();
		}
		if(secret==null){
			result.put("error",-1);
			result.put("error_description","secret not");
			return result.toString();
		}
		if(jobNumber==null){
			result.put("error",-1);
			result.put("error_description","jobNumber not");
			return result.toString();
		}
		log.info("tenant_id="+tenant_id);
		log.info("secret="+secret);
		log.info("jobNumber="+jobNumber);
		String ssoURL=getSSOUrl(tenant_id,secret,jobNumber);
		if(ssoURL!=null&&ssoURL.length()>0){
			result.put("error",0);
			result.put("ssourl",ssoURL);
			result.put("error_description","build success");
			return result.toString();
		}else{
			result.put("error", 1);
			result.put("error_description","build fail");
			return result.toString();
		}
	}
	public static void main(String[] args) {
		//System.out.println(getSSOUrl("110604","cca2f9f28ead40d39cf81cb7e1be247a","236"));
		//test   
		System.out.println(getSSOUrl("110605","7c7eee75c0004819a09c203d724fc0bc","226"));
	}
	
	public static String getSSOUrl(String tenant_id,String secret,String jobNumber){
		try {
        	String _public_key = public_key;
        	String _private_key = private_key;
            String header = BeisenTokenProvider.GetHeader("RS256", _public_key);
            HashMap<String, Object> cls = new HashMap<String, Object>();
            cls.put("appid", "100");
            cls.put("uty", "id");//用户ID，如果用户是使用邮箱登陆，则此处为邮箱
            cls.put("url_type", "0");
            cls.put("isv_type", "0");
            long iat = SafeTools.getNowTimeStamp();
            long exp = iat + 900L + 28800L; 
            String payload = BeisenTokenProvider.GetPayload("www.italent.cn", getUserId(tenant_id,secret,jobNumber), tenant_id, exp, iat, cls);
            String token =BeisenTokenProvider.GetIdToken(header, payload, _private_key);
            return "https://oapi.italent.cn/SSO/AuthCenter?id_token="+token+"&return_url=https%3a%2f%2fwww.italent.cn%2f";
        } catch (Exception e) {
            e.printStackTrace();
        }
		return null;
	}
	
	public static String getUserId(String tenant_id,String secret,String jobNumber) {
		String result1=HttpUtil.GET_API("http://openapi.italent.cn/swaggerfile/token?app_id=908&tenant_id="+tenant_id+"&secret="+secret+"&grant_type=client_credentials", new HashMap());
		String token=JSONObject.fromObject(result1).getString("content");
		String result2=HttpUtil.GET_API("https://openapi.italent.cn/userframework/v1/"+tenant_id+"/staffs?staffcode="+jobNumber, new HashMap<String,String>(){
			{
				put("Authorization","Bearer "+token);
			}
		});
		return JSONObject.fromObject(result2).getJSONArray("items").getJSONObject(0).getJSONObject("staffDto").getString("userId");
	}
	
	public static String public_key;
	@Value("${com.sense.beisen.public_key}")
	public void public_key(String public_key) {
		this.public_key = public_key;
	}
	
	
	
	

	public static String private_key;
	@Value("${com.sense.beisen.private_key}")
	public void private_key(String private_key) {
		this.private_key = private_key;
	}
	
	
	
	public static String app_id; 
	@Value("${com.sense.beisen.app_id}")
	public void app_id(String app_id) {
		this.app_id = app_id;
	}
	*/
	
}