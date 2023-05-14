package org.iam.commpoment.timertask;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.core.util.ContextUtil;
import com.sense.core.util.StringUtils;
import com.sense.core.util.TimeUtil;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.TaskInterface;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.UserService;

/**
 * 用户延迟离职
 * 
 * Description:  当用户离职后，检索用户最后离职时间，自动进行用户离职
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */
@Name("用户离职扫描")
public class UserLeavelSuspend implements TaskInterface{
	
	protected Log log=LogFactory.getLog(getClass());
	
	@Param("用户表")
	private String tableNames="";

	
	@Override
	public void run(Long timerTaskId, Date runTime) {
		log.debug("execute timerTaskId="+timerTaskId+",execute time:"+String.format("%TF %TT", runTime,runTime));
		JdbcService jdbcService=(JdbcService) ContextUtil.getBean("jdbcService");
		
		String tableName[]=tableNames.split(",");
		String querySql;
		for(int i=0;i<tableName.length;i++){
			if(StringUtils.isTrimEmpty(tableName[i])){
				querySql="SELECT ID FROM "+tableName+" where SUSPEND_TIME > '"+TimeUtil.getNYDTime()+"'";
				log.info("querySql==="+querySql);
				List<Map<String,Object>> list=jdbcService.findList(querySql);
				log.info(list);
				List<Long> idList=new ArrayList<Long>();
				if(list!=null){
					for (Map<String,Object> user : list) {
						idList.add(Long.valueOf(user.get("ID").toString()));
					}
				}
				UserService userService=(UserService) ContextUtil.getBean("imUserService");
				userService.suspend(idList.toArray(new Long[idList.size()]));
			}
		}
	}
}