package com.sense.iam.api.action.im;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sense.core.util.CurrentAccount;
import com.sense.core.util.ExcelUtils;
import com.sense.core.util.GatewayHttpUtil;
import com.sense.core.util.PageList;
import com.sense.core.util.PageModel;
import com.sense.core.util.StringUtils;
import com.sense.iam.api.SessionManager;
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.api.model.BindAccByOrg;
import com.sense.iam.api.model.BindApp;
import com.sense.iam.api.model.BindPosition;
import com.sense.iam.api.model.EnterAuthUserModel;
import com.sense.iam.api.model.im.AppReq;
import com.sense.iam.api.model.im.LoadOrgReq;
import com.sense.iam.api.model.im.OrgApiTree;
import com.sense.iam.api.model.im.OrgMoveReq;
import com.sense.iam.api.model.im.OrgPortalReq;
import com.sense.iam.api.model.im.OrgReq;
import com.sense.iam.api.model.im.OrgSyncReq;
import com.sense.iam.api.model.im.OrgTree;
import com.sense.iam.api.model.im.PositionReq;
import com.sense.iam.api.model.im.UserReq;
import com.sense.iam.api.model.sys.SAcctReq;
import com.sense.iam.api.util.OrgCUtil;
import com.sense.iam.api.util.OrgUtil;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.OrgTreeNode;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.cam.TreeNode;
import com.sense.iam.model.im.App;
import com.sense.iam.model.im.Org;
import com.sense.iam.model.im.OrgType;
import com.sense.iam.model.im.Position;
import com.sense.iam.model.im.User;
import com.sense.iam.model.im.UserPosition;
import com.sense.iam.model.sys.Acct;
import com.sense.iam.model.sys.Field;
import com.sense.iam.service.AppService;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.OrgPositionService;
import com.sense.iam.service.OrgService;
import com.sense.iam.service.OrgTypeService;
import com.sense.iam.service.PositionService;
import com.sense.iam.service.SysAcctService;
import com.sense.iam.service.SysFieldService;
import com.sense.iam.service.UserMultiOrgService;
import com.sense.iam.service.UserPositionService;
import com.sense.iam.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiSort;

@Api(tags = "组织管理")
@Controller
@RestController
@RequestMapping("im/org")
@ApiSort(value = 2)
public class OrgAction extends AbstractAction<Org,Long>{

	@Resource
	JdbcService jdbcService;
	@Resource
	PositionService positionService;
	@Resource
	OrgPositionService orgPositionService;
	@Resource
	OrgTypeService orgTypeService;
	@Resource
	SysFieldService sysFieldService;
	@Resource
	OrgService orgService;
	@Resource
	private UserMultiOrgService userMultiOrgService;
	@Resource
	UserService userService;
	
	@Resource
	AppService appService;

	@Resource
	SysAcctService sysAcctService;
	
