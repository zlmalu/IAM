package org.iam.commpoment.timertask.mainData;

import com.sense.core.util.ContextUtil;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.TaskInterface;
import com.sense.iam.model.im.Org;
import com.sense.iam.model.im.User;
import com.sense.iam.service.OrgService;
import com.sense.iam.service.UserService;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Name("用户同步")
public class SyncUser4 extends mainDataUtil implements TaskInterface {

    @Param("获取用户接口")
    private String orgUrl = "https://webtest100.u-zf.com/api/mdm/queryOrgSubsetStaff";
    @Param("参数1")
    private String license = "316653e3dbc3daa45a432d8d6d2e68c2";
    @Param("参数2")
    private String orgId = "0ca473f9b8910e4df4330fb4f640f75c";
    @Param("参数3")
    private String auth = "Basic YWRtaW46YWRtaW4=";
    @Param("页条数")
    private int pageSize = 1000;
    @Param("用户类型")
    private Long userTypeId = 20000L;
    @Param("是否全量")
    private String isTotal = "1";

    UserService userService = (UserService) ContextUtil.getBean("imUserService");
    OrgService orgService = (OrgService) ContextUtil.getBean("imOrgService");

    @Override
    public void run(Long aLong, Date date) {

        String firstResult = super.getData(orgUrl,license,orgId,auth,pageSize,1,isTotal);

        JSONObject firstJson = JSONUtil.parseObj(firstResult);
        JSONArray firstData = firstJson.getJSONObject("result").getJSONArray("list");

        handle(firstData);

        int total = firstJson.getJSONObject("result").getInt("total");
        int page = (total / pageSize) +1 ;

        if(page <= 1){
            return;
        }

        for(int i=2; i<= page; i++){
            try{
                String pageResult = super.getData(orgUrl,license,orgId,auth,pageSize,1,isTotal);

                JSONObject pageJson = JSONUtil.parseObj(pageResult);
                JSONArray pageData = pageJson.getJSONObject("result").getJSONArray("list");

                handle(pageData);
            } catch (Exception e) {
                log.info(e.toString());
            }
        }
    }

    public void handle(JSONArray arr){
        for (int i = 0; i < arr.size(); i++) {
            try {
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
    }
}
