package com.dburyak.vertx.gcp.secretmanager;

import com.google.cloud.secretmanager.v1.SecretName;
import jakarta.inject.Singleton;

@Singleton
public class SecretManagerUtil {
    public static final String FQN_PREFIX = "projects/";

    public boolean isFqn(String secretName) {
        return secretName.startsWith(FQN_PREFIX);
    }

    public String fqnSecret(String projectId, String secretName) {
        return FQN_PREFIX + projectId + "/secrets/" + secretName;
    }

    public SecretName fqnSecretName(String projectId, String secretName) {
        if (isFqn(secretName)) {
            return SecretName.parse(secretName);
        }
        return SecretName.of(projectId, secretName);
    }

    public String ensureFqnSecret(String projectId, String secretName) {
        if (isFqn(secretName)) {
            return secretName;
        }
        return fqnSecret(projectId, secretName);
    }

    public String forceProjectForSecret(String projectId, String secretName) {
        if (!isFqn(secretName)) {
            return fqnSecret(projectId, secretName);
        } else if (secretName.startsWith(FQN_PREFIX + projectId + "/")) {
            return secretName;
        } else { // is FQN but not for this project
            var secret = SecretName.parse(secretName);
            return secret.toBuilder()
                    .setProject(projectId)
                    .build()
                    .toString();
        }
    }

    public String secretShortName(String secretName) {
        if (!isFqn(secretName)) {
            return secretName;
        }
        return secretName.substring(secretName.lastIndexOf('/') + 1);
    }
}
