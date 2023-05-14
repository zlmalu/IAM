package com.sense.am.policy;

import static com.sense.am.policy.PolicyManager.acUsersCache;
import static com.sense.am.policy.PolicyManager.authReamCache;
import static com.sense.am.policy.PolicyManager.resourceCache;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.sense.am.exception.IpPolicyException;
import com.sense.am.exception.ResourceAuthUserException;
import com.sense.am.exception.StrongAuthenticationException;
import com.sense.am.exception.TimePolicyException;
import com.sense.am.model.IpPolicy;
import com.sense.am.model.SSORequest;
import com.sense.am.model.TimePolicy;
import com.sense.core.util.CurrentAccount;
import com.sense.iam.model.am.AuthReaml;
import com.sense.iam.model.am.Ip;
import com.sense.iam.model.am.Resource;
import com.sense.iam.model.am.Time;
import com.sense.iam.service.AmLoginModuleService;

@Component
public class ResourceFilter  extends BaseFilter{
	
	@javax.annotation.Resource
	private AmLoginModuleService amLoginModuleService;
	
	@Override
	public void doFilter(SSORequest request, String resource) {
		Resource jo=resourceCache.get(resource);
		
		if(jo==null)return;
		
		//系统资源默认安全级别为1
		System.out.println(jo.getLeval());
		
		if(jo.getLeval().intValue()>request.getCurrentLevel() && !request.getAllowRes().contains(resource)){//资源安全级别
			//查找认证域编码
			Long reamId=jo.getReamlId();
			if(reamId!=null){
				AuthReaml authReamJo=authReamCache.get(reamId.toString());
				//设置本次认证的安全级别
				if(authReamJo!=null){
					throw new StrongAuthenticationException(authReamJo.getSn());
				}
			}
		}
		this.checkUserAuth(request, resource);
		this.checkTimeAuth(request, jo);
		this.checkIpAuth(request, jo);
	}
	
	/**
	 * 检查用户权限
	 * 
	 * description :  
	 * wenjianfeng 2019年12月12日
	 */
	private void checkUserAuth(SSORequest request, String resource){
		//资源授权用户
		Set<String> sets =acUsersCache.get(resource);
		if(sets==null || sets.size()==0)return;
		if(!sets.contains(request.getUsername())) {
			throw new ResourceAuthUserException("没有权限访问该资源!");
		}
	}
	
	/**
	 * 检查ip权限
	 * 
	 * description :  
	 * wenjianfeng 2019年12月12日
	 */
	private void checkIpAuth(SSORequest request, Resource jo){
		if(jo.getListip()!=null && jo.getListip().size()>0){

			for (Ip ip : jo.getListip()) {
				IpPolicy ipPolicy=PolicyManager.ipPolicyCache.get(ip.getIpId().toString());

				if(ipPolicy==null)continue;
				System.out.println(ipPolicy.getStartIp());
				System.out.println(ipPolicy.getEndIp());
				System.out.println(ipPolicy.isAllow());
				System.out.println(ipPolicy.isMatch(request.getLoginIp()));
				if(ipPolicy.isMatch(request.getLoginIp()) && ipPolicy.isAllow()){
					return;
				}
			}
			throw new IpPolicyException("IP不允许访问该资源!");
		}
	}
	
	/**
	 * 检查时间权限
	 * 
	 * description :  
	 * wenjianfeng 2019年12月12日
	 */
	private void checkTimeAuth(SSORequest request, Resource jo){
		if(jo.getListtime()!=null && jo.getListtime().size()>0){
			for (Time time : jo.getListtime()) {
				TimePolicy timePolicy=PolicyManager.timePolicyCache.get(time.getTimeId().toString());
				if(timePolicy==null)continue;
				if(timePolicy.isMatch() && timePolicy.isAllow()){
					return;
				}
			}
			throw new TimePolicyException("时间段不允许访问该资源!");
		}
	}

}
