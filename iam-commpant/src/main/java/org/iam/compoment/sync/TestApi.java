package org.iam.compoment.sync;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.SyncInteface;

@Name("同步样例代码")
public class TestApi implements SyncInteface{

	protected Log log=LogFactory.getLog(getClass());
	@Param("动态1")
	private String param1="";
	@Param("动态参数2")
	private String param2="";
	
	@Override
	public ResultCode execute(String content) {
		log.info(content); //同步内容content,此处为数据同步发送的参数对象;
		return new ResultCode(SUCCESS);
	}

}
