package com.dburyak.vertx.core;

import io.reactivex.rxjava3.core.Completable;

public interface AsyncAction {
    Completable execute();
}
