package org.iam.compoment.find.noreg;

import com.sense.core.exception.UserAuthentionException;
import com.sense.core.util.ContextUtil;
import com.sense.core.util.CurrentAccount;
import com.sense.iam.compoment.FindInterface;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.model.im.Account;
import com.sense.iam.model.sys.Config;
import com.sense.iam.service.SysConfigService;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.iam.compoment.find.dingding.WebServiceRestUtil;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Name("钉钉免登用户查找")
public class NoRegUserFind implements FindInterface {

	private Log log = LogFactory.getLog(getClass());

	@Param("用户查找sql")
	private String findSql="select acct1.ID,acct1.LOGIN_NAME,user1.NAME AS USER_NAME,acct1.STATUS from IM_ACCOUNT acct1 left JOIN IM_APP app on acct1.APP_ID=app.ID left JOIN IM_USER user1 on user1.ID=acct1.USER_ID where user1.TELEPHONE=? and user1.COMPANY_SN=? and user1.STATUS =1 and app.SN='APP001'";

	SysConfigService sysConfigService = (SysConfigService) ContextUtil.getBean("sysConfigService");

	@Override
	public Account find(String data) {

		String code = data.substring(0,data.length()-6);
		String appId = data.substring(data.length()-6,data.length());

		log.info("code=" + code);
		log.info("appId=" + appId);
		
		String key;
		log.info("dingding code:"+code);
		try {
			key = getUsername(code,appId);
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



	private String getUsername(String code,String appId) throws IOException {
		String phone=null;
		String appkey_name = "DINGDING_APPKEY_" + appId;
		String secret_name = "DINGDING_APPSECRET_" + appId;

		Config config;
		config = new Config();
		config.setName(appkey_name);
		config.setIsControl(false);
		config = sysConfigService.findByObject(config);
		log.info("appkey_config=" + config==null);
		String appkey = config != null?config.getValue() : "";

		config = new Config();
		config.setName(secret_name);
		config.setIsControl(false);
		config = sysConfigService.findByObject(config);
		log.info("appsecret_config=" + config==null);
		String appsecret = config != null ? config.getValue() : "";

		log.info(appkey_name + "=" + appkey);
		log.info(secret_name + "=" + appsecret);

		JSONObject getTokenResult = get("https://oapi.dingtalk.com/gettoken?appkey=" + appkey+ "&appsecret=" + appsecret);
		if (getTokenResult.getInt("errcode") != 0) {
			log.info("钉钉免登查找组件获取钉钉access_token失败:" + getTokenResult);
			return phone;
		}
		String accessToken = getTokenResult.getString("access_token");
		String timestamp = String.valueOf(System.currentTimeMillis());

		//通过免登码获取用户信息
		JSONObject unionParam = new JSONObject();
		unionParam.put("code", code);
		JSONObject getUserIdResult = get("https://oapi.dingtalk.com/user/getuserinfo?access_token=" + accessToken + "&code=" + code);
		if (getUserIdResult.getInt("errcode") != 0) {
			log.info("钉钉免登查找组件获取免登用户失败:" + getUserIdResult);
			return phone;
		}

		log.info(getUserIdResult.toString());

		// 通过userid获取授权用户的详细信息
		String userid = getUserIdResult.getString("userid");
		JSONObject userIdParam = new JSONObject();
		userIdParam.put("userid", userid);
		JSONObject getUserInfoResult = WebServiceRestUtil.post("https://oapi.dingtalk.com/topapi/v2/user/get?access_token=" + accessToken, userIdParam);
		if (getUserInfoResult.getInt("errcode") != 0) {
			log.info("钉钉免登查找组件获取授权用户信息失败:" + getUserInfoResult);
			return phone;
		}
		if (getUserInfoResult.getInt("errcode") == 0) {
			JSONObject jsonObject = getUserInfoResult.getJSONObject("result");
			phone = jsonObject.getString("mobile");
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
