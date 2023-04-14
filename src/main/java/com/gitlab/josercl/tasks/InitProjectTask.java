package com.gitlab.josercl.tasks;

import com.gitlab.josercl.init.ApplicationConfigurationCreator;
import com.gitlab.josercl.init.BasePageMapperCreator;
import com.gitlab.josercl.init.ClassCreator;
import com.gitlab.josercl.init.DomainPageCreator;
import com.gitlab.josercl.init.ErrorHandlerCreator;
import com.gitlab.josercl.init.MainApplicationCreator;
import com.gitlab.josercl.init.PersistenceConfigurationCreator;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.mapstruct.Mapping;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class InitProjectTask extends DefaultTask {
    private final Map<String, List<String>> modulesDirs = Map.of(
        "boot", List.of(),
        "infrastructure", List.of("persistence/adapter", "persistence/config", "persistence/entity/mapper", "persistence/repository"),
        "domain", List.of("domain/api/impl", "domain/exception", "domain/model", "domain/spi"),
        "application", List.of("application/configuration", "application/rest/controller", "application/rest/model/mapper")
    );

    @TaskAction
    public void run() {
        String basePackage = Optional.ofNullable(getProject().getProperties().get("basePackage"))
            .map(String.class::cast)
            .orElse((String) getProject().getGroup());

        String baseFolder = basePackage.replaceAll("\\.", File.separator);

        initDirectories(baseFolder);
        createClasses(basePackage);
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
            MainApplicationCreator.getInstance().createClass(basePackage);
            ApplicationConfigurationCreator.getInstance().createClass(basePackage);
            PersistenceConfigurationCreator.getInstance().createClass(basePackage);
            ErrorHandlerCreator.getInstance().createClass(basePackage);
            JavaFile domainPageFile = DomainPageCreator.getInstance().createClass(basePackage);
            BasePageMapperCreator.getInstance().createClass(basePackage, domainPageFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
