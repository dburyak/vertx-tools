package com.archiuse.mindis.call

import javax.inject.Singleton

@Singleton
class ServiceHelper {
    private static final String HEALTH_ACTION = 'health'
    private static final String READINESS_ACTION = 'ready'

    String getHealthAction() {
        HEALTH_ACTION
    }

    String getReadinessAction() {
        READINESS_ACTION
    }
}
