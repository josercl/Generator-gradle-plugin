package com.gitlab.josercl.init;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class DomainPageCreator implements ClassCreator {
    private volatile static DomainPageCreator instance;

    private DomainPageCreator() {}

    public static DomainPageCreator getInstance() {
        if (instance != null) { return instance; }
        synchronized (new Object()) {
            if (instance != null) { return instance; }

            instance = new DomainPageCreator();
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
            System.getProperty("user.dir"),
            "domain",
            "src", "main", "java"
        ));
        return domainPageFile;
    }
}
