package com.sense.iam.api.action.im;

import java.util.*;

import javax.annotation.Resource;

import com.sense.iam.service.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sense.core.util.CurrentAccount;
import com.sense.core.util.StringUtils;
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.api.model.im.LoadAppfuncInfoModelDataObjectReq;
import com.sense.iam.api.model.im.LoadAppfuncInfoModelDataReq;
import com.sense.iam.api.model.im.SaveModelReq;
import com.sense.iam.api.model.im.SearchFuncReq;
import com.sense.iam.api.model.im.TopActive;
import com.sense.iam.api.model.im.TopData;
import com.sense.iam.api.model.im.TopHead;
import com.sense.iam.api.util.LoadAppFuncTreeUtil;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.im.Account;
import com.sense.iam.model.im.AccountAppFunc;
import com.sense.iam.model.im.App;
import com.sense.iam.model.im.AppFunc;
import com.sense.iam.model.im.AppFuncM;
import com.sense.iam.model.im.OrgType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiSort;

@Api(tags = "权限模型")
@Controller
@RestController
@RequestMapping("im/funcModel")
@ApiSort(value = 1)
public class FuncModelAction extends AbstractAction<OrgType,Long>{


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
	@Resource
	private PositionService positionService;




	@ApiOperation(value="关联权限")
	@RequestMapping(value="relationFunc", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode relationFunc(@RequestBody AccountAppFunc entity) {
		try{
			log.info("entity:"+entity.getAccountId());
			log.info("entity:"+entity.getAuthObj());
			if(entity.getAccountId()!=null&&entity.getAccountId().longValue()!=0){
				//移除老权限
				jdbcService.executeSql("DELETE FROM IM_APP_FUNC_M WHERE SRC_FUNC_ID="+entity.getAccountId());
				JSONArray acfData=entity.getAuthJsonObject().getJSONArray("active");
				for(int i=0;i<acfData.size();i++){
					AppFuncM model=new AppFuncM();
					model.setDestFuncId(Long.valueOf(acfData.getJSONObject(i).getString("funcId")));
					if(!StringUtils.isEmpty(acfData.getJSONObject(i).getString("radioTypeId"))){
						model.setDestZdType(Long.valueOf(acfData.getJSONObject(i).getString("radioTypeId")));
					}else{
						model.setDestZdType(0L);
					}
					model.setDestZdId(Long.valueOf(acfData.getJSONObject(i).getString("zdId")));
					model.setSrcFuncId(entity.getAccountId());
					appFuncService.saveRelationFuncsAdd(model);
				}
				return new ResultCode(Constants.OPERATION_SUCCESS);
			}
		}catch(Exception e){
			e.printStackTrace();
			log.error("relationFunc error:",e);
		}
		return new ResultCode(Constants.OPERATION_FAIL);
	}

	@ApiOperation(value="授权帐号权限")
	@RequestMapping(value="authFunc", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode authFunc(@RequestBody AccountAppFunc entity) {
		try{
			log.info("entity:"+entity.getAccountId());
			log.info("entity:"+entity.getAuthObj());
			return accountAppFuncService.save(entity);
		}catch(Exception e){
			log.error("authFunc error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}



	@ApiOperation(value="保存权限模型")
	@RequestMapping(value="saveModel", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode saveModel(@RequestBody SaveModelReq entity) {
		try{
			App app=appService.findById(entity.getAppId());
			if(app!=null){
				app.setModelConfig(entity.getJsonModelData());
				return appService.edit(app);
			}
		}catch(Exception e){
			log.error("edit error:",e);
		}
		return new ResultCode(Constants.OPERATION_FAIL);
	}


	/**
	 *
	 * @param entity 查询对象
	 * @return
	 */
	@ApiOperation(value="加载权限模型数据")
	@RequestMapping(value="loadAppfuncInfoModelData", method=RequestMethod.POST)
	@ResponseBody
	public List<LoadAppfuncInfoModelDataReq> loadAppfuncInfoModelData(@RequestBody SearchFuncReq searchFuncReq){
		List<LoadAppfuncInfoModelDataReq> resp=new ArrayList<LoadAppfuncInfoModelDataReq>();
		try{
			if(searchFuncReq.getObjId()!=null&&searchFuncReq.getObjId().longValue()!=0){
				//声明已勾选权限对象集合，用于下面判断
				Map<String, Boolean> maps=new HashMap<String, Boolean>();
				if(searchFuncReq.getAccountId()!=null&&searchFuncReq.getAccountId().longValue()!=0){
					//查询帐号管理权限表
					List<Map<String, Object>> acctFuncList=jdbcService.findList("SELECT AUTH_OBJ FROM IM_ACCOUNT_APP_FUNC WHERE ACCOUNT_ID="+searchFuncReq.getAccountId()+" and STATUS=1");
					for(Map<String, Object> map:acctFuncList){
						JSONArray data=JSONObject.fromObject(map.get("AUTH_OBJ").toString()).getJSONArray("active");
						for(int i=0;i<data.size();i++){
							//定义权限权限ID_字典ID格式
							String key=map.get("funcId").toString()+"_"+map.get("zdId").toString();
							maps.put(key, true);
						}
					}
				}

				//查询元素对象
				List<Map<String, Object>> rootList=jdbcService.findList("SELECT ID,SN,NAME,APP_ID,AUTH_TYPE,CONFIG FROM IM_APP_ELEMENT WHERE ID IN("+searchFuncReq.getIds()+") and COMPANY_SN='"+CurrentAccount.getCurrentAccount().getCompanySn()+"' and IS_DELETE=1");
				if(rootList!=null&&rootList.size()>0){
					for(Map<String, Object> map:rootList){
						LoadAppfuncInfoModelDataReq newload=new LoadAppfuncInfoModelDataReq();
						newload.setTabHead(new ArrayList<TopHead>());
						newload.setCheack(new ArrayList<Long>());
						newload.setData(new ArrayList<TopData>());
						newload.setId(Long.valueOf(map.get("ID").toString()));
						if(map.containsKey("NAME")){
							newload.setName(map.get("NAME").toString());
						}
						if(map.containsKey("AUTH_TYPE")){
							newload.setAuthType(Integer.valueOf(map.get("AUTH_TYPE").toString()));
						}
						//判断授权类型是否为矩阵或者混合来加载数据字典
						if(newload.getAuthType()!=null&&newload.getAuthType().intValue()==1||newload.getAuthType()!=null&&newload.getAuthType().intValue()==3){
							if(map.containsKey("CONFIG")){
								//查询元素关联的权限字典，放入TabHead集合中
								List<Map<String, Object>> zdList=jdbcService.findList("SELECT ID,SN,NAME,IS_ONE,PARENT_ID FROM IM_APP_DICTIONARY  WHERE PARENT_ID IN ("+map.get("CONFIG").toString()+") and COMPANY_SN='"+CurrentAccount.getCurrentAccount().getCompanySn()+"' and IS_DELETE=1 ORDER BY ID ASC");
								for(Map<String, Object> zdMap:zdList){
									TopHead top=new TopHead();
									top.setId(Long.valueOf(zdMap.get("ID").toString()));
									top.setLabel(zdMap.get("NAME").toString());
									top.setProperty(zdMap.get("ID").toString());
									top.setIsOne(zdMap.get("IS_ONE").toString());
									//字典类型ID
									top.setRadioName(zdMap.get("PARENT_ID").toString());
									top.setRadioVal("");
									newload.getTabHead().add(top);
								}
							}
						}

						//查询关联元素的对象集合
						List<Map<String, Object>> list=jdbcService.findList("SELECT ID,SN,NAME,PARENT_ID,APP_ID,IS_DEFAULT,INFO,AUTH_TYPE,STATUS FROM IM_APP_FUNC WHERE APP_ID ="+newload.getId()+" and  IS_DELETE=1 ORDER BY CREATE_TIME ASC");
						if(list!=null&&list.size()>0){
							for(Map<String, Object> mapFunc:list){
								TopData topData=new TopData();
								topData.setId(Long.valueOf(mapFunc.get("ID").toString()));
								if(mapFunc.containsKey("NAME")){
									topData.setName(mapFunc.get("NAME").toString());
								}
								if(mapFunc.containsKey("SN")){
									topData.setSn(mapFunc.get("SN").toString());
								}
								if(mapFunc.containsKey("PARENT_ID")){
									if(Long.valueOf(mapFunc.get("PARENT_ID").toString()).longValue()==Long.valueOf(mapFunc.get("APP_ID").toString()).longValue()){
										topData.setParentId(-1L);
									}else{
										topData.setParentId(Long.valueOf(mapFunc.get("PARENT_ID").toString()));
									}
								}else{
									topData.setParentId(-1L);
								}
								topData.setChildren(new ArrayList<TopData>());

								//判断存在权限字典
								if(newload.getTabHead().size()>0){
									topData.setRadioNames(new HashMap<String, Object>());
									Map<String, Object> tabHeadValue=new HashMap<String, Object>();
									//分组聚合radio
									Map<String, Object> radioNames=new HashMap<String, Object>();
									for(TopHead top:newload.getTabHead()){
										//判断权限有没有选中，//格式     权限ID_字典ID
										String cheackKey=topData.getId()+"_"+top.getId();
										if(maps.containsKey(cheackKey)){
											tabHeadValue.put(top.getProperty(), true);
										}else{
											tabHeadValue.put(top.getProperty(), false);
										}
										//字典类型+字典类型+数据ID
										String key=top.getRadioName()+"_"+topData.getId();
										if(!topData.getRadioNames().containsKey(key)){
											radioNames.put(key, key);
										}
									}
									topData.setRadioNames(radioNames);
									topData.setTabHeadValue(tabHeadValue);
								}else{
									//判断权限有没有选中，//格式     权限ID_权限ID,用于树形权限勾选
									String cheackKey=topData.getId()+"_"+topData.getId();
									if(maps.containsKey(cheackKey)){
										newload.getCheack().add(topData.getId());
										topData.setCheack(true);
									}

								}
								newload.getData().add(topData);

							}
						}


						newload.setData(LoadAppFuncTreeUtil.menuList(newload.getData()));
						resp.add(newload);
					}
				}
			}
		}catch(Exception e){
			log.error("loadAppfuncInfoModelData error:",e);
			e.printStackTrace();
		}
		return resp;
	}



	/**
	 *
	 * @param entity 查询对象
	 * @return
	 */
	@ApiOperation(value="加载权限模型数据-根据帐号唯一标识")
	@RequestMapping(value="loadAppfuncInfoModelDataByAccountId", method=RequestMethod.POST)
	@ResponseBody
	public LoadAppfuncInfoModelDataObjectReq loadAppfuncInfoModelDataByAccountId(@RequestBody SearchFuncReq searchFuncReq){
		List<LoadAppfuncInfoModelDataReq> resp=new ArrayList<LoadAppfuncInfoModelDataReq>();
		LoadAppfuncInfoModelDataObjectReq respData=new LoadAppfuncInfoModelDataObjectReq();
		respData.setAuthActive(new ArrayList<TopActive>());
		Map<Long, Boolean> mapsda=new HashMap<Long, Boolean>();
		try{

			if(searchFuncReq.getAccountId()!=null&&searchFuncReq.getAccountId().longValue()!=0){
				//声明已勾选权限对象集合，用于下面判断
				Map<String, Boolean> maps=new HashMap<String, Boolean>();
				Account account=accountService.findById(searchFuncReq.getAccountId());
				App app=appService.findById(account.getAppId());
				if(!StringUtils.isEmpty(app.getModelConfig())){
					try{
						JSONObject modelJson=app.getModelJson();
						String ids=null;
						if(modelJson.containsKey("nodeList")){
							JSONArray data=modelJson.getJSONArray("nodeList");
							JSONArray lineList=modelJson.getJSONArray("lineList");
							String elemengId=null;
							Iterator<Object> o = data.iterator();
							while (o.hasNext()) {
							    JSONObject jo = (JSONObject) o.next();
							    if("user".equals(jo.getString("type"))){
							    	elemengId=jo.getString("id");
							        o.remove();
							    }
							}
							if(elemengId==null){
								log.info("当前应用："+app.getName()+"不存在授权对象");
								respData.setModel(resp);
								return respData;
							}
							for(int i=0;i<data.size();i++){
								String cheackRgObj="{\"from\":\""+ data.getJSONObject(i).getString("id")+"";
								//检查当前授权元素是否存在关联其他元素
		                        if(lineList.toString().indexOf(cheackRgObj)!=-1){
		                        	mapsda.put(Long.valueOf(data.getJSONObject(i).getString("id")), true);
		                        }else{
		                        	mapsda.put(Long.valueOf(data.getJSONObject(i).getString("id")), false);
		                        }
							}
							for(int i=0;i<data.size();i++){
		                        //检测当前授权对象和授权元素的连接线
		                        String cheackObj="{\"from\":\""+ elemengId+"\",\"to\":\""+data.getJSONObject(i).getString("id")+"\"}";
								if(lineList.toString().indexOf(cheackObj)!=-1){
									if(ids==null){
										ids=data.getJSONObject(i).getString("id");
									}else{
										ids+=","+data.getJSONObject(i).getString("id");
									}
								}
							}
							if(StringUtils.isEmpty(ids)){
								log.info("当前应用："+app.getName()+"不存在权限元素");
								respData.setModel(resp);
								return respData;
							}
							searchFuncReq.setIds(ids);
							searchFuncReq.setObjId(app.getId());
						}else{
							log.info("当前应用："+app.getName()+"不存在权限元素");
						}
					}catch(Exception e){
						e.printStackTrace();
						log.error("当前应用："+app.getName()+"权限模型格式转换异常");
						respData.setModel(resp);
						return respData;
					}
				}else{
					log.info("当前应用："+app.getName()+"不存在权限模型");
					respData.setModel(resp);
					return respData;
				}

				//查询帐号管理权限表
				List<Map<String, Object>> acctFuncList=jdbcService.findList("SELECT AUTH_OBJ FROM IM_ACCOUNT_APP_FUNC WHERE ACCOUNT_ID="+searchFuncReq.getAccountId()+" and STATUS=1");
				for(Map<String, Object> map:acctFuncList){
					JSONArray data=JSONObject.fromObject(map.get("AUTH_OBJ").toString()).getJSONArray("active");
					for(int i=0;i<data.size();i++){
						TopActive topActive=new TopActive();
						topActive.setFuncId(data.getJSONObject(i).getString("funcId"));
						topActive.setZdId(data.getJSONObject(i).getString("zdId"));
						if(data.getJSONObject(i).containsKey("radioTypeId")){
							topActive.setRadioTypeId(data.getJSONObject(i).getString("radioTypeId"));
						}else{
							topActive.setRadioTypeId("");
						}
						respData.getAuthActive().add(topActive);
						//定义权限权限ID_字典ID格式

						String key=data.getJSONObject(i).getString("funcId")+"_"+data.getJSONObject(i).getString("zdId");
						maps.put(key, true);
					}
				}

				//查询元素对象
				List<Map<String, Object>> rootList=jdbcService.findList("SELECT ID,SN,NAME,APP_ID,AUTH_TYPE,CONFIG FROM IM_APP_ELEMENT WHERE ID IN("+searchFuncReq.getIds()+") and COMPANY_SN='"+CurrentAccount.getCurrentAccount().getCompanySn()+"' and IS_DELETE=1");
				if(rootList!=null&&rootList.size()>0){
					for(Map<String, Object> map:rootList){
						LoadAppfuncInfoModelDataReq newload=new LoadAppfuncInfoModelDataReq();
						newload.setTabHead(new ArrayList<TopHead>());
						newload.setCheack(new ArrayList<Long>());
						newload.setData(new ArrayList<TopData>());
						newload.setId(Long.valueOf(map.get("ID").toString()));
						newload.setIsRelation(mapsda.get(newload.getId())==null?false:mapsda.get(newload.getId()));
						if(map.containsKey("NAME")){
							newload.setName(map.get("NAME").toString());
						}
						if(map.containsKey("AUTH_TYPE")){
							newload.setAuthType(Integer.valueOf(map.get("AUTH_TYPE").toString()));
						}
						//判断授权类型是否为矩阵或者混合来加载数据字典
						if(newload.getAuthType()!=null&&newload.getAuthType().intValue()==1||newload.getAuthType()!=null&&newload.getAuthType().intValue()==3){
							if(map.containsKey("CONFIG")){
								//查询元素关联的权限字典，放入TabHead集合中
								List<Map<String, Object>> zdList=jdbcService.findList("SELECT ID,SN,NAME,IS_ONE,PARENT_ID FROM IM_APP_DICTIONARY  WHERE PARENT_ID IN ("+map.get("CONFIG").toString()+") and COMPANY_SN='"+CurrentAccount.getCurrentAccount().getCompanySn()+"' and IS_DELETE=1 ORDER BY ID ASC");
								for(Map<String, Object> zdMap:zdList){
									TopHead top=new TopHead();
									top.setId(Long.valueOf(zdMap.get("ID").toString()));
									top.setLabel(zdMap.get("NAME").toString());
									top.setProperty(zdMap.get("ID").toString());
									top.setIsOne(zdMap.get("IS_ONE").toString());
									//字典类型ID
									top.setRadioName(zdMap.get("PARENT_ID").toString());
									top.setRadioVal("");
									newload.getTabHead().add(top);
								}
							}
						}

						//查询关联元素的对象集合
						List<Map<String, Object>> list=jdbcService.findList("SELECT ID,SN,NAME,PARENT_ID,APP_ID,IS_DEFAULT,INFO,AUTH_TYPE,STATUS FROM IM_APP_FUNC WHERE APP_ID ="+newload.getId()+" and  IS_DELETE=1 ORDER BY CREATE_TIME ASC");
						if(list!=null&&list.size()>0){
							for(Map<String, Object> mapFunc:list){
								TopData topData=new TopData();
								topData.setId(Long.valueOf(mapFunc.get("ID").toString()));
								if(mapFunc.containsKey("NAME")){
									topData.setName(mapFunc.get("NAME").toString());
								}
								if(mapFunc.containsKey("SN")){
									topData.setSn(mapFunc.get("SN").toString());
								}
								if(mapFunc.containsKey("PARENT_ID")){
									if(Long.valueOf(mapFunc.get("PARENT_ID").toString()).longValue()==Long.valueOf(mapFunc.get("APP_ID").toString()).longValue()){
										topData.setParentId(-1L);
									}else{
										topData.setParentId(Long.valueOf(mapFunc.get("PARENT_ID").toString()));
									}
								}else{
									topData.setParentId(-1L);
								}
								topData.setChildren(new ArrayList<TopData>());
								//设置关联权限
								topData.setRelationFunc(new ArrayList<TopData>());
								//判断存在权限字典
								if(newload.getTabHead().size()>0){
									topData.setRadioNames(new HashMap<String, Object>());
									Map<String, Object> tabHeadValue=new HashMap<String, Object>();
									//分组聚合radio，key=字典类型+数据ID
									Map<String, Object> radioNames=new HashMap<String, Object>();
									for(TopHead top:newload.getTabHead()){
										//字典类型+数据ID
										String key=top.getRadioName()+"_"+topData.getId();
										//判断权限有没有选中，//格式     权限ID_字典ID
										String cheackKey=topData.getId()+"_"+top.getId();
										if(maps.containsKey(cheackKey)){
											//判断是否单点 2==单选
											if(top.getIsOne()!=null&&top.getIsOne().equals("2")){
												radioNames.remove(key);
												String value=top.getRadioName()+"_"+top.getProperty()+"_"+topData.getId();
												radioNames.put(key, value);
											}
											tabHeadValue.put(top.getProperty(), true);
										}else{
											tabHeadValue.put(top.getProperty(), false);
											//判断是否单点 2==单选
											if(top.getIsOne()!=null&&top.getIsOne().equals("2")){
												if(!radioNames.containsKey(key)){
													radioNames.put(key, key);
												}
											}
										}

									}
									topData.setRadioNames(radioNames);
									topData.setTabHeadValue(tabHeadValue);
								}else{
									//判断权限有没有选中，//格式     权限ID_权限ID,用于树形权限勾选
									List list1 = jdbcService.findList("select APP_FUNC_ID from IM_POSITION_APP_FUNC where APP_FUNC_ID =" + topData.getId());
									if(list1.size()>0){
										newload.getCheack().add(topData.getId());
										topData.setCheack(true);
									}
								}
								newload.getData().add(topData);
							}
						}
						newload.setData(LoadAppFuncTreeUtil.menuList(newload.getData()));
						resp.add(newload);
					}
				}
			}
		}catch(Exception e){
			log.error("loadAppfuncInfoModelDataByAccountId error:",e);
			e.printStackTrace();
		}
		respData.setModel(resp);
		return respData;
	}



	/**
	 *
	 * @param entity 查询对象
	 * @return
	 */
	@ApiOperation(value="加载权限关联权限模型数据-根据权限唯一标识和元素ID集合")
	@RequestMapping(value="loadAppfuncInfoModelDataByFuncId", method=RequestMethod.POST)
	@ResponseBody
	public LoadAppfuncInfoModelDataObjectReq loadAppfuncInfoModelDataByFuncId(@RequestBody SearchFuncReq searchFuncReq){
		List<LoadAppfuncInfoModelDataReq> resp=new ArrayList<LoadAppfuncInfoModelDataReq>();
		LoadAppfuncInfoModelDataObjectReq respData=new LoadAppfuncInfoModelDataObjectReq();
		respData.setAuthActive(new ArrayList<TopActive>());
		Map<Long, Boolean> mapsda=new HashMap<Long, Boolean>();
		try{
			if(searchFuncReq.getObjId()!=null&&searchFuncReq.getObjId().longValue()!=0){
				//是否存在元素ID集合，如果没有则根据权限ID去加载关联关系
				if(StringUtils.isEmpty(searchFuncReq.getIds())){
					//查询当前权限所属的元素ID
					List<Map<String, Object>> element=jdbcService.findList("SELECT APP_ID FROM IM_APP_FUNC WHERE ID="+searchFuncReq.getObjId()+" and IS_DELETE=1");
					String ids=null;
					for(Map<String, Object> map:element){
						if(ids==null){
							ids=map.get("APP_ID").toString();
						}
					}
					if(ids==null)ids="";

					//查询帐号关联的应用模型,获取当前帐号关联应用模型数据
					List<Map<String, Object>> modelView=jdbcService.findList("SELECT MODEL_CONFIG FROM IM_APP WHERE ID IN (SELECT APP_ID FROM IM_ACCOUNT WHERE ID="+searchFuncReq.getAccountId()+") and COMPANY_SN='"+CurrentAccount.getCurrentAccount().getCompanySn()+"'");
					//预览模型传入accountId为应用ID
					if(modelView!=null&&modelView.size()==0){
						modelView=jdbcService.findList("SELECT MODEL_CONFIG FROM IM_APP WHERE ID ="+searchFuncReq.getAccountId()+" and COMPANY_SN='"+CurrentAccount.getCurrentAccount().getCompanySn()+"'");
					}
					if(modelView!=null&&modelView.size()>0){
						//获取模型的元素的连接关系
						JSONArray data=JSONObject.fromObject(modelView.get(0).get("MODEL_CONFIG").toString()).getJSONArray("lineList");
						String newids=null;
						for(int i=0;i<data.size();i++){
							//判断当前元素
							if(data.getJSONObject(i).getString("from").equals(ids)){
								if(newids==null){
									//获取关联的元素ID
									newids=data.getJSONObject(i).getString("to");
								}else{
									newids+=","+data.getJSONObject(i).getString("to");
								}
							}
						}
						searchFuncReq.setIds(newids);
					}
				}
				if(StringUtils.isEmpty(searchFuncReq.getIds())){
					log.info("查询当前权限关联权限所属的元素ID，权限ID："+searchFuncReq.getObjId());
					return respData;
				}
				//声明已勾选权限对象集合，用于下面判断
				Map<String, Boolean> maps=new HashMap<String, Boolean>();
				//查询当前权限已关联的权限表
				List<Map<String, Object>> acctFuncList=jdbcService.findList("SELECT DEST_FUNC_ID,DEST_ZD_ID,DESC_ZD_TYPE FROM IM_APP_FUNC_M WHERE SRC_FUNC_ID="+searchFuncReq.getObjId()+"");
				for(Map<String, Object> map:acctFuncList){
					TopActive topActive=new TopActive();
					topActive.setFuncId(map.get("DEST_FUNC_ID").toString());
					topActive.setZdId(map.get("DEST_ZD_ID").toString());
					topActive.setRadioTypeId(map.get("DESC_ZD_TYPE").toString());

					respData.getAuthActive().add(topActive);
					//定义权限权限ID_字典ID格式
					String key=topActive.getFuncId()+"_"+topActive.getZdId();
					maps.put(key, true);

				}

				//查询元素对象
				List<Map<String, Object>> rootList=jdbcService.findList("SELECT ID,SN,NAME,APP_ID,AUTH_TYPE,CONFIG FROM IM_APP_ELEMENT WHERE ID IN("+searchFuncReq.getIds()+") and COMPANY_SN='"+CurrentAccount.getCurrentAccount().getCompanySn()+"' and IS_DELETE=1");
				if(rootList!=null&&rootList.size()>0){
					for(Map<String, Object> map:rootList){
						LoadAppfuncInfoModelDataReq newload=new LoadAppfuncInfoModelDataReq();
						newload.setTabHead(new ArrayList<TopHead>());
						newload.setCheack(new ArrayList<Long>());
						newload.setData(new ArrayList<TopData>());
						newload.setId(Long.valueOf(map.get("ID").toString()));
						newload.setIsRelation(mapsda.get(newload.getId())==null?false:mapsda.get(newload.getId()));
						if(map.containsKey("NAME")){
							newload.setName(map.get("NAME").toString());
						}
						if(map.containsKey("AUTH_TYPE")){
							newload.setAuthType(Integer.valueOf(map.get("AUTH_TYPE").toString()));
						}
						//判断授权类型是否为矩阵或者混合来加载数据字典
						if(newload.getAuthType()!=null&&newload.getAuthType().intValue()==1||newload.getAuthType()!=null&&newload.getAuthType().intValue()==3){
							if(map.containsKey("CONFIG")){
								//查询元素关联的权限字典，放入TabHead集合中
								List<Map<String, Object>> zdList=jdbcService.findList("SELECT ID,SN,NAME,IS_ONE,PARENT_ID FROM IM_APP_DICTIONARY  WHERE PARENT_ID IN ("+map.get("CONFIG").toString()+") and COMPANY_SN='"+CurrentAccount.getCurrentAccount().getCompanySn()+"' and IS_DELETE=1 ORDER BY ID ASC");
								for(Map<String, Object> zdMap:zdList){
									TopHead top=new TopHead();
									top.setId(Long.valueOf(zdMap.get("ID").toString()));
									top.setLabel(zdMap.get("NAME").toString());
									top.setProperty(zdMap.get("ID").toString());
									top.setIsOne(zdMap.get("IS_ONE").toString());
									//字典类型ID
									top.setRadioName(zdMap.get("PARENT_ID").toString());
									top.setRadioVal("");
									newload.getTabHead().add(top);
								}
							}
						}

						//查询关联元素的对象集合
						List<Map<String, Object>> list=jdbcService.findList("SELECT ID,SN,NAME,PARENT_ID,APP_ID,IS_DEFAULT,INFO,AUTH_TYPE,STATUS FROM IM_APP_FUNC WHERE APP_ID ="+newload.getId()+" and  IS_DELETE=1 ORDER BY CREATE_TIME ASC");
						if(list!=null&&list.size()>0){
							for(Map<String, Object> mapFunc:list){
								TopData topData=new TopData();
								topData.setId(Long.valueOf(mapFunc.get("ID").toString()));
								if(mapFunc.containsKey("NAME")){
									topData.setName(mapFunc.get("NAME").toString());
								}
								if(mapFunc.containsKey("SN")){
									topData.setSn(mapFunc.get("SN").toString());
								}
								if(mapFunc.containsKey("PARENT_ID")){
									if(Long.valueOf(mapFunc.get("PARENT_ID").toString()).longValue()==Long.valueOf(mapFunc.get("APP_ID").toString()).longValue()){
										topData.setParentId(-1L);
									}else{
										topData.setParentId(Long.valueOf(mapFunc.get("PARENT_ID").toString()));
									}
								}else{
									topData.setParentId(-1L);
								}
								topData.setChildren(new ArrayList<TopData>());
								//设置关联权限
								topData.setRelationFunc(new ArrayList<TopData>());
								//判断存在权限字典
								if(newload.getTabHead().size()>0){
									topData.setRadioNames(new HashMap<String, Object>());
									Map<String, Object> tabHeadValue=new HashMap<String, Object>();
									//分组聚合radio，key=字典类型+数据ID
									Map<String, Object> radioNames=new HashMap<String, Object>();
									for(TopHead top:newload.getTabHead()){
										//字典类型+数据ID
										String key=top.getRadioName()+"_"+topData.getId();
										//判断权限有没有选中，//格式     权限ID_字典ID
										String cheackKey=topData.getId()+"_"+top.getId();
										if(maps.containsKey(cheackKey)){
											//判断是否单点 2==单选
											if(top.getIsOne()!=null&&top.getIsOne().equals("2")){
												radioNames.remove(key);
												String value=top.getRadioName()+"_"+top.getProperty()+"_"+topData.getId();
												radioNames.put(key, value);
											}
											tabHeadValue.put(top.getProperty(), true);
										}else{
											tabHeadValue.put(top.getProperty(), false);
											//判断是否单点 2==单选
											if(top.getIsOne()!=null&&top.getIsOne().equals("2")){
												if(!radioNames.containsKey(key)){
													radioNames.put(key, key);
												}
											}
										}

									}
									topData.setRadioNames(radioNames);
									topData.setTabHeadValue(tabHeadValue);
								}else{
									//判断权限有没有选中，//格式     权限ID_权限ID,用于树形权限勾选
									String cheackKey=topData.getId()+"_"+topData.getId();
									if(maps.containsKey(cheackKey)){
										newload.getCheack().add(topData.getId());
										topData.setCheack(true);
									}
								}
								newload.getData().add(topData);
							}
						}
						newload.setData(LoadAppFuncTreeUtil.menuList(newload.getData()));
						resp.add(newload);
					}
				}
			}
		}catch(Exception e){
			log.error("loadAppfuncInfoModelDataByFuncId error:",e);
			e.printStackTrace();
		}
		respData.setModel(resp);
		return respData;
	}
}
