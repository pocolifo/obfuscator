package com.pocolifo.obfuscator.engine;

import com.pocolifo.obfuscator.engine.passes.ArchivePass;
import com.pocolifo.obfuscator.engine.passes.ArchivePassRunTime;
import com.pocolifo.obfuscator.engine.passes.ClassPass;
import com.pocolifo.obfuscator.engine.passes.antidecompile.AntiDecompileArchivePass;
import com.pocolifo.obfuscator.engine.passes.antidecompile.AntiDecompilePass;
import com.pocolifo.obfuscator.engine.passes.antidecompile.FakeClassAsResourceArchivePass;
import com.pocolifo.obfuscator.engine.passes.flowcontrol.FlowControlPass;
import com.pocolifo.obfuscator.engine.passes.garbagemembers.GarbageMembersPass;
import com.pocolifo.obfuscator.engine.passes.number.NumberManglerPass;
import com.pocolifo.obfuscator.engine.passes.obfannotations.RemoveObfuscatorAnnotationsPass;
import com.pocolifo.obfuscator.engine.passes.remapping.RemapNamesPass;
import com.pocolifo.obfuscator.engine.passes.remapping.resource.RemapResourceNamesArchivePass;
import com.pocolifo.obfuscator.engine.passes.shufflemembers.ShuffleMembersPass;
import com.pocolifo.obfuscator.engine.passes.sourcehints.RemoveSourceHintsPass;
import com.pocolifo.obfuscator.engine.passes.string.StringManglerPass;
import com.pocolifo.obfuscator.engine.util.DynamicOption;
import com.pocolifo.obfuscator.engine.util.Logging;
import com.pocolifo.obfuscator.engine.util.NotConfigOption;
import lombok.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


@Data
public class ObfuscatorOptions {
    @NotConfigOption protected long initTimestamp;
    @NotConfigOption public File inJar;
    @DynamicOption public File javaHome = new File(System.getProperty("java.home"));

    public List<File> libraryJars = new ArrayList<>();
    public File outJar = new File("output.jar");
    public boolean dumpHierarchy = false;

    public List<ClassPass<?>> passes = Arrays.asList(
            new GarbageMembersPass(),
            new RemapNamesPass(),
            new RemoveSourceHintsPass(),
            new StringManglerPass(),
            new NumberManglerPass(),
            new ShuffleMembersPass(),
            new FlowControlPass(),
            new AntiDecompilePass(),
            new RemoveObfuscatorAnnotationsPass()
    );

    public List<ArchivePass<?>> archivePasses = Arrays.asList(
            new FakeClassAsResourceArchivePass(),
            new AntiDecompileArchivePass(),
            new RemapResourceNamesArchivePass()
    );

    protected Collection<ArchivePass<?>> getApplicableArchivePasses(ArchivePassRunTime runTime) {
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
