package com.sense.iam.api.action.im;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.web.multipart.MultipartFile;

import com.sense.core.util.ExcelUtils;
import com.sense.core.util.PageList;
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.api.model.im.AppFuncReq;
import com.sense.iam.api.model.sys.FieldReq;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.im.Account;
import com.sense.iam.model.im.App;
import com.sense.iam.model.im.AppElement;
import com.sense.iam.model.im.AppFunc;
import com.sense.iam.model.im.AppFuncM;
import com.sense.iam.model.im.OrgAppFunc;
import com.sense.iam.model.sys.Field;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.AppDictionaryService;
import com.sense.iam.service.AppElementService;
import com.sense.iam.service.AppFuncService;
import com.sense.iam.service.AppService;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.SysFieldService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.sf.json.JSONObject;

@Api(tags = "应用权限管理")
@Controller
@RestController
@RequestMapping("im/appFunc")
public class AppFuncAction  extends  AbstractAction<AppFunc,Long>{

	@Resource
	private AccountService accountService;
	
	@Resource
	private AppFuncService appFuncService;
	
	@Resource
	private AppElementService appElementService;
	
	@Resource
	private AppService appService;
	
	@Resource
	private SysFieldService sysFieldService;
	@Resource
	private JdbcService jdbcService;
	
	@Resource
	private AppDictionaryService appDictionaryService;
	
	/**
	 * 分页查询应用列表
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	@ApiOperation(value="查询应用权限列表")
	@RequestMapping(value="findList", method=RequestMethod.POST)
	@ResponseBody
	protected List findList(@RequestBody AppFuncReq entity){
		try{
			if(entity.getParentId()==null){//如果上级ID为空，则从帐号中读取appId作为加载上级ID
				
				if(entity.getAcctId()==null){//如果帐号ID为空，则返回空列表，代表非帐号授权查询
					return new ArrayList();
				}
				Account account=accountService.findById(entity.getAcctId());
				entity.setParentId(account.getAppId());//此处设置上级ID为APPID则获取的是授权类型的ID
				entity.setStatus(1);
				entity.setFuncType(1L);
			}
			return getBaseService().findList(entity.toAppFunc());
		}catch(Exception e){
			log.error("findList error:",e);
			return new ArrayList();
		}
	}
	
	/**
	 * 根据id获取权限列表
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	@ApiOperation(value="根据id获取权限列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "id", value = "应用id，权限id", required = true, paramType="path", dataType = "Long"),
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findListById/{id}", method=RequestMethod.POST)
	@ResponseBody
	public PageList<AppFunc> findListById(@RequestBody AppFuncReq entity, @PathVariable Long id, @RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			entity.setParentId(id);
			return getBaseService().findPage(entity.toAppFunc(),page,limit);
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<AppFunc>();
		}
	}
	
	@ApiOperation(value="新增")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "id", value = "应用id，权限id", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="save/{id}", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode save(@RequestBody AppFuncReq entity, @PathVariable Long id) {
		entity.setParentId(id);
		return super.save(entity.toAppFunc());
	}
	
	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findById/{id}", method=RequestMethod.GET)
	@ResponseBody
	public AppFunc findById(@PathVariable Long id) {
		return super.findById(id);
	}
	
	@ApiOperation(value="更新")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode edit(@RequestBody AppFuncReq entity) {
		return super.edit(entity.toAppFunc());
	}
	
	@ApiOperation(value="移除")
    @RequestMapping(value="remove",method = RequestMethod.POST)
	@ResponseBody
    public ResultCode remove(@RequestBody @ApiParam(name="唯一标识集合",value="多数据采用英文逗号分割",required=true)List<String> ids) {
		Params params=new Params();
		params.setIds(ids);
		return super.remove(params);
    }
	
	/**
	 * 根据ID获取绑定的其他功能
	 * @return
	 */
	@ApiOperation(value="关联权限--根据ID获取绑定的其他功能")
    @RequestMapping(value="findAppFuncsById/{id}",method = RequestMethod.POST)
	@ResponseBody
	public PageList findAppFuncsById(@PathVariable Long id){
		PageList pageList=new PageList(10000,1);
		List list=appFuncService.findBindAppFuncsById(id);
		if(list!=null){
			pageList.setTotalcount(list.size());
			pageList.setDataList(list);
		}
		return pageList;
	}
	
