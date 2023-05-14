package org.iam.compoment.sync.oa;
import java.util.HashMap;
import java.util.Map;

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
public class UserSyncApi implements SyncInteface{
	
	@Param("操作类型 1新增  2编辑  3删除")
	private String optType="1";//1 add  2 edit 3delete

	@Param("地址")
	private String address="http://106.15.232.78";
	
	@Param("白名单")
	private String ip="139.196.148.8";

	
	public static void main(String[] args) {
		//private String address="http://106.15.232.78";
		UserSyncApi sd=new UserSyncApi();
		Map<String, String> contentMap=new HashMap<String, String>();
		/*userxml+="<workcode>"+map.get("id")+"</workcode>";//帐号ID
		//动态参数-必填项不能留空
		userxml+="<loginid>"+map.get("loginname")+"</loginid>";//用户登录名
		userxml+="<password>"+map.get("password")+"</password>";//密码
		userxml+="<subcompany>"+map.get("subcompany")+"</subcompany>";//分部
		userxml+="<department>"+map.get("department")+"</department>";//部门
		userxml+="<sex>"+map.get("sex")+"</sex>";//性别
		userxml+="<lastname>"+map.get("name")+"</lastname>";//姓名
		userxml+="<telephone>"+map.get("telephone")+"</telephone>";//手机号
		userxml+="<mobile>"+map.get("telephone")+"</mobile>";//联系方式
		userxml+="<email>"+map.get("email")+"</email>";//邮件地址
		userxml+="<status>"+map.get("status")+"</status>";//状态
*/		
		contentMap.put("id", "31123");
		contentMap.put("loginname", "test0021");
		contentMap.put("password", "123456");
		contentMap.put("name_path", "/盛煦集团/区域公司/北京城市公司/");
		contentMap.put("orgname", "北京-财务部");
		contentMap.put("sex", "男");
		contentMap.put("name", "测试用户123");
		contentMap.put("telephone", "18661261002");
		contentMap.put("email", "");
		contentMap.put("status", "正式");
		
		System.out.println(sd.addUser(contentMap));
		
	}
	
	

	@Override
	public com.sense.iam.cam.ResultCode execute(String content) {
		ResultCode code = null;
		try{
			Map<String, String> contentMap=XMLUtil.simpleXml2Map(content);
			System.out.println("============地址："+address);
			System.out.println("============白名单："+ip);
			System.out.println("============optType："+optType);
			System.out.println("============SIM下推数据："+contentMap);
			if(optType.equals("1")){
			  code = addUser(contentMap);
			}else if(optType.equals("2")){
				code = editUser(contentMap);
			}else if(optType.equals("3")){
				code = delUser(contentMap);
			}
			System.out.println("============返回结果"+code);
		}catch(Exception e){
			e.printStackTrace();
		}
		return code;
	}
	
	
	
	
	
	
	public ResultCode delUser(Map<String, String> map){
		String  userxml="";
		userxml+="<![CDATA[";
		userxml+="<?xml version='1.0' encoding='UTF-8'?>";
		userxml+="<root>";
		userxml+="<hrmlist>";
		userxml+="<hrm action=\"delete\">";
		//动态参数-必填项不能留空
		userxml+="<workcode>"+map.get("id")+"</workcode>";//用户编号
		userxml+="<loginid>"+map.get("loginname")+"</loginid>";//用户登录名
		userxml+="<status>无效</status>";//状态
		userxml+="</hrm>";
		userxml+="</hrmlist>";
		userxml+="</root>";
		userxml+="]]>";
		System.out.println(userxml);
		String resp=OAService.exceSyncUser(address+"/services/HrmService",ip, userxml);
		System.out.println(resp);
		if(resp.indexOf("成功")>0){
			return new ResultCode(SUCCESS,resp);
		}else{
			return new ResultCode(FAIL,resp);
		}
		
	}
	
