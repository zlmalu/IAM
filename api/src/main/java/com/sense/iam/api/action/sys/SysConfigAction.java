package com.sense.iam.api.action.sys;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sense.core.util.PageList;
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.api.model.sys.SaasOpenReq;
import com.sense.iam.api.model.sys.SysConfigReq;
import com.sense.iam.cache.CacheSender;
import com.sense.iam.cache.SysConfigCache;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.am.AuthReaml;
import com.sense.iam.model.sso.Oauth;
import com.sense.iam.model.sys.Company;
import com.sense.iam.model.sys.Config;
import com.sense.iam.service.SysCompanyService;
import com.sense.iam.service.SysConfigService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;


/**
 * 系统配置模块
 * 
 * Description: 系统配置模块
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Api(tags = "系统配置模块")
@RestController
@Controller
@RequestMapping("sys/config")
public class SysConfigAction extends AbstractAction<Config,Long>{

	@Resource
	private SysConfigCache sysConfigCache;
	
	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findById/{id}", method=RequestMethod.GET)
	@ResponseBody
	public Config findById(@PathVariable Long id) {
		return super.findById(id);
	}
	
	/**
	 * 分页查询系统配置列表
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	@ApiOperation(value="分页查询系统配置列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findList", method=RequestMethod.POST)
	@ResponseBody
	protected PageList<Config> findList(@RequestBody SysConfigReq entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			Config entity1=entity.getConfig();
			entity1.setIsLikeQuery(true);
			return getBaseService().findPage(entity1,page,limit);
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<Config>();
		}
	}
	
	@ApiOperation(value="新增")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode save(@RequestBody SysConfigReq entity) {
		return super.save(entity.getConfig());
	}
	
	@ApiOperation(value="更新")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode edit(@RequestBody SysConfigReq entity) {
		Config config=entity.getConfig();
		return super.edit(config);
	}
	
	@ApiOperation(value="移除")
    @RequestMapping(value="remove",method = RequestMethod.POST)
	@ResponseBody
    public ResultCode remove(@RequestBody @ApiParam(name="唯一标识集合",value="多数据采用英文逗号分割",required=true)List<String> ids) {
		Params params=new Params();
		params.setIds(ids);
		return super.remove(params);
    }
	
	/**
	 * 查询许可证信息
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@ApiOperation(value="许可证信息")
    @RequestMapping(value="licenceInfo",method = RequestMethod.GET)
	@ResponseBody
    public List<Map<String, String>> licenceInfo() {
		Config entity = new Config();
		Object findAll = super.findAll(entity);
		List<Config> configList = new ArrayList<Config>();
		configList.addAll((Collection<? extends Config>) findAll);
		Map<String, String> map = new HashMap<String, String>();
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		for (Config config : configList) {
			map.put(config.getName(), config.getValue());
		}
		list.add(map);
		return list;
    }
	
	@Resource
	private SysConfigService sysConfigService;
	
	@Resource
	protected CacheSender cacheSender;
	
	/**
	 * 许可证导入
	 * @param file
	 * @return
	 */
	@ApiOperation(value="许可证导入")
	@RequestMapping(value="licenceRegister",method = RequestMethod.POST)
	@ResponseBody
	public Object licenceRegister(@ApiParam(name="file",value="许可证导入",required=false) @RequestParam(required=false,name="file",value="file") MultipartFile file){
		if(file!=null){
			BufferedReader br=null;
			try {
				br=new BufferedReader(new InputStreamReader(file.getInputStream(),"UTF-8"));
				String line;
				Config config;
				int startType=1;
				while((line=br.readLine())!=null){
					//读取版本和签名信息
					if(line.startsWith("version:")){
						config=new Config();
						config.setName("SERVER_VERSION");
						config=sysConfigService.findByObject(config);
						config.setValue(line.replace("version:", "").trim());
						sysConfigService.edit(config);
						cacheSender.sendMsg(config);
					}
					if(line.startsWith("RAW-UTF8:")){
						config=new Config();
						config.setName("SERVER_LICENCE_SGIN");
						config=sysConfigService.findByObject(config);
						config.setValue(line.replace("RAW-UTF8:", "").trim());
						sysConfigService.edit(config);
						cacheSender.sendMsg(config);
					}
					
					if(line.indexOf("-------------------- Information ---------------")!=-1){
						startType=2;
						continue;
					}
					
					if(line.indexOf("---------------------system function----------------------")!=-1){
						startType=3;
						continue;
					}
					//功能权限信息
					if(startType==2 && !line.trim().equals("")){
						config=new Config();
						int position=line.indexOf(":");
						config.setName(line.substring(0, position));
						config=sysConfigService.findByObject(config);
						config.setValue(line.substring(position+1));
						sysConfigService.edit(config);
						cacheSender.sendMsg(config);
					}
					//功能权限信息
					if(startType==3){
						//funcList.add(line);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				if(br!=null){
					try{br.close();}catch(Exception e){}
				}
			}
		}
		//刷新许可证
		sysConfigCache.loadAll();
		return new ResultCode(Constants.OPERATION_SUCCESS);
	}
	

	@Resource
	private SysCompanyService sysCompanyService;
	
	/**
	 * 开通公司SAAS服务
	 * @return
	 * description :  
	 * wenjianfeng 2019年7月29日
	 * update shibanglin,date:2019-12-10 10:01
	 */
	@RequestMapping(value="openIam",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value="IMASS开通企业用户")
	public ResultCode openIam(@RequestBody Company company){
		try{
			ResultCode result=sysCompanyService.openIAM(company);
			cacheSender.sendMsg(new AuthReaml());//发送认证域的缓存刷新
			cacheSender.sendMsg(new Oauth());
			return result;
		}catch(Exception e){
			log.error(e);
			return new ResultCode(Constants.OPERATION_UNKNOWN);
		}
	}
	
	/**
	 * 开通公司SAAS应用访问权限
	 * @return
	 * description :  
	 * wenjianfeng 2019年7月29日
	 * update shibanglin,date:2019-12-10 10:01
	 */
	@RequestMapping(value="openApp",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value="IMASS开通应用资源")
	public ResultCode openApp(@RequestBody SaasOpenReq param){
		try{
			ResultCode result=sysCompanyService.openApp(param.getCompanySn(),param.getAppId(),param.getAppPath());
			cacheSender.sendMsg(new AuthReaml());//发送认证域的缓存刷新
			cacheSender.sendMsg(new Oauth());
			return result;
		}catch(Exception e){
			log.error(e);
			return new ResultCode(Constants.OPERATION_UNKNOWN);
		}
	}
	
}
