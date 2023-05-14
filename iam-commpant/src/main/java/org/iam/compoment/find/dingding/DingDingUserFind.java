package org.iam.compoment.find.dingding;

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

@Name("钉钉用户查找")
public class DingDingUserFind implements FindInterface{

	private Log log=org.apache.commons.logging.LogFactory.getLog(getClass());
	
	private String findSql="select acct1.ID,acct1.LOGIN_NAME,user1.NAME AS USER_NAME,acct1.STATUS from IM_ACCOUNT acct1 left JOIN IM_APP app on acct1.APP_ID=app.ID left JOIN IM_USER user1 on user1.ID=acct1.USER_ID where user1.TELEPHONE=? and user1.COMPANY_SN=? and app.SN='APP001'";
	
	
	
	@Override
	public Account find(String code) {
		
		SysConfigCache sysConfigCache=(SysConfigCache) ContextUtil.context.getBean("sysConfigCache");
		
		String key;
		log.info("dingding code:"+code);
		try {
			key = getUsername(code,sysConfigCache);
			log.info("dingding key:"+key);
			if(key==null){
				
				throw new UserAuthentionException("dingding user get is null");
			}
		} catch (Exception e) {
			log.error("dingding get error",e);
			throw new UserAuthentionException("dingding user get error");
		}
		
		Connection con=null;
		ResultSet rs=null;
		PreparedStatement st=null;
		try {
			con=getConnection();
			st=con.prepareStatement(findSql);
			st.setString(1,key);
			if(findSql.split("[?]").length>=3){
				st.setString(2, CurrentAccount.getCurrentAccount().getCompanySn());
			}
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
			throw new UserAuthentionException("account is not exist",e);
		} finally{
			if(rs!=null){try{rs.close();}catch(Exception e){}}
			if(st!=null){try{st.close();}catch(Exception e){}}
			if(con!=null){try{con.close();}catch(Exception e){}}
		}
	}
	
	

	private String getUsername(String code,SysConfigCache sysConfigCache) throws MalformedURLException, IOException{
		String phone=null;
		log.info("sysConfigCache.DINGDING_APPID="+sysConfigCache.DINGDING_APPID);
		log.info("sysConfigCache.DINGDING_APP_SCA_KEY="+sysConfigCache.DINGDING_APP_SCA_KEY);

		JSONObject result=get("https://oapi.dingtalk.com/sns/gettoken?appid="+sysConfigCache.DINGDING_APPID+"&appsecret="+sysConfigCache.DINGDING_APP_SCA_KEY);
		if(result.getInt("errcode")!=0){
			log.info("result="+result);
		}
		
		if(result.getInt("errcode")==0){
			JSONObject tokenJson=WebServiceRestUtil.get("https://oapi.dingtalk.com/gettoken?corpid="+sysConfigCache.DINGDING_CORPID+"&corpsecret="+sysConfigCache.DINGDING_CORPSECRET);
			log.debug("tokenJson=:"+tokenJson);
			if(tokenJson.getInt("errcode")!=0){
				log.info("tokenJson="+tokenJson);
			}
			
			JSONObject jsonParam=new JSONObject();
			jsonParam.put("tmp_auth_code", code);
			JSONObject result1=WebServiceRestUtil.post("https://oapi.dingtalk.com/sns/get_persistent_code?access_token="+result.getString("access_token"),jsonParam);
		
			if(result1.getInt("errcode")!=0){
				log.info("result1="+result1);
			}
			
			JSONObject jsonParam1=new JSONObject();
			jsonParam1.put("openid", result1.getString("openid"));
			jsonParam1.put("persistent_code", result1.getString("persistent_code"));
			
			JSONObject result2=WebServiceRestUtil.post("https://oapi.dingtalk.com/sns/get_sns_token?access_token="+result.getString("access_token"),jsonParam1);
			if(result2.getInt("errcode")!=0){
				log.info("result2="+result2);
			}
			JSONObject result3=WebServiceRestUtil.get("https://oapi.dingtalk.com/sns/getuserinfo?sns_token="+result2.getString("sns_token"));
			if(result3.getInt("errcode")!=0){
				log.info("result3="+result3);
			}
			JSONObject result4=WebServiceRestUtil.get("https://oapi.dingtalk.com/user/getUseridByUnionid?access_token="+tokenJson.getString("access_token")+"&unionid="+result3.getJSONObject("user_info").getString("unionid"));
			if(result4.getInt("errcode")==0){
				String userId=result4.getString("userid");
				JSONObject result5=WebServiceRestUtil.get("https://oapi.dingtalk.com/user/get?access_token="+tokenJson.getString("access_token")+"&userid="+userId);
				log.info("result5="+result5);
				if(result5.getInt("errcode")==0) {
					phone=result5.getString("mobile");
				}
			}else{
				log.info("result4="+result4);
			}
		}
		return phone;
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

	
	private Connection getConnection() throws Exception{
		DataSource factory=(DataSource)ContextUtil.getBean("dataSource");
		return factory.getConnection();
	}


}
