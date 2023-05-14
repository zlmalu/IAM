package org.iam.commpoment.timertask.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.sql.DataSource;

import com.sense.core.util.ContextUtil;
import com.sense.core.util.StringUtils;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.TaskInterface;
import com.sense.iam.service.JdbcService;

@Name("基于DB回收岗位机构关联")
public class PositionOrgRecover  extends BaseRecover implements TaskInterface{
	
	@Param("连接池名称")
	private String poolName="";
	@Param("服务器地址")
	private String driverClass="oracle.jdbc.driver.OracleDriver";
	@Param("端口号")
	private String jdbcUrl="jdbc:oracle:thin:@//139.196.252.217:1521/orcl";
	@Param("用户名")
	private String username="SIM";
	@Param("密码")
	private String password="SIM";
	@Param("查询sql")
	private String querySql="select ORG_SN,POSITION_SN,DATA_IS_VALID from IM_POSITION";

	
	@Override
	public void run(Long timerTaskId, Date runTime) {
		//查询同步数据
				log.debug("execute timerTaskId="+timerTaskId+",execute time:"+String.format("%TF %TT", runTime,runTime));
				Connection con=null;
				Statement st=null;
				ResultSet rs=null;
				List<Map<String,Object>> syncContents=new ArrayList<Map<String,Object>>();
				try {
					con=getConnection();
					st=con.createStatement();
					rs=st.executeQuery(querySql);
					ResultSetMetaData rmd=rs.getMetaData();
					int colSize=rmd.getColumnCount();
					Map<String,Object> contentMap;
					if(rs!=null){
						while(rs.next()){
							contentMap=new HashMap<String,Object>();
							for (int i=1;i<=colSize;i++) {
								String colName=rmd.getColumnLabel(i);
								contentMap.put(colName, rs.getObject(rmd.getColumnLabel(i)));
							}
							syncContents.add(contentMap);
						}
					}
				
				} catch (Exception e) {
					log.error("timer execute Exception",e);
				}finally{
					if(rs!=null)try{rs.close();}catch(Exception e){}
					if(st!=null)try{st.close();}catch(Exception e){}
					if(con!=null)try{con.close();}catch(Exception e){}
				}
				if(syncContents.size()==0)return;
				JdbcService jdbcService=(JdbcService) ContextUtil.getBean("jdbcService");
				for (Map<String,Object> contentMap : syncContents) {
					if(StringUtils.isEqual(contentMap.remove("DATA_IS_VALID"), 1)){//是否有效数据
						jdbcService.executeSql("insert into im_org_position(POSITION_ID,ORG_ID) "+
"select * from (select (select ID from im_position where sn='"+contentMap.remove("POSITION_SN")+"') position_id, (select ID from im_org where sn='"+contentMap.remove("ORG_SN")+"') org_id "+
"from dual) c where "+
" POSITION_ID  not in (select a.POSITION_ID from im_org_position a where a.POSITION_ID= c.POSITION_ID and a.ORG_ID=c.ORG_ID)");
						
					}else{//无效数据进行删除
						jdbcService.executeSql("delete from im_org_position where POSITION_ID=(select ID from im_position where sn='"+contentMap.remove("POSITION_SN")+"') and ORG_ID=(select ID from im_org where sn='"+contentMap.remove("ORG_SN")+"')");
					}
					
				}
	}
	

	private Connection getConnection() throws Exception{
		if(poolName!=null && poolName.trim().length()>0){
			Context context=new javax.naming.InitialContext();
			DataSource ds=(DataSource) context.lookup(poolName);
			return ds.getConnection();
		}else{
			Class.forName(driverClass);
			return DriverManager.getConnection(jdbcUrl,username,password);
		}
	}

}
