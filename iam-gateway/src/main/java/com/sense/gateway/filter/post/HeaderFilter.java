package com.sense.gateway.filter.post;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.sense.gateway.filter.HttpPostFilter;

@Component("headerPostFilter")
public class HeaderFilter extends HttpPostFilter{
	private static Log log=LogFactory.getLog(HeaderFilter.class);
	@Override
	public Object run() throws ZuulException {
		 RequestContext context = RequestContext.getCurrentContext();
		  try {
			   String path=context.getRequest().getRequestURL().toString();
			   String httpsd="http";
			   if(path.contains("https")){
				   httpsd="https";
			   }
			   String errorURL=httpsd+"://"+context.getRequest().getServerName()+":"+context.getRequest().getServerPort()+"/";
			  //如果请求API，则由API自己的接口进行管理
			   //过滤格式，http://127.0.0.1:8882/api
			   //log.info("errorURL:"+errorURL);
			   if(path.toLowerCase().contains(errorURL+"api")){
				   //过滤响应状态，如果是一些异常状态，则返回数据信息，200，302则放开过滤
				   if(context.getResponseStatusCode()==HttpStatus.NOT_FOUND.value()
						   ||context.getResponseStatusCode()==HttpStatus.INTERNAL_SERVER_ERROR.value()
						   ||context.getResponseStatusCode()==HttpStatus.FORBIDDEN.value()
						   ){
					   InputStream stream = context.getResponseDataStream();
					   String body = StreamUtils.copyToString(stream, Charset.forName("UTF-8"));
					   context.setResponseBody(body);
					   return null;
				   }else{
					   return null;
				   }
			   }else{
				   //404
				   if(context.getResponseStatusCode()==HttpStatus.NOT_FOUND.value()){
					   context.getResponse().sendRedirect(errorURL+HttpStatus.NOT_FOUND.value()+".html");
					   return null;
				   }
				   //500
				   if(context.getResponseStatusCode()==HttpStatus.INTERNAL_SERVER_ERROR.value()){
					   context.getResponse().sendRedirect(errorURL+HttpStatus.INTERNAL_SERVER_ERROR.value()+".html");
					   return null;
				   } 
				   //505
				   if(context.getResponseStatusCode()==HttpStatus.HTTP_VERSION_NOT_SUPPORTED.value()){
					   context.getResponse().sendRedirect(errorURL+HttpStatus.HTTP_VERSION_NOT_SUPPORTED.value()+".html");
					   return null;
				   } 
				   //403
				   if(context.getResponseStatusCode()==HttpStatus.FORBIDDEN.value()){
					   context.getResponse().sendRedirect(errorURL+HttpStatus.FORBIDDEN.value()+".html");
					   return null;
				   }
			   }
		  } catch (IOException e) {
			  e.printStackTrace();
		  }
		  return null;
	}
}
