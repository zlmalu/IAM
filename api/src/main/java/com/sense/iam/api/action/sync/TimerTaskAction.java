package com.sense.iam.api.action.sync;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiSort;

import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sense.core.util.ArrayUtils;
import com.sense.core.util.CrontabUtil;
import com.sense.core.util.CurrentAccount;
import com.sense.core.util.PageList;
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.api.model.sync.TimerTaskReq;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.data.process.TimerTaskProcess;
import com.sense.iam.model.sync.TimerTask;
import com.sense.iam.service.TimerTaskService;

/**
 * 定时任务模块
 * 
 * Description: 提供与页面交互的定时任务查询、添加、删除、修改的操作
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Api(tags = "定时任务模块")
@RestController
@Controller
@RequestMapping("timerTask")
@ApiSort(value = 10)
public class TimerTaskAction extends AbstractAction<TimerTask,Long>{
	

	@ApiOperation(value="新增")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody TimerTask entity) {
		//变更下次执行时间
		try{
			entity.setNextExecuteTime(CrontabUtil.getNextExecuteTime(entity.getCronExpression()));
		}catch(Exception e){
			return new ResultCode(Constants.OPERATION_FAIL,"表达式格式错误!");
		}
		return super.save(entity);
	}
	
	@ApiOperation(value="更新")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	public ResultCode edit(@RequestBody TimerTaskReq entity) {
		//变更下次执行时间
		try{
			entity.setNextExecuteTime(CrontabUtil.getNextExecuteTime(entity.getCronExpression()));
		}catch(Exception e){
			return new ResultCode(Constants.OPERATION_FAIL,"表达式格式错误!");
		}
		return super.edit(entity.getTimerTask());
	}
	
	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findById/{id}", method=RequestMethod.GET)
	@ResponseBody
	public TimerTask findById(@PathVariable Long id) {
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
	protected PageList<TimerTask> findList(@RequestBody TimerTaskReq entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			TimerTask timerTask = entity.getTimerTask();
			timerTask.setIsLikeQuery(true);
			return getBaseService().findPage(timerTask,page,limit);
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<TimerTask>();
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

	@Resource
	private TimerTaskService timerTaskService;
	@Resource
	private TimerTaskProcess timerTaskProcess;
	
	@ApiOperation(value="立即执行")
    @RequestMapping(value="run",method = RequestMethod.POST)
	@ResponseBody
	public ResultCode run(@RequestBody TimerTaskReq entity){
		try{
			TimerTask timerTask = timerTaskService.findById(entity.getTimerTask().getId());
			if(timerTask!=null && timerTask.getStatus().intValue()!=Constants.TASK_PROCESSING){
				CronTrigger c=new CronTrigger(timerTask.getCronExpression());
				timerTask.setPreExecuteTime(new Date());
				timerTask.setNextExecuteTime(c.nextExecutionTime(new SimpleTriggerContext()));
				timerTask.setStatus(Constants.TASK_PROCESSING);
				timerTaskService.edit(timerTask);
				final CurrentAccount currentAccount=CurrentAccount.getCurrentAccount();
				new Thread(new Runnable(){
					@Override
					public void run() {
						CurrentAccount.setCurrentAccount(currentAccount);
						timerTaskProcess.process(timerTask);
						CurrentAccount.setCurrentAccount(null);
					}
				}).start();
			}
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}	
	
	@ApiOperation(value="启动")
    @RequestMapping(value="modifyStatus",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 5)
	public ResultCode modifyStatus(@RequestBody @ApiParam(name = "唯一标识集合", value="多数据采取英文逗号分割", required = true)List<String> ids){
		try{
			Params params = new Params();
			params.setIds(ids);
			timerTaskService.sfyx(ArrayUtils.stringArrayToLongArray(params.getIds().toArray(new String[params.getIds().size()])));
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
	@ApiOperation(value="禁用")
    @RequestMapping(value="modifyStop",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 6)
	public ResultCode modifyStop(@RequestBody @ApiParam(name = "唯一标识集合", value="多数据采取英文逗号分割", required = true)List<String> ids){
		try{
			Params params = new Params();
			params.setIds(ids);
			timerTaskService.sfyxStop(ArrayUtils.stringArrayToLongArray(params.getIds().toArray(new String[params.getIds().size()])));
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}

}
