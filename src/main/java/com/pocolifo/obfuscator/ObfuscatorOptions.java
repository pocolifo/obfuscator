package com.pocolifo.obfuscator;

import com.pocolifo.obfuscator.passes.ArchivePass;
import com.pocolifo.obfuscator.passes.ArchivePassRunTime;
import com.pocolifo.obfuscator.passes.ClassPass;
import com.pocolifo.obfuscator.passes.antidecompile.AntiDecompileArchivePass;
import com.pocolifo.obfuscator.passes.flowcontrol.FlowControlPass;
import com.pocolifo.obfuscator.passes.garbagemembers.GarbageMembersPass;
import com.pocolifo.obfuscator.passes.number.NumberManglerPass;
import com.pocolifo.obfuscator.passes.remapping.RemapNamesPass;
import com.pocolifo.obfuscator.passes.shufflemembers.ShuffleMembersPass;
import com.pocolifo.obfuscator.passes.sourcehints.RemoveSourceHintsPass;
import com.pocolifo.obfuscator.passes.string.StringManglerPass;
import com.pocolifo.obfuscator.util.DynamicOption;
import com.pocolifo.obfuscator.util.Logging;
import com.pocolifo.obfuscator.util.NotConfigOption;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ObfuscatorOptions {
    @NotConfigOption protected long initTimestamp;
    @NotConfigOption public File inJar;

    @DynamicOption public File javaHome = new File(System.getProperty("java.home"));

    public List<File> libraryJars = new ArrayList<>();
    public File outJar = new File("output.jar");
    public boolean dumpHierarchy = false;

    public Iterable<ClassPass<?>> passes = Arrays.asList(
            new GarbageMembersPass(),
            new RemapNamesPass(),
            new RemoveSourceHintsPass(),
            new StringManglerPass(),
            new NumberManglerPass(),
            new ShuffleMembersPass(),
            new FlowControlPass()
    );

    public Iterable<ArchivePass<?>> archivePasses = Arrays.asList(
            new AntiDecompileArchivePass()
    );

    protected Iterable<ArchivePass<?>> getApplicableArchivePasses(ArchivePassRunTime runTime) {
        List<ArchivePass<?>> passes = new ArrayList<>();

        for (ArchivePass<?> archivePass : archivePasses) {
            if (archivePass.getOptions().enabled && archivePass.getRunTime().equals(runTime))
                passes.add(archivePass);
        }

        return passes;
    }

    public void prepare() throws RuntimeException {
        initTimestamp = System.currentTimeMillis();

        if (inJar == null) Logging.fatal("Input JAR is not set");
        if (!inJar.isFile()) Logging.fatal("Input JAR does not exist or is not a file");
        if (!libraryJars.stream().allMatch(File::exists)) Logging.fatal("Some library files do not exist");
    }
}
