package com.gitlab.josercl.init.creator.impl;

import com.gitlab.josercl.init.creator.ClassCreator;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.gradle.api.Project;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ValidationErrorCreator extends ClassCreator {
    private volatile static ValidationErrorCreator instance;

    private ValidationErrorCreator(Project project) {
        super(project);
    }

    public static ValidationErrorCreator getInstance(Project project) {
        if (instance != null) { return instance; }
        synchronized (new Object()) {
            if (instance != null) { return instance; }

            instance = new ValidationErrorCreator(project);
            return instance;
        }
    }

    @Override
    public JavaFile createClass(String basePackage, JavaFile... deps) throws IOException {
        TypeSpec typeSpec = TypeSpec.recordBuilder("ValidationError")
            .addModifiers(Modifier.PUBLIC)
            .addRecordComponent(ParameterSpec.builder(String.class, "field").build())
            .addRecordComponent(
                ParameterSpec.builder(
                    ParameterizedTypeName.get(ClassName.get(List.class), TypeName.get(String.class)),
                    "errors"
                ).build()
            )
            .build();

        String destPackage = basePackage + ".domain.error";

        JavaFile validationErrorFile = JavaFile.builder(destPackage, typeSpec).build();
        validationErrorFile.writeToPath(Path.of(
            projectPath,
            "domain",
            "src", "main", "java"
        ));
        return validationErrorFile;
    }
}
