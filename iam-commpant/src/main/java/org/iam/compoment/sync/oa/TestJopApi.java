package org.iam.compoment.sync.oa;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.core.queue.QueueSender;
import com.sense.core.util.ContextUtil;
import com.sense.core.util.CurrentAccount;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.SyncInteface;
import com.sense.iam.compoment.TaskInterface;
import com.sense.iam.model.im.Account;
import com.sense.iam.model.im.User;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.UserService;
import com.sense.iam.service.impl.AccountServiceImpl;

/**
 * 泛微OA组织同步组件-soup参数
 * 
 * Description:  
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */
public class TestJopApi implements TaskInterface{

	
	private Log log=LogFactory.getLog(getClass());
	
	
	
	@Override
	public void run(Long timerTaskId, Date runTime) {
		UserService userService=(UserService) ContextUtil.context.getBean(UserService.class);
		JdbcService jdbcService=(JdbcService) ContextUtil.getBean("jdbcService");
		List<Map<String, Object>> list=jdbcService.findList("select id,name from im_user where id in(select user_id from im_org_user)");
		log.info("size :"+list.size());
		for(int i=0;i<list.size();i++){
			try{
				User u = userService.findById(Long.valueOf(list.get(i).get("id").toString()));
				log.info("u :"+u);
				if(u!=null){
					for(int j=0;j<10;j++){
						log.info("username :"+u.getName());
						u.setName(u.getName()+"_sync"+j);
						userService.edit(u);
					}
				}else{
					//log.error("username :"+u.getName());
					log.info("findByid error :"+Long.valueOf(list.get(i).get("id").toString()));
				}
			}catch(Exception e){
				e.printStackTrace();
				continue;
			}
		}
		
		for(int i=0;i<list.size();i++){
			try{
				User u = userService.findById(Long.valueOf(list.get(i).get("id").toString()));
				log.info("u :"+u);
				if(u!=null){
					for(int j=0;j<10;j++){
						log.info("username :"+u.getName());
						
						u.setName(u.getName().replaceAll("_sync"+j, ""));
						userService.edit(u);
					}
				}else{
					//log.error("username :"+u.getName());
					log.info("findByid error :"+Long.valueOf(list.get(i).get("id").toString()));
				}
			}catch(Exception e){
				e.printStackTrace();
				continue;
			}
		}
	}
}
