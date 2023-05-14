package com.sense.iam.auth;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.client.RestTemplate;

import com.sense.iam.auth.radius.RadiusServer;
import com.sense.iam.cache.CacheListener;
import com.sense.iam.cache.memery.CacheProcess;
import com.sense.iam.cache.redis.RadiusStatus;
import com.sense.iam.model.am.Radius;
import com.sense.iam.service.AmRadiusService;


@SpringBootApplication(scanBasePackages={"com.sense.*"})
@ImportResource(locations = { "classpath:applicationContext-web.xml"})
public class AuthApp {
	
	@Bean
	@LoadBalanced//对restTemplate进行负载均衡
	public RestTemplate restTemplate () {
		return new RestTemplate();
	}
	
	@Resource
	private RadiusServer radiusServer;
	
	@Resource
	private CacheListener cacheListener;
	@Resource
	private AmRadiusService amRadiusService;
	@PostConstruct
	public void startRadius(){
		List<Radius> radius=amRadiusService.findAll();
		for (Radius rs : radius) {
			radiusServer.addclient(rs);
		}
		radiusServer.startRadius();
		//启动radius服务
		cacheListener.register(RadiusStatus.class, new CacheProcess(){
			@Override
			public void load(Object message) {
				RadiusStatus status=(RadiusStatus)message;
				if(status.isRun()){
					radiusServer.startRadius();
					List<Radius> radius=amRadiusService.findAll();
					for (Radius rs : radius) {
						radiusServer.addclient(rs);
					}
					System.out.println("启动radius服务");
				}else{
					radiusServer.stopRadius();
					System.out.println("停止radius服务");
				}
			}
		});
		cacheListener.register(Radius.class, new CacheProcess(){
			@Override
			public void load(Object message) {
				radiusServer.addclient((Radius)message);
			}
		});
		
	}
	
    public static void main(String[] args) {
        SpringApplication.run(AuthApp.class);
        
    }
}
