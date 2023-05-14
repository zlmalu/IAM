package org.iam.compoment.sync.rest.fy;

import java.util.Map;

import com.sense.core.util.XMLUtil;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.SyncInteface;

import net.sf.json.JSONObject;

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
	
	@Param("OA IP+端口")
	private String address="http://106.15.232.78";
	
	@Param("白名单IP")
	private String ip="127.0.0.1";


	@Override
	public com.sense.iam.cam.ResultCode execute(String content) {
		ResultCode code = null;
		try{
			Map<String, String> contentMap=XMLUtil.simpleXml2Map(content);
			System.out.println("============SIM下推数据："+contentMap);
			if(optType.equals("1")){
			  code = addOrg(contentMap);
			}else if(optType.equals("2")){
				code = editOrg(contentMap);
			}else if(optType.equals("3")){
				code = delOrg(contentMap);
			}
			System.out.println("============返回结果"+code);
		}catch(Exception e){
			e.printStackTrace();
		}
		return code;
	}
	
	
	public ResultCode delOrg(Map<String, String> map){
		//创建内容体
		String  orgXML="";
		orgXML+="<![CDATA[";
		orgXML+="<?xml version='1.0' encoding='UTF-8'?>";
		orgXML+="<root>";
		orgXML+="<orglist>";
		orgXML+="<org action=\"del\">";
		//动态参数-必填项不能留空
		orgXML+="<code>"+map.get("code")+"</code>";//部门编号
		orgXML+="<shortname>"+map.get("name")+"</shortname>";//部门名称
		orgXML+="<canceled>0</canceled> ";//状态
		orgXML+="</org>";
		orgXML+="</orglist>";
		orgXML+="</root>";
		orgXML+="]]>";
		
		//创建-声明头部
		StringBuffer  xml= new StringBuffer("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:hrm=\"http://localhost/services/HrmService\">");
		xml.append("<soapenv:Header/>");
		xml.append("<soapenv:Body>");
		xml.append("<hrm:SynDepartment>");
		xml.append("<hrm:in0>"+ip+"</hrm:in0>");
		xml.append("<hrm:in1>"+orgXML.toString()+"</hrm:in1>");
		xml.append("</hrm:SynDepartment>");
		xml.append("</soapenv:Body>");
		xml.append("</soapenv:Envelope>");
		String resp=HttpUtil.soapPostSendXml(address+"/services/HrmService", xml.toString());
		if(resp.indexOf("成功")>0){
			return new ResultCode(SUCCESS,resp);
		}else{
			return new ResultCode(FAIL,resp);
		}
	}
	
	public ResultCode addOrg(Map<String, String> map){
		//根据部门编码获取部门名
	
		String  userxml="";
		userxml+="<![CDATA[";
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
		userxml+="</root>";
		userxml+="]]>";
		
		//创建人员--声明头部
		StringBuffer  xml= new StringBuffer("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:hrm=\"http://localhost/services/HrmService\">");
		xml.append("<soapenv:Header/>");
		xml.append("<soapenv:Body>");
		xml.append("<hrm:SynDepartment>");
		xml.append("<hrm:in0>"+ip+"</hrm:in0>");
		xml.append("<hrm:in1>"+userxml.toString()+"</hrm:in1>");
		xml.append("</hrm:SynDepartment>");
		xml.append("</soapenv:Body>");
		xml.append("</soapenv:Envelope>");
		String resp=HttpUtil.soapPostSendXml("http://106.15.232.78/services/HrmService", xml.toString());
		if(resp.indexOf("成功")>0){
			return new ResultCode(SUCCESS,resp);
		}else{
			return new ResultCode(FAIL,resp);
		}	
	}
	
	public ResultCode editOrg(Map<String, String> map){
		//根据部门编码获取部门名
	
		String  userxml="";
		userxml+="<![CDATA[";
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
		userxml+="]]>";
		
		//创建人员--声明头部
		StringBuffer  xml= new StringBuffer("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:hrm=\"http://localhost/services/HrmService\">");
		xml.append("<soapenv:Header/>");
		xml.append("<soapenv:Body>");
		xml.append("<hrm:SynDepartment>");
		xml.append("<hrm:in0>"+ip+"</hrm:in0>");
		xml.append("<hrm:in1>"+userxml.toString()+"</hrm:in1>");
		xml.append("</hrm:SynDepartment>");
		xml.append("</soapenv:Body>");
		xml.append("</soapenv:Envelope>");
		String resp=HttpUtil.soapPostSendXml("http://106.15.232.78/services/HrmService", xml.toString());
		if(resp.indexOf("成功")>0){
			return new ResultCode(SUCCESS,resp);
		}else{
			return new ResultCode(FAIL,resp);
		}	
	}
	
}
