package com.dburyak.vertx.gcp.pubsub;

import com.google.api.gax.core.ExecutorProvider;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ScheduledExecutorService;

@RequiredArgsConstructor
public class FixedExecutorProvider implements ExecutorProvider {
    private final ScheduledExecutorService executor;

    @Override
    public boolean shouldAutoClose() {
        return false;
    }

    @Override
    public ScheduledExecutorService getExecutor() {
        return executor;
    }
}
