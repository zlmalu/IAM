package com.sense.iam.api.action.audit;

import java.util.ArrayList;
import java.util.Enumeration;
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

import com.sense.core.freemark.StringParse;
import com.sense.core.model.BaseModel;
import com.sense.core.util.CurrentAccount;
import com.sense.core.util.ExcelUtils;
import com.sense.core.util.PageList;
import com.sense.core.util.StringUtils;
import com.sense.iam.api.action.BaseAction;
import com.sense.iam.api.model.audit.AuditReq;
import com.sense.iam.cam.TreeNode;
import com.sense.iam.model.sys.LogConfig;
import com.sense.iam.model.sys.ReportConfig;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.SysLogConfigService;
import com.sense.iam.service.SysReportConfigService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiSort;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Api(tags = "审计管理")
@Controller
@RestController
@RequestMapping("audit")
@ApiSort(value = 23)
public class AuditAction extends BaseAction {
	@Resource
	private SysLogConfigService syslogConfigService;
	@Resource
	private SysReportConfigService sysReportConfigService;
	
	@SuppressWarnings("unchecked")
	@ApiOperation(value="审计菜单加载")
	@RequestMapping(value="loadNode",method=RequestMethod.GET)
	@ResponseBody
	public Object loadNode(){
		List<TreeNode> list=new ArrayList<TreeNode>();
		TreeNode treeNode;
		//日志审计
		String path=getFuncName(AuditAction.class.getName()+"!loadLogNode");
		Map<String, Object> pfs=CurrentAccount.getCurrentAccount().getPfs();
		if(pfs.containsKey(path)){
			TreeNode logTreeNode=new TreeNode();
			logTreeNode.setId("AUDIT_LOG_MANAGER");
			logTreeNode.setText("日志审计");
			logTreeNode.setChildren(new ArrayList<TreeNode>());
			list.add(logTreeNode);
			LogConfig lConfig = new LogConfig();
			lConfig.setCompanySn(CurrentAccount.getCurrentAccount().getCompanySn());
			List<LogConfig> logConfigList=syslogConfigService.findList(lConfig);
			for (LogConfig logConfig : logConfigList) {
				if(CurrentAccount.getCurrentAccount().getPfs().containsKey(getFuncName(AuditAction.class.getName()+"!"+logConfig.getId()))){
					if(logConfig.getType()==1){
						treeNode=new TreeNode();
						treeNode.setId("AUDIT_"+logConfig.getId());
						treeNode.setText(logConfig.getName());
						treeNode.setLeaf(true);
						treeNode.getAttrMap().put("action", "audit.action?operate=findList&logConfigId="+logConfig.getId());
						logTreeNode.getChildren().add(treeNode);
					}
				}
			}
		}
		//安全审计
		if(CurrentAccount.getCurrentAccount().getPfs().containsKey(getFuncName(AuditAction.class.getName()+"!findList"))){
			TreeNode safetyTreeNode=new TreeNode();
			safetyTreeNode.setId("SAFETY_MANAGER");
			safetyTreeNode.setText("安全审计");
			safetyTreeNode.setChildren(new ArrayList<TreeNode>());
			list.add(safetyTreeNode);
			LogConfig lConfig = new LogConfig();
			lConfig.setCompanySn(CurrentAccount.getCurrentAccount().getCompanySn());
			List<LogConfig> logConfigList=syslogConfigService.findList(lConfig);
			for (LogConfig logConfig : logConfigList) {
				if(CurrentAccount.getCurrentAccount().getPfs().containsKey(getFuncName(AuditAction.class.getName()+"!"+logConfig.getId()))){
					if(logConfig.getType()==2){
						treeNode=new TreeNode();
						treeNode.setId("AUDIT_"+logConfig.getId());
						treeNode.setText(logConfig.getName());
						treeNode.setLeaf(true);
						treeNode.getAttrMap().put("action", "audit.action?operate=findList&logConfigId="+logConfig.getId());
						safetyTreeNode.getChildren().add(treeNode);
					}
				}
			}
		}
		//报表审计
		if(CurrentAccount.getCurrentAccount().getPfs().containsKey(getFuncName(AuditAction.class.getName()+"!loadReportNode"))){
			TreeNode reportTreeNode=new TreeNode();
			reportTreeNode.setId("AUDIT_REPORT_MANAGER");
			reportTreeNode.setText("审计报表");
			reportTreeNode.setChildren(new ArrayList<TreeNode>());
			list.add(reportTreeNode);
			ReportConfig rConfig = new ReportConfig();
			rConfig.setCompanySn(CurrentAccount.getCurrentAccount().getCompanySn());
			List<ReportConfig> reportConfigList=sysReportConfigService.findList(rConfig);
			for (ReportConfig reportConfig : reportConfigList) {
				if(CurrentAccount.getCurrentAccount().getPfs().containsKey(getFuncName(AuditAction.class.getName()+"!"+reportConfig.getId()))){
					treeNode=new TreeNode();
					treeNode.setId("AUDIT_"+reportConfig.getId());
					treeNode.setText(reportConfig.getName());
					treeNode.setLeaf(true);
					treeNode.getAttrMap().put("action", "audit.action?operate=findReport&reportConfigId="+reportConfig.getId());
					reportTreeNode.getChildren().add(treeNode);
				}
			}
		}
		return list;
	}
	
	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="loadGrid/{id}",method=RequestMethod.GET)
	@ResponseBody
	public Object loadGrid(@PathVariable  Long id){
		String gridStr=syslogConfigService.findById(id).getGridConfig();
		Map<String, Object> resultMap=new HashMap<String, Object>();
		Map<String, String> map;
		try {
			map = com.sense.core.util.XMLUtil.simpleXml2Map(gridStr);
			
			resultMap.put("column",JSONArray.fromObject("["+map.get("column").toString().replace("\n","").replace("\t", "")+"]") );
			resultMap.put("control", JSONArray.fromObject("["+map.get("control").toString().replace("\n","").replace("\t", "")+"]"));
			resultMap.put("extcontrol", map.get("extcontrol"));
		} catch (Exception e) {
			log.error("xml parse error",e);
		}
		return resultMap;
	}
	
	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="loadReport/{id}",method=RequestMethod.GET)
	@ResponseBody
	public Object loadReport(@PathVariable  Long id){
		String pageStr=sysReportConfigService.findById(id).getPageConfig();
		Map<String, Object> resultMap=new HashMap<String, Object>();
		Map<String, String> map;
		try {
			map = com.sense.core.util.XMLUtil.simpleXml2Map(pageStr);
			resultMap.put("view",JSONObject.fromObject(map.get("view").toString().replace("\n","").replace("\t", "")));
			resultMap.put("control", JSONArray.fromObject("["+map.get("control").toString().replace("\n","").replace("\t", "")+"]"));
			resultMap.put("extcontrol", map.get("extcontrol"));
		} catch (Exception e) {
			log.error("xml parse error",e);
		}
		return resultMap;
	}
	
	@Resource
	private JdbcService jdbcService;
	
	/**
	 * 分页查询日志配置信息
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	@ApiOperation(value="分页查询日志配置信息")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@SuppressWarnings("rawtypes")
	@RequestMapping(value="findList",method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public Object findList(@RequestBody AuditReq auditReq,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			String sql=syslogConfigService.findById(auditReq.getLogConfigId()).getFindConfig();
			log.debug("find log use sql:"+sql);
			return this.findPage(auditReq.getParamMap(),sql, page, limit);
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList();
		}
	}
	
	
	@ApiOperation(value="分页查询报表配置信息")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@SuppressWarnings("rawtypes")
	@RequestMapping(value="findReport",method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	public Object findReport(@RequestBody AuditReq auditReq,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			String sql=sysReportConfigService.findById(auditReq.getLogConfigId()).getFindConfig();
			log.debug("find report use sql:"+sql);
			return this.findPage(auditReq.getParamMap(),sql, page, limit);
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList();
		}
	}
	
	private Object findPage(Map requestParameterMap,String sql,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit) throws Exception{

			Map<String,String> paramMap=new HashMap<String,String>();
			if(requestParameterMap==null)requestParameterMap=new HashMap();
			BaseModel model=new BaseModel(){};
			for (Object obj : requestParameterMap.keySet()) {
				//判断是否排序字段
				String key=obj.toString();
				String value=requestParameterMap.get(key).toString();
				if(key.equals("sort")){
					model.setSort(StringUtils.getString(value));
					paramMap.put(key,StringUtils.getString(model.getSort()));
				}else{
					paramMap.put(key,StringUtils.getString(value));
				}
			}
			paramMap.put("companySn", CurrentAccount.getCurrentAccount().getCompanySn());//放入公司编码
			paramMap.put("currentAccount", CurrentAccount.getCurrentAccount().getLoginName());
			return jdbcService.findPage(StringParse.parse(sql,paramMap),model,page,limit);
	}
	
	
	@ApiOperation(value="导出Excel")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "logConfigId", value = "日志配置唯一标识", required = true, paramType="path", dataType = "Long"),
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="export",method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 3)
	public void export(Long logConfigId,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="50000") Integer limit){
		try {
			//加载配置信息，并获取导出列
			Map resultMap=(Map) loadGrid(logConfigId);
			JSONArray columns=(JSONArray) resultMap.get("column");
			List list=new ArrayList();
			for (Object object : columns) {
				list.add(new ExcelUtils.ExcelModel(((JSONObject)object).getString("text"),((JSONObject)object).getString("dataIndex"),3000));
			}
			AuditReq auditReq=new AuditReq();
			auditReq.setLogConfigId(logConfigId);
			Map<String,String> paramsMap=new HashMap<String,String>();
			Enumeration<String> enu=request.getParameterNames();
			if(enu!=null){
				while(enu.hasMoreElements()){
					String name=enu.nextElement();
					paramsMap.put(name, request.getParameter(name));
				}
			}
			auditReq.setParamMap(paramsMap);
			super.exportXlsx("account", ((PageList)this.findList(auditReq, 0, 50000)).getDataList(),list);
		} catch (Exception e) {
			log.error("import acct error",e);
		}
	}
}
