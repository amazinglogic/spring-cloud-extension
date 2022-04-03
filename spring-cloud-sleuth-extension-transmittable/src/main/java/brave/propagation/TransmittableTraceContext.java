package brave.propagation;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * @author amazinglogic
 * @date 2022/3/31
 */
public class TransmittableTraceContext extends CurrentTraceContext{

    TransmittableThreadLocal<TraceContext> local;
    Scope revertToNull;

    public TransmittableTraceContext() {
        local = new TransmittableThreadLocal<>();
        revertToNull = new ThreadLocalCurrentTraceContext.RevertToNullScope(local);
    }

    @Override
    public TraceContext get() {
        return local.get();
    }

    @Override
    public Scope newScope(TraceContext context) {
        final TraceContext previous = local.get();
        local.set(context);
        Scope result = previous != null ? new TransmittableRevertToPreviousScope(local, previous) : revertToNull;
        return decorateScope(context, result);
    }

    static class TransmittableRevertToPreviousScope implements Scope{
        final ThreadLocal<TraceContext> local;
        final TraceContext previous;

        public TransmittableRevertToPreviousScope(ThreadLocal<TraceContext> local, TraceContext previous) {
            this.local = local;
            this.previous = previous;
        }

        @Override
        public void close() {
            local.set(previous);
        }
    }
}
