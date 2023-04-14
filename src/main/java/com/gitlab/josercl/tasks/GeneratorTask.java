package com.gitlab.josercl.tasks;

import com.gitlab.josercl.generator.IGenerator;
import com.gitlab.josercl.generator.application.ApplicationGenerator;
import com.gitlab.josercl.generator.domain.DomainGenerator;
import com.gitlab.josercl.generator.infrastructure.InfraGenerator;
import org.gradle.api.DefaultTask;
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
        Map<String, ?> projectProperties = getProject().getProperties();

        Object entities = projectProperties.getOrDefault("entities", null);

        if (entities == null) return;

        Object only = projectProperties.getOrDefault("only", null);
        String basePackage = Optional.ofNullable(projectProperties.getOrDefault("basePackage", null))
            .map(String.class::cast)
            .orElse((String) getProject().getGroup());

        List<IGenerator> generatorsToUse = new ArrayList<>();

        if (only == null) {
            generatorsToUse.add(new DomainGenerator());
            generatorsToUse.add(new InfraGenerator());
            generatorsToUse.add(new ApplicationGenerator());
        } else {
            String[] onlies = ((String) only).split(",");

            for (String s : onlies) {
                switch (s) {
                    case "domain" -> generatorsToUse.add(new DomainGenerator());
                    case "infra", "infrastructure" -> generatorsToUse.add(new InfraGenerator());
                    case "application", "app" -> generatorsToUse.add(new ApplicationGenerator());
                }
            }
        }

        for (String entity : ((String) entities).split(",")) {
            for (IGenerator iGenerator : generatorsToUse) {
                iGenerator.generate(entity, (String) basePackage);
            }
        }
    }
}