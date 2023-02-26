package com.dburyak.vertx.core.fs;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Secondary;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.file.FileSystem;
import jakarta.inject.Singleton;

@Factory
@Secondary
public class FileSystemFactory {

    @Singleton
    @Secondary
    public FileSystem fileSystem(Vertx vertx) {
        return vertx.fileSystem();
    }
}
