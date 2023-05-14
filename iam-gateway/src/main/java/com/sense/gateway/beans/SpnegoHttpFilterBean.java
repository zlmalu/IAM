package com.sense.gateway.beans;

import net.sourceforge.spnego.SpnegoHttpFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sense.iam.cache.SysConfigCache;

/**
 * 用于AD授权免密登录的过滤器bean
 * 
 * Description:  
 * 
 * @author shibanglin
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */
@Configuration
public class SpnegoHttpFilterBean {
	protected Log log=LogFactory.getLog(getClass());
	
	/*@Bean
    public FilterRegistrationBean contextFilterRegistrationBean() {
		log.info("load bean SpnegoHttpFilter....");
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new f());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setName("spnegoHttpFilter");
        registrationBean.setOrder(1);
        registrationBean.addInitParameter("spnego.allow.basic", "true");
        registrationBean.addInitParameter("spnego.allow.localhost", "true");
        registrationBean.addInitParameter("spnego.allow.unsecure.basic", "true");
        registrationBean.addInitParameter("spnego.login.client.module", "spnego-client");
        registrationBean.addInitParameter("spnego.krb5.conf", "C:/krb5/krb5.conf");
        registrationBean.addInitParameter("spnego.login.conf", "C:/krb5/login.conf");
         
        registrationBean.addInitParameter("spnego.preauth.username", "1001@test.com");
        registrationBean.addInitParameter("spnego.preauth.password", "Password@1");
        registrationBean.addInitParameter("spnego.login.server.module", "spnego-server");
        registrationBean.addInitParameter("spnego.prompt.ntlm", "true");
        registrationBean.addInitParameter("spnego.logger.level", "1");
    	log.info("load bean SpnegoHttpFilter success");
        return registrationBean;
    }*/
}

