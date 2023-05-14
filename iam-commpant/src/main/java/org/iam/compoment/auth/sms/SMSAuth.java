package org.iam.compoment.auth.sms;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.core.exception.UserAuthentionException;
import com.sense.core.util.ContextUtil;
import com.sense.core.util.CurrentAccount;
import com.sense.iam.compoment.AuthInterface;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;


/**
 * 进行企业微信认证，主要进行微信的用户名查找，并和当前登陆用户进行对比
 * 
 * Description:  主要用于二次强认证
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */
@Name("短信认证")
public class SMSAuth implements AuthInterface{

	private Log log=LogFactory.getLog(getClass());
	@Param("帐号查找sql")
	private String findSql="select acct1.ID,acct1.LOGIN_NAME,user1.NAME AS USER_NAME,acct1.STATUS from IM_ACCOUNT acct1 left JOIN IM_APP app on acct1.APP_ID=app.ID left JOIN IM_USER user1 on user1.ID=acct1.USER_ID where acct1.LOGIN_NAME=? and user1.COMPANY_SN=? and app.SN='APP001'";

	
	@Override
	public void authentication(String uid, String password, Map<String, Object> params) {
		log.info("auth uid="+uid);
		Connection con=null;
		ResultSet rs=null;
		PreparedStatement st=null;
		try {
			con=getConnection();
			log.info(findSql);
			st=con.prepareStatement(findSql);
			st.setString(1,uid);
			st.setString(2,CurrentAccount.getCurrentAccount().getCompanySn());//当前在线用户名
			rs=st.executeQuery();
			if(rs.next()){
				return ;
			}else{
				throw new UserAuthentionException("account is not exist");
			}
		} catch (Exception e) {
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
