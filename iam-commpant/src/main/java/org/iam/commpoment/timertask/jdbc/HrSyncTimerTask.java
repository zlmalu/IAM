package org.iam.commpoment.timertask.jdbc;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;





import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import com.sense.core.util.ContextUtil;
import com.sense.core.util.CurrentAccount;
import com.sense.core.util.TimeUtil;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.TaskInterface;
import com.sense.iam.model.im.Org;
import com.sense.iam.model.im.Position;
import com.sense.iam.model.im.PostionFuncModel;
import com.sense.iam.model.im.User;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.OrgService;
import com.sense.iam.service.PositionService;
import com.sense.iam.service.UserService;


@Name("同步HR 组织与岗位用户数据")
public class HrSyncTimerTask implements TaskInterface{
	
	private Log log = LogFactory.getLog(getClass());
	
	@Param("HR接口前缀")
	private String address="http://mgtest.genzon.com.cn:8000";
	@Param("请求clientId")
	private String clientId="gz-hr";
	@Param("请求密钥secret")
	private String secret="788d295cb4e58997a4afa0cf22f48fe0";
	@Param("IAM默认根组织ID")
	private String root="1000022714";
	@Param("IAM默认用户类型ID")
	private String rootUserType="1000022716";
	@Override
	public void run(Long timerTaskId, Date runTime) {
		log.info("execute timerTaskId="+timerTaskId+",execute time:"+String.format("%TF %TT", runTime,runTime));
		if(CurrentAccount.getCurrentAccount()==null){
			CurrentAccount account=new CurrentAccount("100001");
			account.setId(1000000765L);
			account.setLoginName("admin");
			account.setRemoteHost("127.0.0.1");
			account.setValid(true);
			account.setSessionId(UUID.randomUUID().toString());
			account.setName("管理员");
			CurrentAccount.setCurrentAccount(account);
		}
		
		OrgService orgService=(OrgService) ContextUtil.context.getBean(OrgService.class);
		UserService userService=(UserService) ContextUtil.context.getBean(UserService.class);
		PositionService positionService=(PositionService) ContextUtil.context.getBean(PositionService.class);


		JdbcService jdbcService=(JdbcService) ContextUtil.getBean("jdbcService");
		JSONArray data=getHrOrgData();
		if(data==null)return;
		JSONArray orgData=getDataByType(data, "dept");
		JSONArray personData=getDataByType(data, "person");
		JSONArray postData=getDataByType(data, "post");
		
		
		for(int i=0;i<orgData.size();i++){
			JSONObject model=orgData.getJSONObject(i);
			List<Map<String, Object>> lists=jdbcService.findList("select ID from im_org where sn='"+model.getString("no")+"'");
			List<Map<String, Object>> par=jdbcService.findList("select ID from im_org where sn='"+model.getString("parent")+"'");
			if(lists!=null&&lists.size()>0){
				Org o=orgService.findById(Long.valueOf(lists.get(0).get("ID").toString()));
				o.setName(model.getString("name"));
				o.setStatus(model.getString("isAvailable").equals("true")?1:2);
				o.setSortNum(model.getInt("order"));
				if(o.getParentId()!=-1){
					if(par!=null&&par.size()==0){
						o.setParentId(Long.valueOf(root));
					}else{
						o.setParentId(Long.valueOf(par.get(0).get("ID").toString()));
					}
				}
				orgService.edit(o);
			}else{
				Org o=new Org();
				o.setSn(model.getString("no"));
				o.setName(model.getString("name"));
				o.setStatus(model.getString("isAvailable").equals("true")?1:2);
				if(par!=null&&par.size()==0){
					o.setParentId(Long.valueOf(root));
				}else{
					o.setParentId(Long.valueOf(par.get(0).get("ID").toString()));
				}
				o.setSortNum(model.getInt("order"));
				orgService.save(o);
			}
			//记录日志
			//sync_hr_org
			StringBuffer sql=new StringBuffer("insert into sync_hr_org(id,oid,isavailable,no,name,parent,sync_hr_org.order,fulldata,create_time) values(");
			sql.append("(SELECT NEXTVAL('UIM_SEQ') as id),");
			sql.append("'"+model.getString("id")+"',");
			sql.append("'"+model.getString("isAvailable")+"',");
			sql.append("'"+model.getString("no")+"',");
			sql.append("'"+model.getString("name")+"',");
			sql.append("'"+model.getString("parent")+"',");
			sql.append(""+model.getInt("order")+",");
			sql.append("'"+model.toString()+"',");
			sql.append("now())");
			jdbcService.executeSql(sql.toString());
		}
		
		//同步岗位
		for(int i=0;i<postData.size();i++){
			JSONObject model=postData.getJSONObject(i);
			List<Map<String, Object>> lists=jdbcService.findList("select ID from im_position where sn='"+model.getString("no")+"'");
			List<Map<String, Object>> par=jdbcService.findList("select ID from im_position where sn='"+model.getString("parent")+"'");
			if(lists!=null&&lists.size()>0){
				Position o=positionService.findById(Long.valueOf(lists.get(0).get("ID").toString()));
				o.setName(model.getString("name"));
				positionService.edit(o);
			}else{
				Position o=new Position();
				o.setSn(model.getString("no"));
				o.setName(model.getString("name"));
				positionService.save(o);
			}
			model.remove("persons");
			//记录日志
			//sync_hr_post
			StringBuffer sql=new StringBuffer("insert into sync_hr_post(id,oid,isavailable,no,name,parent,sync_hr_post.order,fulldata,create_time) values(");
			sql.append("(SELECT NEXTVAL('UIM_SEQ') as id),");
			sql.append("'"+model.getString("id")+"',");
			sql.append("'"+model.getBoolean("isAvailable")+"',");
			sql.append("'"+model.getString("no")+"',");
			sql.append("'"+model.getString("name")+"',");
			sql.append("'"+model.getString("parent")+"',");
			sql.append(""+model.getInt("order")+",");
			sql.append("'"+model.toString()+"',");
			sql.append("now())");
			jdbcService.executeSql(sql.toString());
		}
		
		//同步用户
		for(int i=0;i<personData.size();i++){
			JSONObject model=personData.getJSONObject(i);
			log.info("psrsonData:"+model.toString());
			List<Map<String, Object>> lists=jdbcService.findList("select ID from im_user where sn='"+model.getString("no")+"'");
			List<Map<String, Object>> par=jdbcService.findList("select ID from im_org where sn='"+model.getString("parent")+"'");
			if(lists!=null&&lists.size()>0){
				User o=userService.findById(Long.valueOf(lists.get(0).get("ID").toString()));
				o.setName(model.getString("name"));	
				if(par!=null&&par.size()==0){
					o.setOrgId(Long.valueOf(root));
				}else{
					if(Long.valueOf(par.get(0).get("ID").toString()).longValue()!=o.getOrgId()){
						o.setOrgId(Long.valueOf(par.get(0).get("ID").toString()));
					}
				}
				if(model.containsKey("mobileNo")){
					o.setTelephone(model.getString("mobileNo"));
				}
				if(model.containsKey("email")){
					o.setEmail(model.getString("email"));
				}
				o.setSex(model.getString("sex").equals("M")?1:2);
				o.setStatus(model.getString("isAvailable").equals("true")?1:2);
				userService.edit(o);
			}else{
				User o=new User();
				o.setUserTypeId(Long.valueOf(rootUserType));
				o.setName(model.getString("name"));	
				o.setSn(model.getString("no"));
				if(par!=null&&par.size()==0){
					o.setOrgId(Long.valueOf(root));
				}else{
					o.setOrgId(Long.valueOf(par.get(0).get("ID").toString()));
				}
				if(model.containsKey("mobileNo")){
					o.setTelephone(model.getString("mobileNo"));
				}
				if(model.containsKey("email")){
					o.setEmail(model.getString("email"));
				}
				o.setSex(model.getString("sex").equals("M")?1:2);
				o.setStatus(model.getString("isAvailable").equals("true")?1:2);
				
				userService.save(o);
			}
			//记录日志
			//sync_hr_user
			StringBuffer sql=new StringBuffer("insert into sync_hr_user(id,oid,isavailable,no,name,parent,sync_hr_user.order,fulldata,create_time) values(");
			sql.append("(SELECT NEXTVAL('UIM_SEQ') as id),");
			sql.append("'"+model.getString("id")+"',");
			sql.append("'"+model.getBoolean("isAvailable")+"',");
			sql.append("'"+model.getString("no")+"',");
			sql.append("'"+model.getString("name")+"',");
			sql.append("'"+model.getString("parent")+"',");
			sql.append(""+model.getInt("order")+",");
			sql.append("'"+model.toString()+"',");
			sql.append("now())");
			jdbcService.executeSql(sql.toString());
		}
	}
	
	
	public JSONArray getHrOrgData(){
		String tokenUrl=address+"/api/auth/client/getAccessToken";
		String DATAUrl=address+"/api/hr/getOrgStruct";
		String parameters="clientId="+clientId+"&secret="+secret;
		JSONObject tokenObj=null;
		String token=null;
		try{
			log.info("tokenUrl:"+tokenUrl+"?"+parameters);
			tokenObj=JSONObject.fromObject(POST_API(null,tokenUrl, parameters));
			if(tokenObj.getInt("status")==200){
				token=tokenObj.getString("token");
				try{
					//parameters="time="+TimeUtil.DateToString(new Date());
					JSONObject orgInfo=JSONObject.fromObject(POST_API(token, DATAUrl, parameters));
					if(orgInfo.getInt("status")==200){
						return orgInfo.getJSONArray("data");
					}else{
						log.info("get data fail:"+orgInfo.toString());
					}
				}catch(Exception e){
					log.info("get data fail:"+e.getMessage());
				}
				
			}else{
				log.info("get token fail:"+tokenObj.toString());
			}
			
		}catch (Exception e) {
			log.info("get token fail:"+e.getMessage());
		}
		return null;
	}

	
	/**
	 * 获取数据类型数据
	 * @param data 所有数据集合
	 * @param type 过滤集合
	 * @return
	 */
	public JSONArray getDataByType(JSONArray data,String type){
		JSONArray newData=new JSONArray();
		for(int i=0;i<data.size();i++){
			if(data.getJSONObject(i).containsKey("type")){
				if(data.getJSONObject(i).getString("type").equals(type)){
					newData.add(data.getJSONObject(i));
				}
			}
		}
		return newData;
	}
    
    

    
    
    /**
	 * 采用POST方式进行
	 * @param url 
	 * @param paramStr
	 * @return
	 */
	public String POST_API(String token,String urlStr,String paramStr) {
		BufferedReader br=null;
		OutputStream os=null;
		StringBuffer strBuf=new StringBuffer();
		try {
			URL url=new URL(urlStr);
			HttpURLConnection con=(HttpURLConnection) url.openConnection();
			con.setReadTimeout(5000);//5秒过期
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestMethod("POST");
			if(token!=null){
				log.info("call x-client-token:"+token);
				con.setRequestProperty("x-client-token", token);
	        }
			os=con.getOutputStream();
			if(paramStr!=null){
				os.write(paramStr.getBytes("UTF-8"));
			}
			if(con.getResponseCode()==200){
				br=new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
				String line;
				while((line=br.readLine())!=null){
					strBuf.append(line);
				}
			}
		} catch (Exception e) {
			log.error("post_api error",e);
		} finally{
			if(os!=null){
				try{os.close();}catch(Exception e){}
			}
			if(br!=null){
				try{br.close();}catch(Exception e){}
			}
		}
		//log.info("call rest:"+strBuf.toString());
		return strBuf.toString();
	}
}
