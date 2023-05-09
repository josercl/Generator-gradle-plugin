package com.gitlab.josercl.init.creator.impl;

import com.gitlab.josercl.init.creator.ClassCreator;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.gradle.api.Project;
import org.springframework.context.annotation.Configuration;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;

public class PersistenceConfigurationCreator extends ClassCreator {
    private volatile static PersistenceConfigurationCreator instance;

    public PersistenceConfigurationCreator(Project project) {
        super(project);
    }

    public static PersistenceConfigurationCreator getInstance(Project project) {
        if (instance != null) { return instance; }
        synchronized (new Object()) {
            if (instance != null) { return instance; }

            instance = new PersistenceConfigurationCreator(project);
            return instance;
        }
    }

    @Override
    public JavaFile createClass(String basePackage, JavaFile ...deps) throws IOException {
        TypeSpec persistenceConfigurationSpec = TypeSpec.classBuilder("PersistenceConfiguration")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(AnnotationSpec.builder(Configuration.class).build())
            .build();

        String destPackage = basePackage + ".persistence.config";

        JavaFile javaFile = JavaFile.builder(destPackage, persistenceConfigurationSpec).build();
        javaFile.writeToPath(Path.of(
                projectPath,
                "infrastructure",
                "src", "main", "java"
            ));
        return javaFile;
    }
}
