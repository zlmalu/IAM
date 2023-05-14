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

public class SyncUSer1 implements TaskInterface {

    protected Log log = LogFactory.getLog(getClass());

    @Param("获取用户接口")
    private String poolName = "https://webtest100.u-zf.com/api/mdm/queryOrgSubsetStaff";
    @Param("组织code")
    private String orgCode = "100200";
    @Param("系统标识")
    private String sysIdentify = "mz_sso";
    @Param("用户类型")
    private long userTypeId = 1000022716;

    UserService userService = (UserService) ContextUtil.getBean("imUserService");
    OrgService orgService = (OrgService) ContextUtil.getBean("imOrgService");

    @Override
    public void run(Long aLong, Date date) {
        JSONObject o = new JSONObject();
        Date day = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, -7);
        Date d = c.getTime();
        o.put("queryCode", orgCode);
        o.put("systemId", sysIdentify);

        String result = HttpRequest.post(poolName)
                .header("license", "316653e3dbc3daa45a432d8d6d2e68c2")
                .header("orgId", "0ca473f9b8910e4df4330fb4f640f75c")
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .header("Content-Type", "application/json")
                .body(o.toString()).execute().body();

        JSONObject b = JSONUtil.parseObj(result);
        JSONArray arr = b.getJSONArray("result");

        int m = 0;
        log.info("m=" + m);
        for (int i = 0; i < arr.size(); i++) {
            try {
                m++;

                String jobNo = arr.getJSONObject(i).getStr("jobNo");
                String name = arr.getJSONObject(i).getStr("name");
                String sex = arr.getJSONObject(i).getStr("sex");
                String mobile = arr.getJSONObject(i).getStr("mobile");
                String email = arr.getJSONObject(i).getStr("email");
                int onTheJobStatus = arr.getJSONObject(i).getInt("onTheJobStatus") == null ? 1 : arr.getJSONObject(i).getInt("onTheJobStatus");
                String orgCode = arr.getJSONObject(i).getStr("belongingToDeptOrgCode");

                Org org = new Org();
                org.setIsControl(false);
                org.setSn(orgCode);
                org = orgService.findByObject(org);

                if (org == null) {
                    log.info("Find Org Error : " + "找不到用户所属组织");
                    continue;
                }

                User user = new User();
                user.setSn(jobNo);
                user = userService.findByObject(user);

                if(user == null){  //用户不存在则保存用户
                    user = new User();
                    user.setSn(jobNo);
                    user.setName(name);
                    if (sex.equals("女"))
                        user.setSex(2);
                    else
                        user.setSex(1); //默认男
                    user.setTelephone(mobile);
                    user.setEmail(email);
                    user.setStatus(onTheJobStatus);
                    user.setUserTypeId(userTypeId);
                    user.setOrgId(org.getId());

                    userService.save(user);
                } else {  //存在修改
                    List<String> list = new ArrayList();
                    list.add("" + user.getId());
                    log.info(user.getOrgId()+ "   " +user.getName());
                    log.info(list);
                    userService.moveOrg(list, userService.findById(user.getId()).getOrgId(), org.getId());

                    user.setName(name);
                    if (sex.equals("女"))
                        user.setSex(2);
                    else
                        user.setSex(1); //默认男
                    user.setTelephone(mobile);
                    user.setEmail(email);
                    user.setStatus(onTheJobStatus);
                    user.setUserTypeId(userTypeId);
                    userService.edit(user);
                }
            } catch (Exception e) {
                log.info(e.toString());
            }
        }
        log.info("m=" + m);
    }
}
