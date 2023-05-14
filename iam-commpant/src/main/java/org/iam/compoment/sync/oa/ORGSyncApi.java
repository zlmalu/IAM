package org.iam.compoment.sync.oa;

import java.util.Map;

import localhost.services.HrmService.HrmServiceHttpBindingStub;
import localhost.services.HrmService.HrmServiceLocator;

import com.sense.OAService;
import com.sense.core.util.XMLUtil;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.SyncInteface;

/**
 * 泛微OA组织同步组件-soup参数
 * 
 * Description:  
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */
public class ORGSyncApi implements SyncInteface{
	
	@Param("操作类型 1新增  2修改 3删除")
	private String optType="1";//1 add  2 edit 3delete
	
	@Param("地址")
	private String address;
	
	@Param("白名单")
	private String ip;

	@Override
	public com.sense.iam.cam.ResultCode execute(String content) {
		ResultCode code = null;
		try{
			Map<String, String> contentMap=XMLUtil.simpleXml2Map(content);
			System.out.println("============地址："+address);
			System.out.println("============白名单："+ip);
			System.out.println("============SIM下推数据："+contentMap);
			if(optType.equals("1")){
			  code = addOrg(contentMap);
			}else if(optType.equals("2")){
				code = editOrg(contentMap);
			}else if(optType.equals("3")){
				code = delOrg(contentMap);
			}
			System.out.println("============返回结果"+code.getSuccess()+",响应数据"+code.getMsg());
		}catch(Exception e){
			e.printStackTrace();
		}
		return code;
	}
	
	
	public ResultCode delOrg(Map<String, String> map){
		if(map.get("parentCode").equals("0") && map.get("parentCode").equals("0")){
			System.out.println("删除分部");
			ResultCode code=deleteCompany(map);
			return code;
		}else{
			//创建内容体
			String  orgXML="";
			orgXML+="<?xml version='1.0' encoding='UTF-8'?>";
			orgXML+="<root>";
			orgXML+="<orglist>";
			orgXML+="<org action=\"delete\">";
			//动态参数-必填项不能留空
			orgXML+="<code>"+map.get("code")+"</code>";//部门编号
			orgXML+="<shortname>"+map.get("name")+"</shortname>";//部门名称
			orgXML+="<canceled>0</canceled> ";//状态
			orgXML+="</org>";
			orgXML+="</orglist>";
			orgXML+="</root>";
			String resp=OAService.exceSyncDept(address+"/services/HrmService",ip, orgXML);
			System.out.println(resp);
			if(resp.indexOf("成功")>0){
				return new ResultCode(SUCCESS,resp);
			}else{
				return new ResultCode(FAIL,resp);
			}
		}
	}
	
	public ResultCode addOrg(Map<String, String> map){
		//根据部门编码获取部门名
		//上级为0，则添加为分部
		if(map.get("parentCode").equals("0") && map.get("parentCode").equals("0")){
			System.out.println("添加分部");
			ResultCode code=addCompany(map);
			return code;
		}else{
			String  userxml="";
			userxml+="<?xml version='1.0' encoding='UTF-8'?>";
			userxml+="<root>";
			userxml+="<orglist>";
			userxml+="<org action=\"add\">";
			userxml+="<code>"+map.get("code")+"</code>"
					+ "<shortname>"+map.get("name")+"</shortname>"
					+ "<fullname>"+map.get("name")+"</fullname>"
					+ "<org_code>"+map.get("companyCode")+"</org_code>"
					+ "<parent_code>"+map.get("parentCode")+"</parent_code>"
					+ "<canceled>0</canceled>"
					+ "<order>0</order>"; 
			userxml+="</org>";
			userxml+="</orglist>";
			String resp=OAService.exceSyncDept(address+"/services/HrmService",ip, userxml);
			System.out.println(resp);
			if(resp.indexOf("成功")>0){
				return new ResultCode(SUCCESS,resp);
			}else{
				return new ResultCode(FAIL,resp);
			}	
		}
	}
	
