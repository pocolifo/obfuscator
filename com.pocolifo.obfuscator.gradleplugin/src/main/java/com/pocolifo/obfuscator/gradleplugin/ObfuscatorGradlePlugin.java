package com.pocolifo.obfuscator.gradleplugin;

import com.pocolifo.obfuscator.gradleplugin.config.ObfuscatorExtension;
import com.pocolifo.obfuscator.gradleplugin.tasks.ObfuscateTask;
import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ObfuscatorGradlePlugin implements Plugin<Project> {
    public static final String GROUP = "obfuscator";
    @Getter
    private static ObfuscatorGradlePlugin plugin;
    public ObfuscatorExtension config;

    @Override
    public void apply(Project project) {
        plugin = this;
        config = project.getExtensions().create("obfuscator", ObfuscatorExtension.class);

        project.getTasks().create("obfuscate", ObfuscateTask.class);
    }
}
