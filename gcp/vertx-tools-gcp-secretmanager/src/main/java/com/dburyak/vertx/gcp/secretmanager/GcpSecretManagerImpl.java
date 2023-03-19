package com.dburyak.vertx.gcp.secretmanager;

import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;

public class GcpSecretManagerImpl implements GcpSecretManager {
    private final Vertx vertx;
    private final String projectId;
    private final SecretManagerServiceClient secretManagerServiceClient;

    public GcpSecretManagerImpl(Vertx vertx, String projectId, SecretManagerServiceClient secretManagerServiceClient) {
        this.vertx = vertx;
        this.projectId = projectId;
        this.secretManagerServiceClient = secretManagerServiceClient;
    }

    @Override
    public Single<String> getSecretString(String secretId) {
        return getSecretString(projectId, secretId, null);
    }

    @Override
    public Single<String> getSecretString(String secretId, String version) {
        return getSecretString(projectId, secretId, version);
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
    public Single<byte[]> getSecretBinary(String secretId, String version) {
        return getSecretBinary(projectId, secretId, version);
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
                var versionName = SecretVersionName.newBuilder()
                        .setProject(projectId)
                        .setSecret(secretId)
                        .setSecretVersion(version != null ? version : "latest")
                        .build();
                var req = AccessSecretVersionRequest.newBuilder()
                        .setName(versionName.toString())
                        .build();
                var reqFuture = secretManagerServiceClient.accessSecretVersionCallable().futureCall(req);
                reqFuture.addListener(() -> {
                    try {
                        if (reqFuture.isCancelled()) {
                            vertxCtx.runOnContext(v -> emitter.onError(new RuntimeException("request cancelled")));
                            return;
                        }
                        var secret = reqFuture.get();
                        vertxCtx.runOnContext(v -> emitter.onSuccess(secret.getPayload()));
                    } catch (Exception e) {
                        vertxCtx.runOnContext(v -> emitter.onError(e));
                    }
                }, action -> vertxCtx.runOnContext(ignr -> action.run()));
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }
}
