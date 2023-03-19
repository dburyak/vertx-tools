package com.dburyak.vertx.gcp.secretmanager;

import com.dburyak.vertx.gcp.ProjectIdProvider;
import com.google.api.gax.core.CredentialsProvider;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.vertx.rxjava3.core.Vertx;
import jakarta.inject.Singleton;

import java.io.IOException;

@Factory
public class GcpSecretManagerFactory {

    @Singleton
    @Requires(missingBeans = SecretManagerServiceClient.class)
    public SecretManagerServiceClient secretManagerServiceClient(CredentialsProvider credentialsProvider)
            throws IOException {
        var settings = SecretManagerServiceSettings.newBuilder()
                .setCredentialsProvider(credentialsProvider)
                .build();
        return SecretManagerServiceClient.create(settings);
    }

    @Singleton
    @Requires(missingBeans = GcpSecretManager.class)
    public GcpSecretManager gcpSecretManager(Vertx vertx, ProjectIdProvider projectIdProvider,
            SecretManagerServiceClient secretManagerServiceClient) {
        return new GcpSecretManagerImpl(vertx, projectIdProvider.getProjectId(), secretManagerServiceClient);
    }
}
