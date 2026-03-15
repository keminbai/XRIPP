package com.xripp.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class UploadStorageProperties {

    private final Path baseDir;

    public UploadStorageProperties(@Value("${xripp.upload.base-dir:./uploads}") String configuredBaseDir) {
        this.baseDir = Paths.get(configuredBaseDir == null || configuredBaseDir.isBlank() ? "./uploads" : configuredBaseDir)
                .toAbsolutePath()
                .normalize();
    }

    public Path getBaseDir() {
        return baseDir;
    }

    public String getResourceLocation() {
        String uri = baseDir.toUri().toString();
        return uri.endsWith("/") ? uri : (uri + "/");
    }
}
