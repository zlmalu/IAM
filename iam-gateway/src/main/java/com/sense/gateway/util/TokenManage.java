package com.sense.gateway.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.sense.iam.config.RedisCache;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import com.sense.core.util.ContextUtil;
import com.sense.core.util.JWTUtil;
import com.sense.core.util.TimeUtil;
import com.sense.iam.cache.SysConfigCache;
import com.sense.iam.cam.Constants;
import com.sense.iam.service.JdbcService;

import javax.annotation.Resource;


@Component
public class TokenManage{

	private static Log log=LogFactory.getLog(TokenManage.class);


	private static RedisCache getRedisCache(){
		return (RedisCache) ContextUtil.getBean("redisCache");
	};




	
	/**
	 * 移除
	 * @param sessionId
	 * @param sessionId
	 * @return type1API，2portal
	 */
	public static void remove(String sessionId,int type) {
		if(getRedisCache()!=null){
			String rediesKey;
			if(type==1){
				rediesKey=Constants.CURRENT_SESSION_ID+":"+sessionId;
			}else{
				rediesKey=Constants.CURRENT_REDIS_SESSION_ID+":"+sessionId;
			}
			getRedisCache().deleteObject(rediesKey);
		}
	}
	
	/**
	 * 放行token
	 * @param sessionId
	 * @return
	 */
	public static String releaseToken(String sessionId) {	
		String token=null;
		if(getRedisCache()==null)return token;
		String rediesKey=Constants.CURRENT_REDIS_SESSION_ID+":"+sessionId;
		token=getRedisCache().getCacheObject(rediesKey);
		if(token!=null){
			try{
				//延迟时间
				getRedisCache().expire(rediesKey, SysConfigCache.SESSION_TIMEOUT, TimeUnit.SECONDS);
				String payload=JWTUtil.parseToken(token, Constants.JWT_SECRECTKEY);
				JSONObject payloads=JSONObject.fromObject(payload);
				//移除json key 重新赋值
				payloads.remove("validataPwd");
				payloads.put("validataPwd", 0);
				String jwtToken=JWTUtil.generateToken(payloads.toString(), Constants.JWT_SECRECTKEY, SysConfigCache.SESSION_TIMEOUT);
				return jwtToken;
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return token;
	}
	
	/**
	 * 操作Redis读取senseToken
	 * @param sessionId
	 * @return
	 */
	public static String getToken(String sessionId,String path) {	
		String token=null;
		if(getRedisCache()==null)return token;
		String rediesKey=Constants.CURRENT_REDIS_SESSION_ID+":"+sessionId;
		String uimrediesKey=Constants.CURRENT_SESSION_ID+":"+sessionId;
		try{
			//延长redis的失效时间
			if(path.contains("/api/")){
				getRedisCache().expire(uimrediesKey, SysConfigCache.SESSION_TIMEOUT, TimeUnit.MILLISECONDS);
			}else{
				//过滤portal定时检测会话URL
				if(!path.contains("/portal/testing.action")){
					//延迟时间
					getRedisCache().expire(rediesKey, SysConfigCache.SESSION_TIMEOUT, TimeUnit.MILLISECONDS);
				}
				token = getRedisCache().getCacheObject(rediesKey);
				if(StringUtils.isNotEmpty(token)){
					return token;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return token;
	}
	
	
	/**
	 * 会话复制，新会话代替老会话
	 * @param newsessionId 新会话
	 * @param oidsessionId
	 * @return
	 */
	public static void buiIs(String newsessionId,String oidsessionId,String path) {	
		String token=null;
		if(getRedisCache()==null)return;
		String rediesKey=Constants.CURRENT_REDIS_SESSION_ID+":"+oidsessionId;
		try{
			if(path.contains("/sso/request")){
				token = getRedisCache().getCacheObject(rediesKey);
				//判断老会话是否存在JTW认证数据
				if(StringUtils.isNotEmpty(token)){
					//设置新会话的redis数据 并且设置超时时间超时时间，创建新会话设置数据
					getRedisCache().setCacheObject(Constants.CURRENT_REDIS_SESSION_ID+":"+newsessionId,token,SysConfigCache.SESSION_TIMEOUT.intValue(),TimeUnit.MILLISECONDS);
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * 根据域帐号自动授权登录
	 * @param sessionId
	 * @param remoteUser
	 * @return
	 */
	public static boolean authADConversationBuild(String sessionId,String remoteUser,String ip,String host) {	
		boolean isFlag=false;
		if(getRedisCache()==null)return isFlag;
		String rediesKey=Constants.CURRENT_REDIS_SESSION_ID+":"+sessionId;
		JdbcService jdbcService=(JdbcService) ContextUtil.context.getBean("jdbcService");
		try{
			String portaltoken=getRedisCache().getCacheObject(rediesKey);
			if(portaltoken==null){
				String sql="select a.ID,a.LOGIN_NAME,b.SN,b.NAME,a.COMPANY_SN,a.USER_ID from IM_ACCOUNT a left join IM_USER b on a.USER_ID=b.ID left join IM_APP c on a.APP_ID=c.ID where a.LOGIN_NAME='"+remoteUser+"' AND c.SN='APP001' and a.COMPANY_SN IN(SELECT SN FROM SYS_COMPANY WHERE COMPANY_HOST='"+host+"') and a.STATUS=1";
				List<Map<String, Object>> list=jdbcService.findList(sql);
				if(list!=null&&list.size()==1){
					//生成门户会话
					JSONObject payloadM=new JSONObject();
					payloadM.put("accountId", list.get(0).get("ID"));
					payloadM.put("loginName", list.get(0).get("LOGIN_NAME"));
					payloadM.put("companySn",list.get(0).get("COMPANY_SN"));//将当前公司编码放入，以便在日志打印中进行打印
					payloadM.put("sn",list.get(0).get("SN"));
					payloadM.put("name", list.get(0).get("NAME"));
					payloadM.put("userId", list.get(0).get("USER_ID"));
					payloadM.put("userName", list.get(0).get("LOGIN_NAME"));
					payloadM.put("device", "PC");
					payloadM.put("ip", ip);			
					payloadM.put("remark", "AD域授权登录");
					payloadM.put("createTime",  TimeUtil.getHmsTime(new Date()));
					payloadM.put("destSrc", new JSONArray());
					payloadM.put("validataPwd", 0);
					payloadM.put("validataPwdMsg", "正常");
					log.info("payloads:"+payloadM.toString());
					String jwtToken=JWTUtil.generateToken(payloadM.toString(), Constants.JWT_SECRECTKEY);
					
					//设置redis数据 并且设置超时时间超时时间
					getRedisCache().setCacheObject(Constants.CURRENT_REDIS_SESSION_ID+":"+sessionId,jwtToken,SysConfigCache.SESSION_TIMEOUT.intValue(),TimeUnit.MILLISECONDS);
				}
			}
			isFlag=true;
		}catch(Exception e){
			e.printStackTrace();
		}
		log.info("authADConversationBuild is :"+isFlag);
		return isFlag;
	}
	
	
}
