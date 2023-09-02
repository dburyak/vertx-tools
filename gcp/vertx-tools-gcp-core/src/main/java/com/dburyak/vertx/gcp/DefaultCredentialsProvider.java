package com.dburyak.vertx.gcp;

import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

import java.io.IOException;

/**
 * {@link CredentialsProvider} default provider bean. This credentials provider follows credentials
 * discovery mechanism documented in
 * <a href="https://github.com/googleapis/google-cloud-java#application-default-credentials">gcp-java-sdk-docs</a>.
 */
@Singleton
@Requires(missingBeans = CredentialsProvider.class)
public class DefaultCredentialsProvider implements CredentialsProvider {
    private volatile Credentials credentials;

    /**
     * Get default GCP credentials.
     *
     * @return default GCP credentials
     *
     * @throws IOException if credentials loading failed
     */
    @Override
    public Credentials getCredentials() throws IOException {
        var credentialsResult = this.credentials;
        // this code is not thread safe, but we are totally fine to call it multiple times on initialization and use
        // cached value afterwards, as it:
        //  - always produces the same result
        //  - calculating it is not very expensive
        if (credentialsResult == null) {
            credentialsResult = GoogleCredentials.getApplicationDefault();
            this.credentials = credentialsResult;
        }
        return credentialsResult;
    }
}
