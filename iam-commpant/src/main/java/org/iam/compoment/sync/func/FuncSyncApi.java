package org.iam.compoment.sync.func;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.core.util.ContextUtil;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.SyncInteface;
import com.sense.iam.service.JdbcService;

/**
 * 
 * 权限同步示例
 * 
 * 
 * Description: 
 * 
 * @author shil
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Name("权限同步示例")
public class FuncSyncApi implements SyncInteface{

	private Log log=LogFactory.getLog(getClass());
	
	@Override
	public ResultCode execute(String content) {
		log.info("sync content:"+content);
		JdbcService jdbcService=(JdbcService) ContextUtil.getBean("jdbcService");
		Long id=Long.valueOf(content);
		String sql="select * from im_app_func where id="+id;
		List<Map<String, Object>> authList=jdbcService.findList(sql);
		if(authList!=null&&authList.size()>0){
			for(Map<String, Object> authObj:authList){
				log.info("func:"+authObj);
			}
		}
		return new ResultCode(SUCCESS);
		
	}

	
}