	public ResultCode addUser(Map<String, String> map){
		String name_path=map.get("name_path")+map.get("orgname");
		String subName=map.get("orgname");
		String deptName=map.get("orgname");		
		try{
			subName=name_path.split("/")[1]+">"+name_path.split("/")[2];
			deptName="";
			String[] ags=name_path.split("/");
			for(int i=3;i<ags.length;i++){
				if(i+1==ags.length){
					deptName+=ags[i];
				}else{
					deptName+=ags[i]+">";
				}
			}
		}catch(Exception e){
			
		}
		System.out.println("subName="+subName);
		System.out.println("deptName="+deptName);
		//参数说明
		//loginname:登录帐号
		//password:登录密码
		//subCode：所在分部编码
		//deptcode:所在部门编码
		//name_path,orgname
		//执行新增，查询不到OA登录名
		String  userxml="";
		userxml+="<?xml version='1.0' encoding='UTF-8'?>";
		userxml+="<root>";
		userxml+="<hrmlist>";
		userxml+="<hrm action=\"add\">";
	
		//固定参数-对象必须传，可留空
		userxml+="<jobtitle></jobtitle>";//岗位-职务，名称对象
		userxml+="<jobgroupid></jobgroupid>";//职务等级
		userxml+="<jobactivityid></jobactivityid>";//职务
		userxml+="<managerid></managerid>";//直接上级
		userxml+="<workroom></workroom>";//办公地点
		userxml+="<locationid></locationid>";//办公位置
		userxml+="<systemlanguage>简体中文</systemlanguage>";//系统语言
		userxml+="<birthday></birthday>";//出生日期
		userxml+="<maritalstatus>未婚</maritalstatus>";
		//动态参数-必填项不能留空
		userxml+="<workcode>"+map.get("id")+"</workcode>";//帐号编号
		userxml+="<loginid>"+map.get("loginid")+"</loginid>";//用户登录名
		userxml+="<password>"+map.get("pwd")+"</password>";//密码
		userxml+="<subcompany>"+subName+"</subcompany>";//分部
		userxml+="<department>"+deptName+"</department>";//部门
		userxml+="<sex>"+map.get("sex")+"</sex>";//性别
		userxml+="<lastname>"+map.get("name")+"</lastname>";//姓名
		userxml+="<telephone>"+map.get("telephone")+"</telephone>";//手机号
		userxml+="<mobile>"+map.get("telephone")+"</mobile>";//联系方式
		userxml+="<email>"+map.get("email")+"</email>";//邮件地址
		userxml+="<status>"+map.get("status")+"</status>";//状态
		userxml+="</hrm>";
		userxml+="</hrmlist>";
		userxml+="</root>";
		System.out.println(userxml);
		String resp=OAService.exceSyncUser(address+"/services/HrmService",ip, userxml);
		System.out.println(resp);
		if(resp.indexOf("成功")>0){
			return new ResultCode(SUCCESS,resp);
		}else{
			return new ResultCode(FAIL,resp);
		}
	}
	

	

	

	public ResultCode editUser(Map<String, String> map){
		
		//参数说明
		//loginname:登录帐号
		//password:登录密码
		//subCode：所在分部编码
		//deptcode:所在部门编码
		//
		String name_path=map.get("name_path")+map.get("orgname");
		String subName=map.get("orgname");
		String deptName=map.get("orgname");		
		try{
			subName=name_path.split("/")[1]+">"+name_path.split("/")[2];
			deptName="";
			String[] ags=name_path.split("/");
			for(int i=3;i<ags.length;i++){
				if(i+1==ags.length){
					deptName+=ags[i];
				}else{
					deptName+=ags[i]+">";
				}
			}
		}catch(Exception e){
			
		}
		System.out.println("subName="+subName);
		System.out.println("deptName="+deptName);
		//执行新增，查询不到OA登录名
		String  userxml="";
		
		userxml+="<?xml version='1.0' encoding='UTF-8'?>";
		userxml+="<root>";
		userxml+="<hrmlist>";

		userxml+="<hrm action=\"edit\">";
		userxml+="<workcode>"+map.get("id")+"</workcode>";//帐号ID
		//动态参数-必填项不能留空
		userxml+="<loginid>"+map.get("loginid")+"</loginid>";//用户登录名
		//判断是否存密码，否则不修改密码
		if(map.get("pwd")!=null){
			userxml+="<password>"+map.get("pwd")+"</password>";//密码
		}
		userxml+="<subcompany>"+subName+"</subcompany>";//分部
		userxml+="<department>"+deptName+"</department>";//部门
		userxml+="<sex>"+map.get("sex")+"</sex>";//性别
		userxml+="<lastname>"+map.get("name")+"</lastname>";//姓名
		userxml+="<telephone>"+map.get("telephone")+"</telephone>";//手机号
		userxml+="<mobile>"+map.get("telephone")+"</mobile>";//联系方式
		userxml+="<email>"+map.get("email")+"</email>";//邮件地址
		userxml+="<status>"+map.get("status")+"</status>";//状态
		userxml+="</hrm>";
		userxml+="</hrmlist>";
		userxml+="</root>";
		
		System.out.println(userxml);
		String resp=OAService.exceSyncUser(address+"/services/HrmService",ip, userxml);
		System.out.println(resp);
		if(resp.indexOf("成功")>0){
			return new ResultCode(SUCCESS,resp);
		}else{
			return new ResultCode(FAIL,resp);
		}
	}
	
}
