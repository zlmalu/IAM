package org.iam.compoment.sync.oa;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.sense.core.util.ContextUtil;
import com.sense.iam.compoment.TaskInterface;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.OrgService;

public class Times implements TaskInterface {

	@Override
	public void run(Long timerTaskId, Date runTime) {
		System.out.println("time run ....");
		OrgService orgService=(OrgService) ContextUtil.context.getBean(OrgService.class);
		JdbcService jdbcService=(JdbcService) ContextUtil.getBean("jdbcService");
		List<Map<String, Object>> list = jdbcService.findList("select o.id from im_org  o left join (select org_id ,value from im_org_attr where name='ORG_STATUS') attr on attr.org_id=o.id where o.COMPANY_SN='100001' and o.parent_id!=-1 and o.attr.value=1 ORDER BY o.CREATE_TIME asc");
		if(list!=null&&list.size()>0){
			System.out.println("update org size:"+list.size());
			Long[] ids=new Long[list.size()];
			for(int i=0;i<list.size();i++){
				ids[i]=Long.valueOf(list.get(i).get("id").toString());
				
			}
			orgService.updateSync(ids);
		}
		System.out.println("time end ....");
	}

}
