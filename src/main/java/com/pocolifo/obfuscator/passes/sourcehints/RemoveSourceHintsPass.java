package com.pocolifo.obfuscator.passes.sourcehints;

import com.pocolifo.obfuscator.ObfuscatorEngine;
import com.pocolifo.obfuscator.passes.ClassPass;
import com.pocolifo.obfuscator.util.ProgressUtil;
import me.tongfei.progressbar.ProgressBar;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collection;

public class RemoveSourceHintsPass implements ClassPass {
    @Override
    public Collection<ClassNode> run(ObfuscatorEngine engine, Collection<ClassNode> inClasses) {
        if (engine.getOptions().isRemoveSourceHints()) {
            try (ProgressBar bar = ProgressUtil.bar("Removing source hints", inClasses.size())) {
                for (ClassNode node : inClasses) {
                    node.sourceFile = null;
                    node.sourceDebug = null;

                    bar.step();
                }
            }
        }

        return inClasses;
    }
}
