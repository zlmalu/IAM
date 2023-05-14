package com.sense.iam.api.config;

import java.io.File;

import javax.servlet.MultipartConfigElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 注入文件上传临时目录，防止系统删除
 * @author admin
 *
 */
@Configuration
public class MultipartConfig {
	
   private Log log=LogFactory.getLog(getClass());

   @Value("${com.sense.file.upload.tempdir}")
   private String path;
   @Bean
   MultipartConfigElement multipartConfigElement() {
	  log.info("file upload fileDir:"+path);
      MultipartConfigFactory factory = new MultipartConfigFactory();
      File tmpFile = new File(path);
      if (!tmpFile.exists()) {
         tmpFile.mkdirs();
      }
      factory.setLocation(path);
      return factory.createMultipartConfig();
   }

}