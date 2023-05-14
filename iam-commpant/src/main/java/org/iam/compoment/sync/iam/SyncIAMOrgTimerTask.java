package org.iam.compoment.sync.iam;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.core.util.ContextUtil;
import com.sense.core.util.CurrentAccount;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.TaskInterface;
import com.sense.iam.model.im.AppFunc;
import com.sense.iam.service.JdbcService;



@Name("同步IAM组织数据到权限模型(JDBC同步)")
public class SyncIAMOrgTimerTask implements TaskInterface{
	
	private Log log = LogFactory.getLog(getClass());
	
	@Param("同步权限元素编号")
	private String APP_ELEMENT="IAM_ORGS";
	@Param("同步组织类型ID范围（-1代表所有组织类型）")
	private String ORG_TYPE_ID="1000022709";
	@Param("同步是否同步禁用组织")
	private String IS_ACTIVE="否";
	
	@Override
	public void run(Long timerTaskId, Date runTime) {
		log.info("execute timerTaskId="+timerTaskId+",execute time:"+String.format("%TF %TT", runTime,runTime));
		JdbcService jdbcService=(JdbcService) ContextUtil.getBean("jdbcService");
		String sql="SELECT ID,SN,NAME,PARENT_ID,STATUS FROM IM_ORG WHERE";
		sql+=" COMPANY_SN='"+CurrentAccount.getCurrentAccount().getCompanySn()+"'";
		if(!"-1".equals(ORG_TYPE_ID)){
			sql+=" and ORG_TYPE_ID='"+ORG_TYPE_ID+"'";
		}
		if("否".equals(IS_ACTIVE)){
			sql+=" and STATUS=1";
		}
		//ID进行降序排序，请根据实际环境进行排序
		sql+=" ORDER BY ID ASC";
		log.info("sync query sql:::"+sql);
		
		List<Map<String, Object>> appElements=jdbcService.findList("select ID FROM IM_APP_ELEMENT WHERE SN='"+APP_ELEMENT+"' and COMPANY_SN='"+CurrentAccount.getCurrentAccount().getCompanySn()+"'");
		List<Map<String, Object>> lists=jdbcService.findList(sql);
		log.info("sync query data size:::"+lists.size());
		if(appElements!=null&&appElements.size()>0){
			for(int i=0;i<appElements.size();i++){
				if(lists!=null&&lists.size()>0){
					Long appELEmentId=Long.valueOf(appElements.get(i).get("ID").toString());
					for(int j=0;j<lists.size();j++){
						Long id=Long.valueOf(lists.get(j).get("ID").toString());
						Long parentId=Long.valueOf(lists.get(j).get("PARENT_ID").toString());
						String name=lists.get(j).get("NAME").toString();
						String sn=lists.get(j).get("SN").toString();
						Integer status=Integer.valueOf(lists.get(j).get("STATUS").toString());
						log.info("data:::"+lists.get(j).toString());
						log.info("id:::"+id);
						log.info("sn:::"+sn);
						log.info("name:::"+name);
						log.info("parentId:::"+parentId);
						log.info("status:::"+status);
						log.info("appELEmentId:::"+appELEmentId);
						log.info("optUser:::"+CurrentAccount.getCurrentAccount().getLoginName());
						String SQL=null;
						//查询是否已经存在数据
						List<Map<String, Object>> casdSQL=jdbcService.findList("SELECT ID,NAME,SN FROM IM_APP_FUNC WHERE ID="+id);
						log.info("query data:::"+casdSQL);
						if(casdSQL!=null&&casdSQL.size()==0){
							SQL="INSERT INTO IM_APP_FUNC(ID,SN,NAME,PARENT_ID,APP_ID,IS_DEFAULT,INFO,STATUS,IS_DELETE,CREATE_TIME,UPDATE_TIME,OPT_USER) VALUES(";
							if(parentId.longValue()==-1){
								SQL+=id+",'"+sn+"','"+name+"',"+appELEmentId+","+appELEmentId+",2,'IAM同步',"+status+",1,now(),now(),'"+CurrentAccount.getCurrentAccount().getLoginName()+"')";
							}else{
								SQL+=id+",'"+sn+"','"+name+"',"+parentId+","+appELEmentId+",2,'IAM同步',"+status+",1,now(),now(),'"+CurrentAccount.getCurrentAccount().getLoginName()+"')";
							}
							log.info("action save:::"+SQL);
						}else{
							SQL="UPDATE IM_APP_FUNC SET NAME='"+name+"'";
							SQL+=",SN='"+sn+"'";
							if(parentId.longValue()==-1){
								SQL+=",PARENT_ID="+appELEmentId;

							}else{
								SQL+=",PARENT_ID="+parentId;
							}
							SQL+=",UPDATE_TIME=now()";
							SQL+=",OPT_USER='"+CurrentAccount.getCurrentAccount().getLoginName()+"'";
							SQL+=",STATUS="+status;
							SQL+=" WHERE ID="+id;
							log.info("action edit:::"+SQL);
						}
						jdbcService.executeSql(SQL);
						log.info("====================================");
						log.info("");
					}
				}
			}
		}
	}
	

}
