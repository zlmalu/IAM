package com.sense.iam.auth;

import javax.annotation.Resource;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfigurer implements WebMvcConfigurer{

	@Resource
	private LoginInterceptor loginInterceptor;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		
		registry.addInterceptor(loginInterceptor);
	}

	
	
}
