package com.sense.iam.sso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;



/**
 * Hello SSO APP!
 */
@SpringBootApplication(scanBasePackages={"com.sense.*"})
@ImportResource(locations = { "classpath:applicationContext-web.xml"})
public class SSOApp {
	
    public static void main( String[] args ){
    	SpringApplication.run(SSOApp.class,args);
    } 
}
