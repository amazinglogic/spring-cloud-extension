package cn.amazinglogic.sleuth.feign;

import brave.Span;
import brave.Tracer;
import brave.propagation.TraceContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author amazinglogic
 * @date 2022/4/5
 **/
public class FeignInterceptor implements RequestInterceptor {

    @Autowired
    Tracer tracer;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        Span span = tracer.nextSpan();
        span.tag("feign","feign");
        TraceContext context = span.context();
        requestTemplate.header("X-B3-ParentSpanId", context.parentIdString());
        requestTemplate.header("X-B3-TraceId", context.traceIdString());
        requestTemplate.header("X-B3-SpanId", context.spanIdString());
        span.abandon();
    }
}
