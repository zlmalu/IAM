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
import com.sense.iam.model.im.OrgType;
import com.sense.iam.model.im.User;
import com.sense.iam.model.im.UserType;
import com.sense.iam.service.OrgTypeService;
import com.sense.iam.service.UserService;
import com.sense.iam.service.UserTypeService;

@Name("基于DB回收用户")
public class UserRecovery implements TaskInterface{
	
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
	private String querySql="select * from (select u.USER_CODE as SN,u.USER_NAME as NAME,g.GRP_CODE as ORG_SN,case u.USER_SEX when 'M' then '1' else '2' end as SEX,u.USER_STATE as STATUS ,1 as DATA_IS_VALID,u.USER_MAIL as EMAIL,u.USER_PHONE TELEPHONE  from security_user_group_rel ug left join security_user u on ug.USER_ID=u.USER_ID left join security_group g on ug.GRP_ID=g.GRP_ID where u.USER_NAME is not null and g.GRP_CODE is not null) a";
	@Param("用户类型表名")
	private String userTypeName="HRUSER";
	@Param("机构类型名称")
	private String orgTypeName="内部机构";
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
			
			UserService userService=(UserService) ContextUtil.getBean("imUserService");
			UserTypeService userTypeService=(UserTypeService) ContextUtil.getBean("imUserTypeService");
			OrgTypeService orgTypeService=(OrgTypeService)ContextUtil.context.getBean(OrgTypeService.class);
			
			OrgType orgType=new OrgType();
			orgType.setName(orgTypeName);
			UserType userType=new UserType();
			userType.setTableName(userTypeName);
			userService.importUser(saveContents, orgTypeService.findByObject(orgType).getId(), userTypeService.findByObject(userType).getId());

			//清空失效数据
			User user=new User();
			for (Map<String,Object> conMap : delContents) {
				String userSn=StringUtils.getString(conMap.get("SN"));
				user.setSn(userSn);
				List<User> userList=userService.findList(user);
				if(userList!=null){
					for (User u : userList) {
						userService.removeByIds(new Long[]{u.getId()});
					}
				}
			}
		} catch (Exception e) {
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
