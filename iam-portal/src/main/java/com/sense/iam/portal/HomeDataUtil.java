package com.sense.iam.portal;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sense.core.util.CurrentAccount;
import com.sense.core.util.TimeUtil;
import com.sense.iam.service.JdbcService;




import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class HomeDataUtil {










    /**
	 * 获取当前用户应用列表
	 * @param jdbcService
	 * @param user
	 * @return
	 */
    public static JSONObject getAppsByUserId(JdbcService jdbcService,CurrentAccount user) {
    	JSONObject resp=new JSONObject();

    	try{
    		Map<Long,Integer> tem=new HashMap<Long, Integer>();
		  	StringBuffer sql = new StringBuffer("select b.BROWSER_TYPE, acc.USER_ID,acc.ID as ACCTID,acc.LOGIN_NAME, b.ID,b.SN,b.NAME,b.APP_TYPE_ID,b.SSO_TYPE,b.SORT_NUM,b.IS_VIEW,b.REMARK,b.AC_NU,b.PARENTNAME,b.PARENTID,b.TYPE_CREATE_TIME");
		  	sql.append(" from im_account acc LEFT JOIN  (select BROWSER_TYPE,ID,SN,NAME,APP_TYPE_ID,SSO_TYPE,SORT_NUM,IS_VIEW,REMARK,AC_NU,PARENTNAME,PARENTID,TYPE_CREATE_TIME,TAG_END_TYPE from ( ");
		  	sql.append(" select app.*,AC_NU,t.name as PARENTNAME,t.id as PARENTID,t.CREATE_TIME AS TYPE_CREATE_TIME from  IM_APP app left join (select acct.APP_ID,count(1) as ac_nu from sso_log log left join im_account acct on log.ACCOUNT_ID=acct.ID group by acct.APP_ID ) ac  on app.ID=ac.APP_ID  left join im_app_type t on t.id=app.app_type_id) a ) b  on  acc.app_id=b.id ");
		  	//加入关联给我的帐号查询--加入关联帐号的有效期，有效期内的帐号可以使用
		  	sql.append(" where (acc.user_id= ? or acc.id in(select acct_id from im_account_user where  ((CREATE_TIME<=? and VALID_TIME>=?) or CREATE_TIME IS NULL)  and USER_ID=?)) and b.is_view=? and b.sn!=? and b.TAG_END_TYPE=?  and acc.status=?  order by b.SORT_NUM ASC,b.TYPE_CREATE_TIME DESC,acc.ID DESC");
		  	List list=jdbcService.findList(sql.toString(), user.getUserId(),TimeUtil.getHmsTime(new Date(System.currentTimeMillis())),TimeUtil.getHmsTime(new Date(System.currentTimeMillis())),user.getUserId(),1,"APP001",1,1);
			JSONArray data=new JSONArray();
	    	HashMap map=new HashMap();
	    	for(int i=0;i<list.size();i++){
	    		JSONObject model=JSONObject.fromObject(list.get(i));
	    		if(tem.containsKey(model.getLong("ID"))){
	    			int num=tem.get(model.getLong("ID"));
	    			tem.remove(model.getLong("ID"));
	    			tem.put(model.getLong("ID"), num+1);
	    		}else{
	    			tem.put(model.getLong("ID"), 1);
	    		}
	    	}

	    	//一层级  应用类型  二层级 应用  三层级 帐户
	    	for(int i=0;i<list.size();i++){
	    		JSONObject model=JSONObject.fromObject(list.get(i));
	    		int num=tem.get(model.getLong("ID"));
	    		//判断是否存在两个相同的应用，如果相同，则显示应用名称（登录帐号）
	    		if(num!=1){
	    			String name=model.getString("NAME");
	    			model.remove("NAME");
	    			model.put("NAME", name+"("+model.getString("LOGIN_NAME")+")");
	    		}
	    		String appName= model.getString("NAME");
	    		//如果是关联用户的应用，则显示应用名称（关联人的姓名）
	    		if(model.getLong("USER_ID")!=user.getUserId()){
	    			List<Map<String, Object>> users=jdbcService.findList("select NAME,SN from im_user where id=?",model.getLong("USER_ID"));
	    			model.remove("NAME");
	    			if(users!=null&&users.size()>0){
	    				model.put("NAME", appName+"("+users.get(0).get("NAME")+")");
	    			}else{
	    				model.put("NAME", appName+"("+model.getLong("USER_ID")+")");
	    			}
	    		}
	    		//构建类型应用
	    		boolean isadd=false;
	    		if(map.containsKey(model.getLong("PARENTID"))){
	    			for(int j=0;j<data.size();j++){
	    				if(data.getJSONObject(j).getLong("perentId")== model.getLong("PARENTID")){
	    					for(int k=0;k<data.getJSONObject(j).getJSONArray("apps").size();k++){
	    						if(data.getJSONObject(j).getJSONArray("apps").getJSONObject(k).getString("sn").equals(model.getString("SN"))){
	    							data.getJSONObject(j).getJSONArray("apps").getJSONObject(k).getJSONArray("accts").add(model);
	    							isadd=true;
	    							continue;
	    						}
	    					}
	    					//如果帐户找不到对应的应用，则新增一个应该到JSON对象
	    	    			if(isadd==false){
	    		    			JSONObject app=new JSONObject();
	    		    			app.put("name", model.getString("NAME"));
	    		    			app.put("sn", model.getString("SN"));
	    		    			app.put("id", model.getString("ID"));
	    		    			JSONArray accts =new JSONArray();
	    		    			accts.add(model);
	    		    			app.put("accts", accts);
	    		    			data.getJSONObject(j).getJSONArray("apps").add(app);
	    						break;
	    					}
	    				}
	    			}

	    		}else{
	    			//添加应用类型
	    			map.put(model.getLong("PARENTID"), true);
	    			JSONObject newAppType=new JSONObject();
	    			newAppType.put("perentId", model.getLong("PARENTID"));
	    			newAppType.put("perentName", model.getString("PARENTNAME"));
	    			JSONArray appsData=new JSONArray();
	    			JSONObject app=new JSONObject();
	    			app.put("name", model.getString("NAME"));
	    			app.put("sn", model.getString("SN"));
	    			app.put("id", model.getString("ID"));
	    			if(model.has("BROWSER_TYPE")){
	    				app.put("type", model.getString("BROWSER_TYPE"));
	    			}
	    			JSONArray accts =new JSONArray();
	    			accts.add(model);
	    			app.put("accts", accts);
	    			appsData.add(app);
	    			newAppType.put("apps", appsData);
	    			data.add(newAppType);
	    		}
	    	}
	    	resp.put("status", 0);
	    	resp.put("data", data);
    	}catch(Exception e){
    		e.printStackTrace();
    		resp.put("status", -1);
    	}
		return resp;

    }
}
