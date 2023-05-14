package org.iam.commpoment.timertask;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.core.util.ContextUtil;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.TaskInterface;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.JdbcService;

/**
 * 帐号延迟禁用
 * 
 * Description:  当用户离职后，检索用户最后离职时间，自动进行用户离职
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */
@Name("帐号延迟禁用")
public class AccountLeavelDisabled implements TaskInterface{
	
	protected Log log=LogFactory.getLog(getClass());
	
	@Param("查询sql")
	private String querySql="select ID from IM_USER where ";

	
	@Override
	public void run(Long timerTaskId, Date runTime) {
		log.debug("execute timerTaskId="+timerTaskId+",execute time:"+String.format("%TF %TT", runTime,runTime));
		JdbcService jdbcService=(JdbcService) ContextUtil.getBean("jdbcService");
		log.info("querySql==="+querySql);
		List<Map<String,Object>> list=jdbcService.findList(querySql);
		log.info(list);
		List<Long> idList=new ArrayList<Long>();
		if(list!=null){
			for (Map<String,Object> user : list) {
				idList.add(Long.valueOf(user.get("ID").toString()));
			}
		}
		AccountService userService=(AccountService) ContextUtil.getBean("imAccountService");
		userService.disabled(idList.toArray(new Long[idList.size()]));
	}
}