	/**
	 * 保存应用功能和应用权限的映射关系
	 * @param id
	 * @param params
	 * @return
	 */
	@ApiOperation(value="关联权限--保存应用功能和应用权限的映射关系")
    @RequestMapping(value="saveRelationFuncsById/{id}",method = RequestMethod.POST)
	@ResponseBody
    public Object saveRelationFuncsById(@PathVariable Long id,@RequestBody Params params) {
		//组装OrgAppFunc对象集合
		List<AppFuncM> adds=new ArrayList<AppFuncM>();
		List<AppFuncM> dels=new ArrayList<AppFuncM>();
		AppFuncM appFuncM;
		if(params.getIds()!=null){
			for (String afId : params.getIds()) {
				if(afId==null)continue;
				appFuncM=new AppFuncM();
				appFuncM.setSrcFuncId(id);
				appFuncM.setDestFuncId(Long.valueOf(afId));
				adds.add(appFuncM);
			}
		}
		if(params.getOldIds()!=null){
			for (String afId : params.getOldIds()) {
				if(afId==null)continue;
				appFuncM=new AppFuncM();
				appFuncM.setSrcFuncId(id);
				appFuncM.setDestFuncId(Long.valueOf(afId));
				dels.add(appFuncM);
			}
		}
		appFuncService.saveRelationFuncsById(id,adds, dels);
		return new ResultCode(Constants.OPERATION_SUCCESS);
	}

	/**
	 * 关联组织--根据ID获取绑定的组织机构
	 * @return
	 */
	@ApiOperation(value="关联组织--根据ID获取绑定的组织机构")
    @RequestMapping(value="findOrgsById/{id}",method = RequestMethod.POST)
	@ResponseBody
	public PageList findOrgsById(Long id){
		PageList pageList=new PageList(10000,1);
		List list=appFuncService.findBindOrgById(id);
		if(list!=null){
			pageList.setTotalcount(list.size());
			pageList.setDataList(list);
		}
		return pageList;
	}
	

	/**
	 * 关联组织--保存组织机构和应用权限的映射关系
	 * @param id
	 * @param params
	 * @return
	 */
	@ApiOperation(value="关联组织--保存组织机构和应用权限的映射关系")
    @RequestMapping(value="saveOrgsById/{id}",method = RequestMethod.POST)
	@ResponseBody
	public Object saveOrgsById(@PathVariable Long id,@RequestBody Params params){
		//组装OrgAppFunc对象集合
		List<OrgAppFunc> adds=new ArrayList<OrgAppFunc>();
		List<OrgAppFunc> dels=new ArrayList<OrgAppFunc>();
		OrgAppFunc orgAppFunc;
		if(params.getIds()!=null){
			for (String orgId : params.getIds()) {
				if(orgId==null)continue;
				orgAppFunc=new OrgAppFunc();
				orgAppFunc.setAppFuncId(id);
				orgAppFunc.setOrgId(Long.valueOf(orgId));
				adds.add(orgAppFunc);
			}
		}
		if(params.getOldIds()!=null){
			for (String orgId : params.getOldIds()) {
				if(orgId==null)continue;
				orgAppFunc=new OrgAppFunc();
				orgAppFunc.setAppFuncId(id);
				orgAppFunc.setOrgId(Long.valueOf(orgId));
				dels.add(orgAppFunc);
			}
		}
		appFuncService.saveOrgAppFuncs(id,adds, dels);
		return new ResultCode(Constants.OPERATION_SUCCESS);
	}
	
