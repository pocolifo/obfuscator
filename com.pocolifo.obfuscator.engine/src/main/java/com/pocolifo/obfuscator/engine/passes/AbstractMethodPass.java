package com.pocolifo.obfuscator.engine.passes;

import com.pocolifo.obfuscator.engine.ObfuscatorEngine;
import com.pocolifo.obfuscator.engine.util.ObfAnnotationsUtil;
import com.pocolifo.obfuscator.engine.util.ProgressUtil;
import me.tongfei.progressbar.ProgressBar;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;
import java.util.ListIterator;

public abstract class AbstractMethodPass<T extends PassOptions> implements ClassPass<T>, Opcodes {
    @SuppressWarnings("unchecked")
    @Override
    public Collection<ClassNode> run(ObfuscatorEngine engine, Collection<ClassNode> inClasses) {
        try (ProgressBar bar = ProgressUtil.bar(getPassName(), inClasses.size())) {
            inClasses.parallelStream().forEach(cls -> {
                for (MethodNode mtd : cls.methods) {
                    // we've already checked if this pass is enabled
                    T options = (T) ObfAnnotationsUtil.getOptions(mtd, cls, this.getClass(), this.getOptions());
                    doMethod(engine, inClasses, cls, mtd, bar, options);
                }
                bar.step();
            });
        }

        return inClasses;
    }

    public static int[] getNextVarIndexes(int count, MethodNode methodNode) {
        // check instructions
        ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();

        int highestIndex = 0;

        while (iterator.hasNext()) {
            AbstractInsnNode next = iterator.next();

            if (next instanceof VarInsnNode) {
                int index = ((VarInsnNode) next).var;
                highestIndex = Math.max(highestIndex, index);
            }
        }

        // allocate
        int[] indexes = new int[count];

        for (int i = 0; count > i; i++) {
            indexes[i] = highestIndex + i + 3;
        }

        return indexes;
    }

    public abstract String getPassName();

    public abstract void doMethod(ObfuscatorEngine engine, Collection<ClassNode> inClasses, ClassNode classNode, MethodNode methodNode, ProgressBar bar, T specificOptions);
}
