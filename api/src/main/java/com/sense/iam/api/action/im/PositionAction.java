package com.sense.iam.api.action.im;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiSort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.springframework.web.multipart.MultipartFile;

import com.sense.core.util.ArrayUtils;
import com.sense.core.util.ExcelUtils;
import com.sense.core.util.PageList;
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.api.model.PosTreeNode;
import com.sense.iam.api.model.PostionFuncTree;
import com.sense.iam.api.model.im.PositionReq;
import com.sense.iam.api.model.im.PositionTreeNode;
import com.sense.iam.api.model.im.UserReq;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.im.App;
import com.sense.iam.model.im.AppElement;
import com.sense.iam.model.im.AppFunc;
import com.sense.iam.model.im.AppType;
import com.sense.iam.model.im.Position;
import com.sense.iam.model.im.PostionFuncModel;
import com.sense.iam.model.im.User;
import com.sense.iam.service.AppElementService;
import com.sense.iam.service.AppFuncService;
import com.sense.iam.service.AppService;
import com.sense.iam.service.AppTypeService;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.PositionService;

@Api(tags = "岗位管理")
@Controller
@RestController
@RequestMapping("im/position")
@ApiSort(value = 5)
public class PositionAction extends AbstractAction<Position,Long>{


	@Resource
	private PositionService positionService;

	@Resource
	private AppTypeService appTypeService;

	@Resource
	private AppService appService;

	@Resource
	private AppFuncService appFuncService;

	@Resource
	private AppElementService appElementService;

