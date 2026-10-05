package com.gcu.chinook.generator;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Runs once at startup and logs the Chinook metadata. */
@Component
class EntityGeneratorRunner implements CommandLineRunner {

    private final MetadataReader metadataReader;

    EntityGeneratorRunner(MetadataReader metadataReader) {
        this.metadataReader = metadataReader;
    }

    @Override
    public void run(String... args) throws Exception {
        metadataReader.read();
    }
}
