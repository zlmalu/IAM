package org.iam.compoment.sync.account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import com.sense.core.security.UIM;
import com.sense.core.util.ContextUtil;
import com.sense.core.util.XMLUtil;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.SyncInteface;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.JdbcService;

@Name("修改应用密码")
public class UpdateAppPwd implements SyncInteface{

	@Param("更改密码应用编号")
	private static String UpdatePwdApp="APP002,APP006,APP015,APP007,APP017,APP011,APP019,APP020,APP022,APP023,APP026,APP028,APP027,APP018,APP031,APP025,APP032,APP033,APP034,APP014,APP035,APP030,APP036,APP037";

	
	
	@Override
	public ResultCode execute(String content) {
		try {
			Map<String,String> contentMap=XMLUtil.simpleXml2Map(content);
			String pwd=contentMap.remove("pwd");
			String userid=contentMap.remove("userid");
			String newApps=null;
			String[] appLSD=UpdatePwdApp.split(",");
			for(String key:appLSD){
				if(newApps==null){
					newApps="'"+key+"'";
				}else{
					newApps+=",'"+key+"'";
				}
			}
			String sql="select ID FROM IM_ACCOUNT WHERE USER_ID="+userid+" and APP_ID IN(SELECT ID FROM IM_APP WHERE SN IN("+newApps+"))";
			System.out.println("sql:"+sql);
			JdbcService jdbcService=(JdbcService) ContextUtil.getBean("jdbcService");
			AccountService accountService=(AccountService) ContextUtil.getBean("imAccountService");
			List<Map<String, Object>> maps=jdbcService.findList(sql);
			if(maps!=null&&maps.size()==0){
				return new ResultCode(SUCCESS,"修改成功，当前用户未存在业务系统帐号："+UpdatePwdApp); 
			}
			for(Map<String, Object> map:maps){
				Long[] ids=new Long[1];
				ids[0]=Long.valueOf(map.get("ID").toString());
				//执行密码修改
				accountService.updatePwd(ids, pwd);
			}
			return new ResultCode(SUCCESS,"修改成功，应用密码修改成功："+UpdatePwdApp); 
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultCode(FAIL,"修改密码有误，请检查相关配置"); 
		}
	}


}
