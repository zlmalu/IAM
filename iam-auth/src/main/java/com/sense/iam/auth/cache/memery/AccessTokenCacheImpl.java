package com.sense.iam.auth.cache.memery;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import com.sense.core.util.StringUtils;
import com.sense.iam.auth.Token;
import com.sense.iam.auth.cache.AccessTokenCache;

/**
 * 
 * accessToken处理器的内存模式实现
 * 
 * Description: 令牌存储时间默认为30秒，超过30秒会自动清除
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Component("accessTokenCache")
public class AccessTokenCacheImpl implements AccessTokenCache{
	
	private static Log log=LogFactory.getLog(AccessTokenCacheImpl.class);

	private static long defaultExpriedTime = 5;//默认过期时间定义（分钟）
	
	private static Map<String, Token> map = new HashMap<String, Token>();
	
	
	@Override
	public Token grantToken(String tokenId,Object content) {
		Token token=new Token();
		token.setId(tokenId);
		token.setExpried(System.currentTimeMillis());
		token.setContent(content);
		map.put(tokenId, token);
		log.info("put key:"+tokenId);
		Iterator<String> iterator = map.keySet().iterator();// map中key（键）的迭代器对象
        while (iterator.hasNext()){// 循环取键值进行判断
            String key1 = iterator.next();// 键
            log.info("map key1:"+key1+",value="+map.get(key1));
        }
		return token;
	}

	@Override
	public Token grantToken(Object content) {
		Token token=new Token();
		token.setId(StringUtils.getUuid());
		token.setExpried(System.currentTimeMillis());
		token.setContent(content);
		map.put(token.getId(), token);
		return token;
	}

	@Override
	public Token getToken(String key) {
		Iterator<String> iterator = map.keySet().iterator();// map中key（键）的迭代器对象
        while (iterator.hasNext()){// 循环取键值进行判断
            String key1 = iterator.next();// 键
            log.info("map key1:"+key1+",value="+map.get(key1));
        }
        log.info("get key:"+key);
		return (Token) map.remove(key);
	}

	
	/**
	 * 30秒清理一次内存数据
	 */
	static{
		new Thread(() -> {
			while(true){
				//log.info("run clean accessTokenCache jop......");
				Iterator<String> iterator = map.keySet().iterator();// map中key（键）的迭代器对象
		        while (iterator.hasNext()){// 循环取键值进行判断
		            String key = iterator.next();// 键
		            Token value=map.get(key);
		            if(System.currentTimeMillis()-value.getExpried()>defaultExpriedTime*60*1000){//30分钟
						iterator.remove();
					}
		        }
				try {
					//30秒检查一次
					Thread.sleep(30*1000L);
				} catch (Exception e) {
					//ignore
				}
			}
		}).start();
	}
}
