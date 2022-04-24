package com.pocolifo.obfuscator.engine.util;

import com.pocolifo.obfuscator.engine.classes.ClassHierarchyNode;
import com.pocolifo.obfuscator.engine.passes.remapping.mapping.ClassMapping;
import com.pocolifo.obfuscator.engine.passes.remapping.mapping.FieldMapping;
import com.pocolifo.obfuscator.engine.passes.remapping.mapping.JarMapping;
import com.pocolifo.obfuscator.engine.passes.remapping.mapping.MethodMapping;
import com.pocolifo.obfuscator.engine.passes.remapping.name.NameType;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class RemappingUtil {
    public static void iterateRecursiveParentClasses(ClassHierarchyNode starting, Consumer<ClassHierarchyNode> nodeSupplier) {
        if (starting == null) return;

        for (ClassHierarchyNode parent : starting.parents) {
            if (parent == null) continue;
            nodeSupplier.accept(parent);

            iterateRecursiveParentClasses(parent, nodeSupplier);
        }
    }

    public static void iterateRecursiveChildClasses(ClassHierarchyNode starting, Consumer<ClassHierarchyNode> nodeSupplier) {
        if (starting == null) return;

        for (ClassHierarchyNode child : starting.children) {
            if (child == null) continue;
            nodeSupplier.accept(child);

            iterateRecursiveChildClasses(child, nodeSupplier);
        }
    }

    public static boolean methodNotDeclaredInParent(MethodMapping mapping, ClassHierarchyNode classHierarchyNode) {
        AtomicBoolean declaredInParent = new AtomicBoolean(false);

        iterateRecursiveParentClasses(classHierarchyNode, node -> {
            if (node.classNode.methods.stream().anyMatch(mm -> mm.name.equals(mapping.from) && mm.desc.equals(mapping.desc))) {
                declaredInParent.set(true);
                // todo break
            }
        });

        return !declaredInParent.get();
    }

    public static boolean fieldNotDeclaredInParent(FieldMapping mapping, ClassHierarchyNode classHierarchyNode) {
        AtomicBoolean declaredInParent = new AtomicBoolean(false);

        iterateRecursiveParentClasses(classHierarchyNode, node -> {
            if (node.classNode.methods.stream().anyMatch(fm -> fm.name.equals(mapping.from))) {
                declaredInParent.set(true);
                // todo break
            }
        });

        return !declaredInParent.get();
    }

    public static void propagateMethodNamesToChildren(ClassHierarchyNode classHierarchyNode, MethodMapping methodMapping, JarMapping mapping) {
        iterateRecursiveChildClasses(classHierarchyNode, childNode -> {
            ClassMapping classMapping = mapping.resolveClass(childNode.classNode.name, NameType.FROM);
            classMapping.methods.put(methodMapping.from + methodMapping.desc, methodMapping);
        });
    }

    public static void propagateFieldNamesToChildren(ClassHierarchyNode classHierarchyNode, FieldMapping fieldMapping, JarMapping mapping) {
        iterateRecursiveChildClasses(classHierarchyNode, childNode -> {
            ClassMapping classMapping = mapping.resolveClass(childNode.classNode.name, NameType.FROM);
            classMapping.fields.put(fieldMapping.from, fieldMapping);
        });
    }
}
