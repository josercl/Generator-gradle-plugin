package com.gitlab.josercl.init;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.springframework.context.annotation.Configuration;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;

public class PersistenceConfigurationCreator implements ClassCreator {
    private volatile static PersistenceConfigurationCreator instance;

    private PersistenceConfigurationCreator() {}

    public static PersistenceConfigurationCreator getInstance() {
        if (instance != null) { return instance; }
        synchronized (new Object()) {
            if (instance != null) { return instance; }

            instance = new PersistenceConfigurationCreator();
            return instance;
        }
    }

    @Override
    public JavaFile createClass(String basePackage, JavaFile ...deps) throws IOException {
        TypeSpec persistenceConfigurationSpec = TypeSpec.classBuilder("PersistenceConfiguration")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(
                AnnotationSpec.builder(Configuration.class).build()
            ).build();

        String destPackage = basePackage + ".persistence.config";

        JavaFile javaFile = JavaFile.builder(destPackage, persistenceConfigurationSpec).build();
        javaFile.writeToPath(Path.of(
                System.getProperty("user.dir"),
                "infrastructure",
                "src", "main", "java"
            ));
        return javaFile;
    }
}
