package com.pocolifo.obfuscator.gradleplugin.tasks;

import com.pocolifo.obfuscator.engine.ObfuscatorEngine;
import com.pocolifo.obfuscator.engine.ObfuscatorOptions;
import com.pocolifo.obfuscator.gradleplugin.ObfuscatorGradlePlugin;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.*;
import org.gradle.jvm.tasks.Jar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class ObfuscateTask extends DefaultTask {
    @OutputFile
    public Provider<File> outputFile;

    @InputFile
    public Provider<File> inputFile;

    @InputFiles
    public List<Configuration> libraries;

    @Input
    public Provider<ObfuscatorOptions> obfuscatorOptions;

    public ObfuscateTask() {
        dependsOn("jar");

        setGroup(ObfuscatorGradlePlugin.GROUP);
        setDescription("Generates an obfuscated JAR");

        libraries = new ArrayList<>();
        libraries.add(getProject().getConfigurations().getByName("runtimeClasspath"));

        outputFile = getProject().provider(() -> new File(((Jar) getProject().getTasks().getByName("jar")).getArchiveFile().get().getAsFile().getPath() + "-obfuscated.jar"));
        inputFile = getProject().provider(() -> ((Jar) getProject().getTasks().getByName("jar")).getArchiveFile().get().getAsFile());
        obfuscatorOptions = getProject().provider(() -> ObfuscatorGradlePlugin.getPlugin().config.getObfuscatorOptions());
    }

    @TaskAction
    public void obfuscate() {
        ObfuscatorOptions obfuscatorOptions = getObfuscatorOptions().get();

        obfuscatorOptions.inJar = inputFile.get();
        obfuscatorOptions.outJar = outputFile.get();
        libraries.forEach(files -> obfuscatorOptions.libraryJars.addAll(files.getFiles()));

        obfuscatorOptions.prepare();

        try {
            new ObfuscatorEngine(obfuscatorOptions).obfuscate();
        } catch (Exception e) {
            obfuscatorOptions.outJar.delete();
            e.printStackTrace();
        }
    }

}
