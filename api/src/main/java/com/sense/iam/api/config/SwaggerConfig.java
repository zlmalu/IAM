package com.sense.iam.api.config;
 


import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sense.iam.cam.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

 

 
@Configuration
@EnableSwagger2
public class SwaggerConfig implements WebMvcConfigurer{
     
    @Bean
    public Docket swaggerSpringMvcPlugin() {
    	Docket docket = new Docket(DocumentationType.SWAGGER_2)
        .apiInfo(new ApiInfoBuilder().title("SENSE IAM API 文档中心")
                .description("API文档")
                .version("4.0").build())
        .select()
        .apis(RequestHandlerSelectors.basePackage("com.sense.iam.api"))
        .paths(PathSelectors.any())
        .build().groupName("API数据接口").ignoredParameterTypes(ApiIgnore.class).globalOperationParameters(this.getParameterList());
    	return docket;
    }
    
    @Bean(value = "autpApi")
    public Docket publicApi1() {
    	Docket docket = new Docket(DocumentationType.SWAGGER_2)
        .apiInfo(new ApiInfoBuilder().title("SENSE IAM API 文档中心")
                .description("API文档")
                .version("4.0").build())
        .select()
        .apis(RequestHandlerSelectors.basePackage("com.sense.iam.auth"))
        .paths(PathSelectors.any())
        .build().groupName("获取令牌").ignoredParameterTypes(HttpServletResponse.class, HttpServletRequest.class);
    	return docket;
    }
    
    @Bean(value = "openApi")
    public Docket openApi() {
    	Docket docket = new Docket(DocumentationType.SWAGGER_2)
        .apiInfo(new ApiInfoBuilder().title("SENSE IAM API 文档中心")
                .description("API文档，访问接口无需获取令牌")
                .version("4.0").build())
        .select()
        .apis(RequestHandlerSelectors.basePackage("com.sense.iam.open.action"))
        .paths(PathSelectors.any())
        .build().groupName("开放接口").ignoredParameterTypes(HttpServletResponse.class, HttpServletRequest.class);
    	return docket;
    }
    
    private List<Parameter> getParameterList(){
    	List<Parameter> params=new ArrayList<Parameter>();
    	ParameterBuilder pb=new ParameterBuilder();
    	pb.name(Constants.CURRENT_SESSION_ID).description("接口令牌").modelRef(new ModelRef("string")).parameterType("header").required(true).build();
    	params.add(pb.build());
    	return params;
    }

 
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

    }

     
}