package com.sense.iam.cam.auth.cache.memery;

import org.springframework.stereotype.Component;

import com.sense.iam.cam.auth.OnlineUser;
import com.sense.iam.cam.auth.cache.OnlineUserCache;


@Component("onlineUserCache")
public class OnlineUserCacheImpl  extends BaseCacheImpl implements OnlineUserCache{

	@Override
	public OnlineUser get(String key) {
		return (OnlineUser) cache.get(key);
	}

	@Override
	public void remove(String key) {
		cache.remove(key);
	}

	@Override
	public void put(String key, OnlineUser oUser) {
		cache.put(key, oUser);
	}
	

}
