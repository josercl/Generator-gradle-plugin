package com.gitlab.josercl.init.creator.impl;

import com.gitlab.josercl.init.creator.ClassCreator;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;

public class MainApplicationCreator implements ClassCreator {
    private volatile static MainApplicationCreator instance;

    private MainApplicationCreator() {}

    public static MainApplicationCreator getInstance() {
        if (instance != null) { return instance; }
        synchronized (new Object()) {
            if (instance != null) { return instance; }

            instance = new MainApplicationCreator();
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
                System.getProperty("user.dir"),
                "boot",
                "src", "main", "java"
            ));
        return javaFile;
    }
}