	@ApiOperation(value="应用权限导入")
	@RequestMapping(value="importAppFunc/{appId}", method = RequestMethod.POST)
	@ResponseBody
	public boolean importAppFunc(@PathVariable Long appId, @ApiParam(name="file",value="导入内容",required=false) @RequestParam(required=false,name="file",value="file") MultipartFile file) {
		try {
			App appentity = appService.findById(appId);
			Map<String,String> colM=new HashMap<String,String>();
			colM.put("权限编号","SN");
			colM.put("权限名称","NAME");
			colM.put("上级编号","PARENT_ID");
			colM.put("默认授权","IS_DEFAULT");
			colM.put("权限状态","STATUS");
			colM.put("权限类型编号","TYPE");
			colM.put("权限描述", "INFO");
			List list=ExcelUtils.parseToMapDeft(file.getInputStream(), colM);
			appFuncService.importFunc(appId, list);
		} catch (Exception e) {
			log.error("import acct error",e);
			return false;
		}
		return true;
	}
	
	
	@ApiOperation(value="应用权限导出")
	@RequestMapping(value="export", method = RequestMethod.POST)
	@ResponseBody
	public void export(@RequestBody AppFuncReq entity) {         
		try {
			List<Map<String, Object>> list = jdbcService.findList("SELECT a.FUNC_TYPE,a.INFO,a.SN,a.NAME,b.SN AS PARENT_SN,b.NAME AS PARENT_NAME,a.IS_DEFAULT,a.APP_ID,a.STATUS,c.SN AS FUNC_TYPE_SN,c.NAME AS FUNC_TYPE_NAME,a.CREATE_TIME FROM IM_APP_FUNC a LEFT JOIN IM_APP_FUNC b on a.PARENT_ID=b.ID  LEFT JOIN IM_APP_FUNC c on a.FUNC_TYPE=c.ID  WHERE a.APP_ID="+entity.getId()+" ORDER BY a.FUNC_TYPE,a.ID ASC");
			if(list!=null&&list.size()>0){
				for(int i=0;i<list.size();i++){
					if(Long.valueOf(list.get(i).get("FUNC_TYPE").toString()).intValue()==1){
						list.get(i).remove("FUNC_TYPE_SN");
						list.get(i).remove("FUNC_TYPE_NAME");
						list.get(i).remove("PARENT_SN");
						list.get(i).remove("PARENT_NAME");
						list.get(i).put("FUNC_TYPE_SN", "无");
						list.get(i).put("FUNC_TYPE_NAME", "权限类型");
						list.get(i).put("PARENT_SN", "无");
						list.get(i).put("PARENT_NAME", "无");
					}	
				}
			}
			
			super.exportXlsxSheet("应用权限导出表", list, Arrays.asList(new ExcelUtils.ExcelModel[]{
					new ExcelUtils.ExcelModel("权限编号","SN",5000),
					new ExcelUtils.ExcelModel("权限名称","NAME",5000),
					new ExcelUtils.ExcelModel("上级编号","PARENT_SN",5000),
					new ExcelUtils.ExcelModel("上级名称","PARENT_NAME",5000),
					new ExcelUtils.ExcelModel("默认授权","IS_DEFAULT",5000,new HashMap(){
						{
							put("1","是");
							put("2","否");
						}
					}),
					new ExcelUtils.ExcelModel("权限状态","STATUS",5000,new HashMap(){
						{
							put("1","启用");
							put("2","禁用");
						}
					}),
					new ExcelUtils.ExcelModel("权限类型编号","FUNC_TYPE_SN",5000),
					new ExcelUtils.ExcelModel("权限类型名称","FUNC_TYPE_NAME",5000),
					new ExcelUtils.ExcelModel("权限描述","INFO",12000)
			}),appService.findById(entity.getId()).getName());
			
		} catch (Exception e) {
			log.error("import acct error",e);
		}
	}		
}
