package cn.amazinglogic.sleuth.feign;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author amazinglogic
 * @date 2022/4/5
 **/
@Configuration
public class FeignConfiguration {

    @Bean
    public FeignInterceptor feignInterceptor(){
        return new FeignInterceptor();
    }
}
