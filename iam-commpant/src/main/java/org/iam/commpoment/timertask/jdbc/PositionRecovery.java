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
import com.sense.iam.model.im.Position;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.PositionService;

@Name("基于DB回收岗位")
public class PositionRecovery extends BaseRecover implements TaskInterface{

	
	
	
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
	private String querySql="select SN,NAME,REMARK,DATA_IS_VALID from IM_POSITION";

	
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
		PositionService positionService=(PositionService) ContextUtil.getBean("imPositionService");
		JdbcService jdbcService=(JdbcService) ContextUtil.getBean("jdbcService");
		//获取所有岗位信息,并将岗位编码存放到map中
		List<Position> positions=positionService.findAll();
		//定义保存系统已存在的岗位编码
		Map<String,Position> existPosition=new HashMap<String,Position>();
		for (Position p : positions) {
			existPosition.put(p.getSn(),p);
		}
		
		for (Map<String,Object> contentMap : syncContents) {
			Position pos=existPosition.get(StringUtils.getString(contentMap.get("SN")));//根据编号查询岗位
			if(StringUtils.isEqual(contentMap.remove("DATA_IS_VALID"), 1)){//是否有效数据
				//岗位是否存在
				if(pos==null){//岗位新增
					pos=new Position();
					pos.setSn(StringUtils.getString(contentMap.get("SN")));
					pos.setName(StringUtils.getString(contentMap.get("NAME")));
					pos.setRemark(StringUtils.getString(contentMap.get("REMARK")));
					positionService.save(pos);
				}else if(!pos.getName().equals(StringUtils.getString(contentMap.get("NAME")))){//岗位变更
					jdbcService.executeSql("UPDATE IM_POSITION set NAME='"+StringUtils.getString(contentMap.get("NAME"))+"' where ID="+pos.getId());
				}
			}else{//无效数据进行删除
				if(pos!=null)positionService.removeByIds(new Long[]{pos.getId()});
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
