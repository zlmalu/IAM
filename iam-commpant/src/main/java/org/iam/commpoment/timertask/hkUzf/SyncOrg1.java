package org.iam.commpoment.timertask.hkUzf;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.core.util.ContextUtil;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.TaskInterface;
import com.sense.iam.model.im.Org;
import com.sense.iam.model.im.User;
import com.sense.iam.service.OrgService;
import com.sense.iam.service.UserService;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

public class SyncOrg1  implements TaskInterface {
	protected Log log= LogFactory.getLog(getClass());

    @Param("获取组织接口")
    private String poolName="https://webtest100.u-zf.com/api/mdm/queryOrgSubset";
    
    @Param("组织code")
    private String orgCode = "100200";
    @Param("系统标识")
    private String sysIdentify = "mz_sso";
    OrgService orgService = (OrgService) ContextUtil.getBean("imOrgService");

	@Override
	public void run(Long arg0, Date arg1) {
		// TODO Auto-generated method stub
		JSONObject o = new JSONObject();
//    	o.put("startTime", "2000-07-13 14:10:44");
//    	o.put("endTime", "2022-10-29 16:56:48");
//    	o.put("startTime", LocalDate.now().minusDays(1)+"00:00:00");
//    	o.put("endTime", "2023-10-29 16:56:48");
//    	o.put("pageSize", 10142);
		o.put("queryCode",orgCode);
		o.put("systemId",sysIdentify);
    	log.info(o.toString());
    	String result = HttpRequest.post(poolName)
    	.header("license","316653e3dbc3daa45a432d8d6d2e68c2")
    	.header("orgId","0ca473f9b8910e4df4330fb4f640f75c")
    	.header("Authorization","Basic YWRtaW46YWRtaW4=")
    	.header("Content-Type","application/json")
    	.body(o.toString()).execute().body();
//    	log.info(result.toString());
    	JSONObject b = JSONUtil.parseObj(result);
    	JSONArray arr = b.getJSONObject("result").getJSONArray("list");
    	for(int i=0;i<arr.size();i++){
    		try{
    		Org org = new Org();
    		if(arr.getJSONObject(i).getStr("isAvailable").equals("true"))
    		org.setStatus(1);
    		if(arr.getJSONObject(i).getStr("isAvailable").equals("false"))
    		org.setStatus(2);
    		org.setSn(arr.getJSONObject(i).getStr("orgCode"));
    		org.setName(arr.getJSONObject(i).getStr("name"));
    		org.setOrgTypeId((long) 1000022709);
    		Org org1 = new Org();
    		org1.setIsControl(false);
    		org1.setSn(arr.getJSONObject(i).getStr("parentOrgCode"));
    		boolean flag=true;
    		if(arr.getJSONObject(i).getStr("parentOrgCode").equals(arr.getJSONObject(i).getStr("orgCode")))
    			org.setParentId((long) 1000022714);
    		else{
	    		if(orgService.findByObject(org1)!=null)
	    			org.setParentId(orgService.findByObject(org1).getId());
	    		else
	    			flag=false;
    		}
//        	log.info(arr.getJSONObject(i));
            Map map = new HashMap();
            map.put("address","");

//            user.setExtraAttrs(map);
            log.info(flag);
            if(flag){
            	orgService.save(org);
            }
    		}catch(Exception e){
    			log.info(e.toString());
    		}
    	}
	}

}
