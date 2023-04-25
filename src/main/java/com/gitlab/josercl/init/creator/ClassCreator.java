package com.gitlab.josercl.init.creator;

import com.squareup.javapoet.JavaFile;

import java.io.IOException;

public interface ClassCreator {
    JavaFile createClass(String basePackage, JavaFile... deps) throws IOException;

    default JavaFile createClass(String basePackage) throws IOException {
        return createClass(basePackage, new JavaFile[]{});
    }
}
