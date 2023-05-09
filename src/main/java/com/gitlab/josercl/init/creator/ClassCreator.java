package com.gitlab.josercl.init.creator;

import com.squareup.javapoet.JavaFile;
import org.gradle.api.Project;

import java.io.IOException;

public abstract class ClassCreator {
    protected final String projectPath;

    public ClassCreator(Project project) {
        this.projectPath = project.getProjectDir().getAbsolutePath();
    }

    public abstract JavaFile createClass(String basePackage, JavaFile... deps) throws IOException;

    protected JavaFile createClass(String basePackage) throws IOException {
        return createClass(basePackage, new JavaFile[]{});
    }
}
