package org.iam.compoment.sync.yunxiazi;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.core.security.Base64;

/**
 * 云匣子数据同步
 * 
 * Description:  
 * 
 * @author heyijun
 * 
 * Copyright 2016 Sense Software, Inc. All rights reserved.
 *
 */
public class SyncApi {
	protected Log log = LogFactory.getLog(getClass());
	
	public String getLoginToken(String addrPrefix, String username, String pwd) {
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("username", username);
			jsonObj.put("pwd", getEncryBody("tmppasswordver1.", pwd));
			JSONObject json = new JSONObject();
			json.put("c", "{}");
			json.put("b", jsonObj);
			
			Map<String, String> headers=new HashMap<String, String>();
			headers.put("Referer", addrPrefix);
			headers.put("Origin", addrPrefix);
			headers.put("timestamp", String.valueOf(System.currentTimeMillis()));
			headers.put("nonce", getNonce());
			headers.put("Content-Type", "application/json");
			
			String result = HttpUtil.POST(addrPrefix+"/3.0/authService/login", json, headers);
			Integer code=Integer.parseInt(JSONObject.fromObject(result).get("code").toString());
			if(code!=0){
				log.error("get token error");
			}
			return JSONObject.fromObject(JSONObject.fromObject(result).get("data")).getString("token");
		} catch (Exception e) {
			log.error("get token error",e);
		}
		return "";
	}
	
	public String getEncryBody(String key, String body){
		String iv="8765432112345678";
//		String iv=getRandoms();
		String encrypwd=encrypt(key, iv, body);
		return (iv.substring(0,8)+encrypwd.substring(0,8)+iv.substring(8)+encrypwd.substring(8));
	}
	

	/**
	 * AES加密
	 * @param key
	 * @param iv
	 * @param value
	 * @return
	 */
	public static String encrypt(String key, String iv, String value) {
		try {
			IvParameterSpec ivp = new IvParameterSpec(iv.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
	 
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivp);
	 
			byte[] encrypted = cipher.doFinal(value.getBytes());
			return Base64.encodeToString(encrypted);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/**
	 * AES解密	
	 * @param key
	 * @param iv
	 * @param encrypted
	 * @return
	 */
	public static String decrypt(String key, String iv, String encrypted) {
		try {
			IvParameterSpec ivp = new IvParameterSpec(iv.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
			 
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivp);
			 
			byte[] original = cipher.doFinal(Base64.decode(encrypted.getBytes()));
			return new String(original);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	 
	/**
	 * SHA256加密
	 * @param str
	 * @return
	 */
	private static String getSHA256StrJava(String str){
        MessageDigest messageDigest;
        String encodeStr = "";
        try {
	        messageDigest = MessageDigest.getInstance("SHA-256");
	        messageDigest.update(str.getBytes("UTF-8"));
	        encodeStr = byte2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
        	e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
        	e.printStackTrace();
        }
        return encodeStr;
	}
	private static String byte2Hex(byte[] bytes){
	    StringBuffer stringBuffer = new StringBuffer();
	    String temp = null;
	    for (int i=0;i<bytes.length;i++){
	        temp = Integer.toHexString(bytes[i] & 0xFF);
	        if (temp.length()==1){
	            //1得到一位的进行补0操作
	            stringBuffer.append("0");
	        }
	        stringBuffer.append(temp);
	    }
	    return stringBuffer.toString();
    }	
	
	/**
	 * 请求参数：sha256(随机16位数+当前时间戳)
	 * @return
	 */
	public static String getNonce(){
		String times=String.valueOf(System.currentTimeMillis());
		return getSHA256StrJava(getRandoms()+times);
	}
	
	/**
	 * 获取16位随机数
	 * @return
	 */
	private static String getRandoms(){
		String nums="";
		for (int i = 0; i < 16; i++) {
			nums+=String.valueOf(new Random().nextInt(9));
		}
		return nums;
	}
  
}
