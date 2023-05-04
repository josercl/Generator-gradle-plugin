package com.gitlab.josercl.init.task;

import com.gitlab.josercl.init.creator.impl.ApplicationConfigurationCreator;
import com.gitlab.josercl.init.creator.impl.BasePageMapperCreator;
import com.gitlab.josercl.init.creator.impl.DomainPageCreator;
import com.gitlab.josercl.init.creator.impl.ErrorHandlerCreator;
import com.gitlab.josercl.init.creator.impl.MainApplicationCreator;
import com.gitlab.josercl.init.creator.impl.PersistenceConfigurationCreator;
import com.squareup.javapoet.JavaFile;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InitProjectTask extends DefaultTask {
    private final Map<String, List<String>> modulesDirs = Map.of(
        "boot", List.of(),
        "infrastructure", List.of("persistence/adapter", "persistence/config", "persistence/entity/mapper", "persistence/repository"),
        "domain", List.of("domain/api/impl", "domain/exception", "domain/model", "domain/spi"),
        "application", List.of("application/configuration", "application/rest/controller", "application/rest/model/mapper")
    );
    private final MainApplicationCreator mainApplicationCreator = MainApplicationCreator.getInstance();
    private final ApplicationConfigurationCreator applicationConfigurationCreator = ApplicationConfigurationCreator.getInstance();
    private final PersistenceConfigurationCreator persistenceConfigurationCreator = PersistenceConfigurationCreator.getInstance();
    private final ErrorHandlerCreator errorHandlerCreator = ErrorHandlerCreator.getInstance();
    private final DomainPageCreator domainPageCreator = DomainPageCreator.getInstance();
    private final BasePageMapperCreator basePageMapperCreator = BasePageMapperCreator.getInstance();

    @TaskAction
    public void run() throws IOException {
        Map<String, ?> projectProperties = getProject().getProperties();

        String basePackage = (String) projectProperties.getOrDefault("basePackage", null);

        if (basePackage == null) {
            throw new RuntimeException("Usage: gradle initProject -PbasePackage=xxx.yyy.zzz");
        }

        String baseFolder = basePackage.replaceAll("\\.", File.separator);

        writeBuildGradleFile(basePackage);
        initDirectories(baseFolder);
        createClasses(basePackage);
    }

    private static void writeBuildGradleFile(String basePackage) throws IOException {
        Path buildGradlePath = Path.of(System.getProperty("user.dir"), "build.gradle");
        byte[] bytes = Files.readAllBytes(buildGradlePath.toAbsolutePath());
        String content = new String(bytes).replace("$PKG", basePackage);
        Files.writeString(buildGradlePath, content, StandardOpenOption.WRITE);
    }

    private void initDirectories(String baseFolder) {
        String userDir = System.getProperty("user.dir");

        modulesDirs.entrySet()
            .stream()
            .flatMap(entry -> entry.getValue().stream().map(dir -> Path.of(
                userDir,
                entry.getKey(),
                "src", "main", "java",
                baseFolder,
                dir
            )))
            .toList()
            .forEach(path -> {
                try {
                    Files.createDirectories(path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    }

    private void createClasses(String basePackage) {
        try {
            mainApplicationCreator.createClass(basePackage);
            applicationConfigurationCreator.createClass(basePackage);
            persistenceConfigurationCreator.createClass(basePackage);
            errorHandlerCreator.createClass(basePackage);
            JavaFile domainPageFile = domainPageCreator.createClass(basePackage);
            basePageMapperCreator.createClass(basePackage, domainPageFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
