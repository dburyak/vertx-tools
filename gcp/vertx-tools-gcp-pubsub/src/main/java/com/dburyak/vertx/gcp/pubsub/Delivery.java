package com.dburyak.vertx.gcp.pubsub;

import io.reactivex.rxjava3.core.Completable;

public interface Delivery {
    Completable ack();

    Completable nack();
}
