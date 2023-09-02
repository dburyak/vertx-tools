package com.dburyak.vertx.gcp;

import jakarta.inject.Singleton;

/**
 * GCP project ID provider.
 */
@Singleton
public interface ProjectIdProvider {

    /**
     * Get GCP project ID.
     *
     * @return GCP project ID
     */
    String getProjectId();
}
