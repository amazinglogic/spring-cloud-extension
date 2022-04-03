package brave.propagation;

/**
 * @author amazinglogic
 * @date 2022/3/31
 */
public class TraceContextBuilder extends CurrentTraceContext.Builder {
    @Override
    public CurrentTraceContext build() {
        return new TransmittableTraceContext();
    }
}
