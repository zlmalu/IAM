package com.sense.iam.api.action.sys;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.sense.iam.api.model.sys.MessageReq;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.sys.Message;
import com.sense.iam.service.MessageService;
import com.sense.iam.websocket.WebSocket;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiSort;

@Api(tags = "系统消息")
@Controller
@RestController
@RequestMapping("sys/message")
@ApiSort(value = 35)
public class MessageAction extends AbstractAction<Message, Long>{
	@Resource
	private MessageService messageService;
	@Resource
	private WebSocket webSocket;
	
	@ApiOperation(value="新增",code=0)
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody MessageReq entity) {
		try {
			super.save(entity.getMessage());
			webSocket.send("1");
			return new ResultCode(Constants.OPERATION_SUCCESS);
		} catch (Exception e) {
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
	@ApiOperation(value="更新")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	public ResultCode edit(@RequestBody MessageReq entity) {
		try {
			super.edit(entity.getMessage());
			webSocket.send("2");
			return new ResultCode(Constants.OPERATION_SUCCESS);
		} catch (Exception e) {
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findById/{id}", method=RequestMethod.GET)
	@ResponseBody
	public Message findById(@PathVariable Long id) {
		return super.findById(id);
	}
	
	@ApiOperation(value="移除")
    @RequestMapping(value="remove",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 3)
    public ResultCode remove(@RequestBody @ApiParam(name="唯一标识集合",value="多数据采用英文逗号分割",required=true)List<String> ids) {
		try {
			Params params=new Params();
			params.setIds(ids);
			super.remove(params);
			webSocket.send("2");
			return new ResultCode(Constants.OPERATION_SUCCESS);
		} catch(Exception e) {
			return new ResultCode(Constants.OPERATION_FAIL);
		}
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
	protected PageList<Message> findList(@RequestBody MessageReq entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			Message message = entity.getMessage();
			message.setIsLikeQuery(true);
			return getBaseService().findPage(message,page,limit);
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<Message>();
		}
	}
}
