package brave.propagation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author amazinglogic
 * @date 2022/3/31
 */
@Configuration
public class SleuthExtensionConfiguration {

    @Bean
    public CurrentTraceContext.Builder traceContextBuilder() {
        return new TraceContextBuilder();
    }
}
