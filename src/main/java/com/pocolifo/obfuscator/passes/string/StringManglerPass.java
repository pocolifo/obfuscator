package com.pocolifo.obfuscator.passes.string;

import com.pocolifo.obfuscator.ObfuscatorEngine;
import com.pocolifo.obfuscator.passes.AbstractMethodPass;
import lombok.Getter;
import me.tongfei.progressbar.ProgressBar;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.PrintStream;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public class StringManglerPass extends AbstractMethodPass<StringManglerOptions> implements Opcodes {
    @Getter public StringManglerOptions options = new StringManglerOptions();

    @Override
    public String getPassName() {
        return "Mangling strings";
    }

    @Override
    public void doMethod(ObfuscatorEngine engine, Collection<ClassNode> inClasses, ClassNode classNode, MethodNode methodNode, ProgressBar bar) {
        for (AbstractInsnNode instruction : methodNode.instructions) {
            if (instruction instanceof LdcInsnNode) {
                LdcInsnNode node = (LdcInsnNode) instruction;

                if (node.cst instanceof String) {
                    methodNode.instructions.insert(instruction, expandString((String) node.cst, methodNode));
                    methodNode.instructions.remove(instruction);
                }
            }
        }
    }

    private InsnList expandString(String obfString, MethodNode methodNode) {
        // todo encrypt obfString, then at usage, run decrypt method

        char[] characters = obfString.toCharArray();
        InsnList insns = new InsnList();
        int[] allocated = getNextVarIndexes(obfString.length() * 2 + 1, methodNode);

        // create and store a stringbuilder to append to
        insns.add(new TypeInsnNode(NEW, "java/lang/StringBuilder"));
        insns.add(new InsnNode(Opcodes.DUP));
        insns.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V"));
        insns.add(new VarInsnNode(ASTORE, allocated[0]));

        // generate and store keys for each character
        int[] key = new int[obfString.length()];

        for (int k = 0; key.length > k; k++) {
            key[k] = ThreadLocalRandom.current().nextInt(-256, 256);
        }

        for (int i = 0; obfString.length() > i; i++) {
            // enc: val ^ key = enc
            // dec: key ^ enc = key

            // xor the character with its key
            insns.add(new LdcInsnNode(characters[i] ^ key[i]));

            // add some randomness to possible mess up static analysis
            // store the result
            insns.add(new MethodInsnNode(INVOKESTATIC, "java/util/concurrent/ThreadLocalRandom", "current", "()Ljava/util/concurrent/ThreadLocalRandom;"));
            insns.add(new MethodInsnNode(INVOKEVIRTUAL, "java/util/concurrent/ThreadLocalRandom", "nextInt", "()I"));
            insns.add(new VarInsnNode(ISTORE, allocated[i + 1]));

            insns.add(new VarInsnNode(ILOAD, allocated[i + 1]));
            insns.add(new InsnNode(IADD));
            insns.add(new VarInsnNode(ISTORE, allocated[i + 1 + obfString.length()]));
        }

        // append to stringbuilder
        for (int i = 0; obfString.length() > i; i++) {
            insns.add(new VarInsnNode(ALOAD, allocated[0]));  // load stringbuilder

            insns.add(new VarInsnNode(ILOAD, allocated[i + 1 + obfString.length()]));  // load number
            insns.add(new VarInsnNode(ILOAD, allocated[i + 1]));  // load random
            insns.add(new InsnNode(ISUB));  // undoes the IADD
            insns.add(new InsnNode(I2C));  // convert to char
            insns.add(new LdcInsnNode((char) key[i]));  // load the key (as a char to annoy ppl)
            insns.add(new InsnNode(IXOR));  // xor

            insns.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", Type.getMethodDescriptor(Type.getType(StringBuilder.class), Type.getType(char.class))));  // append to stringbuilder
        }

        // pop all the stringbuilder appends
        for (int i = 0; obfString.length() > i; i++) {
            insns.add(new InsnNode(POP));
        }

        // load string
        insns.add(new VarInsnNode(ALOAD, allocated[0]));
        insns.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;"));

        // now you have it lol
        return insns;
    }
}
