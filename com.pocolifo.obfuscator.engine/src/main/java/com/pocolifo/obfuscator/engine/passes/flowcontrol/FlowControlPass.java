package com.pocolifo.obfuscator.engine.passes.flowcontrol;

import com.pocolifo.obfuscator.engine.ObfuscatorEngine;
import com.pocolifo.obfuscator.engine.passes.AbstractMethodPass;
import com.pocolifo.obfuscator.engine.passes.PassOptions;
import lombok.Getter;
import me.tongfei.progressbar.ProgressBar;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;

public class FlowControlPass extends AbstractMethodPass<PassOptions> implements Opcodes {
    @Getter
    public PassOptions options = new PassOptions();

    @Override
    public String getPassName() {
        return "Mangling flow";
    }

    @Override
    public void doMethod(ObfuscatorEngine engine, Collection<ClassNode> inClasses, ClassNode classNode, MethodNode methodNode, ProgressBar bar, PassOptions options) {
        LabelNode prevLabel = null;

        for (AbstractInsnNode instruction : methodNode.instructions) {
            if (instruction instanceof LabelNode) {
                if (prevLabel != null) {
                    // todo
//                    methodNode.instructions.insertBefore(prevLabel, new LdcInsnNode(0));
//                    methodNode.instructions.insertBefore(prevLabel, new LdcInsnNode(0));
//                    methodNode.instructions.insertBefore(prevLabel, new JumpInsnNode(IF_ICMPNE, (LabelNode) instruction));
                }

                prevLabel = (LabelNode) instruction;
            }
        }

    }
}
