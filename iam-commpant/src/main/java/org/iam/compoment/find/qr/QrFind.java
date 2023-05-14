package org.iam.compoment.find.qr;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.core.exception.UserAuthentionException;
import com.sense.core.util.ContextUtil;
import com.sense.core.util.CurrentAccount;
import com.sense.iam.compoment.FindInterface;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.model.im.Account;

/**
 * 扫码认证
 * 
 * Description:  
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */
@Name("扫码用户查找")
public class QrFind implements FindInterface{
	private Log log=LogFactory.getLog(getClass());
	
	@Param("用户查找sql")
	private String findSql="select acct1.ID,acct1.LOGIN_NAME,user1.NAME AS USER_NAME,acct1.STATUS from IM_ACCOUNT acct1 left JOIN IM_APP app on acct1.APP_ID=app.ID left JOIN IM_USER user1 on user1.ID=acct1.USER_ID where acct1.LOGIN_NAME=? and user1.COMPANY_SN=? and app.SN='APP001'";
	
	@Override
	public Account find(String code) {
		log.info("resp qywx code="+code);
		Object key=null;
		
		try {
			Object obj=ContextUtil.getBean("accessTokenCache");
			Object token=obj.getClass().getMethod("getToken", String.class).invoke(obj, code);
			key=token.getClass().getMethod("getContent").invoke(token);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e1) {
			e1.printStackTrace();
		}
		if(key==null){
			throw new RuntimeException("");
		}
		log.info("get user  code="+key);
		Connection con=null;
		ResultSet rs=null;
		PreparedStatement st=null;
		try {
			con=getConnection();
			st=con.prepareStatement(findSql);
			st.setString(1,key.toString());
			st.setString(2, CurrentAccount.getCurrentAccount().getCompanySn());
			rs=st.executeQuery();
			if(rs.next()){
				Account account=new Account();
				account.setId(rs.getLong("ID"));
				account.setLoginName(rs.getString("LOGIN_NAME"));
				account.setUserName(rs.getString("USER_NAME"));
				account.setStatus(rs.getInt("STATUS"));
				return account;
			}else{
				throw new UserAuthentionException("account is not exist");
			}
		} catch (Exception e) {
			log.error("account login error",e);
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
