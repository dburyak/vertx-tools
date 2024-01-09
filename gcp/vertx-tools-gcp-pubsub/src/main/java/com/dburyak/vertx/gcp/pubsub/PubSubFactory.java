package com.dburyak.vertx.gcp.pubsub;

import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

import java.io.IOException;

@Factory
public class PubSubFactory {

    @Singleton
    @Bean(preDestroy = "close")
    @Requires(missingBeans = SubscriptionAdminClient.class)
    public SubscriptionAdminClient subscriptionAdminClient(SubscriptionAdminSettings subscriptionAdminSettings)
            throws IOException {
        return SubscriptionAdminClient.create(subscriptionAdminSettings);
    }

    @Singleton
    @Requires(missingBeans = SubscriptionAdminSettings.class)
    public SubscriptionAdminSettings subscriptionAdminSettings() throws IOException {
        return SubscriptionAdminSettings.newBuilder()
                .build();
    }
}
