package com.sense.iam.api.action.im;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.springframework.web.multipart.MultipartFile;

import com.sense.core.util.ArrayUtils;
import com.sense.core.util.ExcelUtils;
import com.sense.core.util.PageList;
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.api.model.im.EquipmentReq;
import com.sense.iam.api.model.im.EquipmentUserReq;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.im.Equipment;
import com.sense.iam.model.im.EquipmentRelationUser;
import com.sense.iam.model.im.EquipmentType;
import com.sense.iam.model.im.EquipmentUser;
import com.sense.iam.model.im.User;
import com.sense.iam.model.im.UserRelation;
import com.sense.iam.model.sys.Field;
import com.sense.iam.service.EquipmentService;
import com.sense.iam.service.EquipmentTypeService;
import com.sense.iam.service.SysFieldService;
import com.sense.iam.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiSort;

@Api(tags = "设备管理")
@Controller
@RestController
@RequestMapping("im/equipment")
@ApiSort(value = 3)
public class EquipmentAction extends AbstractAction<Equipment,Long>{
	
	@Resource
	private EquipmentService equipmentService;
	@Resource
	private SysFieldService sysFieldService;
	@Resource
	private UserService userService;
	@Resource
	private EquipmentTypeService equipmentTypeService;
	
	@ApiOperation(value="新增")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody EquipmentReq entity) {
		Equipment equipment=entity.getEquipment();
		ResultCode code=super.save(equipment);
		if(code.getSuccess()){
			code.setMsg(equipment.getId()+"");
		}
		return code;
	}
	
	@ApiOperation(value="更新")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	public ResultCode edit(@RequestBody EquipmentReq entity) {
		return super.edit(entity.getEquipment());
	}
	
	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findById/{id}", method=RequestMethod.GET)
	@ResponseBody
	@ApiOperationSupport(order = 6)
	public Equipment findById(@PathVariable Long id) {
		return super.findById(id);
	}
	
	
	@ApiOperation(value="查询所有用户类型")
	@RequestMapping(value="findAll", method=RequestMethod.GET)
	@ResponseBody
	@ApiOperationSupport(order = 5)
	public Object findAll() {
		return super.findAll(new Equipment());
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
	
	/**
	 * 分页查询用户列表
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	@ApiOperation(value="分页查询用户列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findList", method=RequestMethod.POST)
	@ResponseBody
	protected PageList<Equipment> findList(@RequestBody EquipmentReq entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			Equipment p = entity.getEquipment();
			p.setIsLikeQuery(true);
			return getBaseService().findPage(p,page,limit);
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<Equipment>();
		}
	}
	
	@ApiOperation(value="查询对应用户名称")
    @RequestMapping(value="finUserNameList",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 4)
    public Object finUserNameList(@RequestBody @ApiParam(name="唯一标识集合",value="多数据采用英文逗号分割",required=true)List<String> ids) {
		List list = new ArrayList();
		for (String id : ids) {
			User user = userService.findById(Long.parseLong(id));
			if(null!=user){
				list.add(user);
			}
		}
		return list;
    }
	
	@ApiOperation(value="修改设备状态")
    @RequestMapping(value="updateStatus/{status}",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 4)
    public Object updateStatus(@RequestBody Params params,@PathVariable Integer status) {
		try {
			Long[] ids = ArrayUtils.stringArrayToLongArray(params.getIds().toArray(new String[params.getIds().size()]));
			for (Long id : ids) {
				Equipment equipment = new Equipment();
				equipment.setId(id);
				equipment.setStatus(status);
				equipmentService.updateStatus(equipment);
			}
			return new ResultCode(Constants.OPERATION_SUCCESS); 
		} catch (Exception e) {
			return new ResultCode(Constants.OPERATION_FAIL); 
		}
    }
	
	@ApiOperation(value="设备导入")
    @RequestMapping(value="import",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 4)
	public boolean importUser(@ApiParam(name="file",value="导入内容",required=false) @RequestParam(required=false,name="file",value="file") MultipartFile file){
 		List<EquipmentType> equipmentTypes = equipmentTypeService.findAll();
		Map<String,String> colM=new HashMap<String,String>();
		for (EquipmentType equipmentType : equipmentTypes) {
			Field eField=new Field();
			eField.setObjId(equipmentType.getId());
			List<Field> fields=sysFieldService.findList(eField);
			colM.clear();
			for (Field field : fields) {
				colM.put(field.getRemark(),field.getName());
			}
			try {
				List list=ExcelUtils.parseToMap(file.getInputStream(), equipmentType.getRemark(), colM);
				if(list != null) {
					equipmentService.importEquipment(list, equipmentType.getId());
					return true;					
				}
			} catch (IOException e) {
				log.error("parse excel exception",e);
				return false;
			}
		}
		return true;
	}
	
	@ApiOperation(value = "设备导出")
 	@RequestMapping(value = "export", method = RequestMethod.POST)
 	@ResponseBody
 	public void export(@RequestBody EquipmentReq entity) {
 		try {
	 		Equipment equipment = entity.getEquipment();
	 		PageList<Equipment> pageList = getBaseService().findPage(equipment,0, 50000);
	 		//循环生成打印模型
	 		List<ExcelUtils.ExcelModel> models = new ArrayList<ExcelUtils.ExcelModel>();
	 		models.add(new ExcelUtils.ExcelModel("设备编号", "sn",5000));
	 		models.add(new ExcelUtils.ExcelModel("设备标识","token",5000));
			models.add(new ExcelUtils.ExcelModel("设备来源","source",5000));
			models.add(new ExcelUtils.ExcelModel("设备状态","status",5000));
			models.add(new ExcelUtils.ExcelModel("设备类型","equipmentTypeName",5000));
			models.add(new ExcelUtils.ExcelModel("设备管理员ID","userIds",5000));
			super.exportXlsx("equipment", pageList.getDataList(), models, "equipmentList");
	 	} catch (Exception e) {
	 		log.error("export user error", e);
	 	}
	}
	

	@ApiOperation(value="获取帐号已关联用户")
    @RequestMapping(value="findEquipmentUserList",method = RequestMethod.POST)
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认10页",example="20")
	})
	@ResponseBody
	public PageList findEquipmentUserList(@RequestBody EquipmentUser eu,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="10") Integer limit){
		eu.setIsLikeQuery(true);
		return equipmentService.findEquipmentUserList(eu,page,limit);
	}
	
	
	/**
	 * 分页查询关联用户列表
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	@ApiOperation(value="分页查询关联用户列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认10页",example="10")
	})
	@RequestMapping(value="findEquipmentUserPage", method=RequestMethod.POST)
	@ResponseBody
	protected PageList findEquipmentUserPage(@RequestBody EquipmentRelationUser entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="10") Integer limit){
		try{
			if(entity.ok().getSuccess()){
				entity.setIsLikeQuery(true);
				return userService.findEquipmentListByRelationUser(entity, page, limit);
			}else{
				PageList list=new PageList<>();
				return list;
			}
		}catch(Exception e){
			log.error("findEquipmentUserPage error:",e);
			return new PageList<>();
		}
	}
	
	@ApiOperation(value="帐号关联用户-累加")
    @RequestMapping(value="saveEquipmentUserList",method = RequestMethod.POST)
	@ResponseBody
	public ResultCode saveEquipmentUserList(@RequestBody EquipmentUserReq equipmentUserReq){
		try{
			equipmentService.saveEquipmentUsers(equipmentUserReq.getEquipmentIds(),equipmentUserReq.getUserIds());
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			e.printStackTrace();
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
	
	@ApiOperation(value="取消帐号关联用户")
    @RequestMapping(value="removeEquipmentUserList",method = RequestMethod.POST)
	@ResponseBody
	public ResultCode removeEquipmentUserList(@RequestBody EquipmentUserReq equipmentUserReq){
		try{
			equipmentService.removeEquipmentUsers(equipmentUserReq.getEquipmentIds(),equipmentUserReq.getUserIds());
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			e.printStackTrace();
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
}
