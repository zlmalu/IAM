package com.sense.iam.api.action.sso;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
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
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.api.model.sso.JwtReq;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.sso.Jwt;
import com.sense.iam.service.AppService;
import com.sense.iam.service.SsoJwtService;

@Api(tags = "JWT认证协议配置")
@Controller
@RestController
@RequestMapping("jwt")
public class JwtAction extends AbstractAction<Jwt,Long>{
	@Resource
	private SsoJwtService ssoJwtService;
	@Resource
	private AppService appService;
	
	@ApiOperation(value="新增")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode save(@RequestBody JwtReq entity) {
		Jwt jwt=entity.getJwt();
		if(jwt.getId()==0)jwt.setId(null);
		if(isExist(jwt.getAppId())){
			return super.save(jwt);
		}
		return new ResultCode(Constants.OPERATION_EXIST);
	}
	
	
	@ApiOperation(value="更新")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode edit(@RequestBody JwtReq entity) {
		Jwt jwt=entity.getJwt();
		return super.edit(jwt);
	}
	
	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findById/{id}", method=RequestMethod.GET)
	@ResponseBody
	public Jwt findById(@PathVariable Long id) {
		return super.findById(id);
	}
	
	@ApiOperation(value="分页查询")
	@RequestMapping(value="findList", method=RequestMethod.POST)
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "appSn", required=false,value = "应用编号", dataType = "String"),
		 @ApiImplicitParam(name = "appName", required=false,value = "应用名称",dataType = "String"),
		 @ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		 @ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@ResponseBody
	public PageList<Jwt> findList(@RequestParam String appSn, @RequestParam String appName, @RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit) {
		try {
			Jwt jwt = new Jwt();
			if(StringUtils.isNotEmpty(appName)){
				jwt.setAppName(appName);
			}
			if(StringUtils.isNotEmpty(appSn)){
				jwt.setAppSn((appSn));
			}
			jwt.setIsLikeQuery(true);
			jwt.setCompanySn(CurrentAccount.getCurrentAccount().getCompanySn());
			return ssoJwtService.findPage(jwt, page, limit);
		} catch (Exception e) {
			e.printStackTrace();
			return new PageList<Jwt>();
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
	
	
	private boolean isExist(Long appId){
		Jwt entity=new Jwt();
		entity.setAppId(appId);
		return ssoJwtService.findList(entity).size()==0?true:false;
	}
	
	
}