	@ApiOperation(value="组织架构图")
	@RequestMapping(value="chart", method=RequestMethod.GET)
	@ResponseBody
	public Object initOrgCount() {
		CurrentAccount account=CurrentAccount.getCurrentAccount();
		if(account==null){
			JSONObject resp=new JSONObject();
			resp.put("name","未知");
			resp.put("id","未知");
			resp.put("usercount",0);
			resp.put("children",new JSONArray());
			return resp;
		}
		OrgUtil outil=new OrgUtil();
		List<?> companyObject =jdbcService.findList("select COMPANY_NAME,ID from sys_company where SN='"+account.getCompanySn()+"'");
		JSONArray datacompanyObject=JSONArray.fromObject(companyObject);
		String companyName="";
		long companyId=0L;
		if(datacompanyObject != null && datacompanyObject.size() > 0){
			companyName=datacompanyObject.getJSONObject(0).getString("COMPANY_NAME");
			companyId=datacompanyObject.getJSONObject(0).getLong("ID");
		}else{
			companyName="公司";
		}
		List<?> alllist =jdbcService.findList("select count(1) as USERCOUNT from im_user where COMPANY_SN='"+account.getCompanySn()+"'");
		JSONArray alllistData=JSONArray.fromObject(alllist);
		List<?> list =jdbcService.findList("select a.ID,a.SN,a.NAME,a.PARENT_ID,b.NUM as USERCOUNT from IM_ORG a LEFT JOIN (select ORG_ID,count(1) NUM from IM_ORG_USER GROUP BY ORG_ID) b on a.ID=b.ORG_ID WHERE a.COMPANY_SN='"+account.getCompanySn()+"' and a.STATUS=1 ORDER BY a.SORT_NUM");
		JSONArray lists= JSONArray.fromObject(list);
		List<OrgTree> strList = new ArrayList<OrgTree>();
		for(int i=0;i<lists.size();i++){
			try{
				OrgTree model=new OrgTree(); 
				model.setId(lists.getJSONObject(i).getLong("ID"));
				model.setName(lists.getJSONObject(i).getString("NAME"));
				model.setParent_id(lists.getJSONObject(i).getLong("PARENT_ID"));
				model.setSn(lists.getJSONObject(i).getString("SN"));
				if(lists.getJSONObject(i).containsKey("USERCOUNT")){
					model.setUsercount(lists.getJSONObject(i).getInt("USERCOUNT"));
				}else{
					model.setUsercount(0);
				}
				strList.add(model);
			}catch (Exception e) {
				continue;
			}
		}
		long usercount=0;
		if(alllist!=null && alllist.size()>0){
			if(alllistData.getJSONObject(0).containsKey("USERCOUNT")){
				usercount=alllistData.getJSONObject(0).getLong("USERCOUNT");
			}else{
				usercount=0;
			}
		}
		List<Object> listOrg=outil.menuList(strList);
		JSONObject resp=new JSONObject();
		resp.put("name",companyName);
		resp.put("id",companyId);
		resp.put("usercount",usercount);
		resp.put("children",JSONArray.fromObject(listOrg));
		return resp;
	}
	
	
	
	
	

	
	/**
	 * 加载组织类型
	 * @param org
	 * @return
	 */
	@ApiOperation(value="加载组织类型")
	@RequestMapping(value="loadOrgType", method=RequestMethod.POST)
	@ResponseBody
	public List<OrgType> loadOrgType(){
		List<OrgType> type=new ArrayList<OrgType>();
		Org org=new Org();
		org.setStatus(null);
		//带权限查询
		List<Org> orgs=orgService.findList(org);
		if(orgs!=null&&orgs.size()>0){
			 List<Map<String, Object>> maps;
			 if(CurrentAccount.getCurrentAccount().getLoginName().equals("admin")){
				 maps=jdbcService.findList("SELECT ID,NAME FROM IM_ORG_TYPE WHERE COMPANY_SN='"+CurrentAccount.getCurrentAccount().getCompanySn()+"' ORDER BY CREATE_TIME ASC");	
			 }else{
				 maps=jdbcService.findList("SELECT ID,NAME FROM IM_ORG_TYPE WHERE ID IN(SELECT ORG_TYPE_ID FROM IM_ORG WHERE ID IN(SELECT IM_ORG_ID from sys_acct_im_org where SYS_ACCT_ID ="+CurrentAccount.getCurrentAccount().getId()+")"
					 		+ "UNION ALL SELECT ORG_TYPE_ID FROM IM_ORG WHERE ID IN(SELECT sugo.ORG_ID from sys_user_group_acct suga LEFT JOIN sys_user_group sug ON suga.USER_GROUP_ID = sug.ID LEFT JOIN sys_user_group_org sugo ON sugo.USER_GROUP_ID = sug.ID where ACCT_ID =" + CurrentAccount.getCurrentAccount().getId() + " AND sug.STATUS = 1"
					 		+ ") ) and COMPANY_SN='"+CurrentAccount.getCurrentAccount().getCompanySn()+"' ORDER BY CREATE_TIME ASC");
			 }
			 if(maps!=null&&maps.size()>0){
				for(Map<String, Object> map:maps){
					 OrgType model=new OrgType();
					 model.setId(Long.valueOf(map.get("ID").toString()));
					 model.setName(map.get("NAME").toString());
					 type.add(model);
				}
			 }
		}
		return type;
	}
	

