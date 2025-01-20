package com.gitlab.josercl.init.creator.impl;

import com.gitlab.josercl.init.creator.ClassCreator;
import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeSpec;
import org.gradle.api.Project;
import org.springframework.context.annotation.Configuration;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;

public class ApplicationConfigurationCreator extends ClassCreator {
    private volatile static ApplicationConfigurationCreator instance;

    private ApplicationConfigurationCreator(Project project) {
        super(project);
    }

    public static ApplicationConfigurationCreator getInstance(Project project) {
        if (instance != null) {
            return instance;
        }
        synchronized (new Object()) {
            if (instance != null) {
                return instance;
            }

            instance = new ApplicationConfigurationCreator(project);
            return instance;
        }
    }

    @Override
    public JavaFile createClass(String basePackage, JavaFile... deps) throws IOException {
        TypeSpec applicationConfigurationSpec = TypeSpec.classBuilder("ApplicationConfiguration")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(AnnotationSpec.builder(Configuration.class).build())
            .build();

        String destPackage = basePackage + ".application.configuration";

        JavaFile javaFile = JavaFile.builder(destPackage, applicationConfigurationSpec).build();
        javaFile.writeToPath(Path.of(
            projectPath,
            "application",
            "src", "main", "java"
        ));
        return javaFile;
    }
}
