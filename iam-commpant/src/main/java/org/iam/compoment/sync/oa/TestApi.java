package org.iam.compoment.sync.oa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.SyncInteface;

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
public class TestApi implements SyncInteface{


	
	private Log log=LogFactory.getLog(getClass());
	

	@Override
	public com.sense.iam.cam.ResultCode execute(String content) {
		ResultCode code = null;
		log.info("sync content:"+content);
		try{
			
			return new ResultCode(SUCCESS,content);
		}catch(Exception e){
			e.printStackTrace();
		}
		return code;
	}
	
	
	
	
	
	
	
}