	/**
	 * 加载组织树
	 * @param org
	 * @return
	 */
	@ApiOperation(value="加载组织树")
	@RequestMapping(value="loadOrg", method=RequestMethod.POST)
	@ResponseBody
	public List<OrgTreeNode> loadTree(@RequestBody LoadOrgReq param){
		//加载顶级还是逐级加载
		if(param.getId()==0L || param.getId()==-1L){
			//加载有权限的属性结构
			Org org=new Org();
			//判断是否需要查询禁用的组织
			if(param.getStatus()==0){
				org.setStatus(null);
			}else{
				org.setStatus(param.getStatus());
			}
			org.setOrgTypeId(param.getOrgTypeId());
			List<Org> orgList=orgService.findList(org);
			OrgTreeNode treeNode;
			//匹配使用
			List<OrgTreeNode> returnList=new ArrayList<OrgTreeNode>();
			Map<Long,Org> tmpMap=new HashMap<Long,Org>();
			for (Org orgEntity : orgList) {
				tmpMap.put(orgEntity.getId(), orgEntity);
			}
			//加载顶级节点
			Iterator<Org> it=orgList.iterator();
			while (it.hasNext()) {
				Org orgEntity=it.next();
				if(!isExistParent(orgEntity.getIdPath(),tmpMap)){
					treeNode=new OrgTreeNode();
					treeNode.setId(orgEntity.getId());
					if(orgEntity.getStatus()==null||orgEntity.getStatus()==2){
						treeNode.setLabel(orgEntity.getName());
					}else{
						treeNode.setLabel(orgEntity.getName());
					}
					treeNode.setTypeId(orgEntity.getOrgTypeId());
					treeNode.setParentId(orgEntity.getParentId());
					treeNode.setStatus(orgEntity.getStatus()==null?2:1);
					treeNode.setClassName("icon-icon_organization");
					it.remove();
					//判断是否存在搜索条件
					if(param.getName()!=null && param.getName().trim().length()>0){
						Org org2=new Org();
						org2.setIsLikeQuery(true);
						org2.setParentId(Long.valueOf(treeNode.getId()));
						org2.setName(param.getName());
						org2.setIsControl(false);
						//判断是否需要查询禁用的组织
						if(param.getStatus()==0){
							org2.setStatus(null);
						}else{
							org2.setStatus(param.getStatus());
						}
						List<OrgTreeNode> list=orgService.findTreeNode(org2);
						if(list.size()==0){
							continue;
						}
						treeNode.setIsLeaf(false);
						treeNode.setChildren(list);	
					}
					returnList.add(treeNode);
				}
			}
			return returnList;
		}else{
			Org org=new Org();
			//判断是否需要查询禁用的组织
			if(param.getStatus()==0){
				org.setStatus(null);
			}else{
				org.setStatus(param.getStatus());
			}
			org.setName(param.getName());
			org.setParentId(param.getId());
			org.setId(null);
			List<OrgTreeNode> list=orgService.findTreeNode(org);
			return list;
		}
	}
	
	/**
	 * 判断是否存在上级组织机构
	 * @param path
	 * @param tmpMap
	 * @return
	 */
	private boolean isExistParent(String idPath,Map<Long,Org> tmpMap){
		String[] ids=idPath.split("/");
		for(String id:ids){
			if(id.length()>0){
				if(tmpMap.containsKey(Long.valueOf(id))){
					return true;
				}
			}
		}
		return false;
	}
	
	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findById/{id}", method=RequestMethod.GET)
	@ResponseBody
	public Org findById(@PathVariable Long id) {
		Org org = super.findById(id);
		Org findOrg = new Org();
		findOrg.setOrgTypeId(org.getOrgTypeId());
		//判断是否是根节点
		if(org.getIdPath().equals("/")){
			findOrg.setIdPath(org.getIdPath());
		}
		else{
			//不是则将该组织的ID存放到idPath中做模糊查询
			findOrg.setIdPath(org.getId().toString());
			findOrg.setId(org.getId());
		}
		//查询该组织下所有用户数量
		Integer userNum = userService.findUserNum(findOrg);
		
		org.setUserNum(userNum==null?0:userNum);
		return org;
	}

