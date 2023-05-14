package com.sense.gateway.provider;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

@Component
public class ProducerFallback implements FallbackProvider {
    private final Logger log = LoggerFactory.getLogger(FallbackProvider.class);
    @Override
    public ClientHttpResponse fallbackResponse(String route, Throwable cause) {
        if (cause != null && cause.getCause() != null) {
            String reason = cause.getCause().getMessage();
            //输出详细的回退原因
            log.info("接口：" + route + ",fallback reason: {}", reason);
        }
        return new ClientHttpResponse() {
 
            @Override
            public InputStream getBody() {
                // 当出现服务调用错误之后返回的数据内容
                return new ByteArrayInputStream("{\"code\":404,\"msg\":\"服务暂不可用\"}".getBytes(StandardCharsets.UTF_8));
            }
 
            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                //和body中的内容编码一致，否则容易乱码
                headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
                return headers;
            }
 
            /**
             * 网关向api服务请求是失败了，但是消费者客户端向网关发起的请求是OK的，
             * 不应该把api的404,500等问题抛给客户端
             * 网关和api服务集群对于客户端来说是黑盒子
             */
            @Override
            public HttpStatus getStatusCode() {
                return HttpStatus.BAD_REQUEST;
            }
 
            @Override
            public int getRawStatusCode() {
                return HttpStatus.BAD_REQUEST.value();
            }
 
            @Override
            public String getStatusText() {
                return HttpStatus.BAD_REQUEST.getReasonPhrase();
            }
 
            @Override
            public void close() {
            }
        };
 
    }
 
    @Override
    public String getRoute() {
        return "*";
    }
}