	/**
	 * 加载应用管理树
	 * @param type 加载类型  当加载类型为1时加载应用，2加载表应用功能
	 * @param id 加载上级ID，
	 * @positionId 当加载应用类型时为-1或null，加载应用时应用类型id，加载应用权限时为应用id
	 * @return
	 */
	@ApiOperation(value="加载应用管理树")
	@SuppressWarnings("unchecked")
	@RequestMapping(value="loadNode",method=RequestMethod.POST)
	@ResponseBody
	public List<PosTreeNode> loadNode(@RequestBody PositionTreeNode treenode){
		List<PosTreeNode> list=new ArrayList<PosTreeNode>();
		PosTreeNode treeNode;
		//type null-1为应用类型，1为应用，2为权限
		if(treenode.getType()==null || treenode.getType()==-1){
			List<AppType> appTypeList=appTypeService.findAll();
			List<Map<String, Object>> rootList=jdbcService.findList("SELECT iat.ID FROM im_position_app ipa,im_app ia,im_app_type iat WHERE ia.ID = ipa.APP_ID AND ia.APP_TYPE_ID = iat.ID AND ipa.POSITION_ID = " + treenode.getPositionId());
			for (AppType appType : appTypeList) {
				treeNode=new PosTreeNode();
				treeNode.setId(appType.getId().toString());
				treeNode.setText(appType.getName());
				treeNode.getAttrMap().put("type", "1");
				treeNode.setPid(0L);
				treeNode.setType(1);
				treeNode.setChecked(false);
				for(Map<String, Object> map : rootList){
					if(map.get("id").equals(appType.getId())){
						treeNode.setChecked(true);
						continue;
					}
				}
				list.add(treeNode);
			}
		}else if(treenode.getType().intValue()==1){
			App sApp=new App();
			sApp.setAppTypeId(treenode.getId());
			List<App> appList=appService.findList(sApp);
			Set<Long> authApps=treenode.getPositionId()==null?new HashSet<Long>():positionService.findAppIds(treenode.getPositionId());
			for (App app : appList) {
				treeNode=new PosTreeNode();
				treeNode.setId(app.getId().toString());
				treeNode.setText(app.getSn()+"-"+app.getName());
//				treeNode.setIconCls("application_list");
				treeNode.setChecked(authApps.contains(app.getId()));
				treeNode.setPid(1L);
				treeNode.setType(2);
				treeNode.getAttrMap().put("type", "2");
				list.add(treeNode);
			}
		}
		//查询app下细粒度权限
		else if(treenode.getType().intValue()==2){
			App app = appService.findById(treenode.getId());
			if(null!=app){
				AppElement appElement = new AppElement();
				appElement.setAppId(app.getId());
				List<AppElement> findList = appElementService.findList(appElement);
				Set<Long> authAppFunc=treenode.getPositionId()==null?new HashSet<Long>():positionService.findAppFuncIds(treenode.getPositionId());
				for (AppElement appEl : findList) {
					treeNode=new PosTreeNode();
					treeNode.setId(appEl.getId().toString());
					treeNode.setText(appEl.getName());
					treeNode.setChecked(authAppFunc.contains(appEl.getId()));
					treeNode.setPid(appEl.getId());
					treeNode.setType(3);
					treeNode.getAttrMap().put("type", "3");
					list.add(treeNode);
				}
			}


	/*		AppFunc entity = new AppFunc();
			entity.setAppId(treenode.getId());
			List<AppFunc> appFuncList = appFuncService.findList(entity);
			//加载权限关联表权限
			appFuncList.addAll(appFuncService.findBindAppFuncsByAppId(treenode.getId()));
			Set<Long> authAppFuncs=treenode.getPositionId()==null?new HashSet<Long>():positionService.findAppFuncIds(treenode.getPositionId());
			this.loadAppFuncTree(list,treenode.getId(),appFuncList,authAppFuncs);
*/		}
		else if(treenode.getType().intValue()==3){
		/*	AppFunc entity = new AppFunc();
			entity.setParentId(treenode.getId());
			List<AppFunc> appFuncList = appFuncService.findList(entity);
			//加载权限关联表权限
			appFuncList.addAll(appFuncService.findBindAppFuncsByAppId(treenode.getId()));
			Set<Long> authAppFuncs=treenode.getPositionId()==null?new HashSet<Long>():positionService.findAppFuncIds(treenode.getPositionId());
			this.loadAppFuncTree(list,treenode.getId(),appFuncList,authAppFuncs);*/
			List<Map<String, Object>> rootList=jdbcService.findList("SELECT ID,SN,NAME,PARENT_ID,APP_ID,IS_DEFAULT,INFO,AUTH_TYPE,STATUS FROM IM_APP_FUNC WHERE APP_ID ="+treenode.getId()+" AND PARENT_ID = "+treenode.getId()+"  and  IS_DELETE=1 ORDER BY CREATE_TIME ASC");
			Set<Long> authAppFunc=treenode.getPositionId()==null?new HashSet<Long>():positionService.findAppFuncIds(treenode.getPositionId());
			if(rootList!=null&&rootList.size()>0){
				for(Map<String, Object> map:rootList){
					if(map.containsKey("NAME")){
						treeNode=new PosTreeNode();
						treeNode.setId(map.get("ID").toString());
						treeNode.setText(map.get("NAME").toString());
//							treeNode.setIconCls("application_list");
						treeNode.setPid(Long.valueOf(map.get("PARENT_ID").toString()));
						treeNode.setChecked(authAppFunc.contains(map.get("ID")));
						treeNode.setType(4);
						treeNode.getAttrMap().put("type", "4");
						list.add(treeNode);
					}
				}
			}

		}
		else if(treenode.getType().intValue()==4){
			List<Map<String, Object>> rootList=jdbcService.findList("SELECT ID,SN,NAME,PARENT_ID,APP_ID,IS_DEFAULT,INFO,AUTH_TYPE,STATUS FROM IM_APP_FUNC WHERE PARENT_ID = "+treenode.getId()+"  and  IS_DELETE=1 ORDER BY CREATE_TIME ASC");
			Set<Long> authAppFunc=treenode.getPositionId()==null?new HashSet<Long>():positionService.findAppFuncIds(treenode.getPositionId());
			if(rootList!=null&&rootList.size()>0&&treenode.getId()!=rootList.get(0).get("ID")){
					for(Map<String, Object> map:rootList){
						if(map.containsKey("NAME")){
							treeNode=new PosTreeNode();
							treeNode.setId(map.get("ID").toString());
							treeNode.setText(map.get("NAME").toString());
//								treeNode.setIconCls("application_list");
							treeNode.setPid(Long.valueOf(map.get("PARENT_ID").toString()));
							treeNode.setType(4);
							treeNode.getAttrMap().put("type", "5");
							treeNode.setChecked(authAppFunc.contains(map.get("ID")));
							list.add(treeNode);
						}
					}
				}

			}
		return list;
	}


