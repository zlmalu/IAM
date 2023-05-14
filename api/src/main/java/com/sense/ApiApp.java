package com.sense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ImportResource;


import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Hello world!
 *
 */
@SpringBootApplication(scanBasePackages={"com.sense.*"})
@ImportResource(locations = { "classpath:applicationContext-web.xml"})
@EnableSwagger2
@ServletComponentScan
public class ApiApp {
	
    public static void main( String[] args ){
    	SpringApplication.run(ApiApp.class,args);
    } 
}
