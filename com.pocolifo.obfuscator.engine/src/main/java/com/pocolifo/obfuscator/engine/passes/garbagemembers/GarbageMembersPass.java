package com.pocolifo.obfuscator.engine.passes.garbagemembers;

import com.pocolifo.obfuscator.engine.ObfuscatorEngine;
import com.pocolifo.obfuscator.engine.passes.ClassPass;
import com.pocolifo.obfuscator.engine.passes.PassOptions;
import com.pocolifo.obfuscator.engine.util.ObfAnnotationsUtil;
import com.pocolifo.obfuscator.engine.util.RemappingUtil;
import com.pocolifo.obfuscator.engine.util.ProgressUtil;
import lombok.Getter;
import me.tongfei.progressbar.ProgressBar;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GarbageMembersPass implements ClassPass<GarbageMembersPass.Options>, Opcodes {
    @Getter public Options options = new Options();

    @Override
    public Collection<ClassNode> run(ObfuscatorEngine engine, Collection<ClassNode> inClasses) {
        try (ProgressBar bar = ProgressUtil.bar("Adding garbage members", inClasses.size())) {
            inClasses.parallelStream().forEach(node -> {
                boolean isInterface = (node.access & Opcodes.ACC_INTERFACE) != 0;
                Options clsOpts = (Options) ObfAnnotationsUtil.getOptions(node, GarbageMembersPass.class, options);

                if (clsOpts.addMethods) {
                    for (int i = 0; ThreadLocalRandom.current().nextInt(4, 10) > i; i++) {
                        MethodNode methodNode = generateRandomMethod(isInterface);
                        node.methods.add(methodNode);

                        // add to children if interface
                        if (isInterface) {
                            RemappingUtil.iterateRecursiveChildClasses(engine.getHierarchy().find(node.name),
                                    classHierarchyNode -> classHierarchyNode.classNode.methods.add(methodNode));
                        }
                    }
                }

                if (clsOpts.addFields && !isInterface) {
                    for (int i = 0; ThreadLocalRandom.current().nextInt(5, 12) > i; i++) {
                        FieldNode fieldNode = generateRandomField();
                        node.fields.add(fieldNode);
                    }
                }

                bar.step();
            });
        }

        return inClasses;
    }

    private FieldNode generateRandomField() {
        Type randomType = getRandomType();

        if (randomType == Type.VOID_TYPE) randomType = Type.LONG_TYPE;

        return new FieldNode(
                getRandomAccess(false),
                "__" + UUID.randomUUID().toString().replace("-", "_"),
                randomType.getDescriptor(),
                null,
                null
        );
    }

    private MethodNode generateRandomMethod(boolean isInterface) {
        MethodNode methodNode = new MethodNode();
        methodNode.name = "__" + UUID.randomUUID().toString().replace("-", "_");

        Type typeToReturn = getRandomType();
        List<Type> params = new ArrayList<>();

        for (int i = 0; ThreadLocalRandom.current().nextInt(0, 5) > i; i++) {
            Type randomType = getRandomType();

            if (randomType != Type.VOID_TYPE) params.add(randomType);
        }

        methodNode.desc = Type.getMethodDescriptor(typeToReturn, params.toArray(new Type[0]));
        methodNode.access = getRandomAccess(isInterface);

        if (typeToReturn == Type.INT_TYPE || typeToReturn == Type.BYTE_TYPE || typeToReturn == Type.CHAR_TYPE) {
            methodNode.instructions.add(new InsnNode(ThreadLocalRandom.current().nextInt(2, 9)));
        } else if (typeToReturn == Type.FLOAT_TYPE) {
            methodNode.instructions.add(new InsnNode(ThreadLocalRandom.current().nextInt(11, 14)));
        } else {
            String path = typeToReturn.getClassName().replaceAll("\\.", "/");
            methodNode.instructions.add(new TypeInsnNode(NEW, path));
            methodNode.instructions.add(new InsnNode(DUP));
            methodNode.instructions.add(new MethodInsnNode(INVOKESPECIAL, path, "<init>", "()V"));
        }

        if (typeToReturn == Type.CHAR_TYPE) {
            methodNode.instructions.add(new InsnNode(I2C));
        }

        if (typeToReturn == Type.VOID_TYPE) {
            methodNode.instructions.add(new InsnNode(RETURN));
        } else if (typeToReturn == Type.FLOAT_TYPE) {
            methodNode.instructions.add(new InsnNode(FRETURN));
        } else if (typeToReturn == Type.INT_TYPE || typeToReturn == Type.CHAR_TYPE || typeToReturn == Type.BYTE_TYPE) {
            methodNode.instructions.add(new InsnNode(IRETURN));
        } else {
            methodNode.instructions.add(new InsnNode(ARETURN));
        }

        return methodNode;
    }

    private int getRandomAccess(boolean isInterface) {
        if (isInterface) return ACC_PUBLIC;

        int a = 0;

        switch (ThreadLocalRandom.current().nextInt(0, 4)) {
            case 0:
                a = ACC_PUBLIC;
                break;

            case 1:
                a = ACC_PRIVATE;
                break;

            default:
                a = ACC_PROTECTED;
                break;
        }

        return a | (ThreadLocalRandom.current().nextBoolean() ? Opcodes.ACC_STATIC : 0);
    }

    private Type getRandomType() {
        switch (ThreadLocalRandom.current().nextInt(0, 12)) {
            case 1:
                return Type.BYTE_TYPE;
            case 2:
                return Type.CHAR_TYPE;
            case 3:
                return Type.FLOAT_TYPE;
            case 4:
                return Type.INT_TYPE;
            case 5:
                return Type.getType(String.class);
            case 6:
                return Type.getType(ArrayList.class);
            case 7:
                return Type.getType(HashMap.class);
            case 8:
                return Type.getType(Object.class);
            default:
                return Type.VOID_TYPE;
        }
    }

    public static class Options extends PassOptions {
        public boolean addFields = false;
        public boolean addMethods = false;
    }
}
