package com.sense.iam.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;


/**
 * Hello portal APP!
 */
@SpringBootApplication(scanBasePackages={"com.sense.*"})
@ImportResource(locations = { "classpath:applicationContext-web.xml"})
public class PortalApp {
	
    public static void main( String[] args ){
    	SpringApplication.run(PortalApp.class,args);
    } 
}
