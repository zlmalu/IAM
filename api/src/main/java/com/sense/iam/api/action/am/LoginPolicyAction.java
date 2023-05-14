package com.sense.iam.api.action.am;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiSort;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.api.model.am.LoginPolicyReq;
import com.sense.iam.api.model.am.PwdPolicyReq;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.am.LoginPolicy;
import com.sense.iam.model.am.PwdPolicy;
import com.sense.iam.service.AmLoginPolicyService;

/**
 * 登录安全 - Action
 * 
 * @author K3w1n
 *
 */
@Api(tags = "登录安全")
@Controller
@RestController
@RequestMapping("amLoginPolicy")
@ApiSort(value = 21)
public class LoginPolicyAction extends AbstractAction<LoginPolicy, Long> {

	@Resource
	AmLoginPolicyService amLoginPolicyService;

	/**
	 * 登录安全保存/修改
	 * 
	 * @param entity
	 * @return
	 */
	@ApiOperation(value = "新增/修改登录安全")
	@RequestMapping(value = "saveFieldedit", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode saveFieldedit(@RequestBody List<LoginPolicyReq> entityList) {
		try {
			Map<String, String> entityMap = new HashMap<String, String>();
			for (LoginPolicyReq loginPolicyReq : entityList) {
				entityMap.put(loginPolicyReq.getName(), loginPolicyReq.getValue());
			}
			return amLoginPolicyService.saveAndEdit(entityMap);
		} catch (Exception e) {
			log.error("edit error:", e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}

	/**
	 * 查询登录安全
	 */
	@ApiOperation(value = "查询全部")
	@RequestMapping(value = "findByAll", method = RequestMethod.GET)
	@ResponseBody
	public List<Map<String, String>> findByAll() {
		List<LoginPolicy> findAll = amLoginPolicyService.findAll();
		Map<String, String> map = new HashMap<String, String>();
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		for (LoginPolicy loginPolicy : findAll) {
			map.put(loginPolicy.getName(), loginPolicy.getValue());
		}
		list.add(map);
		return list;
	}
}
