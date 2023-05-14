package org.iam.compoment.auth.ad;

import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import com.sense.core.exception.UserAuthentionException;
import com.sense.iam.compoment.AuthInterface;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;

/**
 * 
 * 基于AD的认证
 * 
 * Description: 基于AD进行用户认证 
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Name("基于AD认证")
public class ADAuth  implements AuthInterface{
	
	@Param("服务器地址")
	private String host="ldap://10.1.1.1:389";
	@Param("域名")
	private String domain="sense.com";
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void authentication(String uid, String password, Map<String, Object> params) {
		if(password==null || password.trim().length()==0){
			throw new UserAuthentionException("username or password error");
		}
		DirContext ctx=null;
		Hashtable env = new Hashtable();//实例化一个Env
        env.put(Context.SECURITY_AUTHENTICATION, "simple");//LDAP访问安全级别(none,simple,strong),一种模式，这么写就行
        env.put(Context.SECURITY_PRINCIPAL, uid+"@"+domain); //用户名
        env.put(Context.SECURITY_CREDENTIALS, password);//密码
        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");// LDAP工厂类
        env.put(Context.PROVIDER_URL, host);//Url
        try {
        	ctx=new InitialDirContext(env);
		} catch (NamingException e) {
			throw new UserAuthentionException("username or password error",e);
		}finally{
			if(ctx!=null)try{ctx.close();}catch(Exception e){}
		}

	}

}
