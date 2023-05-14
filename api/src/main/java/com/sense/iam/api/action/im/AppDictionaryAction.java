package com.sense.iam.api.action.im;



import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sense.core.util.CurrentAccount;
import com.sense.core.util.PageList;
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.api.model.im.AppDictionaryModel;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.im.AppDictionary;
import com.sense.iam.model.im.OrgType;
import com.sense.iam.service.AccountAppFuncService;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.AppDictionaryService;
import com.sense.iam.service.AppElementService;
import com.sense.iam.service.AppFuncService;
import com.sense.iam.service.AppService;
import com.sense.iam.service.JdbcService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiSort;

@Api(tags = "权限字典")
@Controller
@RestController
@RequestMapping("im/appDictionary")
@ApiSort(value = 1)
public class AppDictionaryAction extends AbstractAction<OrgType,Long>{
	
	
	@Resource
	private AppService appService;
	
	@Resource
	private AppElementService appElementService;
	
	@Resource
	private AppDictionaryService appDictionaryService;
	

	@Resource
	private AppFuncService appFuncService;
	@Resource
	private AccountService accountService;

	@Resource
	private JdbcService jdbcService;
	@Resource
	private AccountAppFuncService accountAppFuncService;
	
	
	/**
	 * 查询所有字典类型
	 * @param entity 查询所有类型
	 * @return
	 */
	@ApiOperation(value="分页查询字典类型列表")
	@RequestMapping(value="findAll", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 4)
	protected List<AppDictionary> findAll(){
		return appDictionaryService.findAll();
	}
	/**
	 * 分页查询字典列表
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	@ApiOperation(value="分页查询字典列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认10页",example="10")
	})
	@RequestMapping(value="findList", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 4)
	protected PageList<AppDictionary> findList(@RequestBody AppDictionaryModel entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="10") Integer limit){
		try{
			AppDictionary mo=new AppDictionary();
			mo.setName(StringEscapeUtils.escapeHtml4(entity.getName()));
			mo.setSn(StringEscapeUtils.escapeHtml4(entity.getSn()));
			mo.setIsLikeQuery(true);
			PageList<AppDictionary> list=appDictionaryService.findPage(mo, page, limit);
			if(list!=null&&list.getDataList()!=null&&list.getDataList().size()>0){
				for(int i=0;i<list.getDataList().size();i++){
					//判断是否存在关联关系
					List<Map<String, Object>> listsd=jdbcService.findList("SELECT NAME FROM IM_APP_ELEMENT WHERE CONFIG LIKE '%"+list.getDataList().get(i).getId()+"%' AND COMPANY_SN='"+CurrentAccount.getCurrentAccount().getCompanySn()+"' and IS_DELETE=1");
					String elements="无";
					if(listsd!=null&&listsd.size()>0){
						elements="";
						for(int j=0;j<listsd.size();j++){
							if(j+1==listsd.size()){
								elements+=listsd.get(j).get("NAME").toString();
							}else{
								elements+=listsd.get(j).get("NAME").toString()+",";
							}
						}
					}
					list.getDataList().get(i).setElements(elements);
				}
			}
			return list;
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<AppDictionary>();
		}
	}
	
	
	
	
	
	@ApiOperation(value="新增字典")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody AppDictionary entity) {
		try{
			return appDictionaryService.save(entity);
		}catch(Exception e){
			log.error("save error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
	@ApiOperation(value="修改字典")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode edit(@RequestBody AppDictionary entity) {
		try{
			return appDictionaryService.edit(entity);
		}catch(Exception e){
			log.error("edit error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
	
	@ApiOperation(value="移除字典")
	@RequestMapping(value="remove", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode remove(@RequestBody AppDictionary entity) {
		try{
			if(entity.getId()!=null){
				Long[] ids=new Long[1];
				ids[0]=entity.getId();
				return appDictionaryService.removeByIds(ids);
			}else{
				return new ResultCode(Constants.OPERATION_FAIL);
			}
		}catch(Exception e){
			log.error("remove error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
}
