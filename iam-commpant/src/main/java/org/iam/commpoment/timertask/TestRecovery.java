package org.iam.commpoment.timertask;

import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sense.core.util.ContextUtil;
import com.sense.iam.compoment.TaskInterface;
import com.sense.iam.model.sys.LogConfig;
import com.sense.iam.model.sys.ReportConfig;
import com.sense.iam.service.SysLogConfigService;
import com.sense.iam.service.SysReportConfigService;



public class TestRecovery implements TaskInterface{
	
	protected Log log=LogFactory.getLog(getClass());
	
	@Override
	public void run(Long timerTaskId, Date runTime) {
		SysLogConfigService syslogConfigService =(SysLogConfigService) ContextUtil.getBean("sysLogConfigService");
		SysReportConfigService sysReportConfigService =(SysReportConfigService) ContextUtil.getBean("sysReportConfigService");
		List<LogConfig> logConfigList=syslogConfigService.findAll();
		List<ReportConfig> reportConfigList=sysReportConfigService.findAll();
		for (ReportConfig reportConfig : reportConfigList) {
			log.info("report:"+reportConfig.getId()+":"+reportConfig.getName());
		}
		for(LogConfig logs:logConfigList){
			log.info("log:"+logs.getId()+":"+logs.getName()+":"+logs.getType());
		}
		
	}
	
	
}