	/**
	 * 获取岗位绑定的权限对象列表-目前支持三个层级权限，应用类型-应用-应用第一层权限
	 *
	 * @return
	 */
	@ApiOperation(value="获取岗位关联权限列表")
	@SuppressWarnings("unchecked")
	@RequestMapping(value="getNodeByPostionIdList/{positionId}",method=RequestMethod.GET)
	@ResponseBody
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "positionId", value = "岗位权限标识", required = true, paramType="path", dataType = "Long")
	})
	public List<PostionFuncTree> loadNodeByPostionIdList(@PathVariable Long positionId){
		List<PostionFuncTree> list=new ArrayList<PostionFuncTree>();
		//如果岗位为0则直接返回空数组
		if(positionId==0)return list;
		PostionFuncTree treeNode;
		//第一层  应用类型
		List<AppType> appTypeList=appTypeService.findAll();
		for (AppType appType : appTypeList) {
			App sApp=new App();
			sApp.setAppTypeId(appType.getId());
			List<App> appList=appService.findList(sApp);
			if(appList.size()>0){
				//第二层  应用
				Set<Long> authApps=positionService.findAppIds(positionId);
				for (App app : appList) {
					//保存第一层信息
					treeNode=new PostionFuncTree();
					treeNode.setId(appType.getId().toString());
					//路径======应用类型名称/应用名称
					treeNode.setText("/"+appType.getName());
					if(authApps.contains(app.getId())==false){
						continue;
					}
					treeNode.setChecked(true);
					treeNode.getAttrMap().put("type", "1");
					list.add(treeNode);

					//保存第二层信息
					treeNode=new PostionFuncTree();
					treeNode.setParentId(appType.getId());
					treeNode.setId(app.getId().toString());
					treeNode.setAppId(app.getId());
					//路径======应用类型名称/应用名称
					treeNode.setText("/"+appType.getName()+"/"+app.getName());
					if(authApps.contains(app.getId())==false){
						continue;
					}
					treeNode.setChecked(true);
					treeNode.getAttrMap().put("type", "2");
					list.add(treeNode);
					AppFunc entity = new AppFunc();
					entity.setAppId(app.getId());
					List<AppFunc> appFuncList = appFuncService.findList(entity);
					if(appFuncList.size()>0){
						//第三层 应用权限
						Set<Long> authAppFuncs=positionService.findAppFuncIds(positionId);
						for (Long appFunc : authAppFuncs) {
							treeNode=new PostionFuncTree();
							treeNode.setId(appFunc.toString());
							//路径======应用类型名称/应用名称
							treeNode.setChecked(true);
							treeNode.getAttrMap().put("type", "3");
							list.add(treeNode);
						}
					}
				}
			}
		}
		return list;
	}

	public static PostionFuncTree getAppFuncById(List<PostionFuncTree> list,long id){
		for(PostionFuncTree model:list){
			if(model.getId().equals(id+"")){
				return model;

			}
		}
		return null;
	}




	/**
	 * 加载应用功能
	 * @param list
	 * @param parentId
	 * @param appFuncs
	 */
	@SuppressWarnings("unchecked")
	private void loadAppFuncTree(List<PosTreeNode> list,Long parentId,List<AppFunc> appFuncs,Set<Long> authAppFuncs){
		Iterator<AppFunc> it=appFuncs.iterator();
		List<PosTreeNode> treeNodelist;
		PosTreeNode node;
		while(it.hasNext()){
			AppFunc af=it.next();
			if(af.getParentId().longValue()==parentId.longValue()){
				node=new PosTreeNode();
				node.setId(af.getId().toString());
				node.setChecked(authAppFuncs.contains(af.getId()));
				node.setText(af.getSn()+"-"+af.getName());
				node.getAttrMap().put("type", "3");
				node.setPid(2L);
				node.setType(3);
				treeNodelist=new ArrayList<PosTreeNode>();
				loadAppFuncTree(treeNodelist,af.getId(),appFuncs,authAppFuncs);
				if(treeNodelist.size()==0){
					node.setLeaf(true);
				}else{
					node.setLeaf(false);
					node.setChildren(treeNodelist);
				}
				list.add(node);
			}
		}
	}

	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findById/{id}", method=RequestMethod.GET)
	@ResponseBody
	public Position findById(@PathVariable Long id) {
		return super.findById(id);
	}



	@ApiOperation(value="新增岗位")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody PositionReq entity) {
		return super.save(entity.getPosition());
	}

	@ApiOperation(value="更新岗位")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	public ResultCode edit(@RequestBody PositionReq entity) {
		return super.edit(entity.getPosition());
	}


	/**
	 * 分页查询岗位列表
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	@ApiOperation(value="分页查询岗位列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findList", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 4)
	protected PageList<Position> findList(@RequestBody PositionReq entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			Position p=entity.getPosition();
			p.setIsLikeQuery(true);
			return getBaseService().findPage(p,page,limit);
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<Position>();
		}
	}


	@ApiOperation(value="移除岗位")
    @RequestMapping(value="remove",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 3)
    public ResultCode remove(@RequestBody @ApiParam(name="唯一标识集合",value="多数据采用英文逗号分割",required=true)List<String> ids) {
		Params params=new Params();
		params.setIds(ids);
		log.info("ids:"+ids.toString());
		return super.remove(params);
    }


	@ApiOperation(value="保存岗位和应用关系")
    @RequestMapping(value="savePositionApp",method = RequestMethod.POST)
	@ResponseBody
    public ResultCode savePositionApp(@RequestBody PostionFuncModel entity) {
		try{
			if(entity.getPositionId().longValue()==0){
				return new ResultCode(Constants.OPERATION_FAIL);
			}
			Position position = positionService.findById(entity.getPositionId());
			if(position.getStatus().equals(2)){
				return new ResultCode(Constants.OPERATION_NOT_ALLOW);
			}
			Set appSet = new HashSet();
			appSet.addAll(entity.getApps());
			ArrayList<Long> arrayList2 = new ArrayList<Long>();
			arrayList2.addAll(appSet);
			entity.setApps(arrayList2);

			Set appFuncSet = new HashSet();
			appFuncSet.addAll(entity.getAppfuns());
			ArrayList<Long> arrayList = new ArrayList<Long>();
			arrayList.addAll(appFuncSet);
			entity.setAppfuns(arrayList);
			positionService.savePositionApp(entity);
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			e.printStackTrace();
			return new ResultCode(Constants.OPERATION_FAIL);
		}
    }




	@ApiOperation(value="添加互斥岗位")
	@ApiImplicitParams({
  	  @ApiImplicitParam(name = "id", value = "岗位唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="saveMutexPosition/{id}", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode saveMutexPosition(@PathVariable Long id,@RequestBody @ApiParam(name="互斥岗位唯一标识集合",value="多数据采用英文逗号分割",required=true)ArrayList<Long> ids) {
		try{
			Position position = positionService.findById(id);
			if(position.getStatus().equals(2)){
				return new ResultCode(Constants.OPERATION_NOT_ALLOW);
			}
			positionService.saveMutexPosition(id, ids);
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			e.printStackTrace();
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}

	@ApiOperation(value="移除互斥岗位")
	@ApiImplicitParams({
  	  @ApiImplicitParam(name = "id", value = "岗位唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="removeMutexPosition/{id}", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode removeMutexPosition(@PathVariable Long id,@RequestBody @ApiParam(name="互斥岗位唯一标识集合",value="多数据采用英文逗号分割",required=true)ArrayList<Long> ids) {
		try{
			Position position = positionService.findById(id);
			if(position.getStatus().equals(2)){
				return new ResultCode(Constants.OPERATION_NOT_ALLOW);
			}
			positionService.removeMutexPosition(id, ids);
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			e.printStackTrace();
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}


	@ApiOperation(value="获取未互斥岗位分页列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "id", value = "岗位唯一标识", required = true, paramType="path", dataType = "Long"),
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findAddMutexPositions/{id}", method=RequestMethod.POST)
	@ResponseBody
	public PageList<?> findAddMutexPositions(@PathVariable Long id,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit) {
		Position entity1= new Position();
		entity1.setId(id);
		return positionService.findAddMutexPositions(entity1, page, limit);
	}

	@ApiOperation(value="获取互斥岗位分页列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "id", value = "岗位唯一标识", required = true, paramType="path", dataType = "Long"),
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findRemoveMutexPositions/{id}", method=RequestMethod.POST)
	@ResponseBody
	public PageList<?> findRemoveMutexPositions(@PathVariable Long id,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit) {
		Position entity1= new Position();
		entity1.setId(id);
		return positionService.findRemoveMutexPositions(entity1, page, limit);
	}
	@Resource
	JdbcService jdbcService;

	@ApiOperation(value="获取岗位用户分页列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "id", value = "岗位唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findUserByPostion/{id}", method=RequestMethod.POST)
	@ResponseBody
	public PageList<User> findPositionsUsers(@PathVariable Long id, @RequestBody UserReq entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit) {
		User user=entity.getUser();
		user.setId(id);
		user.setIsLikeQuery(true);
		return positionService.findUserByPostion(user, page, limit);
	}

	@SuppressWarnings("rawtypes")
	@ApiOperation(value="模板下载")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="downTemplate/{id}",method = RequestMethod.GET)
	@ResponseBody
	public void downTemplate(@PathVariable Long id){
		List<ExcelUtils.ExcelModel> models=new ArrayList<ExcelUtils.ExcelModel>();
		models.add(new ExcelUtils.ExcelModel("岗位编码","SN",5000));
		models.add(new ExcelUtils.ExcelModel("岗位名称","NAME",5000));
		models.add(new ExcelUtils.ExcelModel("岗位描述","REMARK",5000));
		super.exportXlsx("position", new ArrayList(), models,"岗位信息");
	}

	@ApiOperation(value = "岗位导出")
 	@RequestMapping(value = "export", method = RequestMethod.POST)
 	@ResponseBody
 	public void export(@RequestBody Position entity) {
 		try {
 			PageList<Position> findPage = positionService.findPage(entity, 0, 50000);
	 		//循环生成打印模型
	 		List<ExcelUtils.ExcelModel> models = new ArrayList<ExcelUtils.ExcelModel>();
	 		models.add(new ExcelUtils.ExcelModel("岗位编码","sn",5000));
			models.add(new ExcelUtils.ExcelModel("岗位名称","name",5000));
			models.add(new ExcelUtils.ExcelModel("状态","status",5000));
			models.add(new ExcelUtils.ExcelModel("岗位描述","remark",5000));
			List<Position> dataList = findPage.getDataList();
			super.exportXlsx("positionInfo", dataList, models, "岗位信息");
	 	} catch (Exception e) {
	 		log.error("export user error", e);
	 	}
	}

	/**
	 * 岗位导入
	 */
 	@ApiOperation(value="岗位导入")
	@RequestMapping(value="import",method = RequestMethod.POST)
	@ResponseBody
	public boolean importPosition(@ApiParam(name="file",value="导入内容",required=false) @RequestParam(required=false,name="file",value="file") MultipartFile file){
 		List<Position> findAll = positionService.findAll();
 		Map<String,String> colM=new HashMap<String,String>();
		colM.put("岗位编码","sn");
		colM.put("岗位名称","name");
		colM.put("岗位描述","remark");
		try {
			List list=ExcelUtils.parseToMap(file.getInputStream(),  "岗位信息", colM);
			if(list==null||list.size()==0){
				return false;
			}
			for (Object object : list) {
				JSONObject jsonObject = JSONObject.fromObject(object);
				String sn = jsonObject.get("sn").toString();
				String name = jsonObject.get("name").toString();
				//当工号和名称为空的时候，结束当前信息的循环
				if(sn==null||sn==""||sn.length()==0||name==null||name==""||name.length()==0){
					continue;
				}
				Position position = new Position();
				position.setSn(sn);
				List<Position> findList = positionService.findList(position);
				position.setStatus(1);
				if(findList.size()==0){
					position.setName(jsonObject.get("name").toString());
					position.setRemark(jsonObject.get("remark").toString());
					positionService.save(position);
				}
				//不进入循环代表数据已存在
			}

		} catch (IOException e) {
			return false;
		}
		return true;
	}

 	@ApiOperation(value="启用岗位")
    @RequestMapping(value="enablePosts",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 5)
    public ResultCode enablePosts(@RequestBody @ApiParam(name="唯一标识集合",value="多数据采用英文逗号分割",required=true)Params params) {
		try{
			positionService.enablePosts(ArrayUtils.stringArrayToLongArray(params.getIds().toArray(new String[params.getIds().size()])));
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			return new ResultCode(Constants.OPERATION_FAIL);
		}
    }

	@ApiOperation(value="禁用岗位")
    @RequestMapping(value="disablePosts",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 6)
    public ResultCode disablePosts(@RequestBody @ApiParam(name="唯一标识集合",value="多数据采用英文逗号分割",required=true)Params params) {
		try{
			positionService.disablePosts(ArrayUtils.stringArrayToLongArray(params.getIds().toArray(new String[params.getIds().size()])));
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			return new ResultCode(Constants.OPERATION_FAIL);
		}
    }
}
