package xyz.zionjo.littlemall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class LittleFeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor(){

            @Override
            public void apply(RequestTemplate requestTemplate) {
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if(requestAttributes != null){
                    HttpServletRequest request = requestAttributes.getRequest();

                    // 同步请求头数据
                    if(request !=null){
                        String cookie = request.getHeader("Cookie");
                        requestTemplate.header("Cookie",cookie);

                    }
                }


            }
        };
    }
}
