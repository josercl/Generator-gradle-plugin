package com.gitlab.josercl.init;

import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.List;

public interface ClassCreator {
    JavaFile createClass(String basePackage, JavaFile... deps) throws IOException;

    default JavaFile createClass(String basePackage) throws IOException {
        return createClass(basePackage, new JavaFile[]{});
    }
}
