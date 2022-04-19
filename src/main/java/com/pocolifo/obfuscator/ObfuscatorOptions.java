package com.pocolifo.obfuscator;

import com.pocolifo.obfuscator.logger.Logging;
import com.pocolifo.obfuscator.passes.ClassPass;
import com.pocolifo.obfuscator.passes.number.NumberManglerPass;
import com.pocolifo.obfuscator.passes.remapping.RemapNamesPass;
import com.pocolifo.obfuscator.passes.shufflemembers.ShuffleMembersPass;
import com.pocolifo.obfuscator.passes.sourcehints.RemoveSourceHintsPass;
import com.pocolifo.obfuscator.passes.string.StringManglerPass;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ObfuscatorOptions {
    protected long initTimestamp;

    public List<File> libraryJars = new ArrayList<>();
    public File inJar;
    public File outJar;
    public File javaHome = new File(System.getProperty("java.home"));
    public boolean dumpHierarchy = false;

    public Iterable<ClassPass<?>> passes = Arrays.asList(
            new RemapNamesPass(),
            new RemoveSourceHintsPass(),
            new StringManglerPass(),
            new NumberManglerPass(),
            new ShuffleMembersPass()
    );

    public void prepare() throws RuntimeException {
        initTimestamp = System.currentTimeMillis();

        if (inJar == null) Logging.fatal("Input JAR is not set");
        if (!inJar.isFile()) Logging.fatal("Input JAR does not exist or is not a file");
        if (!libraryJars.stream().allMatch(File::exists)) Logging.fatal("Some library files do not exist");

        outJar = new File("output.jar");
    }
}
