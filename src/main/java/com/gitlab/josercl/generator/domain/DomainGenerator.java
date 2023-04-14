package com.gitlab.josercl.generator.domain;

import com.gitlab.josercl.generator.Constants;
import com.gitlab.josercl.generator.IGenerator;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.text.CaseUtils;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class DomainGenerator extends IGenerator {

    private final Path domainPath = Path.of("domain/src/main/java");

    @Override
    public void generate(String entityName, String basePackage) throws IOException {
        String modelPackage = getPackage(basePackage, Constants.Domain.MODEL_PACKAGE);
        createDirectories(modelPackage, domainPath);
        TypeSpec entityDomainSpec = getEntitySpec(entityName, List.of(), List.of());
        JavaFile domainModelFile = JavaFile.builder(modelPackage, entityDomainSpec).build();
        domainModelFile.writeToPath(domainPath);

        String portPackage = getPackage(basePackage, Constants.Domain.SPI_PACKAGE);
        createDirectories(portPackage, domainPath);
        TypeSpec portSpec = getPortSpec(entityName);
        JavaFile portFile = JavaFile.builder(portPackage, portSpec).build();
        portFile.writeToPath(domainPath);

        String servicePackage = getPackage(basePackage, Constants.Domain.API_PACKAGE);
        createDirectories(servicePackage, domainPath);
        TypeSpec serviceSpec = getServiceSpec(entityName);
        JavaFile serviceFile = JavaFile.builder(servicePackage, serviceSpec).build();
        serviceFile.writeToPath(domainPath);

        String serviceImplPackage = getPackage(basePackage, Constants.Domain.API_IMPL_PACKAGE);
        createDirectories(serviceImplPackage, domainPath);
        TypeSpec serviceImplSpec = getServiceImplSpec(serviceFile);
        JavaFile serviceImplFile = JavaFile.builder(serviceImplPackage, serviceImplSpec).build();
        serviceImplFile.writeToPath(domainPath);

        String exceptionPackage = getPackage(basePackage, Constants.Domain.EXCEPTION_PACKAGE);
        createDirectories(exceptionPackage, domainPath);
        TypeSpec exceptionSpec = getExceptionSpec(entityName);
        JavaFile.builder(exceptionPackage, exceptionSpec).build().writeToPath(domainPath);
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

    private TypeSpec getServiceImplSpec(JavaFile serviceFile) {
        return TypeSpec.classBuilder(CaseUtils.toCamelCase(String.format("%s %s", serviceFile.typeSpec.name, "Impl"), true))
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(ClassName.get(serviceFile.packageName, serviceFile.typeSpec.name))
            .build();
    }

    private TypeSpec getExceptionSpec(String entityName) {

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
            .superclass(ClassName.get(Constants.Common.EXCEPTION_PACKAGE, "RecordNotFoundException"))
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
