package com.gcu.chinook.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Reads the Chinook metadata, then writes one entity file per table. */
@Component
class EntityGeneratorRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(EntityGeneratorRunner.class);

    private final MetadataReader metadataReader;
    private final EntityCodeGenerator codeGenerator;
    private final GeneratorProperties properties;

    EntityGeneratorRunner(MetadataReader metadataReader, EntityCodeGenerator codeGenerator,
            GeneratorProperties properties) {
        this.metadataReader = metadataReader;
        this.codeGenerator = codeGenerator;
        this.properties = properties;
    }

    @Override
    public void run(String... args) throws Exception {
        List<TableMeta> tables = metadataReader.read();
        Map<String, String> files = codeGenerator.generate(tables);
        writeFiles(files);
    }

    private void writeFiles(Map<String, String> files) throws IOException {
        Path outputDir = Path.of(properties.outputDir());
        Files.createDirectories(outputDir);

        for (Map.Entry<String, String> file : files.entrySet()) {
            Files.writeString(outputDir.resolve(file.getKey()), file.getValue());
            log.info("Wrote {}", outputDir.resolve(file.getKey()));
        }
        log.info("Generated {} files in {}", files.size(), outputDir.toAbsolutePath());
    }
}
