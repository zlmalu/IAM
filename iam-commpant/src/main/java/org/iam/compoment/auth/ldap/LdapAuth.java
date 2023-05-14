package org.iam.compoment.auth.ldap;

import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import com.sense.core.exception.UserAuthentionException;
import com.sense.iam.compoment.AuthInterface;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;

/**
 * 
 * 使用LDAP进行用户名和密码校验
 * 
 * Description: 
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Name("基于LDAP认证")
@SuppressWarnings({ "rawtypes", "unchecked" })
public class LdapAuth implements AuthInterface{

	@Param("服务器地址")
	private String host;
	@Param("端口好")
	private String port;
	@Param("管理员用户名")
	private String username;
	@Param("管理员密码")
	private String password;
	@Param("用户存储目录")
	private String userDn;
	
	@Override
	public void authentication(String uid, String pwd, Map<String, Object> params) {
		
		DirContext ctx=null;
		Hashtable env = new Hashtable();//实例化一个Env
        env.put(Context.SECURITY_AUTHENTICATION, "simple");//LDAP访问安全级别(none,simple,strong),一种模式，这么写就行
        env.put(Context.SECURITY_PRINCIPAL, username); //用户名
        env.put(Context.SECURITY_CREDENTIALS, password);//密码
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");// LDAP工厂类
        env.put(Context.PROVIDER_URL, "ldap://"+host+":"+port);//Url
        NamingEnumeration<SearchResult> ens=null;
        try {
        	ctx=new InitialDirContext(env);
        	SearchControls controls=new SearchControls();
        	controls.setCountLimit(1);
        	controls.setReturningAttributes(new String[]{"uid"});
        	controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        	//搜索当前用户所属分支
        	ens=ctx.search(userDn, "uid="+uid, controls);
        	if(ens.hasMoreElements() && validateLdapUser(ens.nextElement().getNameInNamespace(),pwd)){
        		return;
        	}
        	throw new UserAuthentionException("username or password error");
		} catch (NamingException e) {
			throw new UserAuthentionException("username or password error",e);
		}finally{
			if(ens!=null)try{ens.close();}catch(Exception e){}
			if(ctx!=null)try{ctx.close();}catch(Exception e){}
		}
	}
	
	public boolean validateLdapUser(String uname,String pwd){
		DirContext ctx=null;
		Hashtable env = new Hashtable();//实例化一个Env
        env.put(Context.SECURITY_AUTHENTICATION, "simple");//LDAP访问安全级别(none,simple,strong),一种模式，这么写就行
        env.put(Context.SECURITY_PRINCIPAL, uname); //用户名
        env.put(Context.SECURITY_CREDENTIALS, pwd);//密码
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");// LDAP工厂类
        env.put(Context.PROVIDER_URL, "ldap://"+host+":"+port);//Url
        
        try {
        	ctx=new InitialDirContext(env);
        	return true;
		} catch (NamingException e) {
			return false;
		}finally{
			if(ctx!=null)try{ctx.close();}catch(Exception e){}
		}
	}

}
