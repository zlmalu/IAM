package com.sense.iam.api.action.am;

import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.api.model.am.PwdPolicyReq;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.am.PwdPolicy;
import com.sense.iam.service.AmPwdPolicyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiSort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 弱密码强度 - Action
 * @author ygd
 *
 */
@Api(tags = "弱密码强度")
@Controller
@RestController
@RequestMapping("amWeakPwdPolicy")
@ApiSort(value = 22)
public class WeakPwdPolicyAction extends AbstractAction<PwdPolicy, Long> {

	@Resource
	AmPwdPolicyService amPwdPolicyService;

	/**
	 * 弱密码强度保存
	 * @param entityList 实体类集合
	 * @return 结果
	 */
	@ApiOperation(value = "新增/修改登录安全")
	@RequestMapping(value = "saveFieldEdit", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode saveFieldEdit(@RequestBody List<PwdPolicyReq> entityList) {
		try {
			Map<String, String> entityMap = new HashMap<>();
			for (PwdPolicyReq pwdPolicyReq : entityList) {
				entityMap.put(pwdPolicyReq.getName(), pwdPolicyReq.getValue());
			}
			Long objId=-1L;
			return amPwdPolicyService.saveAndEdit(objId,entityMap);
		} catch (Exception e) {
			log.error("edit error:", e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}

	/**
	 * 查询密码强度
	 */
	@ApiOperation(value = "查询全部")
	@RequestMapping(value = "findByAll", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	public List<Map<String, String>> findByAll() {
		long objId=-1L;
		List<PwdPolicy> findAll = amPwdPolicyService.findAll();
		Map<String, String> map = new HashMap<>();
		List<Map<String, String>> list = new ArrayList<>();
		for (PwdPolicy pwdPolicy : findAll) {
			if(objId== pwdPolicy.getObjId()){
				map.put(pwdPolicy.getName(), pwdPolicy.getValue());
			}
		}
		if(map.keySet().size()!=0){
			list.add(map);
		}

		return list;
	}
}
