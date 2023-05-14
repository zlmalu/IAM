package com.sense.gateway.filter;

import org.apache.tomcat.util.http.Rfc6265CookieProcessor;
import org.apache.tomcat.util.http.SameSiteCookies;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class SpringSessionConfig {

	public SpringSessionConfig() {
	}

	@Bean
	public CookieSerializer httpSessionIdResolver() {
		DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
		// 取消仅限同一站点设置
		cookieSerializer.setCookieName("SESSION");
        cookieSerializer.setUseHttpOnlyCookie(false);
		cookieSerializer.setSameSite("None");
		cookieSerializer.setUseSecureCookie(true);
		return cookieSerializer;
	}
	
 
	@Bean
	public TomcatContextCustomizer sameSiteCookiesConfig() {
		return context -> {
			final Rfc6265CookieProcessor cookieProcessor = new Rfc6265CookieProcessor();
			// 设置Cookie的SameSite
			cookieProcessor.setSameSiteCookies(SameSiteCookies.NONE.getValue());
			context.setCookieProcessor(cookieProcessor);
		};
	}
	 
	
	 
}