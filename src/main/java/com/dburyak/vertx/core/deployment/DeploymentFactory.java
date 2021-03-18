package com.dburyak.vertx.core.deployment;

import com.dburyak.vertx.core.deployment.spec.Deployment;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Secondary;
import io.micronaut.context.annotation.Value;
import io.vertx.reactivex.core.file.FileSystem;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Factory
@Secondary
public class DeploymentFactory {

    @Bean
    @Secondary
    public Deployment deployment(@Value("${application.deployment.config:deployment.groovy}") String deploymentFilePath,
            List<DeploymentConfigParser> parsers) {
        var deploymentFile = new File(filePath);
        if (deploymentFile.isFile()) { // is file in file system
            try (deploymentStream = new BufferedInputStream(new FileInputStream(deploymentFile))) {

            }
        }
        try (var classpathInStream = getClass().getResourceAsStream(deploymentConfig)) {
            if ()
        } catch (IOException e) {
            e.printStackTrace();
        }
        // TODO: implement parsing of routing config
        return Deployment.builder().build();
    }

    private DeploymentConfigParser findParser(String deploymentFilePath, List<DeploymentConfigParser> parsers) {
        parsers.stream()
                .filter(p -> p.canParse(deploymentFilePath))
                .findFirst().orElseThrow();
    }
}
