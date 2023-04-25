package com.gitlab.josercl;

import com.gitlab.josercl.generate.task.GeneratorTask;
import com.gitlab.josercl.init.task.InitProjectTask;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;

public class GeneratorPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        TaskContainer projectTasks = project.getTasks();

        Action<? super DefaultTask> taskAction = task -> task.setGroup("code");

        projectTasks.register("generate", GeneratorTask.class).configure(taskAction);
        projectTasks.register("generateCrud", GeneratorTask.class).configure(taskAction);
        projectTasks.register("initProject", InitProjectTask.class).configure(taskAction);
    }
}
