package com.dburyak.vertx.core.fs;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Secondary;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.file.FileSystem;

import javax.inject.Singleton;

@Factory
@Secondary
public class FileSystemFactory {

    @Singleton
    @Secondary
    public FileSystem fileSystem(Vertx vertx) {
        return vertx.fileSystem();
    }
}
