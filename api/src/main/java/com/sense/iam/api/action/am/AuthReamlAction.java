package com.sense.iam.api.action.am;

import java.util.List;

import javax.annotation.Resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiSort;

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
import com.sense.iam.api.model.am.AuthReamlReq;
import com.sense.iam.api.model.am.BindAppReq;
import com.sense.iam.cache.CacheSender;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.am.AuthReaml;


@Api(tags = "MFA管理-MFA实例")
@Controller
@RestController
@RequestMapping("am/authReaml")
@ApiSort(value = 15)
public class AuthReamlAction extends AbstractAction<AuthReaml,Long>{

	@Resource
	protected CacheSender cacheSender;
	
	@ApiOperation(value="新增")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody AuthReamlReq entity) {
		AuthReaml authReaml=entity.getAuthReaml();
		ResultCode code=super.save(authReaml);
		if(code.getSuccess()){
			cacheSender.sendMsg(authReaml);
			code.setMsg(authReaml.getId()+"");
		}
		return code;
	}
	
	
	@ApiOperation(value="更新")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	public ResultCode edit(@RequestBody AuthReamlReq entity) {
		AuthReaml authReaml=entity.getAuthReaml();
		ResultCode r=super.edit(authReaml);
		cacheSender.sendMsg(authReaml);
		return r;
	}
	
	@ApiOperation(value="更新状态")
	@RequestMapping(value="changeStatus", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	public ResultCode changeStatus(@RequestBody AuthReamlReq entity) {
		AuthReaml authReaml=entity.getAuthReaml();
		ResultCode r=amauthreamlservice.changeStatus(authReaml);
		cacheSender.sendMsg(authReaml);
		return r;
	}

	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findById/{id}", method=RequestMethod.GET)
	@ResponseBody
	public AuthReaml findById(@PathVariable Long id) {
		return super.findById(id);
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
	protected PageList<AuthReaml> findList(@RequestBody AuthReamlReq entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			AuthReaml authReaml=entity.getAuthReaml();
			authReaml.setIsLikeQuery(true);
			return getBaseService().findPage(authReaml,page,limit);
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<AuthReaml>();
		}
	}
	
	
	
	@Resource
	private com.sense.iam.service.AmAuthReamlService amauthreamlservice;
	
	
	@ApiOperation(value="授权APP")
	@RequestMapping(value="bindApp", method=RequestMethod.POST)
	@ResponseBody
	public Object bindApp(@RequestBody BindAppReq entity){
		try{
			amauthreamlservice.bindApp(entity.getAppId(), entity.getReamlId());
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			log.error("save error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	@ApiOperation(value="取消授权APP")
	@RequestMapping(value="unBindApp", method=RequestMethod.POST)
	@ResponseBody
	public Object unBindApp(@RequestBody BindAppReq entity){
		try{
			amauthreamlservice.unBindApp(entity.getAppId(), entity.getReamlId());
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			log.error("save error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
	@ApiOperation(value="根据应用查询MFA实例")
	@RequestMapping(value="findreamlByAppId", method=RequestMethod.POST)
	@ResponseBody
	public PageList<AuthReaml> findreamlByAppId(@RequestBody AuthReamlReq entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		AuthReaml authReaml=entity.getAuthReaml();
		authReaml.setIsLikeQuery(true);
		log.info("authReaml sn:"+authReaml.getSn());
		log.info("authReaml name:"+authReaml.getName());
		log.info("authReaml AppId:"+authReaml.getAppId());
		return amauthreamlservice.findreamlByAppId(authReaml, page, limit);
	}

	
}
