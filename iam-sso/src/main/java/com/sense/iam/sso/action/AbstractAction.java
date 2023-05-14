package com.sense.iam.sso.action;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestParam;

import com.sense.core.model.BaseModel;
import com.sense.core.util.ArrayUtils;
import com.sense.core.util.GenericsUtil;
import com.sense.core.util.PageList;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.service.BaseService;

/**
 * Action的抽象实现类
 * 
 * Description: 实现Action的所有可继承方法，主要方法有
 * 				toSave 跳转到保存页面
 * 				save 数据保存
 * 				toEdit 跳转到数据编辑页面
 * 				edit 数据编辑
 * 				removeByIds 根据Id数组移除数据
 * 				findById 根据对象ID查询数据
 * 				findList 根据对象列表
 * 				export   导出数据
 * 				upLoad   上传数据
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@SuppressWarnings("unchecked")
public class AbstractAction<T extends BaseModel,PK> extends BaseAction{
	
	public String getPackageName(){
		String name=getClass().getName();
		name=name.substring(name.indexOf("action")+7);
		return name.replace("Action","").replaceAll("[.]", "/").toLowerCase();
	}
	
	/**
	 * 保存数据
	 * @param entity
	 * @return
	 */
	protected ResultCode save(T entity){
		try{
			return getBaseService().save(entity);
		}catch(Exception e){
			log.error("save error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
		
	}
	
	/**
	 * 修改对象
	 * @param entity 需要修改的实体对象
	 * @param id 需要保存实体对象的编号
	 * @return
	 */
	protected ResultCode edit(T entity){
		//判断数据是否存在
		try{
			getBaseService().edit(entity);
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			log.error("edit error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
	/**
	 * 分页查询对象列表
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	protected Object findList(T entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			entity.setIsLikeQuery(true);
			return getBaseService().findPage(entity,page,limit);
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<T>();
		}
	}
	
	protected Object findAll(T entity){
		entity.setIsLikeQuery(true);
		return getBaseService().findList(entity);
	}
	
	/**
	 * 根据ID查询对象
	 * @param id
	 * @return
	 */
	protected T findById(PK id){
		return getBaseService().findById(id);
	}
	
	protected ResultCode remove(Params params){
		try{
			if(params.getIds()!=null){
				if(GenericsUtil.getSuperClassGenricType(getClass(), 1)==Long.class){
					getBaseService().removeByIds((PK[])ArrayUtils.stringArrayToLongArray(params.getIds().toArray(new String[params.getIds().size()])));
				}else{
					getBaseService().removeByIds((PK[])params.getIds().toArray(new String[params.getIds().size()]));
				}
			}
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			log.error("remove error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
		
	}
	
	/**
	 * 构造并初始化基本服务类
	 */
	protected BaseService<T,PK> getBaseService(){
		return (BaseService<T, PK>) getBean(GenericsUtil.getSuperClassGenricType(getClass()).getName().replace(Constants.MODEL_PACKAGE_PATH, "").replace(".","")+"Service");
	}
	
	
	/**
	 * 打印输出json数据
	 * @param response web响应流
	 * @param resultCode 响应结果对象
	 */
	protected void writerResult(HttpServletResponse response,ResultCode resultCode){
		try{
			response.setContentType("text/html;charset=UTF-8");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().print(resultCode.toJsonString());
			response.getWriter().flush();
			response.getWriter().close();
		}catch(Exception e){
			//ignore
		}
	}

}
