package com.sense.iam.open.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.sense.core.security.UIM;
import com.sense.core.util.GatewayHttpUtil;
import com.sense.iam.api.action.BaseAction;
import com.sense.iam.api.model.im.AccountModel;
import com.sense.iam.cam.Constants;
import com.sense.iam.model.im.Account;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.AppService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiSort;

@Api(value = "API - 应用接口", tags = "应用接口")
@Controller
@RestController
@RequestMapping("open/appAccount")
@ApiSort(value = 1)
public class OpenAppAccountAction extends BaseAction{
	
	@Resource
	private AccountService accountService;
	
	@Resource
	private AppService appService;
	
	
	
	/**
	 * 获取应用连接接口
	 * @param appSn
	 * @return tree
	 */
	@ApiOperation(value="获取单点应用连接接口",code=1)
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "userSn", value = "用户工号", required = true, paramType="path", dataType = "String"),
		 @ApiImplicitParam(name = "clientId", value = "应用接口标识", required = true, paramType="path", dataType = "String")
	})
	@RequestMapping(value="getList",method={RequestMethod.POST},produces={"text/html;charset=UTF-8;","application/json;"})
	@ResponseBody
	public List<AccountModel> getList(@RequestParam String userSn,@RequestParam String clientId){
		Account ac=new Account();
		ac.setAppSn(clientId);
		ac.setUserSn(userSn);
		//启用的账号
		ac.setStatus(1);
		log.info("userSn:"+userSn);
		log.info("clientId:"+clientId);
		ac=accountService.findByObject(ac);
		List<AccountModel> newList=new ArrayList<AccountModel>();
		if(ac!=null){
			//用户ID
			Long applyUserId=ac.getUserId();
			log.info("get userId:"+applyUserId);
			ac=new Account();
			ac.setUserId(applyUserId);
			ac.setIsView(1);
			ac.setStatus(1);
			List<Account> list=accountService.findList(ac);
			if(list!=null){
				Iterator it=list.iterator();
				while (it.hasNext()) {
					Account model = (Account) it.next();
					if("APP001".equals(model.getAppSn()))continue;
					AccountModel m=new AccountModel();
					m.setAppName(model.getAppName());
					m.setAppSn(model.getAppSn());
					String ssoLink=GatewayHttpUtil.getKey("RemoteServer", request)+"/sso/request?ssoToken="+UIM.encode(model.getId()+"_"+System.currentTimeMillis()+"_"+GatewayHttpUtil.getKey(Constants.CURRENT_SSO_SESSION_ID, request));
					String imgLink=GatewayHttpUtil.getKey("RemoteServer", request)+"/api/image/viewImage/app/"+model.getAppId();
					m.setImgLink(imgLink);
					m.setSsoLink(ssoLink);
					m.setTokenId(model.getId());
					newList.add(m);
				}
			}
			return newList;
		}else{
			return newList;
		}
	}

}
