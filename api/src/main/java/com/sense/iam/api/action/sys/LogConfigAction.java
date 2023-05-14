package com.sense.iam.api.action.sys;

import java.util.List;

import com.sense.iam.cam.Constants;
import com.sense.iam.service.SysLogConfigService;
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
import com.sense.iam.api.model.sys.LogConfigReq;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.sys.LogConfig;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiSort;
import sun.security.jgss.LoginConfigImpl;

import javax.annotation.Resource;

/**
 * 审计配置模块
 *
 * Description: 审计配置模块
 *
 * @author w_jfwen
 *
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Api(tags = "审计配置模块")
@RestController
@Controller
@RequestMapping("sys/auditcfg/")
@ApiSort(value = 29)
public class LogConfigAction extends AbstractAction<LogConfig,Long>{
	@Resource
	private SysLogConfigService sysLogConfigService;

	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findById/{id}", method=RequestMethod.GET)
	@ResponseBody
	public LogConfig findById(@PathVariable Long id) {
		return super.findById(id);
	}

	/**
	 * 分页查询系统配置列表
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	@ApiOperation(value="分页查询系统配置列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findList", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 4)
	protected PageList<LogConfig> findList(@RequestBody LogConfigReq entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			LogConfig logConfig=entity.getLogConfig();
			logConfig.setIsLikeQuery(true);
			return getBaseService().findPage(logConfig,page,limit);
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<LogConfig>();
		}
	}

	@ApiOperation(value="新增")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody LogConfigReq entity) {
		if(entity.getSn() != null && entity.getSn().length()>0){
			LogConfig logConfig = new LogConfig();
			logConfig.setSn(entity.getSn());
			if(sysLogConfigService.findByObject(logConfig) != null){
				return new ResultCode(Constants.OPERATION_EXIST,"编号已存在");
			}
			return super.save(entity.getLogConfig());
		}
		return new ResultCode(Constants.OPERATION_FAIL,"编号不能为空");
	}

	@ApiOperation(value="更新")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	public ResultCode edit(@RequestBody LogConfigReq entity) {
		LogConfig logConfig=entity.getLogConfig();
		String sn = logConfig.getSn();
		LogConfig logConfig1 = sysLogConfigService.findById(logConfig.getId());
		if(!logConfig1.getSn().equals(sn)){
			return new ResultCode(Constants.OPERATION_FAIL,"编号不能修改");
		}
		return super.edit(entity.getLogConfig());
	}

	@ApiOperation(value="移除")
    @RequestMapping(value="remove",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 3)
    public ResultCode remove(@RequestBody @ApiParam(name="唯一标识集合",value="多数据采用英文逗号分割",required=true)List<String> ids) {
		Params params=new Params();
		params.setIds(ids);
		return super.remove(params);
    }
}
