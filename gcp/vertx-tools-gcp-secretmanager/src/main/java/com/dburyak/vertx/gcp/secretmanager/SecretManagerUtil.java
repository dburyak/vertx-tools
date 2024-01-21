package com.dburyak.vertx.gcp.secretmanager;

import com.google.cloud.secretmanager.v1.SecretName;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import jakarta.inject.Singleton;

import static com.dburyak.vertx.gcp.secretmanager.GcpSecretManagerImpl.LATEST_VERSION;

@Singleton
public class SecretManagerUtil {
    private static final String FQN_PREFIX = "projects/";
    private static final String SECRETS_SUBSTR = "/secrets/";
    private static final String VERSIONS_SUBSTR = "/versions/";
    private static final String LATEST_VERSION_SUFFIX = VERSIONS_SUBSTR + LATEST_VERSION;
    private static final String ERR_TMPL_ALREADY_CONTAINS_PROJECT = "Secret name already contains project: %s";

    public boolean isFqn(String secretId) {
        return secretId.startsWith(FQN_PREFIX)
                && secretId.contains(SECRETS_SUBSTR)
                && secretId.contains(VERSIONS_SUBSTR);
    }

    public boolean hasProject(String secretId) {
        return secretId.startsWith(FQN_PREFIX);
    }

    public boolean hasVersion(String secretId) {
        return secretId.contains(VERSIONS_SUBSTR);
    }

    public String extractProject(String secretId) {
        if (!hasProject(secretId)) {
            throw new IllegalArgumentException(String.format(ERR_TMPL_ALREADY_CONTAINS_PROJECT, secretId));
        }
        return secretId.substring(FQN_PREFIX.length(), secretId.indexOf('/', FQN_PREFIX.length()));
    }

    public String extractSecretName(String secretId) {
        if (!hasProject(secretId)) {
            return secretId;
        }
        var posStart = secretId.indexOf(SECRETS_SUBSTR) + SECRETS_SUBSTR.length();
        var posEnd = secretId.indexOf("/", posStart);
        return posEnd > 0 ? secretId.substring(posStart, posEnd) : secretId.substring(posStart);
    }

    public String extractVersion(String secretId) {
        if (!hasVersion(secretId)) {
            return LATEST_VERSION;
        }
        var posStart = secretId.indexOf(VERSIONS_SUBSTR) + VERSIONS_SUBSTR.length();
        return secretId.substring(posStart);
    }

    public String fqnSecretLatest(String projectId, String secretName) {
        if (hasProject(secretName)) {
            throw new IllegalArgumentException(String.format(ERR_TMPL_ALREADY_CONTAINS_PROJECT, secretName));
        }
        return FQN_PREFIX + projectId + SECRETS_SUBSTR + secretName + LATEST_VERSION_SUFFIX;
    }

    public String fqnSecret(String projectId, String secretName, String secretVersion) {
        if (hasProject(secretName)) {
            throw new IllegalArgumentException(String.format(ERR_TMPL_ALREADY_CONTAINS_PROJECT, secretName));
        }
        return FQN_PREFIX + projectId + SECRETS_SUBSTR + secretName + VERSIONS_SUBSTR + secretVersion;
    }

    public SecretVersionName fqnSecretVersionNameLatest(String projectId, String secretName) {
        if (hasProject(secretName)) {
            throw new IllegalArgumentException(String.format(ERR_TMPL_ALREADY_CONTAINS_PROJECT, secretName));
        }
        return SecretVersionName.of(projectId, secretName, LATEST_VERSION);
    }

    public SecretVersionName fqnSecretVersionName(String projectId, String secretName, String version) {
        if (hasProject(secretName)) {
            throw new IllegalArgumentException(String.format(ERR_TMPL_ALREADY_CONTAINS_PROJECT, secretName));
        }
        var versionResolved = version != null && !version.isBlank() ? version : LATEST_VERSION;
        return SecretVersionName.of(projectId, secretName, versionResolved);
    }

    public SecretName fqnSecretName(String projectId, String secretName) {
        if (hasProject(secretName)) {
            throw new IllegalArgumentException(String.format(ERR_TMPL_ALREADY_CONTAINS_PROJECT, secretName));
        }
        return SecretName.of(projectId, secretName);
    }

    public String ensureFqnSecret(String projectId, String secretName) {
        if (isFqn(secretName)) {
            return secretName;
        }
        return fqnSecretLatest(projectId, secretName);
    }

    public String ensureFqnSecret(String projectId, String secretName, String secretVersion) {
        if (isFqn(secretName)) {
            return secretName;
        }
        return fqnSecret(projectId, secretName, secretVersion);
    }
}
