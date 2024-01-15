package com.dburyak.vertx.gcp.secretmanager;

import com.google.cloud.secretmanager.v1.SecretVersionName;
import jakarta.inject.Singleton;

@Singleton
public class SecretManagerUtil {
    public static final String FQN_PREFIX = "projects/";
    private static final String SECRETS_SUBSTR = "/secrets/";
    private static final String VERSIONS_SUBSTR = "/versions/";
    private static final String LATEST_VERSION = "latest";
    private static final String LATEST_VERSION_SUFFIX = VERSIONS_SUBSTR + LATEST_VERSION;

    public boolean isFqn(String secretName) {
        return secretName.startsWith(FQN_PREFIX)
                && secretName.contains(SECRETS_SUBSTR)
                && secretName.contains(VERSIONS_SUBSTR);
    }

    public boolean hasProject(String secretName) {
        return secretName.startsWith(FQN_PREFIX);
    }

    public String getProject(String secretName) {
        if (!hasProject(secretName)) {
            throw new IllegalArgumentException("Secret name " + secretName + " does not contain project");
        }
        return secretName.substring(FQN_PREFIX.length(), secretName.indexOf('/', FQN_PREFIX.length()));
    }

    public String getSecretName(String secretFullName) {
        if (!hasProject(secretFullName)) {
            throw new IllegalArgumentException("Secret name " + secretFullName + " is not FQN");
        }
        var posStart = secretFullName.indexOf(SECRETS_SUBSTR) + SECRETS_SUBSTR.length();
        var posEnd = secretFullName.indexOf("/", posStart);
        // TODO: here ......................................... fails here, posEnd = -1, run with debugger
        return secretFullName.substring(posStart, posEnd);
    }

    public String fqnSecret(String projectId, String secretName) {
        return FQN_PREFIX + projectId + SECRETS_SUBSTR + secretName + LATEST_VERSION_SUFFIX;
    }

    public String fqnSecret(String projectId, String secretName, String secretVersion) {
        return FQN_PREFIX + projectId + SECRETS_SUBSTR + secretName + VERSIONS_SUBSTR + secretVersion;
    }

    public SecretVersionName fqnSecretVersionName(String projectId, String secretName) {
        if (isFqn(secretName)) {
            return SecretVersionName.parse(secretName);
        } else if (hasProject(secretName)) {
            return SecretVersionName.of(getProject(secretName), getSecretName(secretName), LATEST_VERSION);
        } else {
            return SecretVersionName.of(projectId, secretName, LATEST_VERSION);
        }
    }

    public SecretVersionName fqnSecretVersionName(String projectId, String secretName, String version) {
        var versionResolved = version != null && !version.isBlank() ? version : LATEST_VERSION;
        if (isFqn(secretName)) {
            return SecretVersionName.parse(secretName);
        } else if (hasProject(secretName)) {
            return SecretVersionName.of(getProject(secretName), getSecretName(secretName), versionResolved);
        } else {
            return SecretVersionName.of(projectId, secretName, versionResolved);
        }
    }

    public String ensureFqnSecret(String projectId, String secretName) {
        if (isFqn(secretName)) {
            return secretName;
        }
        return fqnSecret(projectId, secretName);
    }

    public String ensureFqnSecret(String projectId, String secretName, String secretVersion) {
        if (isFqn(secretName)) {
            return secretName;
        }
        return fqnSecret(projectId, secretName, secretVersion);
    }

    public String secretShortName(String secretName) {
        if (!hasProject(secretName)) {
            return secretName;
        }
        return getSecretName(secretName);
    }
}
