package org.iam.commpoment.timertask.dingding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.core.util.ContextUtil;
import com.sense.core.util.CurrentAccount;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.TaskInterface;
import com.sense.iam.model.im.Org;
import com.sense.iam.model.im.User;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.OrgService;
import com.sense.iam.service.UserService;

@Name("钉钉用户同步数据")
public class DingDingUserSync implements TaskInterface {

	protected Log log=LogFactory.getLog(getClass());
	@Param("钉钉应用钥匙")
	private static String DINGDING_APP_KEY="";
	@Param("钉钉应用密钥")
	private static String DINGDING_APP_SECRET="";
	@Param("IAM默认根组织编码")
	private String root="";
	@Param("IAM默认用户类型ID")
	private String rootUserType="";
	
	private String access_token = "";
	
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
		Set<Long> ids = new HashSet<Long>();
		
		JSONArray orgData=getBranchTrees();
		if(orgData==null)return;
		for(int i=0;i<orgData.size();i++){
			JSONObject model=orgData.getJSONObject(i);
			ids.add(Long.valueOf(model.get("id").toString()));
		}
		log.info("ids="+ids);
		getUser(ids);
	}
	
	private JSONArray getBranchTrees(){
		JSONObject result=get("https://oapi.dingtalk.com/gettoken?appkey="+DINGDING_APP_KEY+"&appsecret="+DINGDING_APP_SECRET);
		
		access_token = result.getString("access_token");
		log.info("access_token="+access_token);
		if(result.getInt("errcode")!=0){
			log.info("result="+result);
		}
		
		if(result.getInt("errcode")==0){
			JSONObject result1=get("https://oapi.dingtalk.com/department/list?access_token="+result.getString("access_token"));
			System.out.println(result1);
			if(result1.getInt("errcode")!=0){
				log.info("result1="+result1);
			}
			else{
				String department = result1.getString("department");
				return JSONArray.fromObject(department);
			}
		}
		return null;
	}
	
	private JSONArray getUser(Set<Long> ids){
		JdbcService jdbcService=(JdbcService) ContextUtil.getBean("jdbcService");
		UserService userService=(UserService) ContextUtil.context.getBean(UserService.class);
		JSONArray fromObject = new JSONArray() ;
		int count = 0;
		log.info("access_token="+access_token);
		log.info("ids="+ids);
		for (Long department_id : ids) {
			
			JSONObject result1=get("https://oapi.dingtalk.com/user/listbypage?access_token="+access_token+"&department_id="+department_id+"&offset=0&size=100");
			if(result1.getInt("errcode")!=0){
				log.info("result1="+result1);
			}
			else{
				String department = result1.getString("userlist");
				JSONArray userlist = JSONArray.fromObject(department);
				log.info("userlist="+userlist);

				List<Map<String, Object>> orgS=jdbcService.findList("select ID from im_org where sn='"+department_id+"'");
				
				log.info("orgId="+Long.valueOf(orgS.get(0).get("ID").toString()));
				for (int i = 0; i < userlist.size(); i++) {
					JSONObject model=userlist.getJSONObject(i);
					List<Map<String, Object>> lists;
					log.info("model.containsKey(jobnumber)"+model.containsKey("jobnumber"));
					log.info("model.containsKey(mobile)"+model.containsKey("mobile"));
					if(model.containsKey("jobnumber")){
						lists=jdbcService.findList("select ID from im_user where sn='"+model.getString("jobnumber")+"'");
					}
					else if(model.containsKey("mobile")){
						lists=jdbcService.findList("select ID from im_user where sn='"+model.getString("mobile")+"'");
					}
					else{
						lists=jdbcService.findList("select ID from im_user where sn='"+model.getString("userid")+"'");
					}
					
					if(lists!=null&&lists.size()>0){
						User u=userService.findById(Long.valueOf(lists.get(0).get("ID").toString()));
						u.setName(model.getString("name"));	
						u.setOrgId(Long.valueOf(orgS.get(0).get("ID").toString()));
						if(model.containsKey("mobile")){
							u.setTelephone(model.getString("mobile"));
						}
						if(model.containsKey("email")){
							u.setEmail(model.getString("email"));
						}
						u.setStatus(1);
						userService.edit(u);
					}else{
						User u=new User();
						u.setUserTypeId(Long.valueOf(rootUserType));
						u.setName(model.getString("name"));	
						u.setOrgId(Long.valueOf(orgS.get(0).get("ID").toString()));
						if(model.containsKey("mobile")){
							u.setTelephone(model.getString("mobile"));
						}
						if(model.containsKey("jobnumber")){
							u.setSn(model.getString("jobnumber"));
						}
						else if(model.containsKey("mobile")){
							u.setSn(model.getString("mobile"));
						}
						else{
							u.setSn(model.getString("userid"));
						}
						if(model.containsKey("email")){
							u.setEmail(model.getString("email"));
						}
						u.setStatus(1);
						userService.save(u);
					}
					count++;
				}
			
			}
		}
		
		log.info("count:"+count);
		return fromObject;
	}
	
	public static JSONObject get(String url){
		URLConnection con;
		try {
			con = new URL(url).openConnection();
			BufferedReader br=null;
			try{
				br=new BufferedReader(new InputStreamReader(con.getInputStream()));
				StringBuilder strB=new StringBuilder();
				String line;
				while((line=br.readLine())!=null){
					strB.append(line);
				}
				return JSONObject.fromObject(strB.toString());
			}finally{
				if(br!=null)br.close();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
	
}
