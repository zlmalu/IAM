package com.sense.iam.api.action.sys;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiSort;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sense.core.util.ByteArrayJarClassLoader;
import com.sense.core.util.PageList;
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.api.model.sys.CompomentReq;
import com.sense.iam.cache.CacheSender;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.cam.SimpleModel;
import com.sense.iam.compoment.AuthInterface;
import com.sense.iam.compoment.FindInterface;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.SyncInteface;
import com.sense.iam.compoment.TaskInterface;
import com.sense.iam.model.sys.Compoment;
import com.sense.iam.service.SysCompomentService;


/**
 * 系统组件
 *
 * Description: 系统组件
 *
 * @author w_jfwen
 *
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */


@Api(tags = "系统组件模块")
@Controller
@RestController
@RequestMapping("sys/compoment")
@ApiSort(value = 28)
public class CompomentAction extends AbstractAction<Compoment,Long>{

	@Resource
	private SysCompomentService sysCompomentService;

	@Resource
	private CacheSender cacheSender;



	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findById/{id}", method=RequestMethod.GET)
	@ResponseBody
	public Compoment findById(@PathVariable Long id) {
		return super.findById(id);
	}




	@ApiOperation(value="新增")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(CompomentReq entity, @ApiParam(name="file",value="组件内容",required=false) @RequestParam(required=false,name="file",value="file") MultipartFile file) {
		try {
			Compoment compoment = entity.getCompoment();
			//设置组件内容
			if(file != null && file.getBytes() != null && file.getBytes().length > 0) {
				compoment.setContentByte(file.getBytes());
			}
			ResultCode resultCode = super.save(compoment);
			if(!resultCode.getSuccess()){
				if(resultCode.getCode()==Constants.OPERATION_EXIST){
					return resultCode;
				}
				resultCode.setMsg("组件最大只能上传64kb");
				return resultCode;
			}
			compoment.setContent(null);//设置组件内容未空，方便数据下发
			cacheSender.sendMsg(entity);
			return new ResultCode(Constants.OPERATION_SUCCESS);
		} catch (IOException e) {
			log.error("save error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}

	/**
	 * 分页查询列表
	 * @param compoment 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	@ApiOperation(value="分页查询列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findList", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 4)
	protected PageList<Compoment> findList(@RequestBody Compoment compoment,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			Compoment entity=new Compoment();
			entity.setSn(StringEscapeUtils.escapeHtml4(compoment.getSn()));
			entity.setName(StringEscapeUtils.escapeHtml4(compoment.getName()));
			entity.setIsLikeQuery(true);
			return getBaseService().findPage(entity,page,limit);
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<Compoment>();
		}
	}



	@ApiOperation(value="更新")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	public ResultCode edit(CompomentReq entity,@ApiParam(name="file",value="组件内容",required=false) @RequestParam(required=false,name="file",value="file") MultipartFile file) {
		try {
			Compoment compoment = entity.getCompoment();
			//如果ID==0返回错误信息
			if(entity.getId()==0L){
				return new ResultCode(Constants.OPERATION_FAIL," fail id is 0");
			}
			//判断是否存在附件内容
			if(file!=null && file.getBytes()!= null && file.getBytes().length > 0) {
				compoment.setContentByte(file.getBytes());
			}
			ResultCode resultCode = super.edit(compoment);
			if(!resultCode.getSuccess()){
				resultCode.setMsg("组件最大只能上传64kb");
				return resultCode;
			}
			compoment.setContent(null);//设置组件内容未空，方便数据下发
			cacheSender.sendMsg(compoment);
			return new ResultCode(Constants.OPERATION_SUCCESS);
		} catch (IOException e) {
			log.error("save error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}


	@ApiOperation(value="移除")
    @RequestMapping(value="remove",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 3)
    public ResultCode remove(@RequestBody @ApiParam(name="唯一标识集合",value="多数据采用英文逗号分割",required=true)List<String> ids) {
		Params params=new Params();
		params.setIds(ids);
		return super.remove(params);
    }

	@ApiOperation(value="加载类别")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "id", value = "组件唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="loadRunClass/{id}", method=RequestMethod.GET)
	@ResponseBody
	public Object loadRunClass(@PathVariable Long id){
		List<SimpleModel> datas=new ArrayList<SimpleModel>();
		if(id!=null){
			Compoment compoment=sysCompomentService.findById(id);
			if(compoment!=null){
				ByteArrayJarClassLoader classLoader=new ByteArrayJarClassLoader(compoment.getContent(),Thread.currentThread().getContextClassLoader());
				List<String> allClasses=classLoader.getAllClass();
				for (String clazz : allClasses) {
					try {
						Class<?> clz=classLoader.loadClass(clazz);
						if(compoment.getType().equals(Constants.SYS_COMPOMENT_SYNC) && SyncInteface.class.isAssignableFrom(clz)){
							datas.add(new SimpleModel(clazz,getDescName(clz),getConfig(clz),getDefaultContent(clz)));
						}else if(compoment.getType().equals(Constants.SYS_COMPOMENT_TASK) && TaskInterface.class.isAssignableFrom(clz)){
							datas.add(new SimpleModel(clazz,getDescName(clz),getConfig(clz),getDefaultContent(clz)));
						}else if(compoment.getType().equals(Constants.SYS_COMPOMENT_AUTH) && AuthInterface.class.isAssignableFrom(clz)){
							datas.add(new SimpleModel(clazz,getDescName(clz),getConfig(clz),getDefaultContent(clz)));
						}else if(compoment.getType().equals(Constants.SYS_COMPOMENT_FIND) && FindInterface.class.isAssignableFrom(clz)){
							datas.add(new SimpleModel(clazz,getDescName(clz),getConfig(clz),getDefaultContent(clz)));
						}
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return datas;
	}

	/**
	 * 获取组件类名称
	 * @param clazz 组件类
	 * @return 组件的中文名称
	 * description : 通过组件类注解的NAME内容获取
	 * wenjianfeng  2019年5月10日
	 */
	private String getDescName(Class<?> clazz){
		String result=clazz.getName();
		try{
			Name name=(Name) clazz.getAnnotation(Name.class);
			if(name==null){
				return result;
			}
			return name.value();
		}catch(Exception e){
			log.error(e);
		}
		return result;
	}

	private String getConfig(Class<?> clazz){
		StringBuffer strBuf=new StringBuffer("");
		try{
			Field[] fields=clazz.getDeclaredFields();
			Object obj=clazz.newInstance();
			for (Field field : fields) {
				Param param=field.getAnnotation(Param.class);
				if(param!=null){
					field.setAccessible(true);
					strBuf.append("<!--").append(param.value()).append("-->\n");
					Object defaultValue=field.get(obj);
					strBuf.append("<").append(field.getName()).append("><![CDATA["+(defaultValue!=null?defaultValue:"")+"]]>").append("</").append(field.getName()).append(">\n");
				}

			}
		}catch(Exception e){
			log.error("parse clazz error",e);
		}
		return strBuf.toString();
	}

	/**
	 * 获取默认配置内容
	 * @param clazz
	 * @return
	 * description :
	 * wenjianfeng 2019年5月23日
	 */
	private String getDefaultContent(Class<?> clazz){
		try {
			Field field=clazz.getDeclaredField("defaultContent");
			field.setAccessible(true);
			return (String) field.get(clazz.newInstance());
		} catch (Exception e) {
			//ignore
		}
		return "";
	}

}
