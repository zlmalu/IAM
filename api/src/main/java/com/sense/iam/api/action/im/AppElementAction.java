package com.sense.iam.api.action.im;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.text.StringEscapeUtils;
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
import com.sense.core.util.StringUtils;
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.api.model.im.AppDictionaryReq;
import com.sense.iam.api.model.im.AppElementModel;
import com.sense.iam.api.model.im.FuncModelReq;
import com.sense.iam.api.model.im.RelationInfoFuncTree;
import com.sense.iam.api.model.im.SearchFuncReq;
import com.sense.iam.api.util.AppFuncTreeUtil;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.im.App;
import com.sense.iam.model.im.AppDictionary;
import com.sense.iam.model.im.AppElement;
import com.sense.iam.model.im.AppFunc;
import com.sense.iam.model.im.AppFuncM;
import com.sense.iam.model.im.OrgType;
import com.sense.iam.service.AccountAppFuncService;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.AppDictionaryService;
import com.sense.iam.service.AppElementService;
import com.sense.iam.service.AppFuncService;
import com.sense.iam.service.AppService;
import com.sense.iam.service.JdbcService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiSort;

@Api(tags = "权限元素")
@Controller
@RestController
@RequestMapping("im/appElement")
@ApiSort(value = 1)
public class AppElementAction extends AbstractAction<OrgType,Long>{


	@Resource
	private AppService appService;

	@Resource
	private AppElementService appElementService;

	@Resource
	private AppDictionaryService appDictionaryService;


	@Resource
	private AppFuncService appFuncService;
	@Resource
	private AccountService accountService;

	@Resource
	private JdbcService jdbcService;
	@Resource
	private AccountAppFuncService accountAppFuncService;


