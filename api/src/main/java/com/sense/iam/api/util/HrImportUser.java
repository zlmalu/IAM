package com.sense.iam.api.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import com.sense.core.util.HttpUtil;

public class HrImportUser{
	static String address="http://sso.sensesw.com:8882";
	static String username="admin";
	static String password="123456";
	//定义导入用户类型
	String userTypeId="";
	
	//定义导入组织类型
	String orgTypeId="";
	
	//构建用户集合，map包含，用户编码（sn）,姓名（name）,性别(中文男女区分)（sex），手机号（mobile）,邮件（email）,状态（status）,有扩展属性用EXT[字段名称]代替
	static List<Map<String, Object>> userMap=new ArrayList<Map<String,Object>>();
		

	 //构建组织集合，map包含，组织编码（sn）,组织名称（name）,组织上级编码（prentSn）,有扩展属性用EXT[字段名称]代替，注意需要排好序
	static List<Map<String, Object>> orgMap=new ArrayList<Map<String,Object>>();
	
	
	
	
	public static void initOrg(String token){
		for(Map<String, Object> map:orgMap){
			JSONObject queryParam=new JSONObject();
			queryParam.put("sn", map.get("prentSn"));
			queryParam.put("parentId", "0");
			queryParam.put("uim-login-user-id", token);
			String tokenUrl=address+"/api/im/org/findListByObject";
			String msg=IMPHttpUtil.POST_JSON(tokenUrl, queryParam);
			
			System.out.println("msg="+msg);
			
		}
	}
	public static void initUser(String token){
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static String getToken(){
		String tokenUrl=address+"/api/sys/login";
		JSONObject queryParam=new JSONObject();
		queryParam.put("username", username);
		queryParam.put("password", password);
		String msg=IMPHttpUtil.POST_JSON(tokenUrl, queryParam);
		System.out.println("msg="+msg);
		if(msg!=null){
			JSONObject resp=JSONObject.fromObject(msg);
			if(resp.getBoolean("success")){
				return resp.getString("msg");
			}
		}
		return null;
	}
}
