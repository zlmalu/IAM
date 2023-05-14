package com.sense.am.model;

/**
 * 
 * ip策略模型
 * 
 * Description: 
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class IpPolicy {

	/**
	 * 开始IP
	 */
	private String startIp;
	/**
	 * 终止IP
	 */
	private String endIp;
	/**
	 * 是否允许访问
	 */
	private boolean isAllow;
	public String getStartIp() {
		return startIp;
	}
	public void setStartIp(String startIp) {
		this.sIp=ipStringToLong(startIp);
		this.startIp = startIp;
	}
	public String getEndIp() {
		return endIp;
	}
	public void setEndIp(String endIp) {
		this.eIp=ipStringToLong(endIp);
		this.endIp = endIp;
	}
	public boolean isAllow() {
		return isAllow;
	}
	public void setAllow(boolean isAllow) {
		this.isAllow = isAllow;
	}
	
	private long sIp;
	
	private long eIp;
	
	/**
	 * 是否允许访问
	 * @param ip
	 * @return
	 */
	public boolean isMatch(String ip){
		long ipLong=ipStringToLong(ip);
//		return !((sIp<=ipLong && ipLong<=eIp) ^ isAllow);
		return sIp<=ipLong && ipLong<=eIp;
	}

	private long ipStringToLong(String ip){
		if(ip==null)return 0l;
		String[] addrs=ip.split("[.]");
		if(addrs.length==4){
			return (Long.valueOf(addrs[0])<<24)+(Long.valueOf(addrs[1])<<16)+(Long.valueOf(addrs[2])<<8)+(Long.valueOf(addrs[3]));
		}
		return 0l;
	}

}
