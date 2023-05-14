package com.sense.iam.api.action.im;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.sense.iam.config.RedisCache;
import com.sense.iam.service.ImageService;
import org.apache.xmlbeans.impl.util.Base64;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sense.core.util.ExcelUtils;
import com.sense.core.util.PageList;
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.api.model.AcctBindApp;
import com.sense.iam.api.model.im.AppReq;
import com.sense.iam.api.model.sys.AcctReqT;
import com.sense.iam.cache.ImageCache;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.im.App;
import com.sense.iam.model.im.Image;
import com.sense.iam.model.sys.Acct;
import com.sense.iam.service.AppService;
import com.sense.iam.service.OrgService;
import com.sense.iam.service.SysAcctService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiSort;


@Api(tags = "应用管理")
@Controller
@RestController
@RequestMapping("im/app")
@ApiSort(value = 7)
public class AppAction extends  AbstractAction<App,Long>{

	@Resource
	private SysAcctService sysAcctService;

	@Resource
	private ImageCache imageCache;

	@Resource
	private AppService appService;

	@Resource
	private OrgService orgService;

	@Resource
	private RedisCache redisCache;

	@Resource
	private ImageService imageService;

	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findById/{id}", method=RequestMethod.GET)
	@ResponseBody
	public App findById(@PathVariable Long id) {
		App app=super.findById(id);
		app.setSeqId(Long.valueOf(sysAcctService.getSeqId()));
		return app;
	}

	@ApiOperation(value="新增自动生成应用编码")
	@RequestMapping(value="toAdd", method=RequestMethod.GET)
	@ResponseBody
	public String toAdd() {
		String resultSn = "";
		App entity = new App();
		List<App> apps = appService.findListOrderBySn(entity);
		if(apps!=null&&apps.size()>0){
			String appSn = apps.get(0).getSn();
			String regex = "^[APP]{3}(?!000)\\d{3,}$";
			for (App app : apps) {
				if (!app.getSn().matches(regex)) {
					return resultSn;
				}
			}
			String letter = appSn.substring(0, 3);
			String num = appSn.substring(3);
			int countNum = Integer.parseInt(num) + 1;
			String strNum = String.valueOf(countNum);
			if (strNum.length() == 1 ) {
				resultSn = letter + "00" + strNum;
			} else if(strNum.length() == 2) {
				resultSn = letter + "0" + strNum;
			} else if(strNum.length() >= 3) {
				resultSn = letter + strNum;
			}
			return resultSn;
		}else{
			return "APP001";
		}
	}


