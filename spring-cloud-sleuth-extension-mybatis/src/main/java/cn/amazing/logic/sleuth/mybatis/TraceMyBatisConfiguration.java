package cn.amazing.logic.sleuth.mybatis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;

/**
 * @author sea
 * @date 2022/3/31
 */
@Configuration
public class TraceMyBatisConfiguration {

    @Bean
    public TraceMybatisInterceptor traceMybatisInterceptor() {
        return new TraceMybatisInterceptor();
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(MybatisPlusProperties.class)
    public MybatisPlusProperties mybatisPlusProperties() {
        return new MybatisPlusProperties();
    }
}
