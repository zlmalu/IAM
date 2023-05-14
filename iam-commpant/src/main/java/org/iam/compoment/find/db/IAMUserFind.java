package org.iam.compoment.find.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.core.exception.UserAuthentionException;
import com.sense.core.util.ContextUtil;
import com.sense.core.util.CurrentAccount;
import com.sense.iam.cam.Constants;
import com.sense.iam.compoment.FindInterface;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.model.im.Account;

/**
 * 基于数据库进行用户查找
 * 
 * Description:  基于连接池的或者JDBC方式进行用户查找登陆
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */
@Name("基于数据库用户查找（支持工号和登录帐号）")
public class IAMUserFind implements FindInterface{

	private Log log=LogFactory.getLog(getClass());
	
	@Param("用户查找sql")
	private String findSql="select acct.ID,acct.LOGIN_NAME,u.NAME as USER_NAME,acct.STATUS from IM_ACCOUNT acct left join IM_USER u on acct.USER_ID=u.ID left JOIN IM_APP app on acct.APP_ID=app.ID where (acct.LOGIN_NAME=? or u.SN=?) and app.SN='APP001' and acct.COMPANY_SN = ?";
	
	private static Map<String,Account> cache=new HashMap<String,Account>();
	private static long upCacheTime=0;
	@Override
	public Object find(String key) {
		if(System.currentTimeMillis()-upCacheTime>60*1000){//判断缓存是否大于一分钟，如果大于一分钟则清除缓存
			cache.clear();
			upCacheTime=System.currentTimeMillis();
		}
		log.debug("get cache"+cache);
		Account account=cache.get(CurrentAccount.getCurrentAccount().getCompanySn()+"_"+key);
		if(account!=null && account.getStatus().intValue()!=Constants.ACCOUNT_DISABLED){
			return account;
		}
		log.debug("not  cache   find   user-------------------");
		Connection con=null;
		ResultSet rs=null;
		PreparedStatement st=null;
		try {
			con=getConnection();
			st=con.prepareStatement(findSql);
			log.info(findSql+"      param:"+CurrentAccount.getCurrentAccount().getCompanySn()+" ?count="+findSql.split("[?]").length);
			st.setString(1,key);
			st.setString(2,key);
			st.setString(3,CurrentAccount.getCurrentAccount().getCompanySn());
			rs=st.executeQuery();
			if(rs.next()){
				account=new Account();
				account.setId(rs.getLong("ID"));
				account.setLoginName(rs.getString("LOGIN_NAME"));
				account.setUserName(rs.getString("USER_NAME"));
				account.setStatus(rs.getInt("STATUS"));
				cache.put(CurrentAccount.getCurrentAccount().getCompanySn()+"_"+key,account);
				log.debug("put cache"+cache);
				return account;
			}else{
				throw new UserAuthentionException("account is not exist");
			}
		} catch (Exception e) {
			log.error(e);
			throw new UserAuthentionException("account is not exist",e);
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
