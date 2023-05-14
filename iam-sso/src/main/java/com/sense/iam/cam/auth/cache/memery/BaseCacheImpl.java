package com.sense.iam.cam.auth.cache.memery;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.iam.cam.auth.CacheModel;
import com.sense.iam.model.sso.Cache;
import com.sense.iam.service.SsoCacheService;

/**
 * 
 * 基础缓存类
 * 
 * Description: 
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class BaseCacheImpl extends Thread{

	protected Log log=LogFactory.getLog(getClass());
	//缓存刷新时间
	private Long checkExpriedTime=30*1000L;
	
	@Resource
	private SsoCacheService ssoCacheService;
	
	@SuppressWarnings("serial")
	protected Map<String,CacheModel> cache=new HashMap<String,CacheModel>(){
		private byte[] getByte(Object object){
			ByteArrayOutputStream bos=new ByteArrayOutputStream();
			ObjectOutputStream oos=null;
			try{
				oos=new ObjectOutputStream(bos);
				oos.writeObject(object);
				return bos.toByteArray();
			}catch(Exception e){
				//ignore
			}finally{
				if(oos!=null){
					try{
					oos.close();
					bos.close();
					}catch(Exception e){
						//ignore
					}
				}
			}
			return null;
		}
		
		private Object getObject(byte[] bytes){
			ObjectInputStream os=null;
			try{
				os=new ObjectInputStream(new ByteArrayInputStream(bytes));
				return os.readObject();
			}catch(Exception e){
				//ignore
			}finally{
				if(os!=null){
					try{
						os.close();
					}catch(Exception e){
						//ignore
					}
				}
			}
			return null;
		}
		

		@Override
		public CacheModel get(Object key) {
			CacheModel model=super.get(key);
			if(model==null){
				Cache ssocache=ssoCacheService.findById(key.toString());
				if(ssocache!=null){
					model=(CacheModel) getObject(ssocache.getContent());
				}
				
			}
			
			return model;
		}

		@Override
		public CacheModel put(String key, CacheModel value) {
			Cache ssoCache =new Cache();
			ssoCache.setId(key);
			ssoCache.setContentByte(getByte(value));
			ssoCacheService.save(ssoCache);
			return super.put(key, value);
		}

		@Override
		public CacheModel remove(Object key) {
			CacheModel model = super.remove(key);
			if(model==null){
				Cache ssocache=ssoCacheService.findById(key.toString());
				if(ssocache!=null)model=(CacheModel) getObject(ssocache.getContent());
			}
			ssoCacheService.removeByIds(new String[]{key.toString()});
			return model;
		}
		
	};

	public BaseCacheImpl() {
		this.start();
	}
	
	@Override
	public void run() {
		log.info("cache remove scan started");
		Map.Entry<String,CacheModel> mc;
		CacheModel model;
		while(true){		
			try {
				//清除过期数据由于不能应用map的正常使用，所以使用map.remvoe方式进行移除
				Iterator<Map.Entry<String, CacheModel>> it=cache.entrySet().iterator();
				while(it.hasNext()){
					mc=it.next();
					model=mc.getValue();
					if(System.currentTimeMillis()-model.getExpried()>0){
						log.debug("remove session:"+model);
						ssoCacheService.removeByIds(new String[]{mc.getKey()});
						it.remove();
					}
				}
				Thread.sleep(checkExpriedTime);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
