package com.gitlab.josercl.init.creator.impl;

import com.gitlab.josercl.init.creator.ClassCreator;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.springframework.context.annotation.Configuration;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;

public class ApplicationConfigurationCreator implements ClassCreator {
    private volatile static ApplicationConfigurationCreator instance;

    private ApplicationConfigurationCreator() {
    }

    public static ApplicationConfigurationCreator getInstance() {
        if (instance != null) {
            return instance;
        }
        synchronized (new Object()) {
            if (instance != null) {
                return instance;
            }

            instance = new ApplicationConfigurationCreator();
            return instance;
        }
    }

    @Override
    public JavaFile createClass(String basePackage, JavaFile ...deps) throws IOException {
        TypeSpec applicationConfigurationSpec = TypeSpec.classBuilder("ApplicationConfiguration")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(
                AnnotationSpec.builder(Configuration.class).build()
            ).build();

        String destPackage = basePackage + ".application.configuration";

        JavaFile javaFile = JavaFile.builder(destPackage, applicationConfigurationSpec).build();
        javaFile.writeToPath(Path.of(
            System.getProperty("user.dir"),
            "application",
            "src", "main", "java"
        ));
        return javaFile;
    }
}