	/**
	 * 分页查询元素列表
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	@ApiOperation(value="分页查询元素列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findList", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 4)
	protected PageList<AppElement> findListFuncType(@RequestBody AppElementModel entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="10") Integer limit){
		try{
			AppElement mo=new AppElement();
			mo.setName(StringEscapeUtils.escapeHtml4(entity.getName()));
			mo.setSn(StringEscapeUtils.escapeHtml4(entity.getSn()));
			mo.setIsLikeQuery(true);
			PageList<AppElement> list=appElementService.findPage(mo, page, limit);
			if(list!=null&&list.getDataList()!=null&&list.getDataList().size()>0){
				for(int i=0;i<list.getDataList().size();i++){
					//判断是否存在关联关系
					List<Map<String, Object>> listsd=jdbcService.findList("SELECT NAME FROM IM_APP WHERE MODEL_CONFIG LIKE '%"+list.getDataList().get(i).getId()+"%' AND COMPANY_SN='"+CurrentAccount.getCurrentAccount().getCompanySn()+"'");
					String appNames="暂无";
					if(listsd!=null&&listsd.size()>0){
						appNames="";
						for(int j=0;j<listsd.size();j++){
							if(j+1==listsd.size()){
								appNames+=listsd.get(j).get("NAME").toString();
							}else{
								appNames+=listsd.get(j).get("NAME").toString()+",";
							}
						}
					}
					list.getDataList().get(i).setAppNames(appNames);
				}
			}
			return list;
		}catch(Exception e){
			log.error("findListFuncType error:",e);
			return new PageList<AppElement>();
		}
	}


	@ApiOperation(value="新增元素")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody AppElement entity) {
		try{
			log.info("entity:"+entity);
			return appElementService.save(entity);
		}catch(Exception e){
			log.error("save error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}

	@ApiOperation(value="编辑元素")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode edit(@RequestBody AppElement entity) {
		try{
			log.info("entity:"+entity);
			return appElementService.edit(entity);
		}catch(Exception e){
			log.error("edit error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}

	@ApiOperation(value="移除元素")
	@RequestMapping(value="remove", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode remove(@RequestBody AppElement entity) {
		try{
			if(entity.getId()!=null){
				Long[] ids=new Long[1];
				ids[0]=entity.getId();
				return appElementService.removeByIds(ids);
			}else{
				return new ResultCode(Constants.OPERATION_FAIL);
			}
		}catch(Exception e){
			log.error("funcTypeRemove error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}








	@ApiOperation(value="新增权限")
	@RequestMapping(value="funcSave", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode funcFuncSave(@RequestBody AppFunc entity) {
		try{
			return appFuncService.save(entity);
		}catch(Exception e){
			log.error("save error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}

	@ApiOperation(value="编辑权限")
	@RequestMapping(value="funcEdit", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode funcFuncEdit(@RequestBody AppFunc entity) {
		try{
			return appFuncService.edit(entity);
		}catch(Exception e){
			log.error("save error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}

	@ApiOperation(value="移除权限")
	@RequestMapping(value="funcRemove", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode funcRemove(@RequestBody AppFunc entity) {
		try{
			if(entity.getId()!=null){
				Long[] ids=new Long[1];
				ids[0]=entity.getId();
				return appFuncService.removeByIds(ids);
			}else{
				return new ResultCode(Constants.OPERATION_FAIL);
			}
		}catch(Exception e){
			log.error("funcRemove error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}







	/**
	 * 查询权限元素列表
	 * @return
	 */
	@ApiOperation(value="查询权限元素列表")
	@RequestMapping(value="findFuncModelObject", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 4)
	protected FuncModelReq findFuncModelObject(@RequestBody AppElement entity){
		FuncModelReq resp=new FuncModelReq();
		try{
			if(entity.getAppId()==null){
				return resp;
			}
			App app=appService.findById(entity.getAppId());
			if(app!=null){
				resp.setAppId(entity.getAppId());
				resp.setAppName(app.getName());
				resp.setJsonData(app.getModelConfig());
				List<AppElement> list=appElementService.findAll();
				List<Map<String,Object>> fModel=new ArrayList<Map<String,Object>>();
				Iterator<AppElement> it=list.iterator();
				while (it.hasNext()) {
					AppElement element = (AppElement) it.next();
					//加入当前应用的元素
					if(element.getAppId().longValue()==entity.getAppId().longValue()){
						Map<String,Object> m=new HashMap<String, Object>();
						m.put("id", element.getId());
						m.put("name", element.getName());
						m.put("authType", element.getAuthType());
						m.put("type", "fmodel");
						m.put("ico", "el-icon-odometer");
						m.put("state", "warning");
						fModel.add(m);
						continue;
					}
					//加入元素范围为公有的元素
					if(element.getRange().intValue()==2){
						Map<String,Object> m=new HashMap<String, Object>();
						m.put("id", element.getId());
						m.put("name", element.getName());
						m.put("type", "fmodel");
						m.put("authType", element.getAuthType());
						m.put("ico", "el-icon-odometer");
						m.put("state", "warning");
						fModel.add(m);
						continue;
					}
				}
				resp.setfModel(fModel);
			}
		}catch(Exception e){
			log.error("findFuncModelObject error:",e);
			e.printStackTrace();
		}
		return resp;
	}

