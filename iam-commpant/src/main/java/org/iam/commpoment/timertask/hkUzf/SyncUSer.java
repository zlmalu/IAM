package org.iam.commpoment.timertask.hkUzf;

import com.sense.core.util.ContextUtil;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.TaskInterface;
import com.sense.iam.model.im.Org;
import com.sense.iam.model.im.User;
import com.sense.iam.service.OrgService;
import com.sense.iam.service.UserService;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncUSer implements TaskInterface {

    protected Log log= LogFactory.getLog(getClass());

    @Param("获取用户接口")
    private String poolName="https://webtest100.u-zf.com/api/mdm/queryMdmStaff";

    UserService userService = (UserService) ContextUtil.getBean("imUserService");
    OrgService orgService = (OrgService) ContextUtil.getBean("imOrgService");

    @Override
    public void run(Long aLong, Date date) {
    	JSONObject o = new JSONObject();
    	Date day = new Date();
    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	o.put("endTime", df.format(day));
//    	o.put("startTime", "2000-07-13 14:10:44");
//    	o.put("endTime", "2022-10-29 16:56:48");
//    	o.put("startTime", LocalDate.now().minusDays(1)+"00:00:00");
    	Calendar c = Calendar.getInstance();
    	c.setTime(new Date());
    	c.add(Calendar.DATE, -3);
    	Date d = c.getTime();
//    	o.put("endTime", "2023-10-29 16:56:48");
    	o.put("startTime", df.format(d));
    	o.put("pageSize", 10142);
//    	log.info(o.toString());
    	String result = HttpRequest.post(poolName)
    	.header("license","316653e3dbc3daa45a432d8d6d2e68c2")
    	.header("orgId","0ca473f9b8910e4df4330fb4f640f75c")
    	.header("Authorization","Basic YWRtaW46YWRtaW4=")
    	.header("Content-Type","application/json")
    	.body(o.toString()).execute().body();
//    	log.info("result="+result.toString());
    	JSONObject b = JSONUtil.parseObj(result);
    	JSONArray arr = b.getJSONObject("result").getJSONArray("list");
//    	log.info("arr:"+arr.size());
    	int m = 0;
//    	log.info("m="+m);
    	for(int i=0;i<arr.size();i++){
    		try{
    			m++;
            User user = new User();
            user.setSn(arr.getJSONObject(i).getStr("jobNo"));
            user.setName(arr.getJSONObject(i).getStr("name"));
            if(arr.getJSONObject(i).getStr("sex").equals("女"))
            user.setSex(2);
            if(arr.getJSONObject(i).getStr("sex").equals("男"))
            user.setSex(1);
            user.setTelephone(arr.getJSONObject(i).getStr("mobile"));
            user.setEmail(arr.getJSONObject(i).getStr("email"));
            user.setStatus(arr.getJSONObject(i).getInt("onTheJobStatus"));
//            user.setOrgId((long) 1000022714);
            Org org1 = new Org();
    		org1.setIsControl(false);
    		org1.setSn(arr.getJSONObject(i).getStr("orgCode"));
            List list = new ArrayList();
            list.add(arr.getJSONObject(i).getStr("jobNo"));
    		if(orgService.findByObject(org1)!=null)
                userService.moveOrg(list, (long) 1000022714, orgService.findByObject(org1).getId());
    			user.setOrgId(orgService.findByObject(org1).getId());
            user.setUserTypeId((long) 1000022716);
//        	log.info(arr.getJSONObject(i));
            Map map = new HashMap();
            map.put("address","");
//            userService.moveOrg(list, (long) 1000022714, orgService.findByObject(org1).getId());
            userService.saveOrUpdateUser(user);
//            user.setExtraAttrs(map);
//            ResultCode res = userService.save(user);
//            log.info("resCode="+res.getCode());
//            log.info("resMsg="+res.getMsg());
//            log.info("jobNo="+arr.getJSONObject(i).getStr("jobNo"));
    		}catch(Exception e){
    			log.info(e.toString());
    		}
    	}
    	log.info("m="+m);
    }
}
