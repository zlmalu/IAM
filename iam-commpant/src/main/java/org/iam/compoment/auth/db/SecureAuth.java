package org.iam.compoment.auth.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.core.exception.UserAuthentionException;
import com.sense.core.security.Md5;
import com.sense.core.security.UIM;
import com.sense.core.util.ContextUtil;
import com.sense.core.util.CurrentAccount;
import com.sense.iam.compoment.AuthInterface;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;

/**
 * 
 * 双数据库认证
 * 
 * Description: 
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Name("IAM-MD5加盐")
public class SecureAuth implements AuthInterface{

	private Log log=LogFactory.getLog(getClass());
	@Param("认证时长")
	private String cacheTime="60000";
	private static Map<String,String> cache=new HashMap<String,String>();
	private static long  upCacheTime=0;
	@Override
	public void authentication(String uid, String pwd, Map<String, Object> params) {
		if(System.currentTimeMillis()-upCacheTime>Long.valueOf(cacheTime)){//判断缓存是否大于一分钟，如果大于一分钟则清除缓存
			cache.clear();
			upCacheTime=System.currentTimeMillis();
		}
		String cachePwd=cache.get(CurrentAccount.getCurrentAccount().getCompanySn()+"_"+uid);
		if(cachePwd!=null && cachePwd.equals(pwd.toUpperCase())){
			return;
		}
		Connection con=null;
		ResultSet rs=null;
		PreparedStatement st=null;
		try {
			con=getConnection();
			log.info("select LOGIN_PWD from im_account a where id="+CurrentAccount.getCurrentAccount().getId());
			st=con.prepareStatement("select LOGIN_PWD from im_account a where id=?");
			st.setLong(1,CurrentAccount.getCurrentAccount().getId());
			rs=st.executeQuery();
			if(rs.next()){
				String dbpwd=rs.getString("LOGIN_PWD");
				log.debug(params.get("salf")+":"+UIM.decode(dbpwd));
				if(!Md5.encode((params.get("salf")+":"+UIM.decode(dbpwd)).getBytes("UTF-8")).toUpperCase().equals(pwd.toUpperCase())){
					throw new UserAuthentionException("password error");
				}
				cache.put(CurrentAccount.getCurrentAccount().getCompanySn()+"_"+uid, Md5.encode((params.get("salf")+":"+UIM.decode(dbpwd)).getBytes("UTF-8")).toUpperCase());
				return;
			}else{
				throw new UserAuthentionException("username or password error");
			}
		} catch (Exception e) {
			log.error(e);
			throw new UserAuthentionException("username or password error",e);
		} finally{
			if(rs!=null){try{rs.close();}catch(Exception e){}}
			if(st!=null){try{st.close();}catch(Exception e){}}
			if(con!=null){try{con.close();}catch(Exception e){}}
		}
		
	}
	
	private Connection getConnection() throws Exception{
		DataSource factory=(DataSource)ContextUtil.getBean("dataSource");
		return factory.getConnection();
	}
}
