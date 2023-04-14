package com.gitlab.josercl.generator.application;

import com.gitlab.josercl.generator.Constants;
import com.gitlab.josercl.generator.IGenerator;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.text.CaseUtils;
import org.mapstruct.Mapper;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;

public class ApplicationGenerator extends IGenerator {

    private final Path applicationPath = Path.of("application/src/main/java");

    @Override
    public void generate(String entityName, String basePackage) throws IOException {
        String mapperPackage = getPackage(basePackage, Constants.Application.MAPPER_PACKAGE);
        createDirectories(mapperPackage, applicationPath);
        TypeSpec mapperSpec = getMapperSpec(entityName);
        JavaFile mapperFile = JavaFile.builder(mapperPackage, mapperSpec).build();
        mapperFile.writeToPath(applicationPath);

        String controllerPackage = getPackage(basePackage, Constants.Application.CONTROLLER_PACKAGE);
        TypeSpec controllerSpec = getControllerSpec(entityName, mapperFile, basePackage);
        JavaFile controllerFile = JavaFile.builder(controllerPackage, controllerSpec).build();
        controllerFile.writeToPath(applicationPath);
    }

    private TypeSpec getControllerSpec(String entityName, JavaFile mapperFile, String basePackage) {
        ClassName serviceType = ClassName.get(
            getPackage(basePackage, Constants.Domain.API_PACKAGE),
            String.format("%s%s", CaseUtils.toCamelCase(entityName, true), Constants.Domain.SERVICE_SUFFIX)
        );
        ClassName mapperType = ClassName.get(
            mapperFile.packageName,
            String.format("%s%s", CaseUtils.toCamelCase(entityName, true), Constants.MAPPER_SUFFIX)
        );

        FieldSpec serviceField = FieldSpec.builder(serviceType, "service")
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .build();
        FieldSpec mapperField = FieldSpec.builder(mapperType, "mapper")
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .build();

        ParameterSpec serviceParam = ParameterSpec.builder(serviceType, "service").build();
        ParameterSpec mapperParam = ParameterSpec.builder(mapperType, "mapper").build();

        return TypeSpec.classBuilder(
                CaseUtils.toCamelCase(
                    String.format("%s %s", entityName, Constants.Application.CONTROLLER_SUFFIX),
                    true
                )
            )
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(RestController.class)
            .addField(serviceField)
            .addField(mapperField)
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addParameter(serviceParam)
                    .addParameter(mapperParam)
                    .addStatement("this.$N = $N", serviceField, serviceParam)
                    .addStatement("this.$N = $N", mapperField, mapperParam)
                    .build()
            )
            .build();
    }

    private TypeSpec getMapperSpec(String entityName) {
        return TypeSpec.interfaceBuilder(
                CaseUtils.toCamelCase(
                    String.format("%s %s", entityName, Constants.MAPPER_SUFFIX),
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
