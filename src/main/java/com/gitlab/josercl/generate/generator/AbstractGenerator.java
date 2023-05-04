package com.gitlab.josercl.generate.generator;

import com.gitlab.josercl.generate.Constants;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.text.CaseUtils;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractGenerator implements IGenerator {

    abstract protected Path getModulePath();

    protected JavaFile generateFile(String basePackage, String subPackage, Supplier<TypeSpec> typeSpecSupplier) throws IOException {
        String destPackage = getPackage(basePackage, subPackage);
        createDirectories(destPackage);
        TypeSpec typeSpec = typeSpecSupplier.get();
        JavaFile javaFile = JavaFile.builder(destPackage, typeSpec).build();
        writeFile(javaFile);
        return javaFile;
    }

    protected void writeFile(JavaFile file) throws IOException {
        file.writeToPath(getModulePath());
    }

    protected void createDirectories(String pkg) throws IOException {
        Path first = Path.of(
            System.getProperty("user.dir"),
            getModulePath().toString(),
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
