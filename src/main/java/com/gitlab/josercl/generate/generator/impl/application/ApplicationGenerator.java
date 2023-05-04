package com.gitlab.josercl.generate.generator.impl.application;

import com.gitlab.josercl.generate.Constants;
import com.gitlab.josercl.generate.generator.AbstractGenerator;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.CaseUtils;
import org.mapstruct.Mapper;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;

public class ApplicationGenerator extends AbstractGenerator {

    private final Path applicationPath = Path.of("application/src/main/java");

    @Override
    protected Path getModulePath() {
        return applicationPath;
    }

    @Override
    public void generate(String entityName, String basePackage) throws IOException {
        JavaFile mapperFile = generateFile(
            basePackage,
            Constants.Application.MAPPER_PACKAGE,
            () -> getMapperSpec(entityName)
        );

        generateFile(
            basePackage,
            Constants.Application.CONTROLLER_PACKAGE,
            () -> getControllerSpec(entityName, mapperFile, basePackage)
        );
    }

    private TypeSpec getControllerSpec(String entityName, JavaFile mapperFile, String basePackage) {
        ClassName serviceType = ClassName.get(
            getPackage(basePackage, Constants.Domain.API_PACKAGE),
            String.format("%s%s", CaseUtils.toCamelCase(entityName, true), Constants.Domain.SERVICE_SUFFIX)
        );
        ClassName mapperType = ClassName.get(mapperFile.packageName, mapperFile.typeSpec.name);

        FieldSpec serviceField = FieldSpec.builder(serviceType, "service")
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .build();
        FieldSpec mapperField = FieldSpec.builder(mapperType, "mapper")
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .build();

        return TypeSpec.classBuilder(
                CaseUtils.toCamelCase(
                    String.format("%s %s", entityName, Constants.Application.CONTROLLER_SUFFIX),
                    true
                )
            )
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(RestController.class)
            .addAnnotation(RequiredArgsConstructor.class)
            .addField(serviceField)
            .addField(mapperField)
            .build();
    }

    private TypeSpec getMapperSpec(String entityName) {
        return TypeSpec.interfaceBuilder(
                CaseUtils.toCamelCase(
                    String.format("%s api %s", entityName, Constants.MAPPER_SUFFIX),
                    true
                )
            )
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(
                AnnotationSpec.builder(Mapper.class)
                    .addMember("injectionStrategy", "$L", "org.mapstruct.InjectionStrategy.CONSTRUCTOR")
                    .addMember("componentModel", "$L", "org.mapstruct.MappingConstants.ComponentModel.SPRING")
                    .build()
            )
            .build();
    }
}
