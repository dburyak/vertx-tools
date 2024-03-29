package com.dburyak.vertx.core.di;

import com.dburyak.vertx.core.config.ThreadLocalScopeProperties;
import io.micronaut.context.scope.AbstractConcurrentCustomScope;
import io.micronaut.context.scope.CreatedBean;
import io.micronaut.inject.BeanIdentifier;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Thread local bean scope implementation.
 */
@Singleton
@Slf4j
public class ThreadLocalScopeImpl extends AbstractConcurrentCustomScope<ThreadLocalScope> {
    private final ThreadLocalScopeProperties props;
    private final Map<Thread, Map<BeanIdentifier, CreatedBean<?>>> beans = new ConcurrentHashMap<>();
    private volatile Disposable cleanupTicker;

    /**
     * Constructor.
     *
     * @param props thread local scope properties
     */
    public ThreadLocalScopeImpl(ThreadLocalScopeProperties props) {
        super(ThreadLocalScope.class);
        this.props = props;
    }

    /**
     * Get beans map for current thread.
     *
     * @param forCreation true if map is needed for bean creation, false if for bean destruction
     *
     * @return beans map for current thread
     */
    @Override
    protected Map<BeanIdentifier, CreatedBean<?>> getScopeMap(boolean forCreation) {
        return beans.computeIfAbsent(Thread.currentThread(), tn -> new HashMap<>());
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public void close() {
        cleanupTicker.dispose();
        beans.values().forEach(this::destroyScope);
    }

    @PostConstruct
    void startCleanupTicker() {
        var periodMs = props.getCleanupCheckerPeriod().toMillis();
        log.debug("using thread local cleanup checker period: periodMs={}", periodMs);
        cleanupTicker = Flowable.interval(periodMs, TimeUnit.MILLISECONDS)
                .onBackpressureLatest()
                .flatMapSingle(tick -> Flowable.fromIterable(beans.keySet())
                        .filter(t -> !t.isAlive())
                        .toList()
                )
                .subscribe(deadThreads -> deadThreads.forEach(t -> destroyScope(beans.remove(t))));
    }
}
