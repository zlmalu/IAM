package org.iam.commpoment.timertask;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.core.util.ContextUtil;
import com.sense.core.util.StringUtils;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.TaskInterface;
import com.sense.iam.model.im.Org;
import com.sense.iam.model.im.OrgType;
import com.sense.iam.service.OrgService;
import com.sense.iam.service.OrgTypeService;

/**
 * 
 * 组织机构从数据库回收
 * 
 * Description: 从指定数据库中回收组织机构信息，同步字段主要依赖于机构类型， 数据有效性字段主要用来做数据判断 字段默认为
 * DATA_IS_VALID=1为有效数据，其他为无效数据，将执行数据删除操作
 * SN   数据编码，主要通过此编码来进行数据的同步操作
 * 
 * last date 2019-05-08
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Name("基于DB回收组织")
public class OrgRecovery implements TaskInterface{
	
	protected Log log=LogFactory.getLog(getClass());
	
	@Param("连接池名称")
	private String poolName="";
	@Param("服务器地址")
	private String driverClass="oracle.jdbc.driver.OracleDriver";
	@Param("端口好")
	private String jdbcUrl="jdbc:oracle:thin:@//139.196.252.217:1521/orcl";
	@Param("用户名")
	private String username="SIM";
	@Param("密码")
	private String password="SIM";
	@Param("查询sql")
	private String querySql="select * from (select a.GRP_CODE as SN,a.GRP_NAME as NAME,a.GRP_STATE as DATA_IS_VALID,CASE b.GRP_CODE when 'Root' then 'WLCG' else b.GRP_CODE end as PARENT_SN,a.GRP_SORT as SORT_NUM from security_group a left JOIN security_group b on a.GRP_PID = b.GRP_ID where a.GRP_CODE != 'Root' order by a.CREATE_TIME ASC) a";
	@Param("机构类型名称")
	private String orgTypeName="";
	
	@Override
	public void run(Long timerTaskId, Date runTime) {
		log.debug("execute timerTaskId="+timerTaskId+",execute time:"+String.format("%TF %TT", runTime,runTime));
		
		Connection con=null;
		Statement st=null;
		ResultSet rs=null;
		try {	
			con=getConnection();
			st=con.createStatement();
			rs=st.executeQuery(querySql);
			ResultSetMetaData rmd=rs.getMetaData();
			int colSize=rmd.getColumnCount();
			List<Map<String,Object>> saveContents=new ArrayList<Map<String,Object>>();
			List<Map<String,Object>> delContents=new ArrayList<Map<String,Object>>();
			Map<String,Object> contentMap;
			
			if(rs!=null){
				while(rs.next()){
					contentMap=new HashMap<String,Object>();
					for (int i=1;i<=colSize;i++) {
						String colName=rmd.getColumnLabel(i);
						contentMap.put(colName, rs.getObject(rmd.getColumnLabel(i)));
					}
					if(StringUtils.isEqual(contentMap.remove("DATA_IS_VALID"), 1)){
						saveContents.add(contentMap);
					}else{
						delContents.add(contentMap);
					}
				}
			}
			OrgService orgService=(OrgService) ContextUtil.context.getBean(OrgService.class);
			OrgTypeService orgTypeService=(OrgTypeService)ContextUtil.context.getBean(OrgTypeService.class);
			OrgType orgType=new OrgType();
			orgType.setName(orgTypeName);
			//存储增量数据
			orgService.importData(orgTypeService.findByObject(orgType).getId(), saveContents);
			
			//清空失效数据
			Org org=new Org();
			org.setIsControl(false);
			for (Map<String,Object> conMap : delContents) {
				String orgSn=StringUtils.getString(conMap.get("SN"));
				org.setSn(orgSn);
				List<Org> orgList=orgService.findList(org);
				if(orgList!=null){
					for (Org o : orgList) {
						orgService.removeByIds(new Long[]{o.getId()});
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			log.error("timer execute Exception",e);
		}finally{
			if(rs!=null)try{rs.close();}catch(Exception e){}
			if(st!=null)try{st.close();}catch(Exception e){}
			if(con!=null)try{con.close();}catch(Exception e){}
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
