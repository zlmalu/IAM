package com.sense.iam.api.action.am;


import java.util.List;

import javax.annotation.Resource;

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
import com.sense.iam.api.model.am.BlackReq;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.am.Black;
import com.sense.iam.model.im.User;
import com.sense.iam.service.AmBlackService;
import com.sense.iam.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiSort;

/**
 * 黑名单管理 - Action
 * @author K3w1n
 *
 */
@Api(tags = "黑名单管理")
@Controller
@RestController
@RequestMapping("amBlack")
@ApiSort(value = 17)
public class BlackAction extends AbstractAction<Black, Long> {

	@Resource
	UserService userService;
	@Resource
	AmBlackService amBlackService;
	
	@ApiOperation(value = "新增")
	@RequestMapping(value = "save", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody BlackReq entity) {
		Black black=entity.getBlack();
		User user = new User();
		user.setIsLikeQuery(false);
		user.setSn(black.getUserId());
		if(userService.findByObject(user)==null) {
			return new ResultCode(Constants.OPERATION_NOT_EXIST,"用户不存在");
		}
		if(amBlackService.findByObject(black)!=null) {
			return new ResultCode(Constants.OPERATION_EXIST,"请勿重复添加");
		}
		return amBlackService.save(entity.getBlack());
	}

	@ApiOperation(value = "更新")
	@RequestMapping(value ="edit", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	public ResultCode edit(@RequestBody BlackReq entity) {
		Black black=entity.getBlack();
		User user = new User();
		user.setIsLikeQuery(false);
		user.setSn(black.getUserId());
		if(userService.findByObject(user)==null) {
			return new ResultCode(Constants.OPERATION_FAIL,"用户不存在");
		}

		return amBlackService.edit(black);
	}
	
	@ApiOperation(value = "指定唯一标识查询")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType = "path", dataType = "Long")
	})
	@RequestMapping(value = "findById/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Black findById(@PathVariable Long id) {
		return super.findById(id);
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
	protected PageList<Black> findList(@RequestBody BlackReq entity, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer limit) {
		try {
			Black b = entity.getBlack();
			b.setIsLikeQuery(true);
			return getBaseService().findPage(b, page, limit);
		} catch (Exception e) {
			log.error("findList error:", e);
			return new PageList<Black>();
		}
	}
	
	@ApiOperation(value="移除")
	@RequestMapping(value="remove", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 3)
	public ResultCode remove(@RequestBody @ApiParam(name = "唯一标识集合", value="多数据采取英文逗号分割", required = true)List<String> ids) {
		Params params = new Params();
		params.setIds(ids);
		return super.remove(params);
	}
}
