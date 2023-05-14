package org.iam.compoment.sync.func;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.core.util.ContextUtil;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.SyncInteface;
import com.sense.iam.service.JdbcService;

/**
 * 
 * 细粒度授权示例
 * 
 * 
 * Description: 
 * 
 * @author shil
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Name("细粒度授权示例")
public class AccountSyncApi implements SyncInteface{

	private Log log=LogFactory.getLog(getClass());
	
	@Override
	public ResultCode execute(String content) {
		log.info("sync content:"+content);
		JdbcService jdbcService=(JdbcService) ContextUtil.getBean("jdbcService");
		Long id=Long.valueOf(content);
		String sql="select auth_obj from im_account_app_func where id="+id;
		List<Map<String, Object>> authList=jdbcService.findList(sql);
		if(authList!=null&&authList.size()>0){
			for(Map<String, Object> authObj:authList){
				log.info("authObj:"+authObj);
				JSONObject json=JSONObject.fromObject(authObj.get("auth_obj").toString());
				//获取当前已授权的权限集合
				JSONArray activeArray=json.getJSONArray("active");
				//获取无效的权限集合，对比上一次授权
				JSONArray invalidArray=json.getJSONArray("invalid");
				//获取关联的权限集合
				JSONArray relationArray=json.getJSONArray("relation");
				
				log.info("activeArray:"+activeArray);
				log.info("invalidArray:"+invalidArray);
				log.info("relationArray:"+relationArray);
				
			}
		}
		return new ResultCode(SUCCESS);
		
	}

	
}
