package org.iam.commpoment.timertask.mainData;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sense.core.util.ContextUtil;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.TaskInterface;
import com.sense.iam.model.im.Org;
import com.sense.iam.service.OrgService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Name("组织同步")
public class SyncOrg extends mainDataUtil implements TaskInterface {

    protected Log log = LogFactory.getLog(getClass());

    @Param("获取组织接口")
    private String orgUrl = "https://webtest100.u-zf.com/api/mdm/queryMdmOrg";
    @Param("参数1")
    private String license = "316653e3dbc3daa45a432d8d6d2e68c2";
    @Param("参数2")
    private String orgId = "0ca473f9b8910e4df4330fb4f640f75c";
    @Param("参数3")
    private String auth = "Basic YWRtaW46YWRtaW4=";
    @Param("页条数")
    private int pageSize = 1000;
    @Param("是否全量")
    private String isTotal = "1";

    OrgService orgService = (OrgService) ContextUtil.getBean("imOrgService");

    @Override
    public void run(Long aLong, Date date) {
        String firstResult = super.getData(orgUrl,license,orgId,auth,pageSize,1,isTotal);

        JSONObject firstJson = JSONUtil.parseObj(firstResult);
        JSONArray firstData = firstJson.getJSONObject("result").getJSONArray("list");
        System.out.println("firstData:"+firstData.size());
        handle(firstData);

        int total = firstJson.getJSONObject("result").getInt("total");
        int page = (total / pageSize) +1 ;

        if(page <= 1){
            return;
        }

        for(int i=2; i<= page; i++){
            try{
                String pageResult = super.getData(orgUrl,license,orgId,auth,pageSize,i,isTotal);

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
                Org org = new Org();
                if (arr.getJSONObject(Integer.valueOf(i)).getStr("isAvailable").equals("true"))
                    org.setStatus(Integer.valueOf(1));
                if (arr.getJSONObject(Integer.valueOf(i)).getStr("isAvailable").equals("false"))
                    org.setStatus(Integer.valueOf(2));
                org.setSn(arr.getJSONObject(Integer.valueOf(i)).getStr("orgCode"));
                org.setName(arr.getJSONObject(Integer.valueOf(i)).getStr("name"));
                org.setOrgTypeId(Long.valueOf(1000022709L));
                Org org1 = new Org();
                org1.setIsControl(Boolean.valueOf(false));
                org1.setSn(arr.getJSONObject(Integer.valueOf(i)).getStr("parentOrgCode"));
                boolean flag = true;
                if (arr.getJSONObject(Integer.valueOf(i)).getStr("parentOrgCode").equals(arr.getJSONObject(Integer.valueOf(i)).getStr("orgCode"))) {
                    org.setParentId(Long.valueOf(1000022714L));
                }
                else if (this.orgService.findByObject(org1) != null) {
                    org.setParentId(((Org)this.orgService.findByObject(org1)).getId());
                } else {
                    flag = false;
                }

                Map map = new HashMap();
                map.put("address", "");


                this.log.info(Boolean.valueOf(flag));
                if (flag) {
                    this.orgService.save(org);
                }
            } catch (Exception e) {
                this.log.info(e.toString());
            }
        }
    }
}
