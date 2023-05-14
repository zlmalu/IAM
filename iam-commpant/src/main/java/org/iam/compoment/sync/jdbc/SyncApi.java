package org.iam.compoment.sync.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import javax.naming.Context;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.SyncInteface;

/**
 * 
 * 普通JDBC数据同步操作
 * 
 * jdbc标签
 * 
 * Description: 进行数据库连接并且执行的操作API
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Name("JDBC同步")
public class SyncApi implements SyncInteface{

	private Log log=LogFactory.getLog(getClass());
	
	@Param("数据源名称")
	private String poolName;
	
	@Param("数据库驱动")
	private static String driverClass="com.mysql.jdbc.Driver";
	
	@Param("链接URL")
	private static String addr="jdbc:mysql://127.0.0.1:3306/sso?useUnicode=true&characterEncoding=utf8&autoReconnect=true";
	@Param("管理员帐号")
	private static String username="root";
	@Param("管理员密码")
	private static String password="password";
	
	public static void main(String[] args) throws Exception {
		Connection con=getConnection1();
		System.out.println(con);
		
		
	}
	@Override
	public ResultCode execute(String content) {
		Connection con=null;
		PreparedStatement pst=null;
		try{
			con=getConnection();
			String[] sqls=content.split(";");
			for (String sql : sqls) {
				if(sql!=null && sql.trim().length()>0){
					pst=con.prepareStatement(sql.trim());
					log.debug(sql);
					pst.execute();
				}
			}
			
			return new ResultCode(SUCCESS,"");
		}catch(Exception e){
			e.printStackTrace();
			return new ResultCode(FAIL,e.getMessage());
		}finally{
			if(pst!=null)try{pst.close();}catch(Exception e){}
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
			return DriverManager.getConnection(addr,username,password);
		}
		
	}
	
	private static Connection getConnection1() throws Exception{
		
		Class.forName(driverClass);
		return DriverManager.getConnection(addr,username,password);
		
		
	}
}
