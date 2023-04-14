package com.gitlab.josercl;

import com.gitlab.josercl.tasks.GeneratorTask;
import com.gitlab.josercl.tasks.InitProjectTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class GeneratorPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getTasks().register("generate", GeneratorTask.class);
        project.getTasks().register("initProject", InitProjectTask.class);
    }
}
