package com.gitlab.josercl.init.creator.impl;

import com.gitlab.josercl.init.creator.ClassCreator;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.gradle.api.Project;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;

public class MainApplicationCreator extends ClassCreator {
    private volatile static MainApplicationCreator instance;

    public MainApplicationCreator(Project project) {
        super(project);
    }

    public static MainApplicationCreator getInstance(Project project) {
        if (instance != null) { return instance; }
        synchronized (new Object()) {
            if (instance != null) { return instance; }

            instance = new MainApplicationCreator(project);
            return instance;
        }
    }

    @Override
    public JavaFile createClass(String basePackage, JavaFile ...deps) throws IOException {
        String mainApplicationName = "MainApplication";

        TypeSpec mainApplicationSpec = TypeSpec.classBuilder(mainApplicationName)
            .addModifiers(Modifier.PUBLIC)
            .addMethod(
                MethodSpec.methodBuilder("main")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(String[].class, "args")
                    .addStatement(
                        "$T.run($T.class, args)",
                        ClassName.get(SpringApplication.class),
                        ClassName.get("", mainApplicationName)
                    )
                    .build()
            )
            .addAnnotation(SpringBootApplication.class)
            .build();

        JavaFile javaFile = JavaFile.builder(basePackage, mainApplicationSpec).build();
        javaFile.writeToPath(Path.of(
                projectPath,
                "boot",
                "src", "main", "java"
            ));
        return javaFile;
    }
}
