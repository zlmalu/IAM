package com.sense.iam.api.action.sys;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sense.core.util.CurrentAccount;
import com.sense.core.util.PageList;
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.api.model.sys.FieldReq;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.sys.Field;
import com.sense.iam.service.JdbcService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiParam;

@Api(tags = "平台自定义字段模块")
@Controller
@RestController
@RequestMapping("sys/field")
public class FieldAction extends AbstractAction<Field ,Long> {


	@ApiOperation(value="字段复制")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "objId", value = "对象ID", required = true, paramType="path", dataType = "Long"),
		 @ApiImplicitParam(name = "copyObjId", value = "被复制对象ID", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="copyField/{objId}/{copyObjId}", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode copyField(@PathVariable Long objId,@PathVariable Long copyObjId) {
		try{
			//查询当前的字段列表
			Field field=new Field();
			field.setObjId(objId);
			field.setIsLikeQuery(true);
			List<Field> list1= getBaseService().findList(field);
			
			//查询拷贝的字段列表
			field=new Field();
			field.setObjId(copyObjId);
			field.setIsLikeQuery(true);
			List<Field> list2= getBaseService().findList(field);
			
			//检查字段是否存在，存在则进行复制
			if(list2!=null&&list2.size()>0&&list1!=null&&list1.size()>0){
				for(Field newfield:list2){
					boolean isUpdate=false;
					Long id=null;
					for(Field newfield1:list1){
						//判断两个字段是否相等，相等则进行更新
						if(newfield.getName().equals(newfield1.getName())){
							isUpdate=true;
							id=newfield1.getId();
							break;
						}
					}
					if(isUpdate){
						Field cnewfield=newfield;
						cnewfield.setId(id);
						cnewfield.setObjId(objId);
						super.edit(cnewfield);
					}else{
						Field cnewfield=newfield;
						cnewfield.setId(null);
						cnewfield.setObjId(objId);
						super.save(cnewfield);
					}
				}
			}
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			e.printStackTrace();
			return new ResultCode(Constants.OPERATION_UNKNOWN,e.getMessage());
		}
	}
	
	@Resource
	private JdbcService jdbcService;
	@ApiOperation(value="新增")
	@ApiImplicitParams({
	  	  @ApiImplicitParam(name = "objId", value = "对象id： 组织类型id、用户类型id、帐号策略id", required = true, paramType="path", dataType = "Long")
		})
	@RequestMapping(value="save/{objId}", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode save(@RequestBody FieldReq entity, @PathVariable Long objId) {
		Field f = new Field();
		f.setName(entity.getName());
		f.setObjId(objId);
		JSONObject fromObject = JSONObject.fromObject(super.findList(f, 0, 10));
		if(fromObject.has("dataList")){
			if(fromObject.getString("dataList").length()>2){
				return new ResultCode(Constants.OPERATION_FAIL,"字段名已存在");
			}
		}
		Field field =entity.getField();
		field.setObjId(objId);
		return super.save(field);
	}
	
	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findById/{id}", method=RequestMethod.GET)
	@ResponseBody
	public Field findById(@PathVariable Long id) {
		return super.findById(id);
	}
	
	@ApiOperation(value="更新")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode edit(@RequestBody FieldReq entity) {
		Field field=entity.getField();
		return super.edit(field);
	}
	
	
	/**
	 * 根据对象标识获取所有扩展字段
	 * @return
	 */
	@ApiOperation(value="根据对象标识获取所有扩展字段")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "objId", value = "对象id： 组织类型id、用户类型id、帐号策略id", required = true, paramType="path", dataType = "Long"),
	})
	@RequestMapping(value="findListByObjId/{objId}", method=RequestMethod.GET)
	@ResponseBody
	public List<Field> findList(@PathVariable Long objId){
		try{
			Field field=new Field();
			field.setObjId(objId);
			field.setIsLikeQuery(true);
			List<Field> list= getBaseService().findList(field);
			if(list!=null&&list.size()>0){
				for(Field fields:list){
					//select组件，判断NAME是否等于SYS_APPROVE_ID和是否下拉框类型
					if("SYS_APPROVE_ID".equals(fields.getName())&& "select".equals(fields.getInputType())){
						String defaultValue=null;
						//查询管理员表
						List<Map<String, Object>> accMap=jdbcService.findList("SELECT ID,NAME FROM SYS_ACCT WHERE COMPANY_SN='"+CurrentAccount.getCurrentAccount().getCompanySn()+"'");
						String compant="";
						for(Map<String, Object> map:accMap){
							
							if(defaultValue==null){
								defaultValue=map.get("ID").toString();
							}
							compant+=map.get("ID").toString()+"="+map.get("NAME").toString()+",";
						}
						//去掉最后一个逗号
						if(compant.length()>0){
							compant=compant.substring(0,compant.length()-1);
						}
						//设置默认值，默认值为查询表第一条记录ID
						if(defaultValue!=null){
							fields.setDefaultValue(defaultValue);
						}
						fields.setCompant(compant);
					}
				}
			}
			return list;
		}catch(Exception e){
			log.error("findList error:",e);
			return new ArrayList<Field>();
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
		@ApiImplicitParam(name = "objId", value = "对象id： 组织类型id、用户类型id、帐号策略id", required = true, paramType="path", dataType = "Long"),
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findList/{objId}", method=RequestMethod.POST)
	@ResponseBody
	public PageList<Field> findList(@RequestBody FieldReq entity,@PathVariable Long objId,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			Field field=entity.getField();
			field.setObjId(objId);
			field.setIsLikeQuery(true);
			return getBaseService().findPage(field,page,limit);
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<Field>();
		}
	}
	
	@ApiOperation(value="移除")
    @RequestMapping(value="remove",method = RequestMethod.POST)
	@ResponseBody
    public ResultCode remove(@RequestBody @ApiParam(name="唯一标识集合",value="多数据采用英文逗号分割",required=true)List<String> ids) {
		Params params=new Params();
		params.setIds(ids);
		return super.remove(params);
    }
}


