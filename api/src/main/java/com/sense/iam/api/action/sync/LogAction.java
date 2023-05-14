package com.sense.iam.api.action.sync;

import java.util.List;

import javax.annotation.Resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiSort;

import org.apache.commons.text.StringEscapeUtils;
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
import com.sense.iam.api.model.sync.ConfigReq;
import com.sense.iam.api.model.sync.LogModel;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.data.process.SyncProcess;
import com.sense.iam.data.process.SyncProcess.SyncModel;
import com.sense.iam.model.sync.Config;
import com.sense.iam.model.sync.Log;
import com.sense.iam.model.sync.Queue;
import com.sense.iam.service.SyncConfigService;
import com.sense.iam.service.SyncLogService;
import com.sense.iam.service.SyncQueueService;

/**
 * 同步日志页面交互类
 * 
 * Description: 提供与页面交互的
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Api(tags = "同步日志模块")
@RestController
@Controller
@RequestMapping("sync/log")
@ApiSort(value = 12)
public class LogAction extends AbstractAction<Log,Long>{
	
	@Resource
	private SyncLogService syncLogService;
	
	@Resource
	private SyncProcess syncProcess;
	@Resource
	private SyncQueueService syncQueueService;
	@Resource
	private SyncConfigService syncConfigService;
	
	
	@ApiOperation(value="重新同步数据")
	@RequestMapping(value="resetSync", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode resetSync(@RequestBody @ApiParam(name="id",value="同步日志ID",required=true)Long id){
		//获取同步对象
		Log syncLog=syncLogService.findById(id);
		//获取同步配置
		Queue syncQueue=syncQueueService.findById(syncLog.getSyncQueueId());
		Config syncConfig=syncConfigService.findById(syncLog.getSyncConfigId());
		SyncModel syncModel=new SyncModel(id,syncLog.getSyncQueueId(),syncQueue.getContent(),syncConfig);
		syncProcess.processSync(syncModel);
		return new ResultCode(Constants.OPERATION_SUCCESS);
	}
	
	
	@ApiOperation(value="新增")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody Log entity) {
		return super.save(entity);
	}
	
	@ApiOperation(value="更新")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	public ResultCode edit(@RequestBody Log entity) {
		return super.edit(entity);
	}
	
	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findById/{id}", method=RequestMethod.GET)
	@ResponseBody
	public Log findById(@PathVariable Long id) {
		return super.findById(id);
	}
	
	/**
	 * 分页查询同步配置列表
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	@ApiOperation(value="分页查询同步配置列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findList", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 4)
	protected PageList<Log> findList(@RequestBody LogModel entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			Log log=new Log();
			if(entity.getStatus()!=null){
				log.setStatus(entity.getStatus());
			}
			log.setAppSn(StringEscapeUtils.escapeHtml4(entity.getAppSn()));
			log.setFilterEndTime(StringEscapeUtils.escapeHtml4(entity.getFilterEndTime()));
			log.setFilterStartTime(StringEscapeUtils.escapeHtml4(entity.getFilterStartTime()));
			log.setSysEventName(StringEscapeUtils.escapeHtml4(entity.getSysEventName()));
			log.setIsLikeQuery(true);
			return getBaseService().findPage(log,page,limit);
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<Log>();
		}
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
