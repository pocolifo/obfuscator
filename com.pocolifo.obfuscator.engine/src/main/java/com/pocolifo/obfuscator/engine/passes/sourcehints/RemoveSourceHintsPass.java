package com.pocolifo.obfuscator.engine.passes.sourcehints;

import com.pocolifo.obfuscator.engine.ObfuscatorEngine;
import com.pocolifo.obfuscator.engine.passes.ClassPass;
import com.pocolifo.obfuscator.engine.passes.PassOptions;
import com.pocolifo.obfuscator.engine.util.ObfAnnotationsUtil;
import com.pocolifo.obfuscator.engine.util.ProgressUtil;
import lombok.Getter;
import me.tongfei.progressbar.ProgressBar;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;
import java.util.ListIterator;

public class RemoveSourceHintsPass implements ClassPass<RemoveSourceHintsPass.Options> {
    @Getter public Options options = new Options();

    @Override
    public Collection<ClassNode> run(ObfuscatorEngine engine, Collection<ClassNode> inClasses) {
        try (ProgressBar bar = ProgressUtil.bar("Removing source hints", inClasses.size())) {
            for (ClassNode node : inClasses) {
                RemoveSourceHintsPass.Options clsOpts = (RemoveSourceHintsPass.Options) ObfAnnotationsUtil.getOptions(node, RemoveSourceHintsPass.class, options);

                if (clsOpts.sourceFile) node.sourceFile = null;
                if (clsOpts.sourceDebug) node.sourceDebug = null;

                if (clsOpts.lineNumbers) {
                    for (MethodNode method : node.methods) {
                        ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
                        AbstractInsnNode n;

                        while (iterator.hasNext()) {
                            n = iterator.next();

                            if (n instanceof LineNumberNode) {
                                iterator.remove();
                            }
                        }
                    }
                }

                bar.step();
            }
        }

        return inClasses;
    }

    public static class Options extends PassOptions {
        public boolean sourceFile = true;
        public boolean sourceDebug = true;
        public boolean lineNumbers = true;
    }
}
