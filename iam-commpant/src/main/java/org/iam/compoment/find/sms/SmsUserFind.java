package org.iam.compoment.find.sms;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;

import com.sense.core.exception.UserAuthentionException;
import com.sense.core.security.UIM;
import com.sense.core.util.ContextUtil;
import com.sense.core.util.CurrentAccount;
import com.sense.iam.compoment.FindInterface;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.model.im.Account;

@Name("短信用户查找")
public class SmsUserFind implements FindInterface{

	private Log log=org.apache.commons.logging.LogFactory.getLog(getClass());
	
	@Param("帐号查找sql")
	private String findSql="select acct1.ID,acct1.LOGIN_NAME,user1.NAME AS USER_NAME,acct1.STATUS from IM_ACCOUNT acct1 left JOIN IM_APP app on acct1.APP_ID=app.ID left JOIN IM_USER user1 on user1.ID=acct1.USER_ID where acct1.ID=? and user1.COMPANY_SN=? and app.SN='APP001'";

	
	@Override
	public Account find(String code) {
		log.info("resp sms code="+code);
		Connection con=null;
		ResultSet rs=null;
		PreparedStatement st=null;
		try {
			con=getConnection();
			st=con.prepareStatement(findSql);
			st.setString(1,UIM.decode(code));
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
	
	private String getUsername(String code) throws MalformedURLException, IOException{
		
		
		return null;
	}
	
	
	private Connection getConnection() throws Exception{
		DataSource factory=(DataSource)ContextUtil.getBean("dataSource");
		return factory.getConnection();
	}

}
