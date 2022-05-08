package com.pocolifo.obfuscator.engine.passes.antidecompile;

import com.pocolifo.obfuscator.engine.ObfuscatorEngine;
import com.pocolifo.obfuscator.engine.passes.AbstractMethodPass;
import com.pocolifo.obfuscator.engine.passes.PassOptions;
import lombok.Getter;
import me.tongfei.progressbar.ProgressBar;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public class AntiDecompilePass extends AbstractMethodPass<AntiDecompilePass.Options> implements Opcodes {
    @Getter public Options options = new Options();
    @Override

    public String getPassName() {
        return "Anti-Decompile provisions";
    }

    @Override
    public void doMethod(ObfuscatorEngine engine, Collection<ClassNode> inClasses, ClassNode classNode, MethodNode methodNode, ProgressBar bar, Options options) {
        boolean isInterface = (classNode.access & Opcodes.ACC_INTERFACE) != 0;
        boolean isAbstract = (methodNode.access & Opcodes.ACC_ABSTRACT) != 0;

        if (isAbstract || isInterface) return;

        methodNode.instructions.add(new TypeInsnNode(NEW, "java/lang/Object"));
        methodNode.instructions.add(new TypeInsnNode(NEW, "java/lang/Object"));
        methodNode.instructions.add(new TypeInsnNode(NEW, "java/lang/Object"));
        methodNode.instructions.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/Object", "<init>", "()V"));
        methodNode.instructions.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/Object", "<init>", "()V"));
        methodNode.instructions.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/Object", "<init>", "()V"));

        if (options.parentDirectorySourceFileNames) {
            classNode.sourceFile = getRandomParentDirectories() + classNode.name;
        }
    }

    private String getRandomParentDirectories() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; ThreadLocalRandom.current().nextInt(0, 10) > i; i++) {
            builder.append("../");
        }

        return builder.toString();
    }

    public static class Options extends PassOptions {
        public boolean parentDirectorySourceFileNames = true;
    }
}
