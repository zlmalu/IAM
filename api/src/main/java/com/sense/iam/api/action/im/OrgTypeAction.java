package com.sense.iam.api.action.im;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sense.core.util.CurrentAccount;
import com.sense.core.util.ExcelUtils;
import com.sense.core.util.PageList;
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.api.model.im.OrgTypeReq;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.im.OrgType;
import com.sense.iam.model.sys.Field;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.OrgTypeService;
import com.sense.iam.service.SysFieldService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiSort;

@Api(tags = "组织类型")
@Controller
@RestController
@RequestMapping("im/orgType")
@ApiSort(value = 1)
public class OrgTypeAction extends AbstractAction<OrgType,Long>{
	@Resource
	private OrgTypeService orgTypeService;
	@Resource
	private SysFieldService sysFieldService;
	
	@ApiOperation(value="新增")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody OrgTypeReq entity) {
		OrgType orgType=entity.getOrgType();
		ResultCode code=super.save(orgType);
		if(code.getSuccess()){
			code.setMsg(orgType.getId()+"");
		}
		return code;
	}
	
	@ApiOperation(value="更新")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	public ResultCode edit(@RequestBody OrgTypeReq entity) {
		OrgType orgType =entity.getOrgType();
		return super.edit(orgType);
	}
	
	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findById/{id}", method=RequestMethod.GET)
	@ResponseBody
	public OrgType findById(@PathVariable Long id) {
		return super.findById(id);
	}
	
	@ApiOperation(value="查询所有组织类型")
	@RequestMapping(value="findAll", method=RequestMethod.GET)
	@ResponseBody
	public List<OrgType> findAll() {
		return orgTypeService.findAll();
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
	protected PageList<OrgType> findList(@RequestBody OrgTypeReq entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			OrgType p=entity.getOrgType();
			p.setIsLikeQuery(true);
			return getBaseService().findPage(p,page,limit);
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<OrgType>();
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
	
	@SuppressWarnings("rawtypes")
	@ApiOperation(value="模板下载")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="downTemplate/{id}",method = RequestMethod.GET)
	@ResponseBody
	public void downTemplate(@PathVariable Long id){
		Field entity=new Field();
		entity.setObjId(id);
		List<Field> userFields=sysFieldService.findList(entity);
		List<ExcelUtils.ExcelModel> models=new ArrayList<ExcelUtils.ExcelModel>();
		for (Field field : userFields) {
			models.add(new ExcelUtils.ExcelModel(field.getRemark(),"",5000));
		}
		super.exportXlsx("org", new ArrayList(), models,orgTypeService.findById(id).getName());
	}
	
	
	

}
