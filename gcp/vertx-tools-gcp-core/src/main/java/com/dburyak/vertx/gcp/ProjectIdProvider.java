package com.dburyak.vertx.gcp;

import jakarta.inject.Singleton;

@Singleton
public interface ProjectIdProvider {
    String getProjectId();
}
