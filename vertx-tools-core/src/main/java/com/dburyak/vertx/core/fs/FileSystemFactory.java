package com.dburyak.vertx.core.fs;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.file.FileSystem;
import jakarta.inject.Singleton;

/**
 * Factory for file system related beans.
 */
@Factory
public class FileSystemFactory {

    /**
     * {@link FileSystem} bean.
     *
     * @param vertx vertx instance
     *
     * @return singleton instance of {@link FileSystem}
     */
    @Singleton
    @Requires(missingBeans = FileSystem.class)
    public FileSystem fileSystem(Vertx vertx) {
        return vertx.fileSystem();
    }
}
