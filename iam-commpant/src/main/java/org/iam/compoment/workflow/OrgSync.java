package org.iam.compoment.workflow;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.SyncInteface;

import cn.hutool.http.HttpUtil;

@Name("（新增/修改）组织同步")
public class OrgSync implements SyncInteface {

	@Param("地址")
	private String url = "http://localhost:8082/workflow/api/v1/org/saveOrUpdate";
	@Param("密钥")
	private String secret = "ee58f77294d84fd196f98a505e9780ca";

	@Override
	public ResultCode execute(String content) {

		JSONObject json = JSONObject.parseObject(content);

		String sn = json.getString("sn");
		String parentSn = json.getString("parentSn");
		String name = json.getString("name");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("sn", sn);
		params.put("parentSn", parentSn);
		params.put("name", name);
		params.put("status", 1);
		params.put("secret", secret);

		String data = HttpUtil.post(url, params);
		JSONObject resultJSON = JSONObject.parseObject(data);

		if ("success".equals(resultJSON.getString("result"))) {
			return new ResultCode(SUCCESS, "处理成功");
		} else {
			return new ResultCode(FAIL, resultJSON.getString("msg"));
		}

	}

}
