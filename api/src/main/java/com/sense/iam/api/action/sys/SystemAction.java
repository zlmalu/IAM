package com.sense.iam.api.action.sys;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiSort;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.ibatis.datasource.DataSourceFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sense.core.model.BaseModel;
import com.sense.core.util.ByteArrayJarClassLoader;
import com.sense.core.util.ContextUtil;
import com.sense.core.util.CurrentAccount;
import com.sense.core.util.GatewayHttpUtil;
import com.sense.core.util.PageList;
import com.sense.iam.api.SessionManager;
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.api.model.sys.CompomentReq;
import com.sense.iam.api.model.sys.SystemSummaryReq;
import com.sense.iam.cache.CacheSender;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.cam.SimpleModel;
import com.sense.iam.compoment.AuthInterface;
import com.sense.iam.compoment.FindInterface;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.SyncInteface;
import com.sense.iam.compoment.TaskInterface;
import com.sense.iam.model.im.User;
import com.sense.iam.model.sys.Compoment;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.SysCompomentService;


/**
 * 系统概述
 * 
 * Description: 系统组件
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */


@Api(tags = "系统概述")
@Controller
@RestController
@RequestMapping("system")
@ApiSort(value = 0)
public class SystemAction extends AbstractAction<Compoment,Long>{

	@Resource
	private SysCompomentService sysCompomentService;
	
	@Resource
	private JdbcService jdbcService;