	@ApiOperation(value="组织授权门户")
	@RequestMapping(value="authOrgPortalTemplate", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode authOrgPortalTemplate(@RequestBody OrgPortalReq entity) {
		log.info("param:::"+entity.toString());
		return orgService.authOrgPortalTemplate(entity.getOrgId(), entity.getTemplateId());
	}
	
	@ApiOperation(value="取消组织授权门户")
	@RequestMapping(value="cancelAuthOrgPortalTemplate", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode cancelAuthOrgPortalTemplate(@RequestBody OrgPortalReq entity) {
		log.info("param:::"+entity.toString());
		return orgService.cancelAuthOrgPortalTemplate(entity.getOrgId());
	}
	
	@ApiOperation(value="组织重新同步")
	@RequestMapping(value="syncAll", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode syncAll(@RequestBody OrgSyncReq model){
		if(model==null){
			return new ResultCode(Constants.OPERATION_FAIL,"参数缺失！");
		}
		if(model.getAppIds()==null||model.getAppIds().length==0){
			return new ResultCode(Constants.OPERATION_FAIL,"应用ID集合参数缺失！");
		}
		if(model.getOrgId()==null||model.getOrgId().longValue()==0){
			return new ResultCode(Constants.OPERATION_FAIL,"组织ID参数缺失！");
		}
		
		Org org = orgService.findById(model.getOrgId());
		if(org==null){
			return new ResultCode(Constants.OPERATION_FAIL,"组织ID未存在！");
		}
		String errorMsg=null;
		for(long appId:model.getAppIds()){
			App app=appService.findById(appId);
			if(app==null){
				if(errorMsg!=null){
					errorMsg+=appId+";";
				}else{
					errorMsg=appId+";";
				}
				continue;
			}else{
				orgService.syncAll(model.getOrgId(),appId);
			}
		}
		if(errorMsg!=null){
			return new ResultCode(Constants.OPERATION_FAIL,"部分应用同步失败["+errorMsg+"],失败原因：应用未存在！");
		}else{
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}
	}
	
	/**
	 * 查询组织下的所有子组织
	 * @return
	 */
	@ApiOperation(value="查询组织下的所有子组织")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "orgId", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findOrgChildren/{orgId}", method=RequestMethod.GET)
	@ResponseBody
	protected List<Org> findOrgChildren(@PathVariable Long orgId){
		try{
			Org p=new Org();
			p.setParentId(orgId);
			p.setIsLikeQuery(true);
			return getBaseService().findList(p);
		}catch(Exception e){
			log.error("findOrgChildren error:",e);
			return new ArrayList<Org>();
		}
	}
	
	/**
	 * 组织搜索
	 * @return
	 */
	@ApiOperation(value="组织搜索")
	@RequestMapping(value="findListByObject", method=RequestMethod.POST)
	@ResponseBody
	protected List<Org> findListByObject(@RequestBody OrgReq entity){
		try{
			Org p=entity.getOrg();
			p.setIsLikeQuery(true);
			if(entity.getStatus()==null||entity.getStatus()==0){
				p.setStatus(null);
			}
			return getBaseService().findList(p);
		}catch(Exception e){
			log.error("findListByObject error:",e);
			return new ArrayList<Org>();
		}
	}
	

	
	@ApiOperation(value="新增组织")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
    @ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody OrgReq entity) {
		Org orgs=entity.getOrg();
		if(!StringUtils.isEmpty(entity.getParentSn())){//父级节点编码不为空，则使用父级编码查询机构
			Org org=new Org();
			org.setSn(entity.getParentSn());
			org.setIsControl(false);
			org=getBaseService().findByObject(org);
			if(org!=null){
				orgs.setParentId(org.getId());
			}
		}
		ResultCode code=super.save(orgs);
		if(code.getSuccess()){
			code.setMsg(orgs.getId()+"");
		}
		return code;
	}
	
	@ApiOperation(value="更新组织")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	public ResultCode edit(@RequestBody OrgReq entity) {
		Org orgs=entity.getOrg();
		if(!StringUtils.isEmpty(entity.getParentSn())){//父级节点编码不为空，则使用父级编码查询机构
			Org org=new Org();
			org.setSn(entity.getParentSn());
			org.setIsControl(false);
			org=getBaseService().findByObject(org);
			if(org!=null){
				orgs.setParentId(org.getId());
			}
		}
		return super.edit(orgs);
	}
	
	
	
	@ApiOperation(value="启用组织")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="activeOrg/{id}", method=RequestMethod.GET)
	@ResponseBody
	@ApiOperationSupport(order = 6)
	public ResultCode activeOrg(@PathVariable Long id) {
		try{
			Org model=super.findById(id);
			model.setStatus(1);
			return super.edit(model);
		}catch(Exception e){
			e.printStackTrace();
		}
		return new ResultCode(Constants.OPERATION_FAIL);
	}

	@ApiOperation(value="禁用组织")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="forbidden/{id}", method=RequestMethod.GET)
	@ResponseBody
	@ApiOperationSupport(order = 7)
	public ResultCode enableOrg(@PathVariable Long id) {
		try{
			Org model=super.findById(id);
			model.setStatus(2);
			return super.edit(model);
		}catch(Exception e){
			e.printStackTrace();
		}
		return new ResultCode(Constants.OPERATION_FAIL);
	}
	
	
	/**
	 * 分页查询岗位列表
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	@ApiOperation(value="分页查询组织列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findList", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 5)
	protected PageList<Org> findList(@RequestBody OrgReq entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			Org p=entity.getOrg();
			p.setIsLikeQuery(true);
			if(entity.getStatus()!=null){
				if(entity.getStatus()==-1){
					p.setStatus(null);
				}else{
					p.setStatus(entity.getStatus());
				}
			}
			
			if(p.getParentId()==null || p.getParentId()==-1){
				p.setParentId(null);
				limit=1000000;
				PageList<Org> pageList=getBaseService().findPage(p,page,limit);//根节点查询
				
				Map<Long,Org> tmpMap=new HashMap<Long,Org>();
				for (Org orgEntity : pageList.getDataList()) {
					tmpMap.put(orgEntity.getId(), orgEntity);
				}
				//加载顶级节点
				Iterator<Org> it=pageList.getDataList().iterator();
				while (it.hasNext()) {
					Org orgEntity=it.next();
					if(isExistParent(orgEntity.getIdPath(),tmpMap)){
						it.remove();
					}
				}
				pageList.setTotalcount(pageList.getDataList().size());
				pageList.updateIndex();
				return pageList;
			}else{
				return getBaseService().findPage(p,page,limit);
			}
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<Org>();
		}
	}
	
	
	
	
	@ApiOperation(value="移除组织")
    @RequestMapping(value="remove",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 4)
    public ResultCode remove(@RequestBody @ApiParam(name="唯一标识集合",value="多数据采用英文逗号分割",required=true)List<String> ids) {
		Params params=new Params();
		params.setIds(ids);
		log.info("ids:"+ids.toString());
		return super.remove(params);
    }
	


	/**
	 * 获取组织机构的岗位列表
	 * @param orgId
	 * @param positionId
	 * @return
	 */
	@ApiOperation(value="获取组织机构的岗位列表")
	@RequestMapping(value="findOrgBinPositionList/{orgId}",method = RequestMethod.POST)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "orgId", value = "组织唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@ResponseBody
	public PageList<Position> findPositionList(@PathVariable Long orgId, @RequestBody PositionReq entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		PositionReq entitynew=entity;
		entitynew.setId(orgId);
		Position p = entitynew.getPosition();
		p.setIsLikeQuery(true);
		PageList<Position> positions=positionService.findOrgBinPositionPage(p, page, limit);
		List<Long> defaultPositionList=orgPositionService.findDefaultPositionIdsByOrgId(orgId);
		List<Long> ratainPositionList=orgPositionService.findPositionIdsByOrgId(orgId);
		
		List<Position> newList=positions.getDataList().stream().map(i->{
			i.setIsDefault(defaultPositionList.contains(i.getId()));
			i.setIsRatain(ratainPositionList.contains(i.getId()));
			return i;
		}).collect(Collectors.toList());
		positions.setDataList(newList);
		return positions;
	}
	