	//添加分部
	public ResultCode addCompany(Map<String, String> map) {
		String  userxml="";
		userxml+="<?xml version='1.0' encoding='UTF-8'?>";
		userxml+="<root>";
		userxml+="<orglist>";
		userxml+="<org action=\"add\">";
		userxml+="<code>"+map.get("code")+"</code>"
				+ "<shortname>"+map.get("name")+"</shortname>"
				+ "<fullname>"+map.get("name")+"</fullname>"
				+ "<parent_code>0</parent_code>"
				+ "<canceled>0</canceled>"
				+ "<order>0</order>"; 
		userxml+="</org>";
		userxml+="</orglist>";
		HrmServiceLocator service = new HrmServiceLocator();
		try{
			java.net.URL url = new java.net.URL(address+"/services/HrmService");
			HrmServiceHttpBindingStub stub = new HrmServiceHttpBindingStub(url, service);
			String resp = stub.synSubCompany(ip, userxml);
			if(resp.indexOf("成功")>0){
				return new ResultCode(SUCCESS,resp);
			}else{
				return new ResultCode(FAIL,resp);
			}	
		}catch(Exception e){
			e.printStackTrace();
		}
		return new ResultCode(FAIL,"接口异常");
	}
	
	
	//编辑
	public ResultCode editCompany(Map<String, String> map) {
			String  userxml="";
			userxml+="<?xml version='1.0' encoding='UTF-8'?>";
			userxml+="<root>";
			userxml+="<orglist>";
			userxml+="<org action=\"edit\">";
			userxml+="<code>"+map.get("code")+"</code>"
					+ "<shortname>"+map.get("name")+"</shortname>"
					+ "<fullname>"+map.get("name")+"</fullname>"
					+ "<parent_code>0</parent_code>"
					+ "<canceled>0</canceled>"
					+ "<order>0</order>"; 
			userxml+="</org>";
			userxml+="</orglist>";
			HrmServiceLocator service = new HrmServiceLocator();
			try{
				java.net.URL url = new java.net.URL(address+"/services/HrmService");
				HrmServiceHttpBindingStub stub = new HrmServiceHttpBindingStub(url, service);
				String resp = stub.synSubCompany(ip, userxml);
				if(resp.indexOf("成功")>0){
					return new ResultCode(SUCCESS,resp);
				}else{
					return new ResultCode(FAIL,resp);
				}	
			}catch(Exception e){
				e.printStackTrace();
			}
			return new ResultCode(FAIL,"接口异常");
	}
	
	//编辑
	public ResultCode deleteCompany(Map<String, String> map) {
		String  userxml="";
		userxml+="<?xml version='1.0' encoding='UTF-8'?>";
		userxml+="<root>";
		userxml+="<orglist>";
		userxml+="<org action=\"delete\">";
		userxml+="<code>"+map.get("code")+"</code><canceled>1</canceled>";
		userxml+="</org>";
		userxml+="</orglist>";
		HrmServiceLocator service = new HrmServiceLocator();
		try{
			java.net.URL url = new java.net.URL(address+"/services/HrmService");
			HrmServiceHttpBindingStub stub = new HrmServiceHttpBindingStub(url, service);
			String resp = stub.synSubCompany(ip, userxml);
			if(resp.indexOf("成功")>0){
				return new ResultCode(SUCCESS,resp);
			}else{
				return new ResultCode(FAIL,resp);
			}	
		}catch(Exception e){
			e.printStackTrace();
		}
		return new ResultCode(FAIL,"接口异常");
	}

	public ResultCode editOrg(Map<String, String> map){
		if(map.get("parentCode").equals("0") && map.get("parentCode").equals("0")){
			System.out.println("编辑分部");
			ResultCode code=editCompany(map);
			return code;
		}else{
			String  userxml="";
			userxml+="<?xml version='1.0' encoding='UTF-8'?>";
			userxml+="<root>";
			userxml+="<orglist>";
			userxml+="<org action=\"edit\">";
			userxml+="<code>"+map.get("code")+"</code>"
					+ "<shortname>"+map.get("name")+"</shortname>"
					+ "<fullname>"+map.get("name")+"</fullname>"
					+ "<org_code>"+map.get("companyCode")+"</org_code>"
					+ "<parent_code>"+map.get("parentCode")+"</parent_code>"
					+ "<canceled>0</canceled>"
					+ "<order>0</order>"; 
			userxml+="</org>";
			userxml+="</orglist>";
			userxml+="</root>";
			String resp=OAService.exceSyncDept(address+"/services/HrmService",ip, userxml);
			if(resp.indexOf("成功")>0){
				return new ResultCode(SUCCESS,resp);
			}else{
				return new ResultCode(FAIL,resp);
			}	
		}
	}
	
}
