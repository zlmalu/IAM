
package com.sense.iam.api.config;
 
import java.util.List;
import java.util.stream.Collectors;
 
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
 
import io.swagger.models.parameters.Parameter;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2MapperImpl;
 
/**
 * Created by wujie on 2019/2/16.
 * 重写 将Document转换成Swagger 类, 根据order进行排序
 */
@Primary //同一个接口，可能会有几种不同的实现类，而默认只会采取其中一种的情况下
@Component("ServiceModelToSwagger2Mapper")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CustomModelToSwaggerMapper extends ServiceModelToSwagger2MapperImpl {
 
    @Override
    protected List<Parameter> parameterListToParameterList(List<springfox.documentation.service.Parameter> list) {
        //list需要根据order|postion排序
        list = list.stream().sorted((p1, p2) -> Integer.compare(p1.getOrder(), p2.getOrder())).collect(Collectors.toList());
//        log.debug("************************************list:{}", list.toString());
        return super.parameterListToParameterList(list);
    }
}

