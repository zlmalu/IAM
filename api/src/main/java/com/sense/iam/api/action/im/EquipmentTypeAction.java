package com.sense.iam.api.action.im;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sense.core.util.ExcelUtils;
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.api.model.im.EquipmentTypeReq;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.im.EquipmentType;
import com.sense.iam.model.sys.Field;
import com.sense.iam.service.SysFieldService;
import com.sense.iam.service.EquipmentTypeService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiSort;

@Api(tags = "设备类型")
@Controller
@RestController
@RequestMapping("im/equipmentType")
@ApiSort(value = 3)
public class EquipmentTypeAction extends AbstractAction<EquipmentType,Long>{
	
	@Resource
	private EquipmentTypeService equipmentTypeService;
	@Resource
	private SysFieldService sysFieldService;
	
	@ApiOperation(value="新增")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody EquipmentTypeReq entity) {
		EquipmentType equipmentType=entity.getEquipmentType();
		ResultCode code=super.save(equipmentType);
		if(code.getSuccess()){
			code.setMsg(equipmentType.getId()+"");
		}
		return code;
	}
	
	@ApiOperation(value="更新")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	public ResultCode edit(@RequestBody EquipmentTypeReq entity) {
		return super.edit(entity.getEquipmentType());
	}
	
	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findById/{id}", method=RequestMethod.GET)
	@ResponseBody
	@ApiOperationSupport(order = 6)
	public EquipmentType findById(@PathVariable Long id) {
		return super.findById(id);
	}
	
	
	@ApiOperation(value="查询所有用户类型")
	@RequestMapping(value="findAll", method=RequestMethod.GET)
	@ResponseBody
	@ApiOperationSupport(order = 5)
	public Object findAll() {
		return super.findAll(new EquipmentType());
	}
	
	@ApiOperation(value="移除")
    @RequestMapping(value="remove",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 4)
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
		List<Field> fields=sysFieldService.findList(entity);
		List<ExcelUtils.ExcelModel> models=new ArrayList<ExcelUtils.ExcelModel>();
		for (Field field : fields) {
			if(!field.getName().equals("EQUIPMENT_SOURCE")){
				models.add(new ExcelUtils.ExcelModel(field.getRemark(),"",5000));
			}
		}
		super.exportXlsx("equipment", new ArrayList(), models,equipmentTypeService.findById(id).getRemark());
	}
	
}
