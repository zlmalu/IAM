package org.iam.commpoment.timertask.mainData;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import net.bytebuddy.implementation.bind.annotation.Default;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class mainDataUtil {

    protected Log log = LogFactory.getLog(getClass());

    public String getData(String url, String license, String orgId, String auth, int pageSize, int page, String isTotal){


        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, -3);
        Date d = c.getTime();

        JSONObject param = new JSONObject();
        param.put("pageNum", page);
        param.put("pageSize", Long.valueOf(pageSize));
        if("1".equals(isTotal)){
            param.put("startTime", "2000-01-01 00:00:00");
        }else {
            param.put("startTime", df.format(d));
        }
        param.put("endTime", df.format(new Date()));
        log.info(param);

        String result = HttpRequest.post(url)
                .header("license", license)
                .header("orgId", orgId)
                .header("Authorization", auth)
                .header("Content-Type", "application/json")
                .body(param.toString()).execute().body();

        return result;

    }
}
