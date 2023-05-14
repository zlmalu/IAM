package com.sense.iam.api.action.sys;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import com.sense.core.util.CurrentAccount;
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
import com.sense.iam.api.model.FuncTree;
import com.sense.iam.api.model.sys.RoleReq;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.cam.TreeNode;
import com.sense.iam.model.sys.Func;
import com.sense.iam.model.sys.Role;
import com.sense.iam.service.SysFuncService;
import com.sense.iam.service.SysRoleService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiSort;

@Api(tags = "系统角色")
@Controller
@RestController
@RequestMapping("sys/role")
@ApiSort(value = 25)
public class RoleAction extends AbstractAction<Role,Long>{

	@Resource
	private SysFuncService sysFuncService;
	@Resource
	private SysRoleService sysRoleService;
	
	
	@ApiOperation(value="新增")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody RoleReq entity) {
		return super.save(entity.getRole());
	}
	
	@ApiOperation(value="更新")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	public ResultCode edit(@RequestBody RoleReq entity) {
		Role role=entity.getRole();
		return super.edit(role);
	}
	
	/**
	 * 根据角色ID加载功能树
	 * @param roleId
	 */
	@ApiOperation(value="根据角色标识获取权限列表")
	@ApiImplicitParams({
  	  @ApiImplicitParam(name = "roleId", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="loadFuncTree/{roleId}", method=RequestMethod.POST)
	@ResponseBody
	public FuncTree loadFuncTree(@PathVariable Long roleId){
		//获取所有系统功能
		Func func = new Func();
		func.setCompanySn(CurrentAccount.getCurrentAccount().getCompanySn());
		List<Func> list=sysFuncService.findList(func);
		
		Set<Long> authFunc=roleId==null? new HashSet<>():sysRoleService.findSysfuncIds(roleId);

		//获取角色已授权功能
		//定义树节点，并生成相应的属性结构数据
		List<TreeNode> treeList=new ArrayList<>();
		loadTree(treeList,Constants.TREE_ROOT_ID,list,authFunc);
		FuncTree funcTree=new FuncTree();
		funcTree.setAuthFuncs(authFunc);
		funcTree.setList(treeList);
		return funcTree;
	}
	
	/**
	 * 加载属性菜单
	 */
	private void loadTree(List<TreeNode> list,Long parentId,List<Func> sfs,Set<Long> authFuncList){
		Iterator<Func> it=sfs.iterator();
		List<TreeNode> treeNodeList;
		TreeNode node;
		while(it.hasNext()){
			Func sf=it.next();
			if(sf.getParentId().longValue()==parentId.longValue()){
				node=new TreeNode();
				node.setId(sf.getId().toString());
				node.setChecked(authFuncList.contains(sf.getId()));
				node.setText(sf.getName());
				treeNodeList=new ArrayList<>();
				loadTree(treeNodeList,sf.getId(),sfs,authFuncList);
				if(treeNodeList.size()==0){
					node.setLeaf(true);
				}else{
					node.setLeaf(false);
					node.setChildren(treeNodeList);
				}
				list.add(node);
			}
		}
	}
	
	
	@ApiOperation(value="查询所有角色")
	@RequestMapping(value="findAll", method=RequestMethod.GET)
	@ResponseBody
	public Object findAll() {
		return super.findAll(new Role());
	}
	
	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findById/{id}", method=RequestMethod.GET)
	@ResponseBody
	public Role findById(@PathVariable Long id) {
		return super.findById(id);
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
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findList", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 4)
	protected PageList<Role> findList(@RequestBody RoleReq entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			Role acct=entity.getRole();
			acct.setIsLikeQuery(true);
			return getBaseService().findPage(acct,page,limit);
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<>();
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
