package com.sense.am.policy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.sense.am.model.IpPolicy;
import com.sense.am.model.SSORequest;
import com.sense.am.model.TimePolicy;
import com.sense.iam.model.am.AuthReaml;
import com.sense.iam.model.am.Black;
import com.sense.iam.model.am.Resource;
import com.sense.iam.service.AmAuthReamlService;
import com.sense.iam.service.AmBlackService;
import com.sense.iam.service.AmIpPolicyService;
import com.sense.iam.service.AmResourceService;
import com.sense.iam.service.AmTimePolicyService;



@Component
public class PolicyManager {

	/**
	 * 资源控制缓存
	 */
	public static Map<String,Resource> resourceCache=new HashMap<String,Resource>(); 
	/**
	 * 认证域缓存
	 */
	public static Map<String,AuthReaml> authReamCache=new HashMap<String,AuthReaml>(); 
	/**
	 * 时间策略缓存
	 */
	public static Map<String,TimePolicy> timePolicyCache=new HashMap<String,TimePolicy>(); 
	
	/**
	 * ip策略缓存
	 */
	public static Map<String,IpPolicy> ipPolicyCache=new HashMap<String,IpPolicy>(); 
	
	/**
	 * 黑名单策略缓存
	 */
	public static Set<String> blackUserCache=new HashSet<String>();
	/**
	 * 资源授权用户
	 */
	public static Map<String,Set<String>> acUsersCache=new HashMap<String,Set<String>>(); 
	
	
	
	private static Long upLoadTime=null;
	

	private void load(){
		try {
			loadAc();
			loadAuthReam();
			loadTimePolicy();
			loadIpPolicy();
			loadBlackUser();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@javax.annotation.Resource
	private AmResourceService resourceService;
	@javax.annotation.Resource
	private AmAuthReamlService authReamlService;
	@javax.annotation.Resource
	private AmTimePolicyService timePolicyService;
	@javax.annotation.Resource
	private AmIpPolicyService ipPolicyService;
	@javax.annotation.Resource
	private AmBlackService amBlackService;
	
	/**
	 * 加载所有访问控制资源
	 * @param client
	 * @param serverAddr
	 * @throws Exception
	 */
	private void loadAc() throws Exception{
		resourceCache.clear();
		acUsersCache.clear();
		List<Resource> resources=resourceService.findAll();
		for (Resource resource : resources) {
			resource.setListtime(resourceService.findAmTime(resource.getId()));
			resource.setListip(resourceService.findAmIp(resource.getId()));
			resourceCache.put(resource.getUrl(),resource );
		}
	}
	
	/**
	 * 加载认证域信息
	 * @param client
	 * @param serverAddr
	 * @throws Exception
	 */
	private void loadAuthReam() throws Exception{
		authReamCache.clear();
		List<AuthReaml> authReamls=authReamlService.findAll();
		for (AuthReaml authReaml : authReamls) {
			authReamCache.put(authReaml.getId().toString(), authReaml);
		}
	}
	
	
	private void loadTimePolicy() throws Exception{
		timePolicyCache.clear();
		List<com.sense.iam.model.am.TimePolicy> timePolicys=timePolicyService.findAll();
		TimePolicy timePolicy;
		for (com.sense.iam.model.am.TimePolicy jo : timePolicys) {
			timePolicy=new TimePolicy();
			timePolicy.setAllow(jo.getIsAllow().intValue()==1?true:false);
			timePolicy.setMonth(jo.getMonth());
			timePolicy.setDayMonth(jo.getDayMonth());
			timePolicy.setDayWeek(jo.getDayWeek());
			Date startTime=jo.getStartTime();
			Date endTime=jo.getEndTime();
			timePolicy.setStartTime(startTime==null?0L:Long.valueOf(String.format("%TT", startTime.getTime()).replace(":","")));
			timePolicy.setEndTime(endTime==null?0L:Long.valueOf(String.format("%TT",endTime.getTime()).replace(":", "")));
			timePolicyCache.put(jo.getId().toString(),timePolicy);
		}
	}
	
	private void loadIpPolicy() throws Exception{
		ipPolicyCache.clear();
		List<com.sense.iam.model.am.IpPolicy> ipPolicys=ipPolicyService.findAll();
		IpPolicy ipPolicy;
		for (com.sense.iam.model.am.IpPolicy jo : ipPolicys) {
			ipPolicy=new IpPolicy();
			ipPolicy.setStartIp(jo.getStartIp());
			ipPolicy.setEndIp(jo.getEndIp());
			ipPolicy.setAllow(jo.getIsAllow()==1?true:false);
			ipPolicyCache.put(jo.getId().toString(),ipPolicy);
		}
	}
	
	private void loadBlackUser() throws Exception{
		blackUserCache.clear();
		List<Black> blacks=amBlackService.findAll();
		for (Black black : blacks) {
			blackUserCache.add(black.getUserId());
		}
	}
	
	
	
	
	@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
	private List<BaseFilter> filters=new ArrayList(){{
		add(new BlackUserFilter());
		add(new IpPolicyFilter());
		add(new TimePolicyFilter());
		
	}};
	
	@javax.annotation.Resource
	public void setResourceFilter(ResourceFilter resourceFilter) {
		filters.add(resourceFilter);
	}


	
	/**
	 * 过滤用户请求信息,当错误时抛出运行时异常
	 * @param request 用户请求时用户的相关信息
	 * @param requestURL 用户请求的具体路径
	 */
	public void doFilter(SSORequest request,String requestUrl){
		//5分钟加载一次策略信息
		if(upLoadTime==null || System.currentTimeMillis()-upLoadTime>1*60*1000){
			load();
		}
		for (BaseFilter baseFilter : filters) {
			baseFilter.doFilter(request, requestUrl);
		}
	}
	
	public String getString(Object obj){
		return obj==null?"":obj.toString();
	}
	
}
