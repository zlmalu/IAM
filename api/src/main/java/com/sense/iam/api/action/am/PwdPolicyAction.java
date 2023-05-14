package com.sense.iam.api.action.am;

import java.util.ArrayList;
import java.util.Collection;
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
import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sense.core.util.PageList;
import com.sense.core.util.StringUtils;
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.api.model.am.PwdPolicyReq;
import com.sense.iam.api.model.am.TimePolicyReq;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.am.PwdPolicy;
import com.sense.iam.model.am.TimePolicy;
import com.sense.iam.service.AmPwdPolicyService;

/**
 * 密码强度 - Action
 * @author K3w1n
 *
 */
@Api(tags = "密码强度")
@Controller
@RestController
@RequestMapping("amPwdPolicy")
@ApiSort(value = 22)
public class PwdPolicyAction extends AbstractAction<PwdPolicy, Long> {

	@Resource
	AmPwdPolicyService amPwdPolicyService;
	
	/**
	 * 密码强度保存
	 * @param entity
	 * @return
	 */
	@ApiOperation(value = "新增/修改登录安全")
	@RequestMapping(value = "saveFieldedit", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode saveFieldedit(@RequestBody List<PwdPolicyReq> entityList) {
		try {
			Map<String, String> entityMap = new HashMap<String, String>();
			for (PwdPolicyReq pwdPolicyReq : entityList) {
				entityMap.put(pwdPolicyReq.getName(), pwdPolicyReq.getValue());
			}
			//获取密码最大长度
			Integer PWD_MAX_LEN = Integer.parseInt(entityMap.get("PWD_MAX_LEN"));
			//获取字符最短长度
			Integer PWD_MIN_CHAR_LEN = Integer.parseInt(entityMap.get("PWD_MIN_CHAR_LEN"));
			//获取特殊字符最短长度
			Integer PWD_MIN_SPACIL_CHAR_LEN = Integer.parseInt(entityMap.get("PWD_MIN_SPACIL_CHAR_LEN"));
			//获取数字最少长度
			Integer PWD_MIN_NUM_LEN = Integer.parseInt(entityMap.get("PWD_MIN_NUM_LEN"));
			
			if(PWD_MIN_CHAR_LEN+PWD_MIN_SPACIL_CHAR_LEN+PWD_MIN_NUM_LEN>PWD_MAX_LEN){
				return new ResultCode(-1, "字符、特殊字符、数字长度的总和不能大于密码最大长度！");
			}
			Long objId=request.getParameter("objId")==null?0L:Long.valueOf(request.getParameter("objId"));
			ResultCode saveAndEdit = amPwdPolicyService.saveAndEdit(objId,entityMap);
			return saveAndEdit;
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
		Long objId=request.getParameter("objId")==null?0L:Long.valueOf(request.getParameter("objId"));
		List<PwdPolicy> findAll = amPwdPolicyService.findAll();
		Map<String, String> map = new HashMap<String, String>();
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		for (PwdPolicy pwdPolicy : findAll) {
			if(objId.longValue()==pwdPolicy.getObjId().longValue()){
				map.put(pwdPolicy.getName(), pwdPolicy.getValue());
			}
		}
		if(map.keySet().size()!=0){
			list.add(map);
		}
		
		return list;
	}
}
