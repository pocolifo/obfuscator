package com.pocolifo.obfuscator.passes;

import com.pocolifo.obfuscator.ObfuscatorEngine;
import com.pocolifo.obfuscator.util.ProgressUtil;
import lombok.Getter;
import me.tongfei.progressbar.ProgressBar;
import org.objectweb.asm.tree.*;

import java.util.Collection;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractMethodPass<T extends PassOptions> implements ClassPass<T> {
    @Override
    public Collection<ClassNode> run(ObfuscatorEngine engine, Collection<ClassNode> inClasses) {
        try (ProgressBar bar = ProgressUtil.bar(getPassName(), inClasses.size())) {
            for (ClassNode cls : inClasses) {
                for (MethodNode mtd : cls.methods) {
                    doMethod(engine, inClasses, cls, mtd, bar);
                }

                bar.step();
            }
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
            indexes[i] = highestIndex + i + 1;
        }

        return indexes;
    }

    public abstract String getPassName();

    public abstract void doMethod(ObfuscatorEngine engine, Collection<ClassNode> inClasses, ClassNode classNode, MethodNode methodNode, ProgressBar bar);
}