	/**
	 * 查询元素字典列表
	 * @return
	 */
	@ApiOperation(value="查询元素字典列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "objId", value = "元素ID", required = true, paramType="path", dataType = "Long"),
	})
	@RequestMapping(value="findFuncElementObject/{objId}", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 4)
	public AppDictionaryReq findFuncElementObject(@PathVariable Long objId){
		AppDictionaryReq resp=new AppDictionaryReq();
		resp.setAllList(new ArrayList<AppDictionary>());
		resp.setCheackList(new ArrayList<Long>());
		resp.setNode(new AppElement());
		try{
			resp.setAllList(appDictionaryService.findAll());
			if(objId!=null&&objId.longValue()!=0){
				AppElement appElement =appElementService.findById(objId);
				if(appElement!=null){
					resp.setNode(appElement);
					if(!StringUtils.isEmpty(appElement.getConfig())){
						String[] sd=appElement.getConfig().split(",");
						for(String value:sd){
							resp.getCheackList().add(Long.valueOf(value));
						}
					}
				}
			}
		}catch(Exception e){
			log.error("findFuncModelZdObject error:",e);
			e.printStackTrace();
		}
		return resp;
	}




	/**
	 * 获取权限列表-树形结构
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	@ApiOperation(value="获取权限列表-树形结构")
	@RequestMapping(value="findAppfuncListTree/{objId}", method=RequestMethod.POST)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "objId", value = "元素ID", required = true, paramType="path", dataType = "Long"),
	})
	@ResponseBody
	public List<AppFunc> findAppfuncListTree(@PathVariable Long objId){
		try{
			List<Map<String, Object>> list=jdbcService.findList("SELECT ID,SN,NAME,PARENT_ID,APP_ID,IS_DEFAULT,INFO,AUTH_TYPE,STATUS FROM IM_APP_FUNC WHERE APP_ID="+objId+" and  IS_DELETE=1 ORDER BY CREATE_TIME ASC");
			if(list!=null&&list.size()>0){
				List<AppFunc> newList=new ArrayList<AppFunc>();
				for(Map<String, Object> map:list){
					AppFunc appFunc=new AppFunc();
					appFunc.setId(Long.valueOf(map.get("ID").toString()));
					if(map.containsKey("SN")){
						appFunc.setSn(map.get("SN").toString());
					}
					if(map.containsKey("NAME")){
						appFunc.setName(map.get("NAME").toString());
						appFunc.setLabel(map.get("NAME").toString());
					}
					if(map.containsKey("APP_ID")){
						appFunc.setAppId(Long.valueOf(map.get("APP_ID").toString()));
					}
					if(map.containsKey("PARENT_ID")){
						appFunc.setParentId(Long.valueOf(map.get("PARENT_ID").toString()));
					}
					if(map.containsKey("IS_DEFAULT")){
						appFunc.setIsDefault(Integer.valueOf(map.get("IS_DEFAULT").toString()));
					}
					if(map.containsKey("INFO")){
						appFunc.setInfo(map.get("INFO").toString());
					}
					if(map.containsKey("AUTH_TYPE")&&map.get("AUTH_TYPE")!=null){
						appFunc.setAuthType(Integer.valueOf(map.get("AUTH_TYPE").toString()));
					}
					if(map.containsKey("STATUS")){
						appFunc.setStatus(Integer.valueOf(map.get("STATUS").toString()));
					}
					newList.add(appFunc);
				}
				newList=AppFuncTreeUtil.menuList(newList);
				return newList;
			}
			return new ArrayList<AppFunc>();
		}catch(Exception e){
			log.error("findAppfuncList error:",e);
			return new ArrayList<AppFunc>();
		}
	}


	/**
	 * 获取关联的权限列表-树形结构
	 * @param entity 查询对象
	 * @return
	 */
	@ApiOperation(value="获取关联的权限列表-树形结构")
	@RequestMapping(value="findAppfuncRelationInfoTree", method=RequestMethod.POST)
	@ResponseBody
	public RelationInfoFuncTree findAppfuncRelationInfoTree(@RequestBody SearchFuncReq searchFuncReq){
		RelationInfoFuncTree result=new RelationInfoFuncTree();
		try{
			if(searchFuncReq.getObjId()!=null&&searchFuncReq.getObjId().longValue()!=0){
				List<AppFunc> newList=new ArrayList<AppFunc>();
				//查询元素对象
				List<Map<String, Object>> rootList=jdbcService.findList("SELECT ID,SN,NAME,APP_ID FROM IM_APP_ELEMENT WHERE ID IN("+searchFuncReq.getIds()+") and COMPANY_SN='"+CurrentAccount.getCurrentAccount().getCompanySn()+"' and IS_DELETE=1");
				if(rootList!=null&&rootList.size()>0){
					for(Map<String, Object> map:rootList){
						AppFunc appFunc=new AppFunc();
						appFunc.setId(Long.valueOf(map.get("ID").toString()));
						if(map.containsKey("SN")){
							appFunc.setSn(map.get("SN").toString());
						}
						if(map.containsKey("NAME")){
							appFunc.setName(map.get("NAME").toString());
							appFunc.setLabel(map.get("NAME").toString());
						}
						if(map.containsKey("APP_ID")){
							appFunc.setAppId(Long.valueOf(map.get("APP_ID").toString()));
							appFunc.setParentId(Long.valueOf(map.get("APP_ID").toString()));
						}
						appFunc.setDisabled(true);
						newList.add(appFunc);
					}
					//获取当前权限已关联的ID集合
					List<Map<String, Object>> cheacklist=jdbcService.findList("SELECT DEST_FUNC_ID FROM IM_APP_FUNC_M WHERE SRC_FUNC_ID="+searchFuncReq.getObjId());
					if(cheacklist!=null&&cheacklist.size()>0){
						List<Long> idsc=new ArrayList<Long>();
						for(Map<String, Object> map:cheacklist){
							idsc.add(Long.valueOf(map.get("DEST_FUNC_ID").toString()));
						}
						result.setCheckedKeys(idsc);
					}
					//查询关联元素的对象集合
					List<Map<String, Object>> list=jdbcService.findList("SELECT ID,SN,NAME,PARENT_ID,APP_ID,IS_DEFAULT,INFO,AUTH_TYPE,STATUS FROM IM_APP_FUNC WHERE APP_ID in("+searchFuncReq.getIds()+") and  IS_DELETE=1 ORDER BY CREATE_TIME ASC");
					if(list!=null&&list.size()>0){
						for(Map<String, Object> map:list){
							AppFunc appFunc=new AppFunc();
							appFunc.setId(Long.valueOf(map.get("ID").toString()));
							if(map.containsKey("SN")){
								appFunc.setSn(map.get("SN").toString());
							}
							if(map.containsKey("NAME")){
								appFunc.setName(map.get("NAME").toString());
								appFunc.setLabel(map.get("NAME").toString());
							}
							if(map.containsKey("APP_ID")){
								appFunc.setAppId(Long.valueOf(map.get("APP_ID").toString()));
							}
							if(map.containsKey("PARENT_ID")){
								appFunc.setParentId(Long.valueOf(map.get("PARENT_ID").toString()));
							}
							if(map.containsKey("IS_DEFAULT")){
								appFunc.setIsDefault(Integer.valueOf(map.get("IS_DEFAULT").toString()));
							}
							if(map.containsKey("INFO")){
								appFunc.setInfo(map.get("INFO").toString());
							}
							if(map.containsKey("AUTH_TYPE")){
								appFunc.setAuthType(Integer.valueOf(map.get("AUTH_TYPE").toString()));
							}
							if(map.containsKey("STATUS")){
								appFunc.setStatus(Integer.valueOf(map.get("STATUS").toString()));
							}
							if(appFunc.getParentId().longValue()==appFunc.getAppId().longValue()){
								appFunc.setAppId(-1L);
							}
							newList.add(appFunc);
						}

						newList=AppFuncTreeUtil.menuList(newList);
						result.setElement(newList);
					}
				}
			}
		}catch(Exception e){
			log.error("findAppfuncRelationInfoTree error:",e);
			e.printStackTrace();
		}
		return result;
	}



	/**
	 * 获取权限的关联权限列表-树形结构
	 * @param entity 查询对象
	 * @return
	 */
	@ApiOperation(value="获取权限关联的权限列表-树形结构")
	@RequestMapping(value="findAppfuncRelationInfoByFuncTree", method=RequestMethod.POST)
	@ResponseBody
	public RelationInfoFuncTree findAppfuncRelationInfoByFuncTree(@RequestBody SearchFuncReq searchFuncReq){
		RelationInfoFuncTree result=new RelationInfoFuncTree();
		try{
			if(searchFuncReq.getObjId()!=null&&searchFuncReq.getObjId().longValue()!=0){

				String ids=null;
				//获取当前权限已关联的元素ID集合
				List<Map<String, Object>> elemenglist=jdbcService.findList("SELECT APP_ID FROM IM_APP_FUNC WHERE ID IN(SELECT DEST_FUNC_ID FROM IM_APP_FUNC_M WHERE SRC_FUNC_ID="+searchFuncReq.getObjId()+")");
				if(elemenglist!=null&&elemenglist.size()>0){
					for(Map<String, Object> map:elemenglist){
						if(ids==null){
							ids=map.get("APP_ID").toString();
						}else{
							ids+=","+map.get("APP_ID").toString();
						}
					}
				}
				if(ids==null){
					return result;
				}
				searchFuncReq.setIds(ids);
				List<AppFunc> newList=new ArrayList<AppFunc>();
				//查询元素对象
				List<Map<String, Object>> rootList=jdbcService.findList("SELECT ID,SN,NAME,APP_ID FROM IM_APP_ELEMENT WHERE ID IN("+searchFuncReq.getIds()+") and COMPANY_SN='"+CurrentAccount.getCurrentAccount().getCompanySn()+"' and IS_DELETE=1");
				if(rootList!=null&&rootList.size()>0){
					for(Map<String, Object> map:rootList){
						AppFunc appFunc=new AppFunc();
						appFunc.setId(Long.valueOf(map.get("ID").toString()));
						if(map.containsKey("SN")){
							appFunc.setSn(map.get("SN").toString());
						}
						if(map.containsKey("NAME")){
							appFunc.setName(map.get("NAME").toString());
							appFunc.setLabel(map.get("NAME").toString());
						}
						if(map.containsKey("APP_ID")){
							appFunc.setAppId(Long.valueOf(map.get("APP_ID").toString()));
							appFunc.setParentId(Long.valueOf(map.get("APP_ID").toString()));
						}
						appFunc.setDisabled(true);
						newList.add(appFunc);
					}
					//获取当前权限已关联的ID集合
					List<Map<String, Object>> cheacklist=jdbcService.findList("SELECT DEST_FUNC_ID FROM IM_APP_FUNC_M WHERE SRC_FUNC_ID="+searchFuncReq.getObjId());
					if(cheacklist!=null&&cheacklist.size()>0){
						List<Long> idsc=new ArrayList<Long>();
						for(Map<String, Object> map:cheacklist){
							idsc.add(Long.valueOf(map.get("DEST_FUNC_ID").toString()));
						}
						result.setCheckedKeys(idsc);
					}
					//查询关联元素的对象集合
					List<Map<String, Object>> list=jdbcService.findList("SELECT ID,SN,NAME,PARENT_ID,APP_ID,IS_DEFAULT,INFO,AUTH_TYPE,STATUS FROM IM_APP_FUNC WHERE APP_ID in("+searchFuncReq.getIds()+") and  IS_DELETE=1 ORDER BY CREATE_TIME ASC");
					if(list!=null&&list.size()>0){
						for(Map<String, Object> map:list){
							AppFunc appFunc=new AppFunc();
							appFunc.setId(Long.valueOf(map.get("ID").toString()));
							if(map.containsKey("SN")){
								appFunc.setSn(map.get("SN").toString());
							}
							if(map.containsKey("NAME")){
								appFunc.setName(map.get("NAME").toString());
								appFunc.setLabel(map.get("NAME").toString());
							}
							if(map.containsKey("APP_ID")){
								appFunc.setAppId(Long.valueOf(map.get("APP_ID").toString()));
							}
							if(map.containsKey("PARENT_ID")){
								appFunc.setParentId(Long.valueOf(map.get("PARENT_ID").toString()));
							}
							if(map.containsKey("IS_DEFAULT")){
								appFunc.setIsDefault(Integer.valueOf(map.get("IS_DEFAULT").toString()));
							}
							if(map.containsKey("INFO")){
								appFunc.setInfo(map.get("INFO").toString());
							}
							if(map.containsKey("AUTH_TYPE")){
								appFunc.setAuthType(Integer.valueOf(map.get("AUTH_TYPE").toString()));
							}
							if(map.containsKey("STATUS")){
								appFunc.setStatus(Integer.valueOf(map.get("STATUS").toString()));
							}
							if(appFunc.getParentId().longValue()==appFunc.getAppId().longValue()){
								appFunc.setAppId(-1L);
							}
							appFunc.setDisabled(true);
							newList.add(appFunc);
						}
						newList=AppFuncTreeUtil.menuList(newList);
						result.setElement(newList);
					}
				}
			}
		}catch(Exception e){
			log.error("findAppfuncRelationInfoByFuncTree error:",e);
			e.printStackTrace();
		}
		return result;
	}

}
