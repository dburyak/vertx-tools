package com.dburyak.vertx.gcp.secretmanager;

import io.reactivex.rxjava3.core.Single;

public interface GcpSecretManager {
    Single<String> getSecretString(String secretId);
    Single<String> getSecretString(String secretId, String version);
    Single<String> getSecretString(String projectId, String secretId, String version);
    Single<byte[]> getSecretBinary(String secretId);
    Single<byte[]> getSecretBinary(String secretId, String version);
    Single<byte[]> getSecretBinary(String projectId, String secretId, String version);
}
