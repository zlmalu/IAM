package org.iam.compoment.sync.oa;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.sense.core.util.ContextUtil;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.TaskInterface;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.JdbcService;

public class UserSuspendJop implements TaskInterface {

	@Param("执行语句")
	private String sql = "select id,sn,name from im_user where status=2";
	
	@Override
	public void run(Long timerTaskId, Date runTime) {
		System.out.println("UserSuspendJop run ....");
		AccountService accountService=(AccountService) ContextUtil.context.getBean(AccountService.class);
		JdbcService jdbcService=(JdbcService) ContextUtil.getBean("jdbcService");
		//获取禁用的用户信息
		List<Map<String, Object>> list = jdbcService.findList(sql);
		if(list!=null&&list.size()>0){
			System.out.println("user suspend size:"+list.size());
			for(int i=0;i<list.size();i++){
				String accountSQL="select id from im_account where user_id="+list.get(i).get("id").toString()+" and status=1";
				//获取当前用户已禁用的帐号集合
				List<Map<String, Object>> accountlist = jdbcService.findList(accountSQL);
				System.out.println("=======================================================================");
				if(accountlist!=null&&accountlist.size()>0){
					System.out.println("user suspend sn:"+list.get(i).get("sn").toString()+",name="+list.get(i).get("name").toString()+",account size:"+accountlist.size());
					Long[] ids=new Long[accountlist.size()];
					for(int j=0;j<accountlist.size();j++){
						ids[j]=Long.valueOf(accountlist.get(j).get("id").toString());
					}
					accountService.disabled(ids);
					ids=null;
				}else{
					System.out.println("user suspend sn:"+list.get(i).get("sn").toString()+",name="+list.get(i).get("name").toString()+",account size:0 ,continue");
				}
				System.out.println("=======================================================================");
			}
			
		}
		System.out.println("UserSuspendJop end ....");
	}

}
