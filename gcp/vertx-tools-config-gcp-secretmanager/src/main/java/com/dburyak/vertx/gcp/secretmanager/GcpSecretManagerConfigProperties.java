package com.dburyak.vertx.gcp.secretmanager;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import io.micronaut.core.convert.format.MapFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Duration;
import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * Configuration properties for vertx config store backed by GCP Secret Manager.
 */
@ConfigurationProperties("vertx.gcp.config.secret-manager")
@Context
@Data
public class GcpSecretManagerConfigProperties {

    private boolean enabled = true;

    private boolean optional = false;

    private String projectId;

    /**
     * How often to re-read secrets from Google Secret Manager.
     * Null, zero or negative value means no refresh.
     */
    private Duration refreshPeriod = Duration.ofHours(1);

    @MapFormat(transformation = MapFormat.MapTransformation.FLAT)
    @NotNull
    private Map<String, String> secretConfigOptions = emptyMap();
}
