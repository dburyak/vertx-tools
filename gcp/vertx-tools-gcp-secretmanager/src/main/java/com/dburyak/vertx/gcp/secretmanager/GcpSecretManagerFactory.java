package com.dburyak.vertx.gcp.secretmanager;

import com.google.api.gax.core.CredentialsProvider;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

import java.io.IOException;

/**
 * Factory for GCP Secret Manager related default implementations beans.
 */
@Factory
public class GcpSecretManagerFactory {

    /**
     * Underlying gcp sdk GSM client default bean.
     *
     * @param credentialsProvider credentials provider
     *
     * @return GSM client
     *
     * @throws IOException if client creation failed
     */
    @Singleton
    @Requires(missingBeans = SecretManagerServiceClient.class)
    public SecretManagerServiceClient secretManagerServiceClient(CredentialsProvider credentialsProvider)
            throws IOException {
        var settings = SecretManagerServiceSettings.newBuilder()
                .setCredentialsProvider(credentialsProvider)
                .build();
        return SecretManagerServiceClient.create(settings);
    }
}
