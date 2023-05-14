package org.iam.compoment.workflow;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sense.core.util.ContextUtil;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.TaskInterface;
import com.sense.iam.model.im.App;
import com.sense.iam.service.AppService;

import cn.hutool.http.HttpUtil;

@Name("（新增/修改）应用定时任务")
public class TenantTask implements TaskInterface {

	@Param("地址")
	private String url = "http://localhost:8082/workflow/api/v1/enumeration/item/saveOrUpdate";
	@Param("密钥")
	private String secret = "ee58f77294d84fd196f98a505e9780ca";
	@Param("枚举名称")
	private String enumerationName = "统一门户应用列表";

	@Override
	public void run(Long timerTaskId, Date runTime) {
		// 注入服务
		AppService appService = (AppService) ContextUtil.getBean("imAppService");
		// 遍历所有应用调用接口
		App app = new App();
		app.setIsControl(false);
		List<App> list = appService.findList(app);
		for (App vo : list) {

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("enumerationName", enumerationName);
			params.put("key", vo.getSn());
			params.put("value", vo.getName());
			params.put("status", "1");
			params.put("secret", secret);

			HttpUtil.post(url, params);
		}
	}

}
