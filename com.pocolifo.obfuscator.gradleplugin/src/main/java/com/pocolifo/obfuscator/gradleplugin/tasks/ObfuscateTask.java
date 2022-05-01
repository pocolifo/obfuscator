package com.pocolifo.obfuscator.gradleplugin.tasks;

import com.pocolifo.obfuscator.engine.ObfuscatorEngine;
import com.pocolifo.obfuscator.engine.ObfuscatorOptions;
import com.pocolifo.obfuscator.gradleplugin.ObfuscatorGradlePlugin;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.tasks.Jar;

import java.io.File;

public class ObfuscateTask extends DefaultTask {
    @OutputFile @Getter @Setter
    public Provider<File> outputFile;

    @InputFile @Getter @Setter
    public Provider<File> inputFile;

    public ObfuscateTask() {
        dependsOn("jar");

        setGroup(ObfuscatorGradlePlugin.GROUP);
        setDescription("Generates an obfuscated JAR");

        outputFile = getProject().provider(() -> new File(((Jar) getProject().getTasks().getByName("jar")).getArchiveFile().get().getAsFile().getPath() + "-obfuscated.jar"));
        inputFile = getProject().provider(() -> ((Jar) getProject().getTasks().getByName("jar")).getArchiveFile().get().getAsFile());
    }

    @TaskAction
    public void obfuscate() {
        ObfuscatorOptions obfuscatorOptions = ObfuscatorGradlePlugin.getPlugin().config.getObfuscatorOptions();

        obfuscatorOptions.inJar = inputFile.get();
        obfuscatorOptions.outJar = outputFile.get();
        obfuscatorOptions.libraryJars.addAll(getProject().getConfigurations().getByName("runtimeClasspath").getFiles());

        obfuscatorOptions.prepare();

        try {
            new ObfuscatorEngine(obfuscatorOptions).obfuscate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
