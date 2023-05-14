package org.iam.commpoment.timertask.mainData;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sense.core.util.ContextUtil;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.TaskInterface;
import com.sense.iam.model.im.Org;
import com.sense.iam.model.im.User;
import com.sense.iam.service.OrgService;
import com.sense.iam.service.UserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SyncUSer implements TaskInterface {

    protected Log log= LogFactory.getLog(getClass());

    @Param("获取用户接口")
    private String poolName="https://webtest100.u-zf.com/api/mdm/mdmOrgCallBack";


    UserService userService = (UserService) ContextUtil.getBean("imUserService");
    OrgService orgService = (OrgService) ContextUtil.getBean("imOrgService");

    @Override
    public void run(Long aLong, Date date) {
        JSONObject json = JSONUtil.parseObj("");
        JSONObject res = json.getJSONObject("");
        JSONArray ja = json.getJSONArray("");
        for(int i=0 ;i<ja.size();i++){
            JSONObject jo = ja.getJSONObject(i);
        }
        orgService.findByObject(new Org());

        User user = new User();
        Map map = new HashMap();
        map.put("address","");

        user.setExtraAttrs(map);
        ResultCode resultCode = userService.save(user);
        resultCode.getCode();
        resultCode.getMsg();
    }
}
