package org.iam.compoment.sync.qqmail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.core.security.UIM;
import com.sense.core.util.HttpUtil;
import com.sense.core.util.StringUtils;
import com.sense.iam.compoment.Param;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class InitApi {

	private Log log = LogFactory.getLog(getClass());
	//测试
	/*@Param("企业ID")
	private String corpid = "wm4e9db8f9aa228ee0";
	@Param("管理密钥")
	private String corpsecret = "fFQluabTMTmEaK4zeO6mtqC8bFbPoLGZDVsNpvDwunEFbSF25qLmXpkhR34c_qcd";*/
	
	//生产
	@Param("企业ID")
	private static String corpid = "wm971b7ca8f516026e";
	@Param("管理密钥")
	private static String corpsecret = "oyxEesU51dmI3h0c9NN1a85z2RqfmVcWw_sXThzspiJ8RBtaPMf7_Y5zaCTRGneZ";
	//private String corpsecres = "oyxEesU51dmI3h0c9NN1a85z2RqfmVcWw_sXThzspiJ8RBtaPMf7_Y5zaCTRGneZ";
	
	
	private static Map<String, String> keys=new HashMap<String, String>();
	
	public static void main(String[] args){
		String token=getAccessToken1();
		String jspn="{\"userid\": \"stanley.chung@nova-asia.com\",\"name\": \"仲维豪\",\"department\": [6605788141300372531],\"gender\": \"1\",\"password\":\"Password@1\"}";
		addUser(jspn);
	//	System.out.println("token="+token);
		
	}
	

	/**
	 * 获取AccessToken
	 * @return
	 * description :  
	 * wenjianfeng 2019年12月24日
	 */
	public static String getAccessToken1() {
		
		try {
			String result = HttpUtil.GET_API("https://api.exmail.qq.com/cgi-bin/gettoken?corpid="+corpid+"&corpsecret="+corpsecret,new HashMap());
			
			return JSONObject.fromObject(result).get("access_token").toString();
		} catch (Exception e) {
		
		}
		return "";
	}

	public static void addUser(String content){
		String result=HttpUtil.POST_API("https://api.exmail.qq.com/cgi-bin/user/create?access_token="+getAccessToken1(), content, new HashMap());
		JSONObject jo=JSONObject.fromObject(result);
		System.out.println(jo.toString());
		
		
	}
	
	public static void addOrg(String content){
		JSONObject contObj=JSONObject.fromObject(content);
		Object parentId=contObj.get("parentid");
		Long orgId = contObj.getLong("orgId");
		//判断是否重新设置顶级节点
		if(StringUtils.getString(parentId).trim().length()==0 || StringUtils.getString(parentId).trim().equals("null")|| StringUtils.getString(parentId).trim().equals("1")){
			//设置
			
		}
		System.out.println("sync org add content===="+content);
		
		String result=HttpUtil.POST_API("https://api.exmail.qq.com/cgi-bin/department/create?access_token="+getAccessToken1(), content, new HashMap());
		JSONObject jo=JSONObject.fromObject(result);
		System.out.println(jo.toString());
		
		
	}
		public static void getUser(){
		InitApi syncApi = new InitApi();
		String token=syncApi.getAccessToken();
	   //获取企业微信人员的信息
	    //System.out.println(token);
	    String url="https://api.exmail.qq.com/cgi-bin/user/simplelist?access_token="+token+"&department_id=1&fetch_child=1";
	    //System.out.println(url);
	    String result = HttpUtil.GET_API(url, new HashMap());
	  //  System.out.println(result);
	    JSONObject json=JSONObject.fromObject(result);
	    for(int i=0;i<json.getJSONArray("userlist").size();i++){
	    	
	    	 //根据手机号更新成员的新组织
	    	
	    	 System.out.println(json.getJSONArray("userlist").get(i));
	    }

	}
	
	public static void initUserDB(){
		List<Map<String, String>> list =getUserInfoByID();
		InitApi syncApi = new InitApi();
		int count=0;
	    int fail=0;
		for(int i=0;i<list.size();i++){
			String email=list.get(i).get("email");
			String sn=list.get(i).get("sn");
			String name=list.get(i).get("name");
			String sid=list.get(i).get("id");
			
		 long id=getQId("SELECT NEXTVAL('UIM_SEQ')");
    	 String install="insert into im_account(id,login_name,login_pwd,app_id,user_id,status,CREATE_TIME,OPT_USER,OPEN_TYPE,ACCT_TYPE,COMPANY_SN) values("+id+",'"+sn+"','"+UIM.encode("123456")+"',1000006277,"+sid+",1,now(),'admin',1,1,'100001')";
    	 System.out.println(install);
    	 int flagStatus=exceSQL(install);
		 if(flagStatus>0){
			count++;
			System.out.println("add db success....");
		 }else{
			fail++;
			System.out.println("add db fail....");
		 }
			
		}
		 System.out.println("已插入："+count+",未插入："+fail);
	}
	
	
	
	 public static long getQId(String sql) {
		    Statement st=null;
		    long rs=0;
			Connection con=null;
			try {
				con=getConnection();
				st=con.createStatement();
				ResultSet rt=st.executeQuery(sql);
				while (rt.next()) {
					rs=rt.getLong(1);
				}
			}catch (Exception e) {
				e.printStackTrace();
			}finally{
				if(st!=null)try{st.close();}catch(Exception e){}
				if(con!=null)try{con.close();}catch(Exception e){}
			}
			return rs;
	  }
	  
	
	public static void initOrg(){
		//初始化IAM组织对象
		//声明根
		keys.put("1000005205", "24340235851817743");
		List<Map<String, String>> list = getOrgs();
		InitApi syncApi = new InitApi();
		String token=syncApi.getAccessToken();
		System.out.println(token);
		String url="https://api.exmail.qq.com/cgi-bin/department/list?access_token="+token;
		System.out.println(url);
		String result = HttpUtil.GET_API(url, new HashMap());
		System.out.println(result);
	    for(int i=0;i<list.size();i++){
	    	Map<String, String> map=list.get(i);
	    	JSONObject jssd=new JSONObject();
	    	jssd.put("orgId", map.get("id"));
	    	jssd.put("name", map.get("name"));
	    	//获取企业微信的ID
	    	jssd.put("parentid", Long.valueOf(keys.get(map.get("parent_id"))==null?"1":keys.get(map.get("parent_id"))));
	    	jssd.put("order", (i*1+1));
	    	System.out.println(jssd.toString());
	    	boolean flag=orgAdd(jssd.toString(), token); 
	    	if(flag==false){
	    		break;
	    	}
	    }	
	}
	

	public static boolean orgAdd(String content,String token){
		boolean flag=false;
		try{
			JSONObject contObj=JSONObject.fromObject(content);
			String orgId = contObj.getString("orgId");
			contObj.remove("orgId");
			System.out.println("sync org add content===="+contObj.toString());
			String result=HttpUtil.POST_API("https://api.exmail.qq.com/cgi-bin/department/create?access_token="+token, contObj.toString(), new HashMap());
			JSONObject jo=JSONObject.fromObject(result);
			System.out.println("add result===="+jo.toString());
			if(jo.getInt("errcode")==0){
				//反写本地组织机构对应的企业邮箱组织
				int flagStatus=exceSQL("insert into im_org_ATTR(ORG_ID,NAME,VALUE) values("+orgId+",'QQ_MAIL_ORG_ID','"+jo.get("id")+"')");
				if(flagStatus>0){
					System.out.println("add db success....");
					//添加编码ID到内存中
					keys.put(orgId, jo.get("id").toString());
					flag=true;
				}else{
					System.out.println("add db fail....");
				} 
			}else{
				System.out.println(jo.toString());
			}
		}catch(Exception e){
			
		}
		return flag;
	}
	
	/**
	 * 获取AccessToken
	 * @return
	 * description :  
	 * wenjianfeng 2019年12月24日
	 */
	private String getAccessToken() {
		try {
			System.out.println("corpid="+corpid+",corpsecret="+corpsecret);
			String result = HttpUtil.GET_API("https://api.exmail.qq.com/cgi-bin/gettoken?corpid="+corpid+"&corpsecret="+corpsecret,new HashMap());
			log.info(result);
			if(!result.contains("ok")){
				log.error("获取Token异常");
			}
			return JSONObject.fromObject(result).get("access_token").toString();
		} catch (Exception e) {
			log.error("获取访问token失败",e);
		}
		return "";
	}

	
	
	   
	 /*public static void main(String[] args) {
		InitApi syncApi = new InitApi();
		String token=syncApi.getAccessToken();
	    System.out.println(token);
	    String url="https://api.exmail.qq.com/cgi-bin/department/list?access_token="+token;
	    System.out.println(url);
	    String result2 = HttpUtil.GET_API(url, new HashMap());
	    System.out.println(result2);
	    JSONObject json=JSONObject.fromObject(result2);
	    for(int i=0;i<json.getJSONArray("department").size();i++){
	    	System.out.println(json.getJSONArray("department").getJSONObject(i));
	    }
	 }
	  */

	public static boolean cheack(String token,String userid){
		 String getUserInfoUrl="https://api.exmail.qq.com/cgi-bin/user/get?access_token="+token+"&userid="+userid;
    	 String userinforest = HttpUtil.GET_API(getUserInfoUrl, new HashMap());
    	 JSONObject userinfo=JSONObject.fromObject(userinforest);
    	 //System.out.println(userinfo);
    	 if(userinfo.getInt("errcode")==0){
    		 return true;
    	 }else{
    		 return false;
    	 }
	}
	public static void updateUser(){
		List<Map<String, String>> list = getUserInfo();
		InitApi syncApi = new InitApi();
		String token=syncApi.getAccessToken();
	   //获取企业微信人员的信息
	    //System.out.println(token);
	    String url="https://api.exmail.qq.com/cgi-bin/user/simplelist?access_token="+token+"&department_id=1&fetch_child=1";
	    //System.out.println(url);
	    String result = HttpUtil.GET_API(url, new HashMap());
	  //  System.out.println(result);
	    JSONArray data=new JSONArray();
	    JSONObject json=JSONObject.fromObject(result);
	    for(int i=0;i<json.getJSONArray("userlist").size();i++){
	    	 String getUserInfoUrl="https://api.exmail.qq.com/cgi-bin/user/get?access_token="+token+"&userid="+json.getJSONArray("userlist").getJSONObject(i).getString("userid");
	    	 String userinforest = HttpUtil.GET_API(getUserInfoUrl, new HashMap());
	    	 JSONObject userinfo=JSONObject.fromObject(userinforest);
	    	 //根据手机号更新成员的新组织
	    	
	    	 String updateUserURL="https://api.exmail.qq.com/cgi-bin/user/update";
	    	 JSONObject updateModel=new JSONObject();
	    	 Map<String, String> omap=getUserInfoByTelephone(userinfo.toString(), list);
	    	 if(omap==null){
	    		 //System.out.println("get qywxid fail");
	    		 data.add(userinfo);
	    		 continue;
	    	 }
	    	 if(omap.get("orgid")==null){
	    		 data.add(userinfo);
	    		 //System.out.println("get orgid fail");
	    		 continue;
	    	 }
	    	 System.out.println(userinfo);
	    	 updateModel.put("userid", userinfo.getString("userid"));
	    	 updateModel.put("department", "["+omap.get("orgid")+"]");
	    	 System.out.println("update model:"+updateModel);
	    	 try{
	    		 String result2 = HttpUtil.post(updateUserURL, token, updateModel.toString());
	    		 System.out.println("update resulty:"+result2);
	    	 }catch(Exception e){
	    		 e.printStackTrace();
	    	 }
	    }
	    System.out.println("");
	    System.out.println("");
	    System.out.println("");
	    System.out.println("");
	    System.out.println("=================================");
	    System.out.println("=================================");
	    System.out.println("=================================");
	    System.out.println("=================================");
	    System.out.println(data.size());
	    for(int i=0;i<data.size();i++){
	    	System.out.println("主帐号："+data.getJSONObject(i).get("userid")+",姓名："+data.getJSONObject(i).get("name")+",其他别名帐号："+data.getJSONObject(i).get("slaves"));
	    }
	}
  
	/**
	 * 检查手机号匹配邮件的条目，包含主账号以及别名帐号
	 * @param telephone
	 * @param list
	 * @return
	 */
	public static Map<String, String> getUserInfoByTelephone(String telephone,List<Map<String, String>> list){
		  Map<String, String> map=null;
		  for(Map<String, String> maps:list){
			  if(telephone.indexOf(maps.get("email"))>0){
				  return maps;
			  }
		  }
		  return map; 
	}
	
	/**key邮箱，value 用户ID
	 * @throws Exception **/
	public static List<Map<String, String>> getUserInfoByID() {
	    Statement st=null;
		ResultSet rs=null;
		Connection con=null;
		List<Map<String, String>>  list=null;
		try {
			con=getConnection();
			st=con.createStatement();
			rs=st.executeQuery("SELECT email,id,name,sn FROM im_user");
			if(rs!=null){
				list=new ArrayList<Map<String,String>>();
				while(rs.next()){
					Map<String, String> map=new HashMap<String, String>();
					map.put("email", rs.getString(1));
					map.put("id", rs.getString(2));
					map.put("name", rs.getString(3));
					map.put("sn", rs.getString(4));
					if("null".equals(rs.getString(1)) || rs.getString(1)==null)continue;
					if("null".equals(rs.getString(2)) || rs.getString(2)==null)continue;
					list.add(map);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(rs!=null)try{rs.close();}catch(Exception e){}
			if(st!=null)try{st.close();}catch(Exception e){}
			if(con!=null)try{con.close();}catch(Exception e){}
		}
		//System.out.println(list);
		return list;
	  }
	  
	
	/**key邮箱，value 组织ID
	 * @throws Exception **/
	public static List<Map<String, String>> getUserInfo() {
	    Statement st=null;
		ResultSet rs=null;
		Connection con=null;
		List<Map<String, String>>  list=null;
		try {
			con=getConnection();
			st=con.createStatement();
			rs=st.executeQuery("select * from(SELECT u.email,attr.value FROM im_user u LEFT JOIN im_org_user ou on u.id=ou.user_id LEFT JOIN im_org o on o.id=ou.ORG_ID LEFT JOIN (select org_id,name,value from im_org_attr where name='QQ_MAIL_ORG_ID') attr on o.id=attr.ORG_ID) tab");
			if(rs!=null){
				list=new ArrayList<Map<String,String>>();
				while(rs.next()){
					Map<String, String> map=new HashMap<String, String>();
					map.put("email", rs.getString(1));
					map.put("orgid", rs.getString(2));
					if("null".equals(rs.getString(1)) || rs.getString(1)==null)continue;
					if("null".equals(rs.getString(2)) || rs.getString(2)==null)continue;
					list.add(map);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(rs!=null)try{rs.close();}catch(Exception e){}
			if(st!=null)try{st.close();}catch(Exception e){}
			if(con!=null)try{con.close();}catch(Exception e){}
		}
		//System.out.println(list);
		return list;
	  }
	  
  
	  public static List<Map<String, String>> getOrgs() {
		    Statement st=null;
			ResultSet rs=null;
			Connection con=null;
			List<Map<String, String>>  list=null;
			try {
				con=getConnection();
				st=con.createStatement();
				rs=st.executeQuery("select id,parent_id,name from im_org  o left join (select org_id ,value from im_org_attr where name='ORG_STATUS') attr on attr.org_id=o.id where o.COMPANY_SN='100001' and o.parent_id!=-1 and o.attr.value=1 ORDER BY o.CREATE_TIME asc");
				if(rs!=null){
					list=new ArrayList<Map<String,String>>();
					while(rs.next()){
						Map<String, String> map=new HashMap<String, String>();
						map.put("id", rs.getString(1));
						map.put("parent_id", rs.getString(2));
						map.put("name", rs.getString(3));
						list.add(map);
					}
				}
			}catch (Exception e) {
				e.printStackTrace();
			}finally{
				if(rs!=null)try{rs.close();}catch(Exception e){}
				if(st!=null)try{st.close();}catch(Exception e){}
				if(con!=null)try{con.close();}catch(Exception e){}
			}
			return list;
	  }
		  
	  
	  public static int exceSQL(String sql) {
		    Statement st=null;
			int rs=0;
			Connection con=null;
			try {
				con=getConnection();
				st=con.createStatement();
				rs=st.executeUpdate(sql);
			}catch (Exception e) {
				e.printStackTrace();
			}finally{
				if(st!=null)try{st.close();}catch(Exception e){}
				if(con!=null)try{con.close();}catch(Exception e){}
			}
			return rs;
	  }
	  
	 /* test private static Connection getConnection() throws Exception{
			Class.forName("com.mysql.jdbc.Driver");
			return DriverManager.getConnection("jdbc:mysql://139.196.148.8:3306/sso","novasim","sim868");
	  }*/
	  
	  private static Connection getConnection() throws Exception{
			Class.forName("com.mysql.jdbc.Driver");
			return DriverManager.getConnection("jdbc:mysql://47.103.181.245:3306/sso","novaiam","Novaiam8888");
	  }
	
}
