package service.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pcBuilder.backend.model.build.Build;
import com.pcBuilder.backend.model.component.Component;
import com.pcBuilder.backend.model.scraping.ScrapingLogEntry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final ObjectMapper objectMapper;
    private final Path componentsJsonPath;
    private final Path componentsCsvPath;
    private final Path buildsJsonPath;
    private final Path scrapingLogCsvPath;

    public FileStorageService(ObjectMapper objectMapper,
            @Value("${pcbuilder.storage.components-json:storage/components.json}") String componentsJson,
            @Value("${pcbuilder.storage.components-csv:storage/components.csv}") String componentsCsv,
            @Value("${pcbuilder.storage.builds-json:storage/builds.json}") String buildsJson,
            @Value("${pcbuilder.storage.scraping-log:storage/logs/scraping-log.csv}") String scrapingLog) {
        this.objectMapper = objectMapper.copy()
                .registerModule(new JavaTimeModule());
        this.objectMapper.findAndRegisterModules();
        this.objectMapper.writerWithDefaultPrettyPrinter();
        this.componentsJsonPath = Path.of(componentsJson);
        this.componentsCsvPath = Path.of(componentsCsv);
        this.buildsJsonPath = Path.of(buildsJson);
        this.scrapingLogCsvPath = Path.of(scrapingLog);
    }

    @PostConstruct
    void ensureDirectories() {
        try {
            createParentDirectories(componentsJsonPath);
            createParentDirectories(componentsCsvPath);
            createParentDirectories(buildsJsonPath);
            createParentDirectories(scrapingLogCsvPath);
        } catch (IOException ex) {
            log.error("Unable to create storage directories", ex);
        }
    }

    public synchronized List<Component> loadComponents() {
        if (!Files.exists(componentsJsonPath)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(componentsJsonPath.toFile(), new TypeReference<>() {
            });
        } catch (IOException ex) {
            log.error("Failed to load components from {}", componentsJsonPath, ex);
            return Collections.emptyList();
        }
    }

    public synchronized void saveComponents(List<Component> components) {
        try {
            createParentDirectories(componentsJsonPath);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(componentsJsonPath.toFile(), components);
            writeComponentsCsv(components);
            log.info("Persisted {} components to {}", components != null ? components.size() : 0, componentsJsonPath);
        } catch (IOException ex) {
            log.error("Failed to persist components", ex);
        }
    }

    public synchronized List<Build> loadBuilds() {
        if (!Files.exists(buildsJsonPath)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(buildsJsonPath.toFile(), new TypeReference<>() {
            });
        } catch (IOException ex) {
            log.error("Failed to load builds from {}", buildsJsonPath, ex);
            return Collections.emptyList();
        }
    }

    public synchronized void saveBuilds(Iterable<Build> builds) {
        try {
            createParentDirectories(buildsJsonPath);
            List<Build> snapshot = new ArrayList<>();
            if (builds != null) {
                builds.forEach(snapshot::add);
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(buildsJsonPath.toFile(), snapshot);
            log.info("Persisted {} builds to {}", snapshot.size(), buildsJsonPath);
        } catch (IOException ex) {
            log.error("Failed to persist builds", ex);
        }
    }

    public synchronized void appendScrapingLog(ScrapingLogEntry entry) {
        if (entry == null) {
            return;
        }
        try {
            createParentDirectories(scrapingLogCsvPath);
            boolean newFile = !Files.exists(scrapingLogCsvPath);
            try (BufferedWriter writer = Files.newBufferedWriter(scrapingLogCsvPath,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                if (newFile) {
                    writer.write("timestamp,category,status,durationMs,itemsFound,message");
                    writer.newLine();
                }
                writer.write(formatCsv(entry));
                writer.newLine();
            }
        } catch (IOException ex) {
            log.error("Failed to append scraping log", ex);
        }
    }

    private void writeComponentsCsv(List<Component> components) throws IOException {
        createParentDirectories(componentsCsvPath);
        try (BufferedWriter writer = Files.newBufferedWriter(componentsCsvPath,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write("id,name,brand,category,price,inStock,stockUnits");
            writer.newLine();
            if (components != null) {
                for (Component component : components) {
                    writer.write(String.format("\"%s\",\"%s\",\"%s\",%s,%.2f,%s,%d",
                            escape(component.getId()),
                            escape(component.getName()),
                            escape(component.getBrand()),
                            component.getCategory(),
                            component.getPrice(),
                            component.isInStock(),
                            component.getStockUnits()));
                    writer.newLine();
                }
            }
        }
    }

    private String formatCsv(ScrapingLogEntry entry) {
        return String.join(",",
                escape(entry.getTimestamp() != null ? entry.getTimestamp().toString() : null),
                escape(entry.getCategory() != null ? entry.getCategory().name() : null),
                escape(entry.getStatus()),
                String.valueOf(entry.getDurationMs()),
                String.valueOf(entry.getItemsFound()),
                escape(entry.getMessage()));
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        String sanitized = value.replace("\"", "\"\"");
        if (sanitized.contains(",") || sanitized.contains("\"") || sanitized.contains("\n")) {
            return "\"" + sanitized + "\"";
        }
        return sanitized;
    }

    private void createParentDirectories(Path path) throws IOException {
        if (path != null && path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
    }
}
