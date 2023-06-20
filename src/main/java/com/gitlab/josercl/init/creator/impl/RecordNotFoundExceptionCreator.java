package com.gitlab.josercl.init.creator.impl;

import com.gitlab.josercl.init.creator.ClassCreator;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import org.gradle.api.Project;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;

public class RecordNotFoundExceptionCreator extends ClassCreator {
    private volatile static RecordNotFoundExceptionCreator instance;

    private RecordNotFoundExceptionCreator(Project project) {
        super(project);
    }

    public static RecordNotFoundExceptionCreator getInstance(Project project) {
        if (instance != null) { return instance; }
        synchronized (new Object()) {
            if (instance != null) { return instance; }

            instance = new RecordNotFoundExceptionCreator(project);
            return instance;
        }
    }

    @Override
    public JavaFile createClass(String basePackage, JavaFile... deps) throws IOException {
        ParameterSpec messageParam = ParameterSpec.builder(String.class, "message", Modifier.FINAL).build();

        TypeSpec typeSpec = TypeSpec.classBuilder("RecordNotFoundException")
            .addModifiers(Modifier.PUBLIC)
            .superclass(RuntimeException.class)
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addParameter(messageParam)
                    .addStatement("super($N)", messageParam)
                    .build()
            )
            .build();

        String destPackage = basePackage + ".domain.exception";

        JavaFile recordNotFoundExceptionFile = JavaFile.builder(destPackage, typeSpec).build();
        recordNotFoundExceptionFile.writeToPath(Path.of(
            projectPath,
            "domain",
            "src", "main", "java"
        ));
        return recordNotFoundExceptionFile;
    }
}
