package org.iam.commpoment.timertask;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.core.util.ContextUtil;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.TaskInterface;
import com.sense.iam.service.JdbcService;


@Name("基于DB回收用户")
public class DemoRecovery implements TaskInterface{
	
	protected Log log=LogFactory.getLog(getClass());
	@Param("动态1")
	private String param1="";
	@Param("动态参数2")
	private String param2="";
	@Override
	public void run(Long timerTaskId, Date runTime) {
		
		JdbcService jdbcService=(JdbcService) ContextUtil.getBean("jdbcService");
		
		List<Map<String, Object>> list=jdbcService.findList("select name,remark from SYS_CONFIGURATION where name='CACHE_RELOAD_TIME'");
		if(list!=null&&list.size()>0){
			String key=list.get(0).get("name").toString();
		}
		//jdbcService.executeSql(sql) 可执行sql,编写SQL语句即可
		// List<Map<String, Object>> list=jdbcService.findList(sql) 查询对象集合
		log.debug("execute timerTaskId="+timerTaskId+",execute time:"+String.format("%TF %TT", runTime,runTime));
		
	}
	
	
}
