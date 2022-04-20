package com.pocolifo.obfuscator.passes.remapping;

import com.pocolifo.obfuscator.ObfuscatorEngine;
import com.pocolifo.obfuscator.ObfuscatorOptions;
import com.pocolifo.obfuscator.classes.ClassHierarchy;
import com.pocolifo.obfuscator.classes.ObfuscationClassKeeper;
import com.pocolifo.obfuscator.passes.ClassPass;
import com.pocolifo.obfuscator.passes.PassOptions;
import com.pocolifo.obfuscator.util.ProgressUtil;
import lombok.Getter;
import me.tongfei.progressbar.ProgressBar;
import org.objectweb.asm.tree.ClassNode;

import java.util.*;

public class RemapNamesPass implements ClassPass<RemapNamesPass.Options> {
    @Getter public Options options = new Options();

    @Override
    public Collection<ClassNode> run(ObfuscatorEngine engine, Collection<ClassNode> inClasses) {
        ObfuscationClassKeeper classKeeper = engine.getClassKeeper();
        ClassHierarchy hierarchy = engine.getHierarchy();

        List<ClassNode> remappedClasses = new ArrayList<>();

        try (ProgressBar bar = ProgressUtil.bar("Remapping names", inClasses.size())) {
            // mappings
            bar.setExtraMessage("Generating mappings");
            JarMapping mapping = MappingLoader.generateMapping(hierarchy, classKeeper, options);

            // remap
            bar.setExtraMessage("Remapping");
            JarMappingRemapper mappingProvider = new JarMappingRemapper(mapping);

            for (ClassNode cls : inClasses) {
                ClassNode remapped = new ClassNode();
                ParameterEnhancedClassRemapper remapper = new ParameterEnhancedClassRemapper(remapped, mappingProvider);
                cls.accept(remapper);

                remappedClasses.add(remapped);

                bar.step();
            }
        }

        return remappedClasses;
    }

    public static class Options extends PassOptions {
        public boolean remapClassNames = true;
        public boolean remapFieldNames = true;
        public boolean remapMethodNames = true;
        public boolean remapMethodParameterNames = true;

        public List<String> excludedMethods = Arrays.asList(
                "main ([Ljava/lang/String;)V",
                "<init>",
                "<clinit>"
        );

        public List<String> excludedFields = Collections.singletonList(
                "serialVersionUID"
        );
    }
}
