package org.iam.compoment.sync.iam;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.core.util.ContextUtil;
import com.sense.core.util.CurrentAccount;
import com.sense.core.util.StringUtils;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.SyncInteface;
import com.sense.iam.service.JdbcService;

@Name("IAM角色细粒度授权")
public class IAMApi implements SyncInteface{

	protected Log log=LogFactory.getLog(getClass());
	
	@Override
	public ResultCode execute(String content) {
		log.info("::::content:"+content);
		Long id=Long.valueOf(content);
		try{
			JdbcService jdbcService=(JdbcService) ContextUtil.getBean("jdbcService");
			//查询细粒度同步权限内容
			List<Map<String,Object>> maps=jdbcService.findList("SELECT c.ID AS ACCOUNT_ID,a.AUTH_OBJ,b.LOGIN_NAME FROM IM_ACCOUNT_APP_FUNC a left join IM_ACCOUNT b on a.ACCOUNT_ID=b.ID left join SYS_ACCT c ON c.LOGIN_NAME=b.LOGIN_NAME WHERE a.ID="+id);
			if(maps!=null&&maps.size()>0){
				Long accountId=Long.valueOf(maps.get(0).get("ACCOUNT_ID").toString());
				String funcObj=maps.get(0).get("AUTH_OBJ").toString();
				if(!StringUtils.isEmpty(funcObj)){
					JSONArray data=JSONObject.fromObject(funcObj).getJSONArray("active");
					String funcs=null;
					String orgfuncs=null;
					String appFuns=null;
					for(int i=0;i<data.size();i++){
						//查询角色授权还是组织授权管理员，根据元素编号区分
						List<Map<String,Object>> authType=jdbcService.findList("SELECT SN FROM IM_APP_ELEMENT where ID in(SELECT APP_ID FROM IM_APP_FUNC WHERE ID="+data.getJSONObject(i).getString("funcId")+")");
						log.info("----------"+authType.get(0).get("SN")+"---------");
						if("IAM_ORGS".equals(authType.get(0).get("SN"))){
							if(orgfuncs==null){
								orgfuncs=data.getJSONObject(i).getString("funcId");
							}else{
								orgfuncs+=","+data.getJSONObject(i).getString("funcId");
							}
						}else if("IAM_APPS".equals(authType.get(0).get("SN"))){
							if(appFuns==null){
								appFuns=data.getJSONObject(i).getString("funcId");
							}else{
								appFuns+=","+data.getJSONObject(i).getString("funcId");
							}
							log.info(">>>>>>>>>>"+appFuns+"<<<<<<<<<<");
						}else{
							if(funcs==null){
								funcs=data.getJSONObject(i).getString("funcId");
							}else{
								funcs+=","+data.getJSONObject(i).getString("funcId");
							}
						}
					}
					//进行角色授权
					if(!StringUtils.isEmpty(funcs)){
						//清空系统帐号和角色的关系表,
						jdbcService.executeSql("DELETE FROM SYS_ACCT_ROLE WHERE ACCT_ID="+accountId);
						List<Map<String,Object>> roles=jdbcService.findList("SELECT ID FROM SYS_ROLE WHERE SN IN(SELECT SN FROM IM_APP_FUNC WHERE ID in("+funcs+")) AND COMPANY_SN='"+CurrentAccount.getCurrentAccount().getCompanySn()+"'");
						for(int i=0;i<roles.size();i++){
							//插入新系统角色
							jdbcService.executeSql("INSERT INTO SYS_ACCT_ROLE(ROLE_ID,ACCT_ID) VALUES ("+roles.get(i).get("ID")+","+accountId+")");
							log.info("auth role ok:::"+roles.get(i).get("ID")+"_"+accountId);
							log.info("===============================");
							log.info("");
						}
					}else{
						//清空系统帐号和角色的关系表,
						jdbcService.executeSql("DELETE FROM SYS_ACCT_ROLE WHERE ACCT_ID="+accountId);
					}
					
					//进行组织授权管理员
					if(!StringUtils.isEmpty(orgfuncs)){
						jdbcService.executeSql("DELETE FROM SYS_ACCT_IM_ORG WHERE SYS_ACCT_ID="+accountId);
						for(String orgId:orgfuncs.split(",")){
							//判断当前是否授权
							List<Map<String,Object>> isAUTH=jdbcService.findList("SELECT * FROM SYS_ACCT_IM_ORG  WHERE SYS_ACCT_ID="+accountId+" AND IM_ORG_ID="+orgId);
							//如果没有授权，则进行组织授权管理员
							if(isAUTH!=null&&isAUTH.size()==0){
								jdbcService.executeSql("INSERT INTO SYS_ACCT_IM_ORG (SYS_ACCT_ID,IM_ORG_ID) VALUES("+accountId+","+orgId+")");
								log.info("auth Org ok:::"+accountId+"_"+orgId);
								log.info("===============================");
								log.info("");
							}
						}
					}else{
						jdbcService.executeSql("DELETE FROM SYS_ACCT_IM_ORG WHERE SYS_ACCT_ID="+accountId);
					}
					
					//进行应用授权管理员
					if(!StringUtils.isEmpty(appFuns)){
						jdbcService.executeSql("DELETE FROM SYS_ACCT_APP WHERE SYS_ACCT_ID="+accountId);
						for(String appId:appFuns.split(",")){
							List<Map<String,Object>> appTyeIdMap=jdbcService.findList("SELECT c.APP_TYPE_ID FROM IM_APP_FUNC a inner join IM_APP_ELEMENT b ON a.APP_ID=b.ID inner join IM_APP c ON b.APP_ID=c.ID WHERE a.ID="+appId);
							if(appTyeIdMap!=null&&appTyeIdMap.get(0).get("APP_TYPE_ID")!=null){
								Long appTypeId=Long.parseLong(appTyeIdMap.get(0).get("APP_TYPE_ID").toString());
								List<Map<String,Object>> isAutType=jdbcService.findList("SELECT * FROM SYS_ACCT_APP  WHERE SYS_ACCT_ID="+accountId+" AND IM_APP_ID="+appTypeId);
								//判断当前应用类型是否授权,如果没有授权，则进行应用类型授权管理员
								if(isAutType!=null&&isAutType.size()==0){
									jdbcService.executeSql("INSERT INTO SYS_ACCT_APP (SYS_ACCT_ID,IM_APP_ID) VALUES("+accountId+","+appTypeId+")");
									log.info("auth AppType ok:::"+accountId+"_"+appTypeId);
									log.info("===============================");
									log.info("");
								}
							}
							List<Map<String,Object>> appIdMap=jdbcService.findList("SELECT c.ID FROM IM_APP_FUNC a inner join im_app_element b ON a.APP_ID=b.ID inner join IM_APP c ON c.SN=a.SN WHERE a.ID="+appId);
							if(appIdMap!=null&&appIdMap.get(0).get("ID")!=null){
								Long app_id=Long.parseLong(appIdMap.get(0).get("ID").toString());
								//判断当前应用是否授权，如果没有授权，则进行应用授权管理员
								List<Map<String,Object>> isAuthApp=jdbcService.findList("SELECT * FROM SYS_ACCT_APP  WHERE SYS_ACCT_ID="+accountId+" AND IM_APP_ID="+app_id);
								if(isAuthApp!=null&&isAuthApp.size()==0){
									jdbcService.executeSql("INSERT INTO SYS_ACCT_APP (SYS_ACCT_ID,IM_APP_ID) VALUES("+accountId+","+app_id+")");
									log.info("auth App ok:::"+accountId+"_"+app_id);
									log.info("===============================");
									log.info("");
								}
							}
							
						}
					}else{
						jdbcService.executeSql("DELETE FROM SYS_ACCT_APP WHERE SYS_ACCT_ID="+accountId);
					}
					return new ResultCode(SUCCESS);
				}else{
					log.error("细粒度同步内容为空");
					return new ResultCode(FAIL,"细粒度同步内容为空");
				}
			}
		}catch(Exception e){
			return new ResultCode(FAIL,"同步异常"+e.getMessage());
		}
		return new ResultCode(FAIL);
	}

}
