package com.gitlab.josercl.init.creator.impl;

import com.gitlab.josercl.init.creator.ClassCreator;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.gradle.api.Project;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ErrorHandlerCreator extends ClassCreator {
    private volatile static ErrorHandlerCreator instance;

    private ErrorHandlerCreator(Project project) {
        super(project);
    }

    public static ErrorHandlerCreator getInstance(Project project) {
        if (instance != null) { return instance; }
        synchronized (new Object()) {
            if (instance != null) { return instance; }

            instance = new ErrorHandlerCreator(project);
            return instance;
        }
    }
    @Override
    public JavaFile createClass(String basePackage, JavaFile ...deps) throws IOException {
        TypeSpec errorHandlerSpec = TypeSpec.classBuilder("ErrorHandler")
            .superclass(ResponseEntityExceptionHandler.class)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(ControllerAdvice.class)
            .addMethod(getHandleRecordNotFoundException(basePackage))
            .addMethod(getHandleMethodArgumentNotValid(basePackage))
            .build();

        String destPackage = basePackage + ".application";

        JavaFile javaFile = JavaFile.builder(destPackage, errorHandlerSpec).build();
        javaFile.writeToPath(Path.of(
                projectPath,
                "application",
                "src", "main", "java"
            ));
        return javaFile;
    }

    private MethodSpec getHandleRecordNotFoundException(String basePackage) {
        ClassName responseException = ClassName.get(basePackage + ".domain.exception", "RecordNotFoundException");
        ClassName errorDTO = ClassName.get(basePackage + ".rest.server.model", "ErrorDTO");
        ParameterSpec exArgument = ParameterSpec.builder(responseException, "ex").build();
        ClassName responseEntity = ClassName.get(ResponseEntity.class);

        return MethodSpec.methodBuilder("handleRecordNotFoundException")
            .addAnnotation(
                AnnotationSpec.builder(ExceptionHandler.class)
                    .addMember("value", "{$T.class}", responseException)
                    .build()
            )
            .addModifiers(Modifier.PROTECTED)
            .returns(ParameterizedTypeName.get(responseEntity, errorDTO))
            .addParameter(exArgument)
            .addStatement("""
                return $T.status($T.NOT_FOUND.value())
                .body(
                    new $T()
                        .code($T.NOT_FOUND.value())
                        .message($N.getMessage())
                )""", ResponseEntity.class, HttpStatus.class, errorDTO, HttpStatus.class, exArgument)
            .build();
    }

    private MethodSpec getHandleMethodArgumentNotValid(String basePackage) {
        Method baseMethod = Arrays.stream(ResponseEntityExceptionHandler.class.getDeclaredMethods())
            .filter(method -> method.getName().equalsIgnoreCase("handleMethodArgumentNotValid"))
            .findFirst()
            .orElseThrow();

        MethodSpec.Builder builder = MethodSpec.methodBuilder(baseMethod.getName());
        builder.addAnnotation(Override.class);
        builder.varargs(baseMethod.isVarArgs());

        int modifiers = baseMethod.getModifiers();
        Set<Modifier> modifierSet = new HashSet<>();

        if (java.lang.reflect.Modifier.isPublic(modifiers)) {
            modifierSet.add(Modifier.PUBLIC);
        }
        if (java.lang.reflect.Modifier.isProtected(modifiers)) {
            modifierSet.add(Modifier.PROTECTED);
        }
        if (java.lang.reflect.Modifier.isPrivate(modifiers)) {
            modifierSet.add(Modifier.PRIVATE);
        }
        if (java.lang.reflect.Modifier.isAbstract(modifiers)) {
            modifierSet.add(Modifier.ABSTRACT);
        }
        if (java.lang.reflect.Modifier.isStatic(modifiers)) {
            modifierSet.add(Modifier.STATIC);
        }
        if (java.lang.reflect.Modifier.isFinal(modifiers)) {
            modifierSet.add(Modifier.FINAL);
        }
        if (java.lang.reflect.Modifier.isTransient(modifiers)) {
            modifierSet.add(Modifier.TRANSIENT);
        }
        if (java.lang.reflect.Modifier.isVolatile(modifiers)) {
            modifierSet.add(Modifier.VOLATILE);
        }
        if (java.lang.reflect.Modifier.isSynchronized(modifiers)) {
            modifierSet.add(Modifier.SYNCHRONIZED);
        }
        if (java.lang.reflect.Modifier.isNative(modifiers)) {
            modifierSet.add(Modifier.NATIVE);
        }
        if (java.lang.reflect.Modifier.isStrict(modifiers)) {
            modifierSet.add(Modifier.STRICTFP);
        }

        builder.addModifiers(modifierSet);

        builder.returns(baseMethod.getGenericReturnType());

        ParameterSpec exParameter = null;
        for (Parameter parameter : baseMethod.getParameters()) {
            ParameterSpec parameterSpec = ParameterSpec.builder(
                TypeName.get(parameter.getParameterizedType()),
                parameter.getName()
            ).build();

            if (BindingResult.class.isAssignableFrom(parameter.getType())) {
                exParameter = parameterSpec;
            }

            builder.addParameter(parameterSpec);
        }

        for (Type thrownType : baseMethod.getExceptionTypes()) {
            builder.addException(thrownType);
        }

        builder.addStatement("""
                    $T<$T> errorList = $N.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .collect($T.groupingBy(
                            $T::getField,
                            Collectors.mapping($T::getDefaultMessage, Collectors.toList())
                        ))
                        .entrySet()
                        .stream()
                        .map(entry -> new ValidationError(entry.getKey(), entry.getValue()))
                        .toList()""",
                List.class,
                ClassName.get(basePackage + ".domain.error", "ValidationError"),
                exParameter,
                Collectors.class,
                ClassName.get("org.springframework.validation", "FieldError"),
                ClassName.get("org.springframework.context.support", "DefaultMessageSourceResolvable")
            )
            .addStatement("return $T.unprocessableEntity().body(errorList)", baseMethod.getReturnType());

        return builder.build();
    }
}
