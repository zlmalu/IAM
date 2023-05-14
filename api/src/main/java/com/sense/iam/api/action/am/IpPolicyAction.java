package com.sense.iam.api.action.am;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sense.core.util.PageList;
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.api.model.am.IpPolicyReq;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.am.IpPolicy;
import com.sense.iam.service.AmIpPolicyService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiSort;

/**
 * ip控制管理 - Action
 * @author K3w1n
 *
 */
@Api(tags = "ip控制管理")
@Controller
@RestController
@RequestMapping("amIpPolicy")
@ApiSort(value = 18)
public class IpPolicyAction extends AbstractAction<IpPolicy,Long>{

	@Resource
	AmIpPolicyService amIpPolicyService;
	
	@ApiOperation(value="新增")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody IpPolicyReq entity) {
		return amIpPolicyService.save(entity.getIpPolicy());
	}
	
	@ApiOperation(value = "更新")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	public ResultCode edit(@RequestBody IpPolicyReq entity) {
		IpPolicy ipPolicy = entity.getIpPolicy();
		return amIpPolicyService.edit(ipPolicy);
	}
	
	@ApiOperation(value = "指定唯一标识查询")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType = "path", dataType = "Long")
	})
	@RequestMapping(value = "findById/{id}", method = RequestMethod.GET)
	@ResponseBody
	public IpPolicy findById(@PathVariable Long id) {
		return super.findById(id);
	}
	
	@ApiOperation(value = "查询所有IP控制")
	@RequestMapping(value = "findAll", method = RequestMethod.GET)
	@ResponseBody
	public Object findAll() {
		return super.findAll(new IpPolicy());
	}
	
	/**
	 * 分页查询列表
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	@ApiOperation(value = "分页查询列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", required = true, dataType = "Integer", value = "页码,默认0",example="0"),
		@ApiImplicitParam(name = "limit", required = true, dataType = "Integer", value = "页大小，默认20页", example = "20")
	})
	@RequestMapping(value = "findList", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 4)
	protected PageList<IpPolicy> findList(@RequestBody IpPolicyReq entity, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer limit) {
		try {
			IpPolicy i = entity.getIpPolicy();
			i.setIsLikeQuery(true);
			return getBaseService().findPage(i, page, limit);
		} catch (Exception e) {
			log.error("findList error:", e);
			return new PageList<IpPolicy>();
		}
	}
	
	@ApiOperation(value="移除")
	@RequestMapping(value="remove", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 3)
	public ResultCode remove(@RequestBody @ApiParam(name = "唯一标识集合", value="多数据采取英文逗号分割", required = true)List<String> ids) {
		Params params = new Params();
		params.setIds(ids);
		return super.remove(params);
	}
}