package com.pocolifo.obfuscator.classes;

import com.pocolifo.obfuscator.logger.Logging;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassHierarchy {
    public ClassHierarchyNode root;

    public void loadClassIntoHierarchy(ClassHierarchyNode target, Map<String, ClassHierarchyNode> nodes, Set<String> missingClasses) {
        List<String> parents = new ArrayList<>();
        parents.add(target.classNode.superName);
        parents.addAll(target.classNode.interfaces);

        for (String parent : parents) {
            if (parent == null) continue;

            ClassHierarchyNode chn = nodes.get(parent);

            if (chn == null) {
                missingClasses.add(parent);
            } else {
                chn.children.add(target);
            }
        }
    }

    private void indent(int level, PrintStream stream) {
        for (int i = 0; level > i; i++) {
            stream.print("   ");
        }
    }

    private void printOut(PrintStream stream, ClassHierarchyNode node, int indent) {
        indent(indent, stream);
        stream.print("> ");
        stream.println(node.classNode.name);

        node.children.forEach(child -> {
            printOut(stream, child, indent + 1);
        });
    }

    public void printOut(PrintStream stream) {
        printOut(stream, root, 0);
    }

    private ClassHierarchyNode find(ClassHierarchyNode node, String className) {
        for (ClassHierarchyNode child : node.children) {
            ClassHierarchyNode found;

            if (child.classNode.name.equals(className)) {
                return child;
            } else if ((found = find(child, className)) != null) {
                return found;
            }
        }

        return null;
    }

    public ClassHierarchyNode find(String className) {
        return find(root, className);
    }
}
