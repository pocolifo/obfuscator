package com.pocolifo.obfuscator.engine.passes.shufflemembers;

import com.pocolifo.obfuscator.engine.ObfuscatorEngine;
import com.pocolifo.obfuscator.engine.passes.ClassPass;
import com.pocolifo.obfuscator.engine.passes.PassOptions;
import com.pocolifo.obfuscator.engine.util.ObfAnnotationsUtil;
import com.pocolifo.obfuscator.engine.util.ProgressUtil;
import lombok.Getter;
import me.tongfei.progressbar.ProgressBar;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collection;
import java.util.Collections;

public class ShuffleMembersPass implements ClassPass<ShuffleMembersPass.Options> {
    @Getter public Options options = new Options();

    @Override
    public Collection<ClassNode> run(ObfuscatorEngine engine, Collection<ClassNode> inClasses) {
        try (ProgressBar bar = ProgressUtil.bar("Shuffling members", inClasses.size())) {
            for (ClassNode cls : inClasses) {
                Options clsOpts = (Options) ObfAnnotationsUtil.getOptions(cls, ShuffleMembersPass.class, options);

                if (clsOpts.shuffleMethods) Collections.shuffle(cls.methods);
                if (clsOpts.shuffleFields) Collections.shuffle(cls.fields);

                bar.step();
            }
        }

        return inClasses;
    }

    public static class Options extends PassOptions {
        public boolean shuffleMethods = true;
        public boolean shuffleFields = true;
    }
}
