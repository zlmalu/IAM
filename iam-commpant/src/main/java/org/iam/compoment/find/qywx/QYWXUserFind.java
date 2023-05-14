package org.iam.compoment.find.qywx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;

import com.sense.core.exception.UserAuthentionException;
import com.sense.core.util.ContextUtil;
import com.sense.core.util.CurrentAccount;
import com.sense.iam.cache.SysConfigCache;
import com.sense.iam.compoment.FindInterface;
import com.sense.iam.compoment.Name;
import com.sense.iam.model.im.Account;

import net.sf.json.JSONObject;

@Name("企业微信用户查找")
public class QYWXUserFind implements FindInterface{

	private Log log=org.apache.commons.logging.LogFactory.getLog(getClass());
	
	private String findSql="select acct1.ID,acct1.LOGIN_NAME,user1.NAME AS USER_NAME,acct1.STATUS from IM_ACCOUNT acct1 left JOIN IM_APP app on acct1.APP_ID=app.ID left JOIN IM_USER user1 on user1.ID=acct1.USER_ID where user1.TELEPHONE=? and user1.COMPANY_SN=? and app.SN='APP001'";

	
	@Override
	public Account find(String code) {
		String key;
		log.info("resp qywx code="+code);
		SysConfigCache sysConfigCache=(SysConfigCache) ContextUtil.context.getBean("sysConfigCache");
		try {
			key = getUsername(code,sysConfigCache);
			if(key==null){
				throw new UserAuthentionException("qywx user get is null");
			}
		} catch (Exception e) {
			log.error("qywx get error",e);
			throw new UserAuthentionException("qywx user get error");
		}
		
		Connection con=null;
		ResultSet rs=null;
		PreparedStatement st=null;
		try {
			con=getConnection();
			st=con.prepareStatement(findSql);
			st.setString(1,key);
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
	
	private String getUsername(String code,SysConfigCache sysConfigCache) throws MalformedURLException, IOException{
		log.info("sysConfigCache.QYWX_APPID="+sysConfigCache.QYWX_APPID);
		log.info("sysConfigCache.QYWX_SCA_APP_KEY="+sysConfigCache.QYWX_SCA_APP_KEY);
		log.info("sysConfigCache.QYWX_USERINFO_KEY="+sysConfigCache.QYWX_USERINFO_KEY);
		
		
		String qywxLoginUrl="https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="+sysConfigCache.QYWX_APPID+"&corpsecret="+sysConfigCache.QYWX_SCA_APP_KEY;
		String qywxUserLoginUrl="https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="+sysConfigCache.QYWX_APPID+"&corpsecret="+sysConfigCache.QYWX_USERINFO_KEY;
		String qywxGetUrl="https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo?";
		JSONObject result=get(qywxLoginUrl);
		log.debug("login auth qywxLoginUrl "+qywxLoginUrl);
		log.debug("login auth qywx "+result);
		
		JSONObject userToken=get(qywxUserLoginUrl);
		if(result.getInt("errcode")==0){
			result=get(qywxGetUrl+"access_token="+result.getString("access_token")+"&code="+code);
			//log.info("qywx login get user info :"+result);
			if(result!=null && result.getInt("errcode")==0){
	            String wxuid=result.getString("UserId").toLowerCase();
	            result=get("https://qyapi.weixin.qq.com/cgi-bin/user/get?access_token="+userToken.getString("access_token")+"&userid="+wxuid);
	            log.info("qywx login get user info :"+result);
	            wxuid=result.getString("mobile").toLowerCase();
	            log.info("qywx login get user mobile :"+wxuid);
	            return wxuid;
	        }
		}
		return null;
	}
	
	
	private Connection getConnection() throws Exception{
		DataSource factory=(DataSource)ContextUtil.getBean("dataSource");
		return factory.getConnection();
	}

	public static JSONObject get(String url) throws MalformedURLException, IOException{
		URLConnection con=new URL(url).openConnection();
		BufferedReader br=null;
		try{
			br=new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuilder strB=new StringBuilder();
			String line;
			while((line=br.readLine())!=null){
				strB.append(line);
			}
			return JSONObject.fromObject(strB.toString());
		}finally{
			if(br!=null)br.close();
		}
	}

	
}
