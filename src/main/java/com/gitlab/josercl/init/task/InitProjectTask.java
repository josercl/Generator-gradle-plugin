package com.gitlab.josercl.init.task;

import com.gitlab.josercl.init.creator.impl.ApplicationConfigurationCreator;
import com.gitlab.josercl.init.creator.impl.BasePageMapperCreator;
import com.gitlab.josercl.init.creator.impl.CustomExceptionCreator;
import com.gitlab.josercl.init.creator.impl.DomainPageCreator;
import com.gitlab.josercl.init.creator.impl.ErrorHandlerCreator;
import com.gitlab.josercl.init.creator.impl.ErrorResponseStatusCreator;
import com.gitlab.josercl.init.creator.impl.MainApplicationCreator;
import com.gitlab.josercl.init.creator.impl.PersistenceConfigurationCreator;
import com.gitlab.josercl.init.creator.impl.RecordNotFoundExceptionCreator;
import com.gitlab.josercl.init.creator.impl.ValidationErrorCreator;
import com.gitlab.josercl.init.exception.InitException;
import com.squareup.javapoet.JavaFile;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
    private final Project project = getProject();
    private final MainApplicationCreator mainApplicationCreator = MainApplicationCreator.getInstance(project);
    private final ApplicationConfigurationCreator applicationConfigurationCreator = ApplicationConfigurationCreator.getInstance(project);
    private final PersistenceConfigurationCreator persistenceConfigurationCreator = PersistenceConfigurationCreator.getInstance(project);
    private final ErrorHandlerCreator errorHandlerCreator = ErrorHandlerCreator.getInstance(project);
    private final DomainPageCreator domainPageCreator = DomainPageCreator.getInstance(project);
    private final BasePageMapperCreator basePageMapperCreator = BasePageMapperCreator.getInstance(project);
    private final ValidationErrorCreator validationErrorCreator = ValidationErrorCreator.getInstance(project);
    private final RecordNotFoundExceptionCreator recordNotFoundExceptionCreator = RecordNotFoundExceptionCreator.getInstance(project);
    private final ErrorResponseStatusCreator errorResponseStatusCreator = ErrorResponseStatusCreator.getInstance(project);
    private final CustomExceptionCreator customExceptionCreator = CustomExceptionCreator.getInstance(project);
    private final String projectPath = project.getProjectDir().getAbsolutePath();

    @TaskAction
    public void run() throws IOException {
        Map<String, ?> projectProperties = project.getProperties();

        String basePackage = Optional.ofNullable(projectProperties.get("basePackage"))
            .map(String.class::cast)
            .orElseThrow(InitException::new);

        String projectName = Optional.ofNullable(projectProperties.get("projectName"))
            .map(String.class::cast)
            .orElseThrow(InitException::new);

        String baseFolder = basePackage.replaceAll("\\.", File.separator);

        writeSettingsGradleFile(projectName);
        writeBuildGradleFile(basePackage);
        initDirectories(baseFolder);
        createClasses(basePackage);
    }

    private void writeSettingsGradleFile(String projectName) throws IOException {
        Path settingsGradlePath = Path.of(projectPath, "settings.gradle");
        byte[] bytes = Files.readAllBytes(settingsGradlePath.toAbsolutePath());
        String content = new String(bytes).replace("spring-boot-gradle-template", projectName);
        Files.writeString(settingsGradlePath, content, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void writeBuildGradleFile(String basePackage) throws IOException {
        Path buildGradlePath = Path.of(projectPath, "build.gradle");
        byte[] bytes = Files.readAllBytes(buildGradlePath.toAbsolutePath());
        String content = new String(bytes).replace("$PKG", basePackage);
        Files.writeString(buildGradlePath, content, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void initDirectories(String baseFolder) {
        modulesDirs.entrySet()
            .stream()
            .flatMap(entry -> entry.getValue().stream().map(dir -> Path.of(
                projectPath,
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

    private void createClasses(String basePackage) throws IOException {
//        mainApplicationCreator.createClass(basePackage);
//        applicationConfigurationCreator.createClass(basePackage);
//        persistenceConfigurationCreator.createClass(basePackage);
        errorHandlerCreator.createClass(basePackage);
//        JavaFile domainPageFile = domainPageCreator.createClass(basePackage);
//        basePageMapperCreator.createClass(basePackage, domainPageFile);
//        validationErrorCreator.createClass(basePackage);
//        customExceptionCreator.createClass(basePackage);
//        recordNotFoundExceptionCreator.createClass(basePackage);
//        errorResponseStatusCreator.createClass(basePackage);
    }
}
