package com.sense.iam.console;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ImportResource;

/**
 * 
 *应用启动
 */
@SpringBootApplication
@EnableEurekaClient
@ImportResource(locations = { "classpath:applicationContext-web.xml"})
public class ConsoleApp 
{
    public static void main( String[] args )
    {
    	SpringApplication.run(ConsoleApp.class, args);
    }
}
