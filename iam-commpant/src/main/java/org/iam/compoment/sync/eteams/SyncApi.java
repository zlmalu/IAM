package org.iam.compoment.sync.eteams;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import com.sense.core.util.HttpUtil;
import com.sense.core.util.StringUtils;
import com.sense.core.util.XMLUtil;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.SyncInteface;

/**
 * eteams数据同步
 * 
 * Description:  
 * 
 * @author hyj
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */
public class SyncApi implements SyncInteface{

	@Param("企业ID")
	private String corpid="7061b1134e1d7365185ddfb7d7a7e6c";
	@Param("应用app_key")
	private String app_key="a88aa2dba7211f25f9763355fcf79f9a";
	@Param("应用app_secret")
	private String app_secret="b7000bbeaa341f6422bc42fc3e03372";
	@Param("操作类型")
	private String optType="1";//1 add  2 edit 3delete
	
	
	/**
	 * 新增用户
	 * @param contentMap
	 * @return
	 */
	public String addPerson(Map<String,String> contentMap){
		String result="";
		try {
			String resultCode = HttpUtil.GET_API("https://api.eteams.cn/oauth2/authorize?corpid="+corpid+"&response_type=code&state=", new HashMap());
			String code =JSONObject.fromObject(resultCode).getString("code");
			System.out.println(code);
			String paramStr="&app_key="+app_key+"&app_secret="+app_secret+"&grant_type=authorization_code&code="+code;
			String resultToken =HttpUtil.POST_API("https://api.eteams.cn/oauth2/access_token", paramStr, new HashMap());
			String accessToken=JSONObject.fromObject(resultToken).getString("accessToken");
			System.out.println(accessToken);
			String sn=StringUtils.getString(contentMap.get("SN"));
			String name=StringUtils.getString(contentMap.get("NAME"));
			String telephone=StringUtils.getString(contentMap.get("TELEPHONE"));
			String email=StringUtils.getString(contentMap.get("EMAIL"));
			String sex=StringUtils.getString(contentMap.get("SEX")).equals("1")?"male":"female";
			paramStr="&access_token="+accessToken+"&name="+name+"&mobile="+telephone+"&department=4535540612374329211&email="+email+"&jobNum="+sn+"&sex="+sex;
			result=HttpUtil.POST_API("https://api.eteams.cn/user/v3/createUser", paramStr, new HashMap());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public com.sense.iam.cam.ResultCode execute(String content) {
		System.out.println("============数据："+content);
		String result="";
		try {
			Map<String, String> contentMap=XMLUtil.simpleXml2Map(content);
			result= addPerson(contentMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(result.indexOf("\"errcode\":\"0\"")>-1){
			return new ResultCode(SUCCESS,"");
		}else{
			return new ResultCode(FAIL, result);
		}
	}
	
//	public static void main(String[] args) {
//		String resultCode = HttpUtil.GET_API("https://api.eteams.cn/oauth2/authorize?corpid=7061b1134e1d7365185ddfb7d7a7e6c&response_type=code&state=", new HashMap());
//		String code =JSONObject.fromObject(resultCode).getString("code");
//		System.out.println(code);
//		String paramStr="&app_key=a88aa2dba7211f25f9763355fcf79f9a&app_secret=b7000bbeaa341f6422bc42fc3e03372&grant_type=authorization_code&code="+code;
//		String resultToken =HttpUtil.POST_API("https://api.eteams.cn/oauth2/access_token", paramStr, new HashMap());
//		String accessToken=JSONObject.fromObject(resultToken).getString("accessToken");
//		System.out.println(accessToken);
//		paramStr="&access_token="+accessToken+"&name=张三&mobile=13254321231&department=4535540612374329211&email=zhansgan@126.com&jobNum=0202&sex=male&status=normal";
//		String result=HttpUtil.POST_API("https://api.eteams.cn/user/v3/createUser", paramStr, new HashMap());
//		System.out.println(result);
//		if(result.indexOf("\"errcode\":\"0\"")>-1){
//			System.out.println("success");
//		}else{
//			System.out.println("fail");
//		}
//	}
	
}
