package com.sense.iam.jop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;



/**
 * Hello SSO APP!
 */
@SpringBootApplication(scanBasePackages={"com.sense.*"})
@EnableAutoConfiguration
@ImportResource(locations = { "classpath:applicationContext-web.xml"})
public class JopApp {
	
    public static void main( String[] args ){
        try {
            SpringApplication.run(JopApp.class,args);
        }catch(Exception e) {
            e.printStackTrace();
        }

    } 
}
