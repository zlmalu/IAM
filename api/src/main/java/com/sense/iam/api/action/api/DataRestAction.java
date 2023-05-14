package com.sense.iam.api.action.api;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.sense.core.util.PageList;
import com.sense.iam.api.action.BaseAction;
import com.sense.iam.model.im.Account;
import com.sense.iam.model.im.Org;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.AppService;
import com.sense.iam.service.OrgService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiSort;

@Api(value = "API - 数据获取", tags = "数据获取")
@Controller
@RestController
@RequestMapping("rest/data")
@ApiSort(value = 1)
public class DataRestAction extends BaseAction{
	
	@Resource
	private AccountService accountService;
	
	@Resource
	private AppService appService;
	
	@Resource
	private OrgService orgService;
	
	
	/**
	 * 账号数据查询
	 * 
	 * @param orgId
	 * @param positionId
	 * @return
	 */
	@RequestMapping(value = "/acct", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ApiOperation(value = "账号数据")
	public Object acct(@RequestBody JSONObject json,HttpServletRequest request) {
		
		Account entity=new Account();
		entity.setAppSn(json.getString("appSn"));
		entity.setFilterStartTime(json.getString("startTime"));
		entity.setFilterEndTime(json.getString("endTime"));
		entity.setStatus(json.getInteger("status"));
		entity.setIsControl(false);
		//如果当前页和查询条数有值就按分页查询
		Integer pageSize=json.getInteger("pageSize");
		Integer currPage=json.getInteger("currPage");
		JSONObject res=new JSONObject();
		if(pageSize!=null&&currPage!=null){
			
			PageList<Account> plist=accountService.findPage(entity, currPage, pageSize);
			List<Account> list=plist.getDataList();
			res.put("data", list);
			res.put("total", plist.getTotalPageCount());
			res.put("count", plist.getTotalcount());
		}else{
			List<Account> acctList=accountService.findList(entity);
			res.put("data", acctList);
			res.put("count", acctList.size());
		}
		return res;
	}
	
	/**
	 * 组织数据查询
	 * 
	 * @param orgId
	 * @param positionId
	 * @return
	 */
	@RequestMapping(value = "/org", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ApiOperation(value = "组织数据")
	public Object org(@RequestBody JSONObject json,HttpServletRequest request) {
		Org org=new Org();
		org.setStatus(json.getInteger("status"));
		org.setFilterStartTime(json.getString("startTime"));
		org.setFilterEndTime(json.getString("endTime"));
		//如果当前页和查询条数有值就按分页查询
		Integer pageSize=json.getInteger("pageSize");
		Integer currPage=json.getInteger("currPage");
		JSONObject res=new JSONObject();
		if(pageSize!=null&&currPage!=null){
			
			PageList<Org> plist=orgService.findPage(org, currPage, pageSize);
			res.put("data", plist.getDataList());
			res.put("total", plist.getTotalPageCount());
			res.put("count", plist.getTotalcount());
		}else{
			List<Org> list=orgService.findList(org);
			res.put("data", list);
			res.put("count", list.size());
		}
		return res;
	}
}
