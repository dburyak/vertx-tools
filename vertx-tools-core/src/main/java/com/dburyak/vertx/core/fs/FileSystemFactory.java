package com.dburyak.vertx.core.fs;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.file.FileSystem;
import jakarta.inject.Singleton;

@Factory
public class FileSystemFactory {

    @Singleton
    @Requires(missingBeans = FileSystem.class)
    public FileSystem fileSystem(Vertx vertx) {
        return vertx.fileSystem();
    }
}
