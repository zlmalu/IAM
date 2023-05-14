package org.iam.compoment.sync.seeyon;

import java.util.HashMap;
import java.util.Map;

import com.sense.core.util.XMLUtil;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.SyncInteface;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 致远OA数据同步
 * 
 * Description:  
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */
public class SyncApi implements SyncInteface{

	@Param("操作对象ORG组织机构PERSON人员")
	private String optObj="ORG";//1 ORG  2 PERSON 
	@Param("操作类型 1添加 2编辑 3删除")
	private String optType="1";//1 add  2 edit 3delete
	
	@Param("服务地址")
	private String address="http://10.1.82.105:8888/seeyon";
	
	
	public String addOrg(Map<String,String> contentMap){
		try {
			String result = HttpUtil.GET_API(address+"/rest/token/rest/rest123456");
			String token = JSONObject.fromObject(result).getString("id");
			String parentId=contentMap.get("PARENTID");
			String selectResult = HttpUtil.GET_API(address+"/rest/orgDepartment/code/"+parentId+"?token="+token);
			JSONArray jsonArry=JSONArray.fromObject(selectResult);
			Map<String, String> map = new HashMap<String, String>();
	        map.put("orgAccountId", jsonArry.getJSONObject(0).getLong("orgAccountId")+"");
	        map.put("code", contentMap.get("ID"));
	        map.put("name", contentMap.get("NAME"));
	        map.put("enabled","true");
			map.put("sortId","10000");
			map.put("isGroup","false");
			map.put("description",contentMap.get("NAME"));
			map.put("superior",jsonArry.getJSONObject(0).getLong("superior")+"");
			map.put("superiorName","山东黄金集团");
			map.put("path",contentMap.get("ID"));
			String rs = HttpUtil.POST_API(address+"/rest/orgDepartment?token="+ token, JSONObject.fromObject(map).toString());
			System.out.println("add orgs="+rs);
			return JSONObject.fromObject(rs).getString("success");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "false";
	}
	
	public String addPerson(Map<String,String> contentMap){
		try {
			String result = HttpUtil.GET_API(address+"/rest/token/rest/rest123456");
			String token = JSONObject.fromObject(result).getString("id");
			String parentId=contentMap.get("ORGID");
			String selectResult = HttpUtil.GET_API(address+"/rest/orgDepartment/code/"+parentId+"?token="+token);
			JSONArray jsonArry=JSONArray.fromObject(selectResult);
			Map<String, String> map = new HashMap<String, String>();
			map.put("orgAccountId","-7919060571217049280");
			map.put("code", contentMap.get("SN"));
			map.put("name", contentMap.get("NAME"));
			map.put("orgDepartmentId", jsonArry.getJSONObject(0).getLong("id")+"");
			map.put("birthday","-1497600000");
			map.put("gender","1");
			map.put("password", contentMap.get("PASSWORD"));
			map.put("loginName", contentMap.get("SN"));
			map.put("orgLevelId","3833788818949620962");
			map.put("orgPostId","-3354304922034681182");
			String rs = HttpUtil.POST_API(address+"/rest/orgMember?token="+ token, JSONObject.fromObject(map).toString());
			System.out.println("add addPerson="+rs);
			return JSONObject.fromObject(rs).getString("success");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "false";
	}

	@Override
	public com.sense.iam.cam.ResultCode execute(String content) {
		String code="";
		try{
			Map<String, String> contentMap=XMLUtil.simpleXml2Map(content);
			System.out.println("============SIM下推数据："+contentMap);
			if("ORG".equals(optObj)) {
				if(optType.equals("1")){
					code = addOrg(contentMap);
				}
			}else if("PERSON".equals(optObj)){
				if(optType.equals("1")){
					addPerson(contentMap);
				}
			}
			System.out.println(code);
		}catch(Exception e){
			e.printStackTrace();
		}
		if("true".equals(code)){
			return new ResultCode(SUCCESS);
		}else{
			return new ResultCode(FAIL,code);
		}
	}
	
	public static void main(String[] args) {
		
		new SyncApi().execute("<data><ID>100001001</ID><NAME>测试部门</NAME><PARENTID>10000</PARENTID></data>");
	}
	
}
