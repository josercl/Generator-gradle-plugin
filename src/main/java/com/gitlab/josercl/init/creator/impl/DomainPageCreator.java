package com.gitlab.josercl.init.creator.impl;

import com.gitlab.josercl.init.creator.ClassCreator;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeSpec;
import com.palantir.javapoet.TypeVariableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.gradle.api.Project;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class DomainPageCreator extends ClassCreator {
    private volatile static DomainPageCreator instance;

    private DomainPageCreator(Project project) {
        super(project);
    }

    public static DomainPageCreator getInstance(Project project) {
        if (instance != null) { return instance; }
        synchronized (new Object()) {
            if (instance != null) { return instance; }

            instance = new DomainPageCreator(project);
            return instance;
        }
    }

    @Override
    public JavaFile createClass(String basePackage, JavaFile... deps) throws IOException {
        TypeVariableName typeVariable = TypeVariableName.get("T");
        TypeSpec domainPageSpec = TypeSpec.classBuilder("DomainPage")
            .addTypeVariable(typeVariable)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Data.class)
            .addAnnotation(Builder.class)
            .addAnnotation(AllArgsConstructor.class)
            .addField(ParameterizedTypeName.get(ClassName.get(List.class), typeVariable), "content", Modifier.PRIVATE)
            .addField(Integer.class, "page", Modifier.PRIVATE)
            .addField(Integer.class, "pageSize", Modifier.PRIVATE)
            .addField(Long.class, "totalElements", Modifier.PRIVATE)
            .addField(Integer.class, "totalPages", Modifier.PRIVATE)
            .build();

        String destPackage = basePackage + ".domain.model";

        JavaFile domainPageFile = JavaFile.builder(destPackage, domainPageSpec).build();
        domainPageFile.writeToPath(Path.of(
            projectPath,
            "domain",
            "src", "main", "java"
        ));
        return domainPageFile;
    }
}
