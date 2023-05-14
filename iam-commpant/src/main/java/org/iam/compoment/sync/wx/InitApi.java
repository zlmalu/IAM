package org.iam.compoment.sync.wx;

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
import com.sense.iam.compoment.Param;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class InitApi {

	private Log log = LogFactory.getLog(getClass());
	/*test @Param("企业ID")
	private String corpid = "ww9410bb0b70e44ce1";
	@Param("管理密钥")
	private String corpsecret = "DEyoLxGRyAG1dgby_tuSEPg2Mc9-gLW24lCWEBaUaZs";*/
	 
	//生产
	@Param("企业ID")
	private static String corpid = "wx85c02df3e1109a52";
	@Param("管理密钥")
	private static String corpsecret = "eDHX7nW7J7xVZdHDbwr66PLUMZExqjpL4UcxgTmi9nU";
	  
	
	private static Map<String, String> qywxrespkeys=new HashMap<String, String>();
	
	public static void main(String[] args) {
		//initOrg();
		initUser();
	}
	public static void initOrg(){
		//初始化IAM组织对象
		//声明根
		qywxrespkeys.put("1000005205", "1");
		List<Map<String, String>> list = getOrgs();
		InitApi syncApi = new InitApi();
		String token=syncApi.getAccessToken();
	    for(int i=0;i<list.size();i++){
	    	Map<String, String> map=list.get(i);
	    	JSONObject jssd=new JSONObject();
	    	jssd.put("orgId", map.get("id"));
	    	jssd.put("name", map.get("name"));
	    	//获取企业微信的ID
	    	jssd.put("parentid", qywxrespkeys.get(map.get("parent_id"))==null?"1":qywxrespkeys.get(map.get("parent_id")));
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
			System.out.println("sync org add content===="+content);
			String result=HttpUtil.POST_API("https://qyapi.weixin.qq.com/cgi-bin/department/create?access_token="+token, content, new HashMap());
			JSONObject jo=JSONObject.fromObject(result);
			System.out.println("add status:"+jo.toString());
			System.out.println("");
			if(jo.getInt("errcode")==0){
				//反写本地组织机构对应的企业微信组织ID
				int flagStatus=exceSQL("insert into im_org_ATTR(ORG_ID,NAME,VALUE) values("+orgId+",'WX_ORG_ID','"+jo.get("id")+"')");
				if(flagStatus>0){
					System.out.println("add db success....");
					//添加编码ID到内存中
					qywxrespkeys.put(orgId, jo.get("id").toString());
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
	private static String getAccessToken() {
		try {
			String result = HttpUtil.GET_API("https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="+corpid+"&corpsecret="+corpsecret,new HashMap());
			//log.debug(result);
			if(!result.contains("ok")){
				//log.error("获取Token异常");
			}
			return JSONObject.fromObject(result).get("access_token").toString();
		} catch (Exception e) {
			//log.error("获取访问token失败",e);
		}
		return "";
	}

	/**
	 * 初始化企业微信用户到IAM
	 */
	public static void initUser(){
		List<Map<String, String>> list =getUserInfoByUserId();
		
		InitApi syncApi = new InitApi();
		String token=syncApi.getAccessToken();
	   //获取企业微信人员的信息
	    //System.out.println(token);
	    String url="https://qyapi.weixin.qq.com/cgi-bin/user/simplelist?access_token="+token+"&department_id=1&fetch_child=1";
	    String result = HttpUtil.GET_API(url, new HashMap());
	    JSONObject json=JSONObject.fromObject(result);
	    
	    JSONArray data=new JSONArray();
	    int count=0;
	    int fail=0;
	    for(int i=0;i<json.getJSONArray("userlist").size();i++){
	    	 String getUserInfoUrl="https://qyapi.weixin.qq.com/cgi-bin/user/get?access_token="+token+"&userid="+json.getJSONArray("userlist").getJSONObject(i).getString("userid");
	    	 String userinforest = HttpUtil.GET_API(getUserInfoUrl, new HashMap());
	    	 JSONObject userinfo=JSONObject.fromObject(userinforest);
	    	// System.out.println(userinfo.getString("mobile"));
	    	 Map<String, String> omap=getUserInfoByTelephone(userinfo.getString("mobile"), list);
	    	 if(omap==null){
	    		 data.add(userinfo);
	    		 continue;
	    	 }
	    	 long id=getQId("SELECT NEXTVAL('UIM_SEQ')");
	    	 String install="insert into im_account(id,login_name,login_pwd,app_id,user_id,status,CREATE_TIME,OPT_USER,OPEN_TYPE,ACCT_TYPE,COMPANY_SN) values("+id+",'"+userinfo.getString("userid")+"','"+UIM.encode("123456")+"',1000006832,"+omap.get("id")+",1,now(),'admin',1,1,'100001')";
	    	 System.out.println(install);
	    	 try{
	    		 int flagStatus=exceSQL(install);
				 if(flagStatus>0){
					count++;
					System.out.println("add db success....");
				 }else{
					fail++;
					System.out.println("add db fail....");
				 }
	    	 }catch(Exception e){
	    		 e.printStackTrace();
	    	 }
	    }
	    System.out.println("已插入："+count+",未插入："+fail);
	    System.out.println("配置失败数量："+data.size());
	    for(int i=0;i<data.size();i++){
	    	System.out.println(data.getJSONObject(i));
	    }
	 }
	


	/*public static void main(String[] args){

		List<Map<String, String>> list = getUserInfo();
		
		InitApi syncApi = new InitApi();
		String token=syncApi.getAccessToken();
	   //获取企业微信人员的信息
	    //System.out.println(token);
	    String url="https://qyapi.weixin.qq.com/cgi-bin/user/simplelist?access_token="+token+"&department_id=1&fetch_child=1";
	    System.out.println(url);
	    String result = HttpUtil.GET_API(url, new HashMap());
	   // System.out.println(result);
	    JSONObject json=JSONObject.fromObject(result);
	    JSONArray data=new JSONArray();
	    for(int i=0;i<json.getJSONArray("userlist").size();i++){
	    	 String getUserInfoUrl="https://qyapi.weixin.qq.com/cgi-bin/user/get?access_token="+token+"&userid="+json.getJSONArray("userlist").getJSONObject(i).getString("userid");
	    	 String userinforest = HttpUtil.GET_API(getUserInfoUrl, new HashMap());
	    	 JSONObject userinfo=JSONObject.fromObject(userinforest);
	    	 //根据手机号更新成员的新组织
	    	// System.out.println(userinfo);
	    	 String updateUserURL="https://qyapi.weixin.qq.com/cgi-bin/user/update";
	    	 JSONObject updateModel=new JSONObject();
	    	 Map<String, String> omap=getUserInfoByTelephone(userinfo.getString("mobile"), list);
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
	    	 //updateModel.put("userid", userinfo.getString("userid"));
	    	 //updateModel.put("department", "["+omap.get("orgid")+"]");
	    	// System.out.println("update model:"+updateModel);
	    	 try{
	    		// String result2 = HttpUtil.post(updateUserURL, token, updateModel.toString());
	    		//System.out.println("update resulty:"+result2);
	    	 }catch(Exception e){
	    		 e.printStackTrace();
	    	 }
	    }
	    System.out.println(data.size());
	    for(int i=0;i<data.size();i++){
	    	System.out.println(data.getJSONObject(i));
	    }
	  }
  */
	  
  
	  public static Map<String, String> getUserInfoByTelephone(String telephone,List<Map<String, String>> list){
		  Map<String, String> map=null;
		  for(Map<String, String> maps:list){
			  if(maps.get("telephone").equals(telephone)){
				  return maps;
			  }
		  }
		  return map; 
	  }
	  /**key手机号，value 组织ID
	 * @throws Exception **/
	  public static List<Map<String, String>> getUserInfo() {
		    Statement st=null;
			ResultSet rs=null;
			Connection con=null;
			List<Map<String, String>>  list=null;
			try {
				con=getConnection();
				st=con.createStatement();
				rs=st.executeQuery("select * from(SELECT u.telephone,attr.value FROM im_user u LEFT JOIN im_org_user ou on u.id=ou.user_id LEFT JOIN im_org o on o.id=ou.ORG_ID LEFT JOIN (select org_id,name,value from im_org_attr where name='WX_ORG_ID') attr on o.id=attr.ORG_ID) tab");
				if(rs!=null){
					list=new ArrayList<Map<String,String>>();
					while(rs.next()){
						Map<String, String> map=new HashMap<String, String>();
						map.put("telephone", rs.getString(1));
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
	  
	  /**key手机号，value 用户ID
	* @throws Exception **/
	  public static List<Map<String, String>> getUserInfoByUserId() {
		    Statement st=null;
			ResultSet rs=null;
			Connection con=null;
			List<Map<String, String>>  list=null;
			try {
				con=getConnection();
				st=con.createStatement();
				rs=st.executeQuery("SELECT telephone,id FROM im_user where TELEPHONE !=''");
				if(rs!=null){
					list=new ArrayList<Map<String,String>>();
					while(rs.next()){
						Map<String, String> map=new HashMap<String, String>();
						map.put("telephone", rs.getString(1));
						map.put("id", rs.getString(2));
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
	  
	  private static Connection getConnection() throws Exception{
			Class.forName("com.mysql.jdbc.Driver");
			return DriverManager.getConnection("jdbc:mysql://47.103.181.245:3306/sso","novaiam","Novaiam8888");
	  }
	
}