	@ApiOperation(value="获取组织下兼职用户")
	@RequestMapping(value="findOrgMultiUserList/{orgId}",method = RequestMethod.GET)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "orgId", value = "组织唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@ResponseBody
	public Org findUserList(@PathVariable Long orgId) {
		Org o = super.findById(orgId);
		List<User> multiusers = userMultiOrgService.findUserByOrgId(orgId);
		o.setMultiUsers(multiusers);
		String multiUsersString="";
		int i=0;
		for(User u: multiusers){
			if(i==0) {
				multiUsersString=u.getName();
			} else {
				multiUsersString+=";"+u.getName();
			}
			i++;
		}
		o.setMultiUsersString(multiUsersString);
		return o;
	}
	
	/**
	 * 组织岗位绑定
	 * @param orgId
	 * @param positionId
	 * @return
	 */
	@ApiOperation(value="组织岗位绑定")
	@RequestMapping(value="bindPosition",method = RequestMethod.POST)
	@ResponseBody
	public ResultCode bindPosition(@RequestBody BindPosition bindPosition){
		try{
			//如果是授权默认岗位的话，则进行互斥拦截
			if(bindPosition.getIsDefault().longValue()==1){
				ResultCode code=isMuite(bindPosition.getPositionId(), bindPosition.getOrgId());
				//判断当前授权岗位存不存在互斥，如果存在则返回授权失败信息
				if(!code.getSuccess()){
					return code;
				}
			}
			orgService.bindPosition(bindPosition.getOrgId(), bindPosition.getPositionId(),bindPosition.getIsDefault());
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			log.error("save error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	@Resource
	private UserPositionService userPositionService;
	
	/**
	 * 判断是否存在互斥岗位
	 * @param userId
	 * @param positionId
	 * @return
	 */
	public ResultCode isMuite(Long positionId,Long orgId){
		boolean flag=false;
		String sql="SELECT B_ID FROM IM_MUTEX WHERE A_ID="+positionId;
		List<Map<String, Long>> maps=jdbcService.findList(sql);
		if(maps!=null&&maps.size()>0){
			String sql2="SELECT POSITION_ID FROM IM_ORG_POSITION WHERE ORG_ID="+orgId+" AND IS_DEFAULT=1";
			List<Map<String, Object>> list1s=jdbcService.findList(sql2);
			List<String> ara=new ArrayList<String>();
			//获取组织已勾选的所有集合
			for(Map<String, Object> ms:list1s){
				String dmodel=ms.get("POSITION_ID").toString();
				ara.add(dmodel);
			}
			for(int i=0;i<maps.size();i++){
				for(String id:ara){
					if(maps.get(i).get("B_ID").longValue()==Long.valueOf(id).longValue()){
						//存在已勾选的互斥岗位，进行后端返回提示消息
						flag=true;
					}
				}
			}
		}
		if(flag){
			String name="";
			for(int i=0;i<maps.size();i++){
				Position p=positionService.findById(Long.valueOf(maps.get(i).get("B_ID")));
				name+="["+p.getName()+"],";
			}
			if(name==null||name.length()==0){
				return new ResultCode(Constants.OPERATION_FAIL,"授权失败，当前岗位与其他岗位存在互斥。");
			}else{
				name=name.substring(0, name.length()-1);
				return new ResultCode(Constants.OPERATION_FAIL,"授权失败，当前岗位与"+name+"存在互斥。");
			}
		}
		return new ResultCode(Constants.OPERATION_SUCCESS);
	}
	
	/**
	 * 组织岗位解除
	 * @param orgId
	 * @param positionId
	 * @return
	 */
	@ApiOperation(value="组织岗位解除")
	@RequestMapping(value="unBindPosition",method = RequestMethod.POST)
	@ResponseBody
	public ResultCode unBindPosition(@RequestBody BindPosition bindPosition){
		try{
			orgService.unBindPosition(bindPosition.getOrgId(), bindPosition.getPositionId());
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			log.error("save error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
	
	
	/**
	 * 获取组织机构的应用列表
	 * @param orgId
	 * @param positionId
	 * @return
	 */
	@ApiOperation(value="获取组织机构的应用列表")
	@RequestMapping(value="findOrgAppList/{orgId}",method = RequestMethod.POST)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "orgId", value = "组织唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@ResponseBody
	public Object findAppList(@PathVariable Long orgId,@RequestBody AppReq entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		AppReq entitynew=entity;
		entitynew.setId(orgId);
		App a = entitynew.getApp();
		a.setIsLikeQuery(true);
		PageList<App> apps=appService.findOrgBinAppPage(a, page, limit);
		List<Long> defaultAppList=orgService.findDefaultAppIdsByOrgId(orgId);//默认开通的应用列表
		List<Long> ratainsAppList=orgService.findAppIdsByOrgId(orgId);//机构关联的应用列表
		List<App> newList=apps.getDataList().stream().map(i->{
			i.setIsDefault(defaultAppList.contains(i.getId()));
			i.setIsRatain(ratainsAppList.contains(i.getId()));
			return i;
		}).collect(Collectors.toList());
		apps.setDataList(newList);
		return apps;
	}
	
	
	
	/**
	 * 组织应用绑定
	 * @param orgId
	 * @param positionId
	 * @return
	 */
	@ApiOperation(value="组织应用绑定")
	@RequestMapping(value="bindApp",method = RequestMethod.POST)
	@ResponseBody
	public ResultCode bindApp(@RequestBody BindApp mobel){
		try{
			orgService.bindApp(mobel.getOrgId(), mobel.getAppId(), mobel.getIsDefault());
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			log.error("save error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
	
	/**
	 * 组织应用解除
	 * @param orgId
	 * @param positionId
	 * @return
	 */
	@ApiOperation(value="组织应用解除")
	@RequestMapping(value="unBindApp",method = RequestMethod.POST)
	@ResponseBody
	public ResultCode unBindApp(@RequestBody BindApp mobel){
		try{
			orgService.unBindApp(mobel.getOrgId(), mobel.getAppId());
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			log.error("save error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
	/**
	 * 移动组织
	 * @param orgId
	 * @param positionId
	 * @return
	 */
	@ApiOperation(value="组织移动")
	@RequestMapping(value="moveOrg",method = RequestMethod.POST)
	@ResponseBody
	public ResultCode orgMove(@RequestBody OrgMoveReq model){
		try {
			return orgService.move(model.getOldOrgId(), model.getNewOrgId());
		} catch (Exception e) {
			return new ResultCode(Constants.OPERATION_FAIL,e.getMessage());
		}	
	}
	
	
	/**
	 * 获取拥有组织机构权限的系统账号
	 * @return
	 */
	@ApiOperation(value="获取拥有组织机构权限的账号")
	@RequestMapping(value="findAcctByOrgId",method = RequestMethod.POST)
	@ResponseBody
	protected PageList<Map> findAcctByOrgId(@RequestBody SAcctReq entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			 Acct acct =entity.getAcct();
			 PageList<Map> pageList=sysAcctService.findAcctByOrgId(acct, page, limit);
			 return  pageList;
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<Map>();
		}
	}
	
	
	/**
	 * 组织机构授权管理员
	 * @return
	 */
	@ApiOperation(value="组织机构授权管理员")
	@RequestMapping(value="bindOrg",method = RequestMethod.POST)
	@ResponseBody
	public ResultCode bindOrg(@RequestBody BindAccByOrg acct){
		try{
			if(acct.getAcctIds().size() > 0 ){
				sysAcctService.removeByorgId(acct.getOrgId());
				for(String id:acct.getAcctIds()){
					sysAcctService.bindOrg(acct.getOrgId(), Long.valueOf(id));
				}
				return new ResultCode(Constants.OPERATION_SUCCESS);
			}
			return new ResultCode(Constants.OPERATION_FAIL);
		}catch(Exception e){
			log.error("save error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}


	/**
	 * 取消组织机构授权管理员
	 * @return
	 */
	@ApiOperation(value="取消组织机构授权管理员")
	@RequestMapping(value="removebindOrg",method = RequestMethod.POST)
	@ResponseBody
	public ResultCode removebindOrg(@RequestBody BindAccByOrg acct){
		try{
			Org org = orgService.findById(acct.getOrgId());
			if(org.getParentId().intValue() !=-1) {
				sysAcctService.removeByorgId(acct.getOrgId());
				return new ResultCode(Constants.OPERATION_SUCCESS);
			}else{
				return new ResultCode(Constants.OPERATION_FAIL,"当前授权机构为根组织，至少需要授权一个管理员!");
			}
		}catch(Exception e){
			log.error("removebindOrg error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}


	//组织导入
	@ApiOperation(value="组织导入")
	@RequestMapping(value="import/{orgId}",method = RequestMethod.POST)
	@ResponseBody
	public ResultCode upLoad(@PathVariable Long orgId, @ApiParam(name="file",value="导入内容",required=false) @RequestParam(required=false,name="file",value="file") MultipartFile file){
		try{
			//获取组织类型的表格sheet名
			OrgType orgType=orgTypeService.findById(orgService.findById(orgId).getOrgTypeId());
			String remark=orgType.getName();
			Field entity=new Field();
			entity.setObjId(orgType.getId());
			List<Field> orgFields=sysFieldService.findList(entity);
			Map<String,String> colM=new HashMap<String,String>();
			for (Field field : orgFields) {
				colM.put(field.getRemark(),field.getName());
			}
			List list=ExcelUtils.parseToMap(file.getInputStream(), remark, colM);
			if(list==null){
				return new ResultCode(Constants.OPERATION_UNKNOWN,"导入组织有误");
			}
			return orgService.importData(orgType.getId(), list);
		}catch(Exception e){
			log.error("import acct error",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}

	/**
	 * 组织机构取消授权管理员
	 * @return
	 */
//	@ApiOperation(value="组织机构取消授权管理员")
//	@RequestMapping(value="unBindOrg",method = RequestMethod.POST)
//	@ResponseBody
//	public ResultCode unBindOrg(@RequestBody BindAccByOrg acct){
//		try{
//			if(acct.getAcctIds().size()>0){
//				for(String id:acct.getAcctIds()){
//					sysAcctService.unBindOrg(acct.getOrgId(), Long.valueOf(id));
//				}
//				return new ResultCode(Constants.OPERATION_SUCCESS);
//			}
//			return new ResultCode(Constants.OPERATION_FAIL);
//		}catch(Exception e){
//			log.error("save error:",e);
//			return new ResultCode(Constants.OPERATION_FAIL);
//		}
//	}
	@ApiOperation(value = "组织导出")
	@RequestMapping(value = "export", method = RequestMethod.POST)
	@ResponseBody
	public void export(@RequestBody OrgReq entity){
		try {
			Org org = entity.getOrg();
			org.setIsLikeQuery(true);
			OrgType orgType=orgTypeService.findById(orgService.findById(entity.getId()).getOrgTypeId());
			Field field=new Field();
			field.setObjId(orgType.getId());
			List<Field> orgFields=sysFieldService.findList(field);
			//循环生成打印模型
			List<ExcelUtils.ExcelModel> models=new ArrayList<ExcelUtils.ExcelModel>();
			for (Field of : orgFields) {
				models.add(new ExcelUtils.ExcelModel(of.getRemark(),of.getName(),5000));
				entity.getExtraAttrs().put(of.getName(), "");
			}
			
			super.exportXlsx("org", orgService.export(org),models,orgType.getName());
		} catch (Exception e) {
			log.error("import acct error",e);
		}
	}
	
	
	@ApiOperation(value="根据组织编码获取所有子组织")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "orgSn", value = "组织编码", required = true, paramType="path", dataType = "String")
	})
	@RequestMapping(value="findOrgchildren/{orgSn}", method=RequestMethod.GET)
	@ResponseBody
	public OrgApiTree findOrgchildren(@PathVariable String orgSn) {
		orgSn=org.apache.commons.text.StringEscapeUtils.escapeHtml4(orgSn);
		OrgCUtil outil=new OrgCUtil();
		OrgApiTree resp=new OrgApiTree();
		CurrentAccount account=SessionManager.getSession(GatewayHttpUtil.getKey(Constants.CURRENT_SESSION_ID, request),request);
		String findOrgSQL="select id,name,parent_id,sn,name_path,status from im_org where sn='"+orgSn+"' and company_sn='"+account.getCompanySn()+"'"; 
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> orgs=jdbcService.findList(findOrgSQL);
		if(orgs!=null&&orgs.size()>0){
			List<Map<String, Object>> list =jdbcService.findList("select id,name,parent_id,sn,name_path,status from im_org where  COMPANY_SN='"+account.getCompanySn()+"' and ID_PATH like '%/"+orgs.get(0).get("id")+"/%' ORDER BY sn asc");
			List<OrgApiTree> strList = new ArrayList<OrgApiTree>();
			for(Map<String, Object> map:list){
				try{
					OrgApiTree model=new OrgApiTree(); 
					model.setId(Long.valueOf(map.get("id").toString()));
					model.setName(map.get("name").toString());
					model.setParentId(Long.valueOf(map.get("parent_id").toString()));
					model.setSn(map.get("sn").toString());
					model.setName_path(map.get("name_path").toString());
					model.setStatus(Integer.valueOf(map.get("status").toString()));
					strList.add(model);
				}catch (Exception e) {
					continue;
				}
			}
			List<OrgApiTree> listOrg=outil.menuList(strList);
			resp.setId(Long.valueOf(orgs.get(0).get("id").toString()));
			resp.setSn(orgs.get(0).get("sn").toString());
			resp.setName(orgs.get(0).get("name").toString());
			resp.setParentId(Long.valueOf(orgs.get(0).get("parent_id").toString()));
			resp.setName_path(orgs.get(0).get("name_path").toString());
			resp.setStatus(Integer.valueOf(orgs.get(0).get("status").toString()));
			resp.setChildren(listOrg);
		}
		return resp;
	}
	
	@ApiOperation(value = "授权组织兼职用户")
	@RequestMapping(value = "authOrgMultiUser", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode authOrgMultiUser(@RequestBody EnterAuthUserModel entity) {
		try {
			long orgId = Long.valueOf(entity.getOrgId());
			if(orgId!=0) {
				return userMultiOrgService.auth2(orgId, entity.getIds());
			} else {
				return new ResultCode(Constants.OPERATION_FAIL);
			}
		} catch (Exception e) {
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
	@ApiOperation(value = "取消组织内兼职用户")
	@RequestMapping(value = "clearAuth", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode clearAuth(@RequestBody EnterAuthUserModel entity) {
		try {
			long orgId = Long.valueOf(entity.getOrgId());
			if (orgId!=0) {
				return userMultiOrgService.clearAuth2(orgId, entity.getIds());
			} else {
				return new ResultCode(Constants.OPERATION_FAIL);
			}
		} catch (Exception e) {
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
}
