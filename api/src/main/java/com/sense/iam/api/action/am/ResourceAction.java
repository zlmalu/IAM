package com.sense.iam.api.action.am;

import java.util.List;

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
import com.sense.iam.api.model.am.AuthGroupResourceReq;
import com.sense.iam.api.model.am.AuthUserResourceReq;
import com.sense.iam.api.model.am.ResourceReq;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.am.Resource;
import com.sense.iam.model.am.ResourceGroup;
import com.sense.iam.model.im.UserResource;
import com.sense.iam.service.AmAuthReamlService;
import com.sense.iam.service.AmIpPolicyService;
import com.sense.iam.service.AmResourceService;
import com.sense.iam.service.AmTimePolicyService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiSort;

/**
 * 资源控制管理 - Action
 * @author K3w1n
 *
 */
@Api(tags = "资源控制管理")
@Controller
@RestController
@RequestMapping("amResource")
@ApiSort(value = 20)
public class ResourceAction extends AbstractAction<Resource, Long> {

	@javax.annotation.Resource
	AmResourceService amResourceService;
	@javax.annotation.Resource
	AmIpPolicyService amIpPolicyService;
	@javax.annotation.Resource
	AmTimePolicyService amTimePolicyService;
	@javax.annotation.Resource
	AmAuthReamlService amAuthReamlService;
	
	@ApiOperation(value = "新增资源管理表信息")
	@RequestMapping(value = "save", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody ResourceReq entity) {
		return amResourceService.save(entity.getResource());
	}
	
	@ApiOperation(value = "更新资源管理表信息")
	@RequestMapping(value ="edit", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	public ResultCode edit(@RequestBody ResourceReq entity) {
		return amResourceService.edit(entity.getResource());
	}
	
	@ApiOperation(value = "指定唯一标识查询")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType = "path", dataType = "Long")
	})
	@RequestMapping(value = "findById/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Resource findById(@PathVariable Long id) {
		Resource res = super.findById(id);
		if (res != null) {
			res.setListtime(amResourceService.findAmTime(id));
			res.setListip(amResourceService.findAmIp(id));
		}
		return res;
	}
	
	/**
	 * 分页查询列表
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	@ApiOperation(value = "分页查询列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", required = true, dataType = "Integer", value = "页码,默认0",example="0"),
		@ApiImplicitParam(name = "limit", required = true, dataType = "Integer", value = "页大小，默认20页", example = "20")
	})
	@RequestMapping(value = "findList", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 4)
	protected PageList<Resource> findList(@RequestBody ResourceReq entity, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer limit) {
		try {
			Resource r = entity.getResource();
			r.setIsLikeQuery(true);
			return getBaseService().findPage(r, page, limit);
		} catch (Exception e) {
			log.error("findList error:", e);
			return new PageList<Resource>();
		}
	}
	
	@ApiOperation(value="移除资源控制信息")
	@RequestMapping(value="remove", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 3)
	public ResultCode remove(@RequestBody @ApiParam(name = "唯一标识集合", value="多数据采取英文逗号分割", required = true)List<String> ids) {
		Params params = new Params();
		params.setIds(ids);
		return super.remove(params);
	}
	
	/**
	 * 添加关联人员后的显示
	 * @param id
	 * @param page
	 * @param limit
	 * @return
	 */
	@ApiOperation(value="添加关联人员页面")
	@RequestMapping(value = "finduserresourceid", method=RequestMethod.POST)
	@ResponseBody
	public Object finduserresourceid(@RequestBody Long id, @RequestParam(defaultValue="1") Integer page, @RequestParam(defaultValue="20") Integer limit) {
		UserResource entity = new UserResource();
		entity.setResourceId(id);
		return amResourceService.finduserresourceid(entity, page, limit);
	}
	
	@ApiOperation(value="删除管理人员页面")
	@RequestMapping(value = "removeuserresourceid", method=RequestMethod.POST)
	@ResponseBody
	public Object removeuserresourceid(@RequestBody Long id, @RequestParam(defaultValue="1") Integer page, @RequestParam(defaultValue="20") Integer limit) {
		UserResource entity = new UserResource();
		entity.setResourceId(id);
		return amResourceService.removeuserresourceid(entity, page, limit);
	}
	
	/**
	 * 添加资源控制关联人员
	 * @param userIds
	 * @param resourceIds
	 * @return
	 */
	@ApiOperation(value="添加资源控制相关人员")
	@RequestMapping(value="authResource", method = RequestMethod.POST)
	@ResponseBody
	public ResultCode authResource(@RequestBody AuthUserResourceReq userResourceReq){
		try {
			amResourceService.addresourceacct(userResourceReq.getUserIds(), userResourceReq.getResourceIds());
			return new ResultCode(Constants.OPERATION_SUCCESS);
		} catch (Exception e) {
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
	/**
	 * 移除资源控制关联人员
	 * @param userIds
	 * @param groupIds
	 * @return
	 */
	@ApiOperation(value="移除资源控制相关人员")
	@RequestMapping(value="removeresourceacct",method = RequestMethod.POST)
	@ResponseBody
	public ResultCode removeresourceacct(@RequestBody AuthUserResourceReq userResourceReq){
		try {
			amResourceService.removeresourceacct(userResourceReq.getUserIds(), userResourceReq.getResourceIds());
			return new ResultCode(Constants.OPERATION_SUCCESS);
		} catch (Exception e) {
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
	/**
	 * 添加用户组
	 * @param groupid
	 * @param resourceid
	 * @return
	 */
	@ApiOperation(value="添加用户组")
	@RequestMapping(value="groupResource", method = RequestMethod.POST)
	@ResponseBody
	public ResultCode groupResource(@RequestBody AuthGroupResourceReq groupResourceReq){
		try {
			amResourceService.addresourcegroupacct(groupResourceReq.getGroupIds(), groupResourceReq.getResourceIds());
			return new ResultCode(Constants.OPERATION_SUCCESS);
		} catch (Exception e) {
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
	
	@ApiOperation(value="移除用户组")
	@RequestMapping(value="removeresourcegroupacct",method = RequestMethod.POST)
	@ResponseBody
	public ResultCode removeresourcegroupacct(@RequestBody AuthGroupResourceReq groupResourceReq){
		try {
			amResourceService.removeresourcegroupacct(groupResourceReq.getGroupIds(), groupResourceReq.getResourceIds());
			return new ResultCode(Constants.OPERATION_SUCCESS);
		} catch (Exception e) {
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
	@ApiOperation(value="添加用户组页面")
	@RequestMapping(value = "findgroupresourceid", method=RequestMethod.POST)
	@ResponseBody
	public Object findgroupresourceid(@RequestBody Long id, @RequestParam(defaultValue="1") Integer page, @RequestParam(defaultValue="20") Integer limit) {
		ResourceGroup entity = new ResourceGroup();
		entity.setResourceId(id);
		return amResourceService.findgroupresourceid(entity, page, limit);
	}
	
	@ApiOperation(value="删除用户组页面")
	@RequestMapping(value = "removegroupresourceid", method=RequestMethod.POST)
	@ResponseBody
	public Object removegroupresourceid(@RequestBody Long id, @RequestParam(defaultValue="1") Integer page, @RequestParam(defaultValue="20") Integer limit) {
		ResourceGroup entity = new ResourceGroup();
		entity.setResourceId(id);
		return amResourceService.removegroupresourceid(entity, page, limit);
	}	
}
