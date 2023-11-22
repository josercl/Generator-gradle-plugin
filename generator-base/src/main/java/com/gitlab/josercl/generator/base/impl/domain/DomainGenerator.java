package com.gitlab.josercl.generator.base.impl.domain;

import com.gitlab.josercl.generator.base.Constants;
import com.gitlab.josercl.generator.base.AbstractGenerator;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.CaseUtils;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class DomainGenerator extends AbstractGenerator {

    private final Path domainPath = Path.of("domain/src/main/java");

    public DomainGenerator(String projectPath) {
        super(projectPath);
    }

    @Override
    protected Path getModulePath() {
        return domainPath;
    }

    @Override
    public void generate(String entityName, String basePackage) throws IOException {
        generateFile(
            basePackage,
            Constants.Domain.MODEL_PACKAGE,
            () -> getEntitySpec(entityName, List.of(Data.class), List.of())
        );

        JavaFile portFile = generateFile(basePackage, Constants.Domain.SPI_PACKAGE, () -> getPortSpec(entityName));

        JavaFile serviceFile = generateFile(basePackage, Constants.Domain.API_PACKAGE, () -> getServiceSpec(entityName));

        generateFile(basePackage, Constants.Domain.API_IMPL_PACKAGE, () -> getServiceImplSpec(serviceFile, portFile));

        generateFile(basePackage, Constants.Domain.EXCEPTION_PACKAGE, () -> getExceptionSpec(basePackage, entityName));
    }

    private TypeSpec getPortSpec(String entityName) {
        return TypeSpec.interfaceBuilder(portName(entityName))
            .addModifiers(Modifier.PUBLIC)
            .build();
    }

    private TypeSpec getServiceSpec(String entityName) {
        return TypeSpec.interfaceBuilder(String.format("%s%s", CaseUtils.toCamelCase(entityName, true), Constants.Domain.SERVICE_SUFFIX))
            .addModifiers(Modifier.PUBLIC)
            .build();
    }

    private TypeSpec getServiceImplSpec(JavaFile serviceFile, JavaFile portFile) {
        return TypeSpec.classBuilder(String.format("%sImpl", serviceFile.typeSpec.name))
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(RequiredArgsConstructor.class)
            .addSuperinterface(ClassName.get(serviceFile.packageName, serviceFile.typeSpec.name))
            .addField(
                FieldSpec.builder(
                    ClassName.get(portFile.packageName, portFile.typeSpec.name),
                    CaseUtils.toCamelCase(portFile.typeSpec.name, false),
                    Modifier.PRIVATE, Modifier.FINAL
                ).build()
            )
            .build();
    }

    private TypeSpec getExceptionSpec(String basePackage, String entityName) {

        ParameterSpec idParameterSpec = ParameterSpec.builder(
            Long.class,
            CaseUtils.toCamelCase(String.format("%s %s", entityName, "id"), false)
        ).build();

        return TypeSpec.classBuilder(
                CaseUtils.toCamelCase(
                    String.format("%s not found exception", entityName),
                    true
                )
            )
            .addModifiers(Modifier.PUBLIC)
            .superclass(ClassName.get(getPackage(basePackage, Constants.Domain.EXCEPTION_PACKAGE), "RecordNotFoundException"))
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(idParameterSpec)
                    .addStatement("super($S + $L)", entityName + " not found: ", idParameterSpec.name)
                    .build()
            )
            .build();
    }
}