	String driverClassName;
	public String getDriverClassName() {
		return driverClassName;
	}
	@Value("${com.sense.jdbc.driverClassName}")
	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}





	@ApiOperation(value="系统概述")
	@RequestMapping(value="summary", method=RequestMethod.GET)
	@ResponseBody
	public SystemSummaryReq summary() {
		SystemSummaryReq model=new SystemSummaryReq();
		CurrentAccount account=SessionManager.getSession(GatewayHttpUtil.getKey(Constants.CURRENT_SESSION_ID, request),request);
		
		model.setAccountTypeCount(4);
		//组织相关
		//组织类型统计和组织总数，合并结果集
		//select 'orgType' as name,count(1) as count from im_org_type where COMPANY_SN='100001' UNION all select 'org' as name,count(1) as count from im_org where COMPANY_SN='100001'
		//统计组织层级
		//select id_path,length(id_path) as charlength from im_org where COMPANY_SN='100001' ORDER BY charlength desc
		try{
			List<Map<String, Object>> orgObject=jdbcService.findList("select 'orgType' as NAME,count(1) as COUNT from im_org_type where COMPANY_SN='"+account.getCompanySn()+"' UNION all select 'org' as NAME,count(1) as COUNT from im_org where COMPANY_SN='"+account.getCompanySn()+"'");
			if(orgObject!=null&&orgObject.size()>0){
				for(Map<String, Object> map:orgObject){
					String key=String.valueOf(map.get("NAME"));
					int value=Integer.valueOf(map.get("COUNT").toString());
					if("orgType".equals(key)){
						model.setOrgTypeCount(value);
					}else if("org".equals(key)){
						model.setOrgCount(value);
					}
				}
			}
			PageList list;
			//log.info("driverClassName:"+driverClassName);
			//组织层级，采用length函数去最长的组织路径字符数,目前兼容mysql oracle sqlserver
			if(driverClassName.toLowerCase().indexOf("sqlserver")>0){
				 User u=new User();
				
				 u.setSort("[{\"property\":\"charlength\",\"direction\":\"desc\"}]");
				 list= jdbcService.findPage("select ID_PATH,len(id_path) as charlength from im_org where COMPANY_SN='"+account.getCompanySn()+"'", u, 0, 5);
			}else{
				 User u=new User();
				 u.setSort("[{\"property\":\"charlength\",\"direction\":\"desc\"}]");
				 list= jdbcService.findPage("select ID_PATH,length(id_path) as charlength from im_org where COMPANY_SN='"+account.getCompanySn()+"'", u, 0, 5);
			}
			if(list!=null&&list.getDataList().size()>0){
				List<Map<String, Object>> joMap=list.getDataList();
				model.setOrgLength(joMap.get(0).get("ID_PATH").toString().split("/").length);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
			
			
		//应用相关
		//应用类型统计合并应用总数，合并结果集
		//select 'appType' as name,count(1) as count from im_app_type where COMPANY_SN='100001' UNION all select 'app' as name,count(1) as count from im_app where COMPANY_SN='100001' 
		try{	
			List<Map<String, Object>> appObject=jdbcService.findList("select 'appType' as NAME,count(1) as COUNT from im_app_type where COMPANY_SN='"+account.getCompanySn()+"' UNION all select 'app' as NAME,count(1) as COUNT from im_app where COMPANY_SN='"+account.getCompanySn()+"'");
			if(appObject!=null&&appObject.size()>0){
				for(Map<String, Object> map:appObject){
					String key=String.valueOf(map.get("NAME"));
					int value=Integer.valueOf(map.get("COUNT").toString());
					if("appType".equals(key)){
						model.setAppTypeCount(value);
					}else if("app".equals(key)){
						model.setAppCount(value);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
			

		
		//用户相关-统计用户类型，用户总数，离职数，在职数，合并结果集
		/*select 'userType' as name,count(1) as count from im_user_type where COMPANY_SN='100001' UNION all 
		select 'user' as name,count(1) as count from im_user where COMPANY_SN='100001' UNION all 
		select 'userQuit' as name,count(1) as count from im_user where status=2 and COMPANY_SN='100001'UNION all 
		select 'userIncumbency' as name,count(1) as count from im_user where status=1 and COMPANY_SN='100001'*/
		try{	
			String sql="select 'userType' as NAME,count(1) as COUNT from im_user_type where COMPANY_SN='"+account.getCompanySn()+"' UNION all "; 
				  sql+="select 'user' as NAME,count(1) as COUNT from im_user where COMPANY_SN='"+account.getCompanySn()+"' UNION all "; 
				  sql+="select 'userQuit' as NAME,count(1) as COUNT from im_user where status=2 and COMPANY_SN='"+account.getCompanySn()+"' UNION all ";  
				  sql+="select 'userIncumbency' as NAME,count(1) as COUNT from im_user where status=1 and COMPANY_SN='"+account.getCompanySn()+"'";
			List<Map<String, Object>> userObject=jdbcService.findList(sql);
			if(userObject!=null&&userObject.size()>0){
				for(Map<String, Object> map:userObject){
					String key=String.valueOf(map.get("NAME"));
					int value=0;
					if(map.containsKey("COUNT")){
						value=Integer.valueOf(map.get("COUNT").toString());
					}
					if("userType".equals(key)){
						model.setUserTypeCount(value);
					}else if("user".equals(key)){
						model.setUserCount(value);
					}else if("userQuit".equals(key)){
						model.setUserQuitCount(value);
					}else if("userIncumbency".equals(key)){
						model.setUserIncumbencyCount(value);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
			
			
			
		//帐号相关
		/*select 'account' as name,count(1) as count from im_account where COMPANY_SN='100001' UNION all 
		select 'accountProhibit' as name,count(1) as count from im_account where status=2 and COMPANY_SN='100001' UNION all 
		select 'accountIncumbency' as name,count(1) as count from im_account where status=1 and COMPANY_SN='100001' */
		try{
			String accsql="select 'account' as NAME,count(1) as COUNT from im_account where COMPANY_SN='"+account.getCompanySn()+"' UNION all "; 
				  accsql+="select 'accountProhibit' as NAME,count(1) as COUNT from im_account where status=2 and COMPANY_SN='"+account.getCompanySn()+"' UNION all "; 
				  accsql+="select 'accountIncumbency' as NAME,count(1) as COUNT from im_account where status=1 and COMPANY_SN='"+account.getCompanySn()+"'";  
			List<Map<String, Object>> accObject=jdbcService.findList(accsql);
			if(accObject!=null&&accObject.size()>0){
				for(Map<String, Object> map:accObject){
					String key=String.valueOf(map.get("NAME"));
					int value=0;
					if(map.containsKey("COUNT")){
						value=Integer.valueOf(map.get("COUNT").toString());
					}
					if("account".equals(key)){
						model.setAccountCount(value);
					}else if("accountProhibit".equals(key)){
						model.setAccountProhibitCount(value);
					}else if("accountIncumbency".equals(key)){
						model.setAccountIncumbencyCount(value);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		//数据接口
		//select run_class from sync_config where  COMPANY_SN='100001'
		String sysnSQL="select c.RUN_CLASS,c.sn,app.name as appName,app.sn as appSn,ev.name as eventName from sync_config c left join sys_event ev on c.sys_event_id=ev.id left join im_app app on app.id=c.app_id  where  c.COMPANY_SN='"+account.getCompanySn()+"'";
		Map<String, List<Map<String, Object>>> typekeys=new HashMap<String, List<Map<String,Object>>>();
		int interCount=0;
		try{
			List<Map<String, Object>> syncObject=jdbcService.findList(sysnSQL);
			if(syncObject != null&&syncObject.size() > 0){
				for(Map<String, Object> map:syncObject){
					interCount++;
					String key=map.get("RUN_CLASS").toString();
					//去掉前缀 格式：org.iam.compoment.sync.+接口类型+,实现类，如果不是这个格式则过滤
					if(key.indexOf("org.iam.compoment.sync.")==-1)continue;
					key=key.replace("org.iam.compoment.sync.", "");
					String[] strA=key.split("\\.");
					if(strA.length>0){
						if(typekeys.containsKey(strA[0])){
							List<Map<String, Object>> syncMap=typekeys.get(strA[0]);
							syncMap.add(map);
							typekeys.put(strA[0], syncMap);
							
						}else{
							List<Map<String, Object>> syncMap=new ArrayList<Map<String,Object>>();
							syncMap.add(map);
							typekeys.put(strA[0], syncMap);
						}
					}		
				}
			}
			//接口类型总数
			model.setInterfaceTypeCount(typekeys.keySet().size());
			//接口数
			model.setInterfaceCount(interCount);
			
			model.setInterfaceSyncCount(interCount);
			//具体接口信息
			model.setInterfaceInfo(typekeys);

		}catch(Exception e){
			e.printStackTrace();
		}
		
		return model;
	}
}
