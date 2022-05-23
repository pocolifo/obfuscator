package com.pocolifo.obfuscator.engine.passes.flow;

import com.pocolifo.obfuscator.engine.ObfuscatorEngine;
import com.pocolifo.obfuscator.engine.passes.AbstractMethodPass;
import com.pocolifo.obfuscator.engine.passes.PassOptions;
import lombok.Getter;
import me.tongfei.progressbar.ProgressBar;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;

public class FlowPass extends AbstractMethodPass<PassOptions> implements Opcodes {
    @Getter
    public PassOptions options = new PassOptions();

    @Override
    public String getPassName() {
        return "Mangling flow";
    }

    @Override
    public void doMethod(ObfuscatorEngine engine, Collection<ClassNode> inClasses, ClassNode classNode, MethodNode methodNode, ProgressBar bar, PassOptions options) {
        methodNode.instructions.forEach(insn -> {
            if (insn instanceof JumpInsnNode) {
                doJumpNode((JumpInsnNode) insn);
            }
        });
    }

    private void doJumpNode(JumpInsnNode insn) {

    }
}
