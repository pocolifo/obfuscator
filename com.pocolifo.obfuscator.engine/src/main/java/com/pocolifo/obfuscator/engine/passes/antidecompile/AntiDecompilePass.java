package com.pocolifo.obfuscator.engine.passes.antidecompile;

import com.pocolifo.obfuscator.engine.ObfuscatorEngine;
import com.pocolifo.obfuscator.engine.passes.AbstractMethodPass;
import com.pocolifo.obfuscator.engine.passes.PassOptions;
import lombok.Getter;
import me.tongfei.progressbar.ProgressBar;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;

public class AntiDecompilePass extends AbstractMethodPass<PassOptions> implements Opcodes {
    @Getter public PassOptions options = new PassOptions();
    @Override

    public String getPassName() {
        return "Anti-Decompile provisions";
    }

    @Override
    public void doMethod(ObfuscatorEngine engine, Collection<ClassNode> inClasses, ClassNode classNode, MethodNode methodNode, ProgressBar bar, PassOptions options) {
        boolean isInterface = (classNode.access & Opcodes.ACC_INTERFACE) != 0;
        boolean isAbstract = (methodNode.access & Opcodes.ACC_ABSTRACT) != 0;

        if (isAbstract || isInterface) return;

        methodNode.instructions.add(new TypeInsnNode(NEW, "java/lang/Object"));
        methodNode.instructions.add(new TypeInsnNode(NEW, "java/lang/Object"));
        methodNode.instructions.add(new TypeInsnNode(NEW, "java/lang/Object"));
        methodNode.instructions.add(new InsnNode(POP));
        methodNode.instructions.add(new InsnNode(POP));
        methodNode.instructions.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/Object", "<init>", "()V"));
    }
}
