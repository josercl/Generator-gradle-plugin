package com.gitlab.josercl.init.creator.impl;

import com.gitlab.josercl.init.creator.ClassCreator;
import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import org.gradle.api.Project;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;
import java.util.List;

public class ErrorResponseStatusCreator extends ClassCreator {
    private volatile static ErrorResponseStatusCreator instance;

    private ErrorResponseStatusCreator(Project project) {
        super(project);
    }

    public static ErrorResponseStatusCreator getInstance(Project project) {
        if (instance != null) { return instance; }
        synchronized (new Object()) {
            if (instance != null) { return instance; }

            instance = new ErrorResponseStatusCreator(project);
            return instance;
        }
    }

    @Override
    public JavaFile createClass(String basePackage, JavaFile... deps) throws IOException {
        AnnotationSpec targetAnnotation = AnnotationSpec.builder(Target.class)
            .addMember("value", "{$T.$L}", ElementType.class, ElementType.TYPE)
            .build();
        AnnotationSpec retentionAnnotation = AnnotationSpec.builder(Retention.class)
            .addMember("value", "$T.$L", RetentionPolicy.class, RetentionPolicy.RUNTIME)
            .build();

        TypeSpec typeSpec = TypeSpec.annotationBuilder("ErrorResponseStatus")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotations(List.of(targetAnnotation, retentionAnnotation))
            .addMethod(
                MethodSpec.methodBuilder("value")
                    .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                    .returns(TypeName.INT)
                    .build()
            )
            .build();

        String destPackage = basePackage + ".domain.exception";

        JavaFile errorResponseStatusFile = JavaFile.builder(destPackage, typeSpec).build();
        errorResponseStatusFile.writeToPath(Path.of(
            projectPath,
            "domain",
            "src", "main", "java"
        ));
        return errorResponseStatusFile;
    }
}
