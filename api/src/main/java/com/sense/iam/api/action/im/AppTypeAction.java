package com.sense.iam.api.action.im;

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
import com.sense.iam.api.model.im.AppTypeReq;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.im.AppType;
import com.sense.iam.service.AppTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiSort;

@Api(tags = "应用类型")
@Controller
@RestController
@RequestMapping("im/appType")
@ApiSort(value = 6)
public class AppTypeAction extends AbstractAction<AppType,Long>{
	
	@Resource
	private AppTypeService appTypeService;

	@ApiOperation(value="新增")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody AppTypeReq entity) {
		AppType appType=entity.getAppType();
		ResultCode code=super.save(appType);
		if(code.getSuccess()){
			code.setMsg(appType.getId()+"");
		}
		return code;
	}
	
	
	@ApiOperation(value="更新")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	public ResultCode edit(@RequestBody AppTypeReq entity) {
		return super.edit(entity.getAppType());
	}
	
	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findById/{id}", method=RequestMethod.GET)
	@ResponseBody
	public AppType findById(@PathVariable Long id) {
		return super.findById(id);
	}
	
	@ApiOperation(value="查询所有应用类型")
	@RequestMapping(value="findAll", method=RequestMethod.GET)
	@ResponseBody
	public Object findAll() {
		return super.findAll(new AppType());
	}
	
	
	/**
	 * 分页查询列表
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	@ApiOperation(value="分页查询列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findList", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 4)
	protected PageList<AppType> findList(@RequestBody AppTypeReq entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			AppType p=entity.getAppType();
			p.setIsLikeQuery(true);
			return getBaseService().findPage(p,page,limit);
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<AppType>();
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
