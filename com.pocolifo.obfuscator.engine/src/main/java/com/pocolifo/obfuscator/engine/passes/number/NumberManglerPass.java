package com.pocolifo.obfuscator.engine.passes.number;

import com.pocolifo.obfuscator.engine.ObfuscatorEngine;
import com.pocolifo.obfuscator.engine.passes.AbstractMethodPass;
import lombok.Getter;
import me.tongfei.progressbar.ProgressBar;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class NumberManglerPass extends AbstractMethodPass<NumberManglerPassOptions> implements Opcodes {
    @Getter public NumberManglerPassOptions options = new NumberManglerPassOptions();

    @Override
    public String getPassName() {
        return "Mangling numbers";
    }

    @Override
    public void doMethod(ObfuscatorEngine engine, Collection<ClassNode> inClasses, ClassNode classNode, MethodNode methodNode, ProgressBar bar, NumberManglerPassOptions options) {
        if (methodNode.instructions.getFirst() == null) return;

        // key variable
        int keyVarIndex = getNextVarIndexes(1, methodNode)[0];

        // (reversed)
        methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), new VarInsnNode(ISTORE, keyVarIndex));
        methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), new LdcInsnNode((char) ThreadLocalRandom.current().nextInt()));

        for (AbstractInsnNode instruction : methodNode.instructions) {
            if (instruction instanceof LdcInsnNode && ((LdcInsnNode) instruction).cst instanceof Integer) {
                int obfNumber = (int) ((LdcInsnNode) instruction).cst;

                char[] chars = Integer.toBinaryString(obfNumber).toCharArray();
                int availableShift = chars.length;

                while (true) {
                    if (0 > availableShift - 1) break;
                    if (chars[availableShift - 1] == '1') break;
                    availableShift--;
                }

                availableShift = chars.length - availableShift;

                int shift = availableShift + ThreadLocalRandom.current().nextInt() * 32;
                obfNumber = obfNumber >> availableShift;

                // enc: val ^ key = enc
                // dec: key ^ enc = key
                int key = ThreadLocalRandom.current().nextInt();
                int enc = obfNumber ^ key;

                // store the key
                methodNode.instructions.insertBefore(instruction, new LdcInsnNode(key));
                methodNode.instructions.insertBefore(instruction, new VarInsnNode(ISTORE, keyVarIndex));

                // todo garbage code?

                // decrypt
                methodNode.instructions.insertBefore(instruction, new VarInsnNode(ILOAD, keyVarIndex));
                methodNode.instructions.insertBefore(instruction, new LdcInsnNode(enc));
                methodNode.instructions.insertBefore(instruction, new InsnNode(IXOR));

                methodNode.instructions.insertBefore(instruction, new LdcInsnNode(shift));
                methodNode.instructions.insertBefore(instruction, new InsnNode(ISHL));

                // remove OG insn
                methodNode.instructions.remove(instruction);
            }
        }

        // consts, xipush
        for (AbstractInsnNode instruction : methodNode.instructions) {
            Integer obfNumber = null;

            switch (instruction.getOpcode()) {
                // iconst
                case ICONST_0:
                    obfNumber = 0;
                    break;

                case ICONST_1:
                    obfNumber = 1;
                    break;

                case ICONST_2:
                    obfNumber = 2;
                    break;

                case ICONST_3:
                    obfNumber = 3;
                    break;

                case ICONST_4:
                    obfNumber = 4;
                    break;

                case ICONST_5:
                    obfNumber = 5;
                    break;

                case ICONST_M1:
                    obfNumber = -1;
                    break;

                // ipush
                case SIPUSH:
                case BIPUSH:
                    obfNumber = ((IntInsnNode) instruction).operand;
                    break;
            }

            if (obfNumber != null) {
                int key = ThreadLocalRandom.current().nextInt();

                methodNode.instructions.insertBefore(instruction, new LdcInsnNode(~obfNumber ^ key));
                methodNode.instructions.insertBefore(instruction, new InsnNode(ICONST_M1));
                methodNode.instructions.insertBefore(instruction, new InsnNode(IXOR));
                methodNode.instructions.insertBefore(instruction, new LdcInsnNode(key));
                methodNode.instructions.insertBefore(instruction, new InsnNode(IXOR));

                methodNode.instructions.remove(instruction);
            }
        }
    }
}
