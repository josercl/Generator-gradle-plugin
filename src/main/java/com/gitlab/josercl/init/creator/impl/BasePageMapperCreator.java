package com.gitlab.josercl.init.creator.impl;

import com.gitlab.josercl.init.creator.ClassCreator;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import org.gradle.api.Project;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;

public class BasePageMapperCreator extends ClassCreator {
    private volatile static BasePageMapperCreator instance;

    private BasePageMapperCreator(Project project) {
        super(project);
    }

    public static BasePageMapperCreator getInstance(Project project) {
        if (instance != null) { return instance; }
        synchronized (new Object()) {
            if (instance != null) { return instance; }

            instance = new BasePageMapperCreator(project);
            return instance;
        }
    }
    @Override
    public JavaFile createClass(String basePackage, JavaFile... deps) throws IOException {
        JavaFile domainPageFile = deps[0];
        TypeVariableName t = TypeVariableName.get("T");
        ParameterSpec page = ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Page.class), t), "page").build();
        ClassName domainPage = ClassName.get(domainPageFile.packageName, domainPageFile.typeSpec.name);

        TypeSpec pageMapperSpec = TypeSpec.interfaceBuilder("BasePageMapper")
            .addTypeVariable(t)
            .addModifiers(Modifier.PUBLIC)
            .addMethod(
                MethodSpec.methodBuilder("toDomainPage")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(ParameterizedTypeName.get(domainPage, t))
                    .addParameter(page)
                    .addAnnotation(
                        AnnotationSpec.builder(Mapping.class)
                            .addMember("target", "$S", "pageSize")
                            .addMember("source", "$S", "size")
                            .build()
                    )
                    .addAnnotation(
                        AnnotationSpec.builder(Mapping.class)
                            .addMember("target", "$S", "page")
                            .addMember("source", "$S", "number")
                            .build()
                    )
                    .addAnnotation(
                        AnnotationSpec.builder(Mapping.class)
                            .addMember("target", "$S", "content")
                            .addMember("source", "$S", "content")
                            .addMember("defaultExpression", "$S", "java(java.util.List.of())")
                            .build()
                    )
                    .build()
            )
            .build();

        String destPackage = basePackage + ".persistence.entity.mapper";

        JavaFile mapperFile = JavaFile.builder(destPackage, pageMapperSpec).build();
        mapperFile.writeToPath(Path.of(
            projectPath,
            "infrastructure",
            "src", "main", "java"
        ));
        return mapperFile;
    }
}
