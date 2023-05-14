package com.sense.iam.open.action;

import java.util.List;



import javax.annotation.Resource;



import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sense.core.util.CurrentAccount;
import com.sense.iam.api.action.BaseAction;
import com.sense.iam.api.model.im.AppFuncTree;
import com.sense.iam.api.util.AppFuncUtil;
import com.sense.iam.model.im.App;
import com.sense.iam.model.im.AppFunc;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.AppFuncService;
import com.sense.iam.service.AppService;
import com.sense.iam.service.JdbcService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiSort;

@Api(value = "API - 应用权限接口", tags = "应用权限接口")
@Controller
@RestController
@RequestMapping("open/appFunc")
@ApiSort(value = 1)
public class OpenAppFuncAction extends BaseAction{

	@Resource
	private AppFuncService appFuncService;
	
	@Resource
	private JdbcService jdbcService;
	
	@Resource
	private AccountService accountService;
	
	@Resource
	private AppService appService;
	
	/**
	 * 根据应用编码查询应用权限树
	 * @param appSn
	 * @return tree
	 */
	@ApiOperation(value="根据应用编码查询应用权限树",code=1)
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "appSn", value = "应用编码", required = true, paramType="path", dataType = "String")
	})
	@RequestMapping(value="findAppTreeByAppSn/{appSn}",method={RequestMethod.GET})
	@ResponseBody
	public AppFuncTree findAppTreeByAppSn(@PathVariable String appSn){
		appSn=StringEscapeUtils.escapeHtml4(appSn);
		App app=new App();
		app.setCompanySn(CurrentAccount.getCurrentAccount().getCompanySn());
		app.setSn(appSn);
		app.setIsControl(false);
		app=appService.findByObject(app);
		AppFuncTree resuleCode=new AppFuncTree();
		resuleCode.setAppSn(appSn);
		if(app!=null){
			resuleCode.setAppId(app.getId());
			resuleCode.setAppName(app.getName());
			AppFunc funcModel=new AppFunc();
			funcModel.setAppId(app.getId());
			funcModel.setCompanySn(CurrentAccount.getCurrentAccount().getCompanySn());
			funcModel.setIsControl(false);
			//appFuncService,appFuncDao层重写findList，新增扩展字段和关联权限的数据返回
			List<AppFunc> list=appFuncService.findList(funcModel);
			//构建树形对象
			resuleCode.setChildren(AppFuncUtil.menuList(list));
		}	
		return resuleCode;
	}

}
