package com.gitlab.josercl.generate.task;

import com.gitlab.josercl.generate.generator.IGenerator;
import com.gitlab.josercl.generate.generator.impl.application.ApplicationGenerator;
import com.gitlab.josercl.generate.generator.impl.domain.DomainGenerator;
import com.gitlab.josercl.generate.generator.impl.infrastructure.InfraGenerator;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GeneratorTask extends DefaultTask {
    public GeneratorTask() {
    }

    @TaskAction
    public void generate() throws IOException {
        Project project = getProject();
        Map<String, ?> projectProperties = project.getProperties();

        Object entities = projectProperties.getOrDefault("entities", null);

        if (entities == null) return;

        Object only = projectProperties.getOrDefault("only", null);
        String basePackage = Optional.ofNullable(projectProperties.getOrDefault("basePackage", null))
            .map(String.class::cast)
            .orElse((String) project.getGroup());

        if (basePackage.equalsIgnoreCase("$PKG")) {
            throw new RuntimeException("Init project first, execute: gradlew initProject");
        }

        List<IGenerator> generatorsToUse = new ArrayList<>();

        if (only == null) {
            generatorsToUse.add(new DomainGenerator(project));
            generatorsToUse.add(new InfraGenerator(project));
            generatorsToUse.add(new ApplicationGenerator(project));
        } else {
            String[] onlies = ((String) only).split(",");

            for (String s : onlies) {
                switch (s) {
                    case "domain" -> generatorsToUse.add(new DomainGenerator(project));
                    case "infra", "infrastructure" -> generatorsToUse.add(new InfraGenerator(project));
                    case "application", "app" -> generatorsToUse.add(new ApplicationGenerator(project));
                }
            }
        }

        for (String entity : ((String) entities).split(",")) {
            for (IGenerator iGenerator : generatorsToUse) {
                iGenerator.generate(entity, basePackage);
            }
        }
    }
}