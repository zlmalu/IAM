package com.sense.iam.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.impl.util.Base64;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.sense.core.util.CurrentAccount;
import com.sense.iam.cache.SysConfigCache;
import com.sense.iam.cam.Constants;

@Component
public class SessionManager {


	private static Log log=LogFactory.getLog(SessionManager.class);
	

	private static StringRedisTemplate redisTemplate;
	
	
	public static CurrentAccount getSession(String sessionId,HttpServletRequest request){
		log.debug("get sesion:"+sessionId);
		//获取redis bean 
		CurrentAccount currentAccount=null;
		if(redisTemplate==null){
			redisTemplate=WebApplicationContextUtils.getWebApplicationContext(request.getServletContext()).getBean(StringRedisTemplate.class);
		}
		try {
			//获取根据session 获取认证对象
			String value=redisTemplate.opsForValue().get(Constants.CURRENT_SESSION_ID+":"+sessionId);
			log.debug("get redis data:"+value);
			if(value!=null){
				ObjectInputStream oos=new ObjectInputStream(new ByteArrayInputStream(Base64.decode(value.getBytes())));
				currentAccount= (CurrentAccount)oos.readObject();
			}else{
				return null;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return currentAccount;
	}
	

	public static void putSession(String sessionId,CurrentAccount account,HttpServletRequest request){

		
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos=new ObjectOutputStream(bos);
			oos.writeObject(account);
			oos.close();
			String value=new String(Base64.encode(bos.toByteArray()));
			//获取redis bean 
			if(redisTemplate==null){
				redisTemplate=WebApplicationContextUtils.getWebApplicationContext(request.getServletContext()).getBean(StringRedisTemplate.class);
			}
			log.debug("put session:"+sessionId+",value="+value);
			//存放redis sessionId和认证信息-设置redis有效时间为30分钟,根据配置文件获取默认时长，
			redisTemplate.opsForValue().set(Constants.CURRENT_SESSION_ID+":"+sessionId, value,SysConfigCache.SESSION_TIMEOUT, TimeUnit.MILLISECONDS);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void removeBySessionId(String sessionId){
		try{
			redisTemplate.delete(Constants.CURRENT_SESSION_ID+":"+sessionId);
			log.debug("delete session:"+sessionId);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
