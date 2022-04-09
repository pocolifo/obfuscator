package com.pocolifo.obfuscator.classes;

import org.objectweb.asm.tree.ClassNode;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClassHierarchyNode {
    public final ClassNode classNode;
    public final Queue<ClassHierarchyNode> children = new ConcurrentLinkedQueue<>();

    public ClassHierarchyNode(ClassNode node) {
        classNode = node;
    }
}
