package com.pocolifo.obfuscator.engine.passes.bytecodescrambler;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

public class BytecodeSequenceScrambler implements Opcodes {

    public static void process(InsnList list) {
        LinkedList<Element> sequence = new LinkedList<>();
        InsnList returnList = new InsnList();

        loadIntoSequence(list, sequence);
        addJumpInsns(sequence);
        addShuffledElements(returnList, sequence);

        list.clear();
        list.add(returnList);
    }

    private static void loadIntoSequence(InsnList insns, LinkedList<Element> sequence) {
        InsnList building = new InsnList();

        for (AbstractInsnNode abstractInsnNode : insns) {
            if (abstractInsnNode instanceof LabelNode) {
                Element element = new Element();
                element.list.add(building);
                element.nextLabelNode = (LabelNode) abstractInsnNode;
                sequence.add(element);

                building.clear();
            }

            building.add(abstractInsnNode);
        }
    }

    private static void addJumpInsns(LinkedList<Element> sequence) {
        for (int i = 0; i < sequence.size(); i++) {
            Element element = sequence.get(i);

            if (sequence.size() > i + 1) element.list.add(new JumpInsnNode(GOTO, element.nextLabelNode));
        }
    }

    private static void addShuffledElements(InsnList insns, LinkedList<Element> sequence) {
        // add the initial goto insn
        if (sequence.isEmpty()) return;
        insns.add(new JumpInsnNode(GOTO, sequence.get(0).nextLabelNode));

        // add
        while (!sequence.isEmpty()) {
            Element element = sequence.remove(ThreadLocalRandom.current().nextInt(sequence.size()));
            insns.add(element.list);
        }
    }

    public static class Element {
        public InsnList list = new InsnList();
        private LabelNode nextLabelNode;
    }
}
