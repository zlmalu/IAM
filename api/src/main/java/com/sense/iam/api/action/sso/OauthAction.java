package com.sense.iam.api.action.sso;

import java.util.ArrayList;
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
import com.sense.iam.api.model.sso.OAuthReq;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.sso.Oauth;
import com.sense.iam.service.AppService;
import com.sense.iam.service.SsoOauthService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = "OAUTH SSO认证协议配置")
@Controller
@RestController
@RequestMapping("oauth")
public class OauthAction extends AbstractAction<Oauth,Long>{
	
	@Resource
	private SsoOauthService ssoOauthService;
	@Resource
	private AppService appService;
	
	@ApiOperation(value="新增")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode save(@RequestBody OAuthReq entity) {
		Oauth oauth=entity.getOauth();
		if(oauth.getId()==0)oauth.setId(null);
		if(isExist(oauth.getAppId())){
			return super.save(oauth);
		}
		return new ResultCode(Constants.OPERATION_EXIST);
	}
	
	@ApiOperation(value="更新")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode edit(@RequestBody OAuthReq entity) {
		Oauth oauth=entity.getOauth();
		return super.edit(oauth);
	}
	
	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findById/{id}", method=RequestMethod.GET)
	@ResponseBody
	public Oauth findById(@PathVariable Long id) {
		return super.findById(id);
	}
	
	@ApiOperation(value="分页查询")
	@RequestMapping(value="findList", method=RequestMethod.POST)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "appSn", required=false,value = "应用编号", dataType = "String"),
		 @ApiImplicitParam(name = "appName", required=false,value = "应用名称",dataType = "String"),
		 @ApiImplicitParam(name = "tagEndType", required=false,value = "终端类型",dataType = "String"),
		 @ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		 @ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@ResponseBody
	public PageList<Oauth> findList(@RequestParam String appSn, @RequestParam String appName, @RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit) {
		try {
			Oauth oauth= new Oauth();
			if(StringUtils.isNotEmpty(appName)){
				oauth.setAppName(appName);
			}
			if(StringUtils.isNotEmpty(appSn)){
				oauth.setAppSn((appSn));
			}
			oauth.setCompanySn(CurrentAccount.getCurrentAccount().getCompanySn());
			oauth.setIsLikeQuery(true);
			return ssoOauthService.findPage(oauth, page, limit);
		} catch (Exception e) {
			e.printStackTrace();
			return new PageList<Oauth>();
		}
		
	}
	
	@ApiOperation(value="查询应用配置信息")
	@RequestMapping(value="findSSOList", method=RequestMethod.POST)
	@ApiImplicitParams({
		 @ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		 @ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@ResponseBody
	public PageList<Oauth> findSSOList(@RequestParam(defaultValue="0") Integer page,@RequestParam(defaultValue="20") Integer limit) {
		try {
			return ssoOauthService.findPage(new Oauth(), page, limit);
		} catch (Exception e) {
			e.printStackTrace();
			return new PageList<Oauth>();
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
		Oauth entity=new Oauth();
		entity.setAppId(appId);
		return ssoOauthService.findList(entity).size()==0?true:false;
	}
	
	
}
