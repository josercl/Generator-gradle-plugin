package com.gitlab.josercl.generator;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.text.CaseUtils;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public abstract class IGenerator {
    abstract public void generate(String entityName, String basePackage) throws IOException;

    protected void createDirectories(String pkg, Path modulePath) throws IOException {
        Path first = Path.of(
            System.getProperty("user.dir"),
            modulePath.toString(),
            pkg.replace('.', File.separatorChar)
        );
        Files.createDirectories(first);
    }

    protected String getPackage(String basePackage, String pkg) {
        if (basePackage == null) {
            return pkg;
        }

        return String.format("%s.%s", basePackage, pkg);
    }

    protected TypeSpec getEntitySpec(String entityName, List<Class<?>> annotations, List<FieldSpec> idFieldSpecs) {
        TypeSpec.Builder builder = TypeSpec
            .classBuilder(CaseUtils.toCamelCase(entityName, true))
            .addModifiers(Modifier.PUBLIC)
            .addFields(idFieldSpecs);
        annotations.forEach(builder::addAnnotation);
        return builder.build();
    }

    protected String portName(String name) {
        return String.format("%s%s", CaseUtils.toCamelCase(name, true), Constants.Domain.PORT_SUFFIX);
    }
}
