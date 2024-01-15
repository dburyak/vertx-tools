package com.dburyak.vertx.gcp.secretmanager;

import com.dburyak.vertx.gcp.ProjectIdProvider;
import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretPayload;
import io.micronaut.context.annotation.Requires;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of GCP Secret Manager client.
 */
@Singleton
@Requires(missingBeans = GcpSecretManager.class)
@Slf4j
public class GcpSecretManagerImpl implements GcpSecretManager {
    public static final String LATEST_VERSION = "latest";
    private final Vertx vertx;
    private final String projectId;
    private final SecretManagerServiceClient secretManagerServiceClient;
    private final SecretManagerUtil gsmUtil;

    /**
     * Constructor.
     *
     * @param vertx vertx
     * @param projectIdProvider gcp project id provider
     * @param secretManagerServiceClient GSM client
     */
    public GcpSecretManagerImpl(Vertx vertx, ProjectIdProvider projectIdProvider,
            SecretManagerServiceClient secretManagerServiceClient, SecretManagerUtil secretManagerUtil) {
        this.vertx = vertx;
        this.projectId = projectIdProvider.getProjectId();
        this.secretManagerServiceClient = secretManagerServiceClient;
        this.gsmUtil = secretManagerUtil;
    }

    @Override
    public Single<String> getSecretString(String secretId) {
        return getSecretString(projectId, secretId, null);
    }

    @Override
    public Single<String> getSecretString(String projectId, String secretId) {
        return getSecretString(projectId, secretId, null);
    }

    @Override
    public Single<String> getSecretString(String projectId, String secretId, String version) {
        return getSecretPayload(projectId, secretId, version)
                .map(p -> p.getData().toStringUtf8());
    }

    @Override
    public Single<byte[]> getSecretBinary(String secretId) {
        return getSecretBinary(projectId, secretId, null);
    }

    @Override
    public Single<byte[]> getSecretBinary(String projectId, String secretId) {
        return getSecretBinary(projectId, secretId, null);
    }

    @Override
    public Single<byte[]> getSecretBinary(String projectId, String secretId, String version) {
        return getSecretPayload(projectId, secretId, version)
                .map(p -> p.getData().toByteArray());
    }

    private Single<SecretPayload> getSecretPayload(String projectId, String secretId, String version) {
        var vertxCtx = vertx.getOrCreateContext();
        return Single.create(emitter -> {
            try {
                var evaluatedVersion = (version != null && !version.isBlank()) ? version : LATEST_VERSION;
                var fqnSecret = gsmUtil.fqnSecret(projectId, secretId, evaluatedVersion);
                var req = AccessSecretVersionRequest.newBuilder()
                        .setName(fqnSecret)
                        .build();
                var reqFuture = secretManagerServiceClient.accessSecretVersionCallable().futureCall(req);
                reqFuture.addListener(() -> {
                    try {
                        if (reqFuture.isCancelled()) {
                            log.debug("underlying gsm get secret request was cancelled: secret={}", fqnSecret);
                            emitter.onError(new RuntimeException("underlying gsm get secret request was cancelled"));
                            return;
                        }
                        if (!reqFuture.isDone()) {
                            log.debug("underlying gsm get secret request is not completed: secret={}", fqnSecret);
                            emitter.onError(new RuntimeException("underlying gsm get secret request is not completed"));
                            return;
                        }
                        var secret = reqFuture.get();
                        log.debug("underlying gsm get secret request completed: secret={}", fqnSecret);
                        emitter.onSuccess(secret.getPayload());
                    } catch (Exception e) {
                        log.debug("underlying gsm get secret request failed: secret={}", fqnSecret, e);
                        emitter.onError(e);
                    }
                }, action -> vertxCtx.runOnContext(ignr -> action.run()));
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }
}
