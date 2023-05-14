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
import com.sense.iam.api.model.am.RadiusReq;
import com.sense.iam.cache.CacheSender;
import com.sense.iam.cache.SysConfigCache;
import com.sense.iam.cache.redis.RadiusStatus;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.am.Radius;
import com.sense.iam.service.AmRadiusService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiSort;

@Api(tags = "Radius认证服务")
@Controller
@RestController
@RequestMapping("amRadius")
@ApiSort(value = 22)
public class RadiusAction extends AbstractAction<Radius,Long>{

	@Resource
	private AmRadiusService amRadiusService;
	
	@Resource
	protected CacheSender cacheSender;

	@ApiOperation(value = "新增")
	@RequestMapping(value = "save", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody Radius radius) {
		ResultCode code = amRadiusService.save(radius);
		return code;
	}
	
	
	@ApiOperation(value = "修改")
	@RequestMapping(value = "edit", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	public ResultCode edit(@RequestBody Radius radius) {
		ResultCode code = amRadiusService.edit(radius);
		return code;
	}
	
	
	@ApiOperation(value = "指定唯一标识查询")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType = "path", dataType = "Long")
	})
	@RequestMapping(value = "findById/{id}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperationSupport(order = 5)
	public Radius findById(@PathVariable Long id) {
		return super.findById(id);
	}
	
	
	@ApiOperation(value = "分页查询列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", required = true, dataType = "Integer", value = "页码,默认0",example="0"),
		@ApiImplicitParam(name = "limit", required = true, dataType = "Integer", value = "页大小，默认20页", example = "20")
	})
	@RequestMapping(value = "findList", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 4)
	protected PageList<Radius> findList(@RequestBody RadiusReq entity, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer limit) {
		try {
			Radius r = entity.getRadius();
			r.setIsLikeQuery(true);
			return getBaseService().findPage(r, page, limit);
		} catch (Exception e) {
			log.error("findList error:", e);
			return new PageList<Radius>();
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

	
	@ApiOperation(value = "启动")
	@RequestMapping(value = "start", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 6)
	public ResultCode start() {
		SysConfigCache.IS_STARTED_RADIUS=true;
		cacheSender.sendMsg(new RadiusStatus(true));
		return new ResultCode(Constants.OPERATION_SUCCESS);
	}
	
	@ApiOperation(value = "停止")
	@RequestMapping(value = "stop", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 7)
	public ResultCode stop() {
		SysConfigCache.IS_STARTED_RADIUS=false;
		cacheSender.sendMsg(new RadiusStatus(false));
		return new ResultCode(Constants.OPERATION_SUCCESS);
	}
	
	@ApiOperation(value = "启动状态")
	@RequestMapping(value = "isEnabled", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 3)
	public Object isEnabled() {
		return SysConfigCache.IS_STARTED_RADIUS;
	}
}
