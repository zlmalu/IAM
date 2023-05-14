package com.sense.iam.portal.util;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.json.JSONObject;

import com.sense.core.util.HttpUtil;

public class QywxUtil {
	
	protected Log log=LogFactory.getLog(getClass());
	
	//企业微信---获取用户信息接口
	private static String qy_user_info_url = "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo";
	
	//企业微信---获取企业微信 access_token    
    private static String qy_access_token_url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=CORPID&corpsecret=CORPSECRET";

    //企业微信---获取用户详情信息
    private static String qy_user_detail = "https://qyapi.weixin.qq.com/cgi-bin/user/get";
    
    /**
     * 企业微信---获取accessToken凭证
     * @param CORPID
     * @param CORPSECRET
     * @return
     */
    public String getAccessToken(String CORPID,String CORPSECRET){
    	String requestURL = qy_access_token_url.replace("CORPID", CORPID).replace( "CORPSECRET", CORPSECRET);
    	 try {
 	        String getAccessToken = HttpUtil.GET_API(requestURL, new HashMap<String, String>());
 	       if (getAccessToken != null) {
	        	JSONObject jsonObject = JSONObject.fromObject(getAccessToken);
        		log.info("获取获取凭证信息========"+getAccessToken);
        		String accessToken = jsonObject.getString("access_token");
        		return accessToken;
 	       }
    	 }catch(Exception e){
    		 e.printStackTrace();
    	 }
		return "";
    }
    
    /**
     * 企业微信---获取用户信息及扩展信息凭证
     * @param accessToken
     * @param asscessCode
     * @return
     */
    public String getUserId(String accessToken,String asscessCode){
		try{
    		String userInfoURL=qy_user_info_url+"?access_token="+accessToken+"&code="+asscessCode;
            String getUserInfo = HttpUtil.GET_API(userInfoURL, new HashMap<String, String>());
    		log.info("userInfo========="+getUserInfo);
            if(null!=JSONObject.fromObject(getUserInfo)){
            	String userId = JSONObject.fromObject(getUserInfo).getString("UserId");
            	return userId;
            }
        }catch (Exception e) {
        	e.printStackTrace();
        }
        return "";
    }
    
    /**
     * 企业微信---获取用户详细信息
     * @param accessToken
     * @param userId
     * @return
     */
    public JSONObject getUserInfo(String accessToken,String userId){
    	JSONObject json = new JSONObject();
    	try {
    		String userDetailURL = qy_user_detail+"?access_token="+accessToken+"&userid="+userId;
        	log.info("userDetailURL==========="+userDetailURL);
    		String userInfo = HttpUtil.POST_API(userDetailURL,"", new HashMap<String, String>());
            if(null!=JSONObject.fromObject(userInfo)){
            	JSONObject jsonObject = JSONObject.fromObject(userInfo);
        		log.info("json============"+jsonObject);
            	json.put("name", jsonObject.getString("name"));
            	json.put("mobile", jsonObject.getString("mobile"));
            	json.put("success", true);
            }
            else{
    	     	json.put("success", false);
            }
		} catch (Exception e) {
			e.printStackTrace();
	     	json.put("success", false);
		}
    	return json;
    }
}
