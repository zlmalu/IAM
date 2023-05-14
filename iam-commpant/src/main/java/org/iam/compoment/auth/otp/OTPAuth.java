package org.iam.compoment.auth.otp;

import java.util.Map;

import org.apache.commons.logging.LogFactory;

import com.sense.core.exception.UserAuthentionException;
import com.sense.iam.compoment.AuthInterface;
import com.sense.iam.compoment.Param;

/**
 * 
 * 动态口令验证模块
 * 
 * Description: 使用OATH算法定时根据 用户登录名生成token令牌
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class OTPAuth implements AuthInterface{
	
	@Param("服务器地址")
	private String host;
	
	
	@Override
	public void authentication(String uid, String password, Map<String, Object> params) {
		java.text.SimpleDateFormat format=new java.text.SimpleDateFormat("yyyyMMddHHmm");
		String token=null;
		try {
			//获取当前时间，时间精确到分
			token=OTPUtil.generateOTP(uid.getBytes("UTF-8"),Long.parseLong(format.format(System.currentTimeMillis())),6);
			System.out.println(token);
			LogFactory.getLog(getClass()).debug("current user "+uid+" token :"+token);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(!token.equals(password)){
			throw new UserAuthentionException("opt error");
		}
		
	}
}
