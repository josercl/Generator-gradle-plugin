package com.gitlab.josercl.generator.infrastructure;

import com.gitlab.josercl.generator.Constants;
import com.gitlab.josercl.generator.IGenerator;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import org.apache.commons.text.CaseUtils;
import org.mapstruct.Mapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class InfraGenerator extends IGenerator {

    private final Path infrastructurePath = Path.of("infrastructure/src/main/java");

    @Override
    public void generate(String entityName, String basePackage) throws IOException {
        FieldSpec idFieldSpec = FieldSpec.builder(Long.class, "id", Modifier.PRIVATE)
            .addAnnotation(Id.class)
            .addAnnotation(
                AnnotationSpec.builder(GeneratedValue.class)
                    .addMember("strategy", "$L", "jakarta.persistence.GenerationType.IDENTITY")
                    .build()
            )
            .build();

        TypeSpec entitySpec = getEntitySpec(
            String.format("%s %s", entityName, Constants.Infrastructure.MODEL_SUFFIX),
            List.of(Data.class, Entity.class),
            List.of(idFieldSpec)
        );

        String entityPackage = getPackage(basePackage, Constants.Infrastructure.ENTITY_PACKAGE);
        createDirectories(entityPackage, infrastructurePath);
        JavaFile entityFile = JavaFile.builder(entityPackage, entitySpec).build();
        entityFile.writeToPath(infrastructurePath);

        String repositoryPackage = getPackage(basePackage, Constants.Infrastructure.REPOSITORY_PACKAGE);
        createDirectories(repositoryPackage, infrastructurePath);
        TypeSpec repositorySpec = getRepositorySpec(entityFile, idFieldSpec);
        JavaFile repositoryFile = JavaFile.builder(repositoryPackage, repositorySpec).build();
        repositoryFile.writeToPath(infrastructurePath);

        String mapperPackage = getPackage(basePackage, Constants.Infrastructure.MAPPER_PACKAGE);
        createDirectories(mapperPackage, infrastructurePath);
        TypeSpec mapperSpec = getMapperSpec(entityFile, basePackage);
        JavaFile mapperFile = JavaFile.builder(mapperPackage, mapperSpec).build();
        mapperFile.writeToPath(infrastructurePath);

        String adapterPackage = getPackage(basePackage, Constants.Infrastructure.ADAPTER_PACKAGE);
        createDirectories(adapterPackage, infrastructurePath);
        TypeSpec adapterSpec = getAdapterSpec(entityFile, repositoryFile, mapperFile, basePackage);
        JavaFile adapterFile = JavaFile.builder(adapterPackage, adapterSpec).build();
        adapterFile.writeToPath(infrastructurePath);
    }

    private TypeSpec getRepositorySpec(JavaFile entityFile, FieldSpec idFieldSpec) {
        ClassName entityType = ClassName.get(entityFile.packageName, entityFile.typeSpec.name);

        return TypeSpec
            .interfaceBuilder(String.format("%s%s", entityFile.typeSpec.name, Constants.Infrastructure.REPOSITORY_SUFFIX))
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Repository.class)
            .addSuperinterface(
                ParameterizedTypeName.get(ClassName.get(CrudRepository.class), entityType, idFieldSpec.type)
            )
            .addSuperinterface(
                ParameterizedTypeName.get(ClassName.get(JpaRepository.class), entityType, idFieldSpec.type)
            )
            .build();
    }

    private TypeSpec getMapperSpec(JavaFile entityFile, String basePackage) {
        return TypeSpec.interfaceBuilder(String.format(
                "%s%s",
                entityFile.typeSpec.name,
                Constants.MAPPER_SUFFIX)
            )
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(
                AnnotationSpec.builder(Mapper.class)
                    .addMember("injectionStrategy", "$L", "org.mapstruct.InjectionStrategy.CONSTRUCTOR")
                    .addMember("componentModel", "$L", "org.mapstruct.MappingConstants.ComponentModel.SPRING")
                    .build()
            )
            .addMethods(List.of(
                MethodSpec.methodBuilder("toDomain")
                    .returns(
                        ClassName.get(basePackage + "." + Constants.Domain.MODEL_PACKAGE, entityFile.typeSpec.name.replace(Constants.Infrastructure.MODEL_SUFFIX, ""))
                    )
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addParameter(
                        ParameterSpec.builder(
                            ClassName.get(entityFile.packageName, entityFile.typeSpec.name),
                            CaseUtils.toCamelCase(entityFile.typeSpec.name, false)
                        ).build()
                    )
                    .build()
            ))
            .build();
    }

    private TypeSpec getAdapterSpec(JavaFile entityFile, JavaFile repositoryFile, JavaFile mapperFile, String basePackage) {
        ClassName repositoryType = ClassName.get(repositoryFile.packageName, repositoryFile.typeSpec.name);
        ClassName mapperType = ClassName.get(mapperFile.packageName, mapperFile.typeSpec.name);
        ClassName portType = ClassName.get(
            basePackage + "." + Constants.Domain.SPI_PACKAGE,
            portName(entityFile.typeSpec.name.replace(Constants.Infrastructure.MODEL_SUFFIX, ""))
        );

        FieldSpec repositoryField = FieldSpec.builder(repositoryType, "repository")
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .build();

        FieldSpec mapperField = FieldSpec.builder(mapperType, "mapper")
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .build();

        ParameterSpec repositoryParameter = ParameterSpec.builder(repositoryType, "repository").build();
        ParameterSpec mapperParameter = ParameterSpec.builder(mapperType, "mapper").build();

        MethodSpec constructorSpec = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(repositoryParameter)
            .addStatement("this.$N = $N", repositoryField, repositoryParameter)
            .addParameter(mapperParameter)
            .addStatement("this.$N = $N", mapperField, mapperParameter)
            .build();

        return TypeSpec.classBuilder(
                String.format(
                    "%s%s",
                    entityFile.typeSpec.name.replace(Constants.Infrastructure.MODEL_SUFFIX, ""),
                    Constants.Infrastructure.ADAPTER_SUFFIX
                )
            )
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(portType)
            .addField(repositoryField)
            .addField(mapperField)
            .addMethod(constructorSpec)
            .build();
    }
}
