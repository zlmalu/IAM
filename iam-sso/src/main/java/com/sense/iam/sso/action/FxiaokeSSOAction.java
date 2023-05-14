package com.sense.iam.sso.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.sense.core.util.GatewayHttpUtil;
import com.sense.core.util.StringUtils;
import com.sense.iam.sso.HttpUtil;

/**
 * 
 * Fxiaoke crm buid token sso
 * 
 * Description: 纷享销客CRM单点登录URL生成器
 * 
 * @author yjh
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Controller
@RequestMapping("fxiaoke/sso")
public class FxiaokeSSOAction {
	
	private static ArrayList<String> strList = new ArrayList<String>();
	private static Random random = new Random();
	private static final int RANDOM_LENGTH = 16;
    
    static {
    	init();
    }

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value="ssoUrl",method = {RequestMethod.POST})
	@ResponseBody
	public String ssoUrl(HttpServletRequest request,HttpServletResponse response){
		JSONObject json=new JSONObject();
		json.put("appId",GatewayHttpUtil.getParameterForHtml("appId",request));
		json.put("appSecret",GatewayHttpUtil.getParameterForHtml("appSecret",request));
		json.put("permanentCode",GatewayHttpUtil.getParameterForHtml("permanentCode",request));
		String resp1 = HttpUtil.doPost("https://open.fxiaoke.com/cgi/corpAccessToken/get/V2", json);
		JSONObject tokenJson = JSONObject.fromObject(resp1); 
		String corpAccessToken = tokenJson.getString("corpAccessToken");
		String corpId = tokenJson.getString("corpId");
    	long timestamp = System.currentTimeMillis();
		String nonce = generateRandomStr(RANDOM_LENGTH);
		String account =GatewayHttpUtil.getParameterForHtml("account",request);  
		String appSecret =GatewayHttpUtil.getParameterForHtml("appSecret",request);
		Byte type = 1;
		List signatureParamList = Lists.newArrayList(corpAccessToken,corpId,timestamp,nonce,account,type,appSecret);
		String signature = getSha1Hex(signatureParamList);
		JSONObject loginParam = new JSONObject();
		loginParam.put("corpAccessToken", corpAccessToken);
		loginParam.put("corpId", corpId);
		loginParam.put("timestamp", timestamp);
		loginParam.put("nonce", nonce);
		loginParam.put("account", account);
		loginParam.put("type", 1);
		loginParam.put("signature", signature);
		String resp2 = HttpUtil.doPost("https://open.fxiaoke.com/cgi/sso/loginurl/get", loginParam);
		System.out.println("corpAccessToken："+corpAccessToken);
		System.out.println("corpId："+corpId);
		System.out.println("timestamp："+timestamp);
		System.out.println("nonce："+nonce);
		System.out.println("account："+account);
		System.out.println("type："+1);
		System.out.println("signature："+signature);
		JSONObject urlJson = JSONObject.fromObject(resp2); 
		String url = urlJson.get("loginUrl").toString();
		url = url.replace("\\u003d","=");
		System.out.println("返回URL："+url);
		JSONObject result=new JSONObject();
		result.put("error",0);
		result.put("ssourl",StringEscapeUtils.escapeHtml4(url));
		result.put("message","success");
		return result.toString();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	private static String getSha1Hex(Collection collection) {
        // 创建String列表
        List<String> paramList = Lists.newArrayListWithCapacity(collection.size());
        collection.forEach(o -> {
            if (java.util.Objects.nonNull(o)) {
                paramList.add(o.toString());
            }
        });
        // 按自然序进行排序
        Collections.sort(paramList);
        return DigestUtils.sha1Hex(Joiner.on(org.apache.commons.lang.StringUtils.EMPTY).join(paramList));
    }
    
    @SuppressWarnings("unused")
	private static String generateRandomStr(int length) {
    	StringBuffer sb = new StringBuffer();
    	for (int i = 0; i < length; i++) {
    		int size = strList.size();
    		String randomStr = strList.get(random.nextInt(size));
    		sb.append(randomStr);
    	}
    	return sb.toString();
    }
    
    private static void init() {
    	int begin = 97;
    	//生成小写字母，并加入集合
    	for (int i = begin; i < begin + 26; i++) {
    		strList.add((char)i + "");
    	}
    	
    	//生成大写字母，并加入集合
    	begin = 65;
    	for (int i = begin; i < begin + 26; i++) {
    		strList.add((char)i + "");
    	}
    	
    	//将0-9的数字加入集合
    	for (int i = 0; i < 10; i++) {
    		strList.add(i + "");
    	}
    }
}