	@ApiOperation(value="新增应用")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody AppReq entity) {
		App app=entity.getApp();
		if(entity.getTempImageId()!=null){
			try {
				//获取图片信息
				String value = redisCache.getCacheObject(entity.getTempImageId());
				if(value!=null){
					ObjectInputStream oos=new ObjectInputStream(new ByteArrayInputStream(Base64.decode(value.getBytes())));
					app.setImage((Image)oos.readObject());
					redisCache.deleteObject(entity.getTempImageId());
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ResultCode code=super.save(app);
		if(code.getCode()==Constants.OPERATION_SUCCESS){
			if(entity.getTempImageId()!=null) {
				Image image=app.getImage();
				image.setOid(app.getId());
				imageCache.loadImage(image);
			}
		}
		return code;
	}


	@ApiOperation(value="更新应用")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	public ResultCode edit(@RequestBody AppReq entity) {
		App app=entity.getApp();
		if(entity.getTempImageId()!=null){
			try {
				//获取图片信息
				String value=redisCache.getCacheObject(entity.getTempImageId());
				if(value!=null){
					ObjectInputStream oos=new ObjectInputStream(new ByteArrayInputStream(Base64.decode(value.getBytes())));
					app.setImage((Image)oos.readObject());

					redisCache.deleteObject(entity.getTempImageId());

					Image image=app.getImage();
					image.setOid(app.getId());
					imageCache.loadImage(image);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return super.edit(app);
	}



	/**
	 * 分页查询应用列表-带权限限制
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return 返回分页数据
	 */
	@ApiOperation(value="分页查询应用列表-带权限限制")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findListFunc", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 4)
	protected PageList<App> findListFunc(@RequestBody AppReq entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			return orgService.findAppByFunc(entity.getUserId(), entity.getSn(), entity.getName(), page, limit);
		}catch(Exception e){
			log.error("findListFunc error:",e);
			return new PageList<>();
		}
	}


	/**
	 * 分页查询应用列表
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return 返回分页数据
	 */
	@ApiOperation(value="分页查询应用列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findList", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 4)
	protected PageList<App> findList(@RequestBody AppReq entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			App p=entity.getApp();
			p.setIsLikeQuery(true);
			PageList<App> as = getBaseService().findPage(p,page,limit);
			List<App> newList=as.getDataList().stream().map(i->{
				Image image = new Image();
				image.setOid(i.getId());
				image = imageService.findByObject(image);
				if(null==image){
					i.setImageUrl(null);
				}
				else{
					i.setImageUrl("/api/image/viewImage/app/" + i.getId() + "?" + Math.random());
				}
				return i;
			}).collect(Collectors.toList());
			as.setDataList(newList);
			return as;
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<App>();
		}
	}

	@ApiOperation(value="移除应用")
    @RequestMapping(value="remove",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 3)
    public ResultCode remove(@RequestBody @ApiParam(name="唯一标识集合",value="多数据采用英文逗号分割",required=true)List<String> ids) {
		Params params=new Params();
		params.setIds(ids);
		log.info("ids:"+ids.toString());
		return super.remove(params);
    }



	/**
	 * 获取应用授权管理员列表
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return 返回分页数据
	 */
	@ApiOperation(value="获取应用授权管理员列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findAcctByAppIdList", method=RequestMethod.POST)
	@ResponseBody
	protected PageList<Map> findList(@RequestBody AcctReqT acctReqT,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			 Acct acct=acctReqT.getAcct();
			 acct.setIsLikeQuery(true);
			 return sysAcctService.findAcctByAppId(acct, page, limit);
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<>();
		}
	}

	@ApiOperation(value="应用授权管理员")
	@RequestMapping(value="bindAcctApp", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode bindAcctApp(@RequestBody AcctBindApp entity){
		try{
			if(entity.getAcctIds().size()>0){
				Acct acct = new Acct();
				acct.setAppId(entity.getAppId());
				List<Map> acctList = sysAcctService.findAcctByAppId(acct, 1, 100000).getDataList();

				List<String> bandIds = new ArrayList<>();
				for(Map map : acctList){
					String id = map.get("ID").toString();
					String ischeck = map.get("ischeack").toString();
					if(!entity.getAcctIds().contains(id)){
						sysAcctService.unBindApp(entity.getAppId(), Long.valueOf(id));
					}
					if(entity.getAcctIds().contains(id) && ischeck.equals("true")){
						bandIds.add(id);
					}
				}

//				log
				for(String id:entity.getAcctIds()){
					if(!bandIds.contains(id)){
						sysAcctService.bindApp(entity.getAppId(), Long.valueOf(id));
					}
				}
				return new ResultCode(Constants.OPERATION_SUCCESS);
			}
			return new ResultCode(Constants.OPERATION_FAIL);
		}catch(Exception e){
			log.error("save error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}

	/**
	 *
	 * @param id
	 * @return
	 * description :
	 */
	@ApiOperation(value="保存应用权限模型")
	@RequestMapping(value="saveAppFuncModelDefine",method=RequestMethod.POST)
	@ResponseBody
	public Object saveAppFuncModelDefine(Long id,String config){
		return appService.saveModelConfig(id, config);
	}

//	@ApiOperation(value="应用取消授权管理员")
//	@RequestMapping(value="unbindAcctApp", method=RequestMethod.POST)
//	@ResponseBody
//	public ResultCode unbindAcctApp(@RequestBody AcctBindApp entity){
//		try{
//			sysAcctService.unBindApp(entity.getAppId(), entity.getAcctId());
//			return new ResultCode(Constants.OPERATION_SUCCESS);
//		}catch(Exception e){
//			log.error("save error:",e);
//			return new ResultCode(Constants.OPERATION_FAIL);
//		}
//	}

	@ApiOperation(value="账号导入模板下载")
	@ApiImplicitParams({
		@ApiImplicitParam(name= "id", value= "惟一标识号", required = true, paramType="path", dataType="Long")
	})
	@RequestMapping(value="downTemplate/{id}",method=RequestMethod.GET)
	@ResponseBody
	public void downTemplate(@PathVariable Long id) {
		App app = appService.findById(id);
		String name = app.getSn();
		List<ExcelUtils.ExcelModel> models = new ArrayList<>();
		models.add(new ExcelUtils.ExcelModel("登录账号", "", 5000));
		models.add(new ExcelUtils.ExcelModel("登录密码", "", 5000));
		models.add(new ExcelUtils.ExcelModel("归属用户", "", 5000));
		models.add(new ExcelUtils.ExcelModel("账号类型", "", 5000));
		models.add(new ExcelUtils.ExcelModel("状态", "", 5000));
		super.exportXlsx(name, new ArrayList(), models,name);
	}



}
