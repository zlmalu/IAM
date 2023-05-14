package org.iam.compoment.workflow;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.SyncInteface;

import cn.hutool.http.HttpUtil;

@Name("（新增/修改）用户同步")
public class UserSync implements SyncInteface {

	@Param("地址")
	private String url = "http://localhost:8082/workflow/api/v1/user/saveOrUpdate";
	@Param("密钥")
	private String secret = "ee58f77294d84fd196f98a505e9780ca";

	@Override
	public ResultCode execute(String content) {

		JSONObject json = JSONObject.parseObject(content);

		String loginName = json.getString("loginName");
		String sn = json.getString("sn");
		String password = json.getString("password");
		String name = json.getString("name");
		String sex = json.getString("sex");
		String email = json.getString("email");
		String telephone = json.getString("telephone");
		String status = json.getString("status");
		String orgSn = json.getString("orgSn");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", loginName);
		params.put("sn", sn);
		params.put("password", password);
		params.put("name", name);
		params.put("sex", sex);
		params.put("email", email);
		params.put("telephone", telephone);
		params.put("status", status);
		params.put("orgSn", orgSn);
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
