package com.sense.iam.api.action.sys;


import java.util.Date;
import java.util.List;








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
import com.sense.core.util.SMSUtil;
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.sys.SmsDefine;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiSort;

@Api(tags = "短信接口管理")
@Controller
@RestController
@RequestMapping("sys/smsdefine")
@ApiSort(value = 32)
public class SmsDefineAction extends AbstractAction<SmsDefine,Long>{

	
	@ApiOperation(value="新增")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody SmsDefine entity) {
		return super.save(entity);
	}
	
	@ApiOperation(value="更新")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	public ResultCode edit(@RequestBody SmsDefine entity) {
		entity.setCreateTime(new Date());
		return super.edit(entity);
	}
	
	
	
	
	@ApiOperation(value="查询所有")
	@RequestMapping(value="findAll", method=RequestMethod.GET)
	@ResponseBody
	public Object findAll() {
		return super.findAll(new SmsDefine());
	}
	
	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findById/{id}", method=RequestMethod.GET)
	@ResponseBody
	public SmsDefine findById(@PathVariable Long id) {
		return super.findById(id);
	}
	
	/**
	 * 分页查询对象列表
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	@ApiOperation(value="分页查询")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findList", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 4)
	protected PageList<SmsDefine> findList(@RequestBody SmsDefine entity1,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			SmsDefine entity=new SmsDefine();
			entity.setSn(StringEscapeUtils.escapeHtml4(entity1.getSn()));
			entity.setIsLikeQuery(true);
			return getBaseService().findPage(entity,page,limit);
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<SmsDefine>();
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
	
	
	@ApiOperation(value="测试短信源接口")
	@RequestMapping(value="testInterface", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode testInterface(@RequestBody SmsDefine model) {
		boolean flag = SMSUtil.test(model.getMobile(), model.getConnecttimeout(), model.getReadtimeout(), model.getAccesskeyid(), model.getAccesskeysecret(), model.getProduct(), model.getDomain(), model.getSignname(), model.getTemCode());
		if(flag){
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}else{
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
}
