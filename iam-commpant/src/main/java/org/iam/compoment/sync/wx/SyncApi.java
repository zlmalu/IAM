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

import com.sense.core.util.ContextUtil;
import com.sense.core.util.HttpUtil;
import com.sense.core.util.StringUtils;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.SyncInteface;
import com.sense.iam.service.JdbcService;

import net.sf.json.JSONObject;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class SyncApi implements SyncInteface{

	 private Log log = LogFactory.getLog(getClass());
	  /*test @Param("企业ID")
	  private String corpid = "ww9410bb0b70e44ce1";
	  @Param("管理密钥")
	  private String corpsecret = "DEyoLxGRyAG1dgby_tuSEPg2Mc9-gLW24lCWEBaUaZs";*/
	 
	  @Param("企业ID")
	  private String corpid;
	  @Param("管理密钥")
	  private String corpsecret;
	  
	 
	  @Param("操作类型(1=新增,2=修改,3=删除)")
	  private String optType = "1";
	  @Param("操作目标(1=组织,2=用户)")
	  private String optDest = "1";
	
	/**
	 * 获取AccessToken
	 * @return
	 * description :  
	 * wenjianfeng 2019年12月24日
	 */
	private String getAccessToken() {
		try {
			String result = HttpUtil.GET_API("https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="+corpid+"&corpsecret="+corpsecret,new HashMap());
			log.debug(result);
			if(!result.contains("ok")){
				log.error("获取Token异常");
			}
			return JSONObject.fromObject(result).get("access_token").toString();
		} catch (Exception e) {
			log.error("获取访问token失败",e);
		}
		return "";
	}

	@Override
	public ResultCode execute(String content) {
		content=content.trim();
		System.out.println("corpid="+corpid+",corpsecret="+corpsecret);
		System.out.println("============SIM下推数据："+content);
		if(optDest.equals("1")){
			if(optType.equals("1")){//组织新增
				return orgAdd(content);
			}else if(optType.equals("2")){//组织修改
				return orgEdit(content);
			}else if(optType.equals("3")){//组织删除
				return orgDel(content);
			}
		}else if(optDest.equals("2")){
			if(optType.equals("1")){//用户新增
				return userAdd(content);
			}else if(optType.equals("2")){//用户修改
				return userEdit(content);
			}else if(optType.equals("3")){//用户删除
				return userDel(content);
			}
		}
		return new ResultCode(FAIL,"同步目标为空");
	}
	
	
	public ResultCode orgAdd(String content){
		JdbcService jdbcService=(JdbcService) ContextUtil.getBean("jdbcService");
		try{
			JSONObject contObj=JSONObject.fromObject(content);
			Object parentId=contObj.get("parentid");
			Long orgId = contObj.getLong("orgId");
			//判断是否重新设置顶级节点
			if(StringUtils.getString(parentId).trim().length()==0 || StringUtils.getString(parentId).trim().equals("null")|| StringUtils.getString(parentId).trim().equals("1")){
				//设置
				List list=jdbcService.findList("select attr.value as WX_PARENT_ID from im_org o LEFT JOIN im_org_attr attr on o.parent_id=attr.org_id where attr.NAME='WX_ORG_ID' and id="+orgId);
				if(list!=null && list.size()>0){
					contObj.put("parentid", ((Map)list.get(0)).get("WX_PARENT_ID"));
					content=contObj.toString();
				}
			}
			System.out.println("sync org add content===="+content);
			String result=HttpUtil.POST_API("https://qyapi.weixin.qq.com/cgi-bin/department/create?access_token="+getAccessToken(), content, new HashMap());
			JSONObject jo=JSONObject.fromObject(result);
			//反写本地组织机构对应的qq邮箱组织ID
			
			jdbcService.executeSql("insert into im_org_ATTR(ORG_ID,NAME,VALUE) values("+orgId+",'WX_ORG_ID','"+jo.get("id")+"')");
			
			if(jo.getInt("errcode")==0){
				return new ResultCode(SUCCESS,jo.getString("errmsg"));
			}else{
				return new ResultCode(FAIL,jo.getString("errmsg"));
			}
		}catch(Exception e){
			log.error("sync qq mail add exception",e);
			return new ResultCode(FAIL,"sysn error "+e.getMessage());
		}
	}
	
	public ResultCode orgEdit(String content){
		try{
			String result=HttpUtil.POST_API("https://qyapi.weixin.qq.com/cgi-bin/department/update?access_token="+getAccessToken(), content, new HashMap());
			JSONObject jo=JSONObject.fromObject(result);
			if(jo.getInt("errcode")==0){
				//反写本地组织机构对应的qq邮箱组织ID
				return new ResultCode(SUCCESS,jo.getString("errmsg"));
			}else{
				return new ResultCode(FAIL,jo.getString("errmsg"));
			}
		}catch(Exception e){
			log.error("sync qq mail edit exception",e);
			return new ResultCode(FAIL,"sysn error "+e.getMessage());
		}
	}
	
	public ResultCode orgDel(String content){
		try{
			String result=HttpUtil.GET_API("https://qyapi.weixin.qq.com/cgi-bin/department/delete?access_token="+getAccessToken()+"&id="+content.trim(), new HashMap());
			JSONObject jo=JSONObject.fromObject(result);
			if(jo.getInt("errcode")==0){
				//反写本地组织机构对应的qq邮箱组织ID
				return new ResultCode(SUCCESS,jo.getString("errmsg"));
			}else{
				return new ResultCode(FAIL,jo.getString("errmsg"));
			}
		}catch(Exception e){
			log.error("sync qq mail del exception",e);
			return new ResultCode(FAIL,"sysn error "+e.getMessage());
		}
	}
	
	public ResultCode userAdd(String content){
		try{
			String result=HttpUtil.POST_API("https://qyapi.weixin.qq.com/cgi-bin/user/create?access_token="+getAccessToken(), content, new HashMap());
			JSONObject jo=JSONObject.fromObject(result);
			if(jo.getInt("errcode")==0){
				return new ResultCode(SUCCESS,jo.getString("errmsg"));
			}else{
				return new ResultCode(FAIL,jo.getString("errmsg"));
			}
		}catch(Exception e){
			log.error("sync qq mail add exception",e);
			return new ResultCode(FAIL,"sysn error "+e.getMessage());
		}
	}
	
	public ResultCode userEdit(String content){
		try{
			String result=HttpUtil.POST_API("https://qyapi.weixin.qq.com/cgi-bin/user/update?access_token="+getAccessToken(), content, new HashMap());
			JSONObject jo=JSONObject.fromObject(result);
			if(jo.getInt("errcode")==0){
				return new ResultCode(SUCCESS,jo.getString("errmsg"));
			}else{
				return new ResultCode(FAIL,jo.getString("errmsg"));
			}
		}catch(Exception e){
			log.error("sync qq mail edit exception",e);
			return new ResultCode(FAIL,"sysn error "+e.getMessage());
		}
	}
	
	public ResultCode userDel(String content){
		try{
			String result=HttpUtil.GET_API("https://qyapi.weixin.qq.com/cgi-bin/user/delete?access_token="+getAccessToken()+"&userid="+content.trim(), new HashMap());
			JSONObject jo=JSONObject.fromObject(result);
			if(jo.getInt("errcode")==0){
				return new ResultCode(SUCCESS,jo.getString("errmsg"));
			}else{
				return new ResultCode(FAIL,jo.getString("errmsg"));
			}
		}catch(Exception e){
			log.error("sync qq mail del exception",e);
			return new ResultCode(FAIL,"sysn error "+e.getMessage());
		}
	}
	
	  
	 /* public static void main(String[] args)
	  {
		 SyncApi syncApi = new SyncApi();
		 String token=syncApi.getAccessToken();
	   
	    System.out.println(token);
	    String url="https://qyapi.weixin.qq.com/cgi-bin/department/list?access_token="+token;
	    System.out.println(url);
	    String result = HttpUtil.GET_API(url, new HashMap());
	    System.out.println(result);
	    JSONObject json=JSONObject.fromObject(result);
	    System.out.println(json.getJSONArray("department").size());
	    for(int i=0;i<json.getJSONArray("department").size();i++){
	    	 if(json.getJSONArray("department").getJSONObject(i).getInt("id")==1){
	    		 continue;
	    	 }
	    	 String delurl="https://qyapi.weixin.qq.com/cgi-bin/department/delete?access_token="+token+"&id="+json.getJSONArray("department").getJSONObject(i).getInt("id");
	    	 String resp = HttpUtil.GET_API(delurl, new HashMap());
	    	 System.out.println(resp);
	    }
	 }*/
	  
	  
	  
		 public static void main(String[] args){

			List<Map<String, String>> list = getUserInfo();
			
			SyncApi syncApi = new SyncApi();
			String token=syncApi.getAccessToken();
		   //获取企业微信人员的信息
		    //System.out.println(token);
		    String url="https://qyapi.weixin.qq.com/cgi-bin/user/simplelist?access_token="+token+"&department_id=1&fetch_child=1";
		    System.out.println(url);
		    String result = HttpUtil.GET_API(url, new HashMap());
		    System.out.println(result);
		    JSONObject json=JSONObject.fromObject(result);
		    for(int i=0;i<json.getJSONArray("userlist").size();i++){
		    	 String getUserInfoUrl="https://qyapi.weixin.qq.com/cgi-bin/user/get?access_token="+token+"&userid="+json.getJSONArray("userlist").getJSONObject(i).getString("userid");
		    	 String userinforest = HttpUtil.GET_API(getUserInfoUrl, new HashMap());
		    	 JSONObject userinfo=JSONObject.fromObject(userinforest);
		    	 //根据手机号更新成员的新组织
		    	  System.out.println(userinfo);
		    	  String updateUserURL="https://qyapi.weixin.qq.com/cgi-bin/user/update";
		    	 JSONObject updateModel=new JSONObject();
		    	 Map<String, String> omap=getUserInfoByTelephone(userinfo.getString("mobile"), list);
		    	 if(omap==null){
		    		 System.out.println("get qywxid fail");
		    		 continue;
		    	 }
		    	 if(omap.get("orgid")==null){
		    		 System.out.println("get orgid fail");
		    		 continue;
		    	 }
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
		  }
	  
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
				}
				//System.out.println(list);
				return list;
		  }
	  
		  private static Connection getConnection() throws Exception{
				Class.forName("com.mysql.jdbc.Driver");
				return DriverManager.getConnection("jdbc:mysql://139.196.148.8:3306/sso","novasim","sim868");
		  }
	
}
