package com.dburyak.vertx.gcp;

import com.google.cloud.ServiceOptions;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

@Singleton
@Requires(missingBeans = ProjectIdProvider.class)
public class DefaultProjectIdProvider implements ProjectIdProvider {
    private volatile String projectId;

    @Override
    public String getProjectId() {
        var projectIdResult = this.projectId;
        // this code is not thread safe, but we are totally fine to call it multiple times on initialization and use
        // cached value afterwards, as it:
        //  - always produces the same result
        //  - calculating it is not very expensive
        if (projectIdResult == null) {
            projectIdResult = ServiceOptions.getDefaultProjectId();
            this.projectId = projectIdResult;
        }
        return projectIdResult;
    }
}
