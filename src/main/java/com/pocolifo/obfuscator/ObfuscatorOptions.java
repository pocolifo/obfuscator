package com.pocolifo.obfuscator;

import com.pocolifo.obfuscator.logger.Logging;
import lombok.Data;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Getter
public class ObfuscatorOptions {
    private List<File> libraryJars = new ArrayList<>();
    private File inJar;
    private File outJar;

    private boolean dumpHierarchy = false;
    private RemapOptions remapOptions = new RemapOptions();
    private boolean removeSourceHints = true;

    public ObfuscatorOptions setInJar(File file) throws IOException {
        inJar = file;
        return this;
    }

    public ObfuscatorOptions addLibraryJar(File file) {
        libraryJars.add(file);
        return this;
    }

    public ObfuscatorOptions setOutJar(File stream) {
        outJar = stream;
        return this;
    }

    public ObfuscatorOptions dumpHierarchy() {
        dumpHierarchy = !dumpHierarchy;
        return this;
    }

    public void prepare() throws RuntimeException {
        if (inJar == null) Logging.fatal("Input JAR is not set");

        outJar = new File("output.jar");
    }

    @Data
    public static class RemapOptions {
        private boolean remapClassNames = true;
        private boolean remapFieldNames = true;
        private boolean remapMethodNames = true;
        private boolean remapMethodParameterNames = true;
    }
}
