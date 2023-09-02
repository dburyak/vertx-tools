package com.dburyak.vertx.gcp.secretmanager;

import io.reactivex.rxjava3.core.Single;

/**
 * GCP Secret Manager client.
 */
public interface GcpSecretManager {

    /**
     * Get GSM secret as string.
     *
     * @param secretId secret id
     *
     * @return secret string value
     */
    Single<String> getSecretString(String secretId);

    /**
     * Get GSM secret of specific version as string.
     *
     * @param secretId secret id
     * @param version secret version
     *
     * @return secret string value
     */
    Single<String> getSecretString(String secretId, String version);

    /**
     * Get GSM secret of specific version from specific gcp project as string.
     *
     * @param projectId gcp project id
     * @param secretId secret id
     * @param version secret version
     *
     * @return secret string value
     */
    Single<String> getSecretString(String projectId, String secretId, String version);

    /**
     * Get GSM secret as binary.
     *
     * @param secretId secret id
     *
     * @return secret binary value
     */
    Single<byte[]> getSecretBinary(String secretId);

    /**
     * Get GSM secret of specific version as binary.
     *
     * @param secretId secret id
     * @param version secret version
     *
     * @return secret binary value
     */
    Single<byte[]> getSecretBinary(String secretId, String version);

    /**
     * Get GSM secret of specific version from specific gcp project as binary.
     *
     * @param projectId gcp project id
     * @param secretId secret id
     * @param version secret version
     *
     * @return secret binary value
     */
    Single<byte[]> getSecretBinary(String projectId, String secretId, String version);
}
