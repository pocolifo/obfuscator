package com.pocolifo.obfuscator.passes.remapping;

import com.pocolifo.obfuscator.ObfuscatorOptions;
import com.pocolifo.obfuscator.classes.ClassHierarchy;
import com.pocolifo.obfuscator.classes.ClassHierarchyNode;
import com.pocolifo.obfuscator.classes.ObfuscationClassKeeper;
import com.pocolifo.obfuscator.logger.Logging;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MappingLoader {
    private static final List<String> OBFUSCATED_STRINGS = new ArrayList<>();
    private static final List<String> EXCLUDED_METHODS = Arrays.asList(
            "main ([Ljava/lang/String;)V",
            "<init>",
            "<clinit>"
    );

    private static String getUniqueObfuscatedString() {
        String s = getObfuscatedString();

        if (OBFUSCATED_STRINGS.contains(s)) {
            return getUniqueObfuscatedString();
        } else {
            OBFUSCATED_STRINGS.add(s);
            return s;
        }
    }

    private static String getObfuscatedString() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; 24 > i; i++) {
            if (ThreadLocalRandom.current().nextBoolean()) {
                builder.append("I");
            } else {
                builder.append("l");
            }
        }

        return builder.toString();
    }

    public static JarMapping generateMapping(ClassHierarchy hierarchy, ObfuscationClassKeeper classKeeper, ObfuscatorOptions.RemapOptions options) {
        JarMapping mapping = new JarMapping();

        for (ClassNode cls : classKeeper.inputClasses) {
            doClass(options, cls, mapping);
        }

        if (options.isRemapFieldNames()) {
            for (ClassMapping cls : mapping.classes.values()) {
                for (FieldNode fd : cls.node.fields) {
                    doField(fd, cls);
                }
            }
        }

        if (options.isRemapMethodNames()) {
            for (ClassMapping cls : mapping.classes.values()) {
                for (MethodNode md : cls.node.methods) {
                    boolean exclude = EXCLUDED_METHODS.stream().anyMatch(s -> (md.name + " " + md.desc).startsWith(s));

                    if (!exclude) doMethod(cls, md, hierarchy, mapping);
                }
            }
        }

        // update child field names
        for (ClassNode cls : classKeeper.inputClasses) {
            ClassHierarchyNode hNode = hierarchy.find(cls.name);
            ClassMapping cMapping = mapping.resolveClass(cls.name);

            for (ClassHierarchyNode child : hNode.children) {
                ClassMapping childCMapping = mapping.resolveClass(child.classNode.name);

                childCMapping.fields.putAll(cMapping.fields);
            }
        }

        // update nested class names
        for (ClassNode cls : classKeeper.inputClasses) {
            ClassMapping cMapping = mapping.resolveClass(cls.name);
            ClassName className = new ClassName(cMapping.to);

            if (className.parentClass != null && !className.anonymousClass) {
                ClassMapping parentMapping = mapping.resolveClass(className.parentClass.toString());
                className.parentClass = new ClassName(parentMapping.to);

                cMapping.to = className.toString();
            }
        }


        return mapping;
    }

    private static void doMethod(ClassMapping cMapping, MethodNode md, ClassHierarchy hierarchy, JarMapping mapping) {
        if (cMapping.methods.containsKey(md.name)) return;

        MethodName methodName = new MethodName(new ClassName(cMapping.to), md.name, md.desc);
        ClassHierarchyNode chn = hierarchy.find(cMapping.from);
        MethodMapping mMapping = new MethodMapping();

        if (chn == null) {
            Logging.fatal(cMapping.from + " not found on hierarchy! Did you include all of your JARs (dependencies, libraries)?");
            return;
        }

        // check if this method is overridden
        for (ClassHierarchyNode parent : chn.parents) {
            ClassMapping parentMapping = mapping.resolveClass(parent.classNode.name);
            if (parentMapping == null) {
                if (parent.classNode.methods.stream().anyMatch(node -> node.name.equals(md.name) && node.desc.equals(md.desc))) {
                    return;
                }
            } else if (parentMapping.methods.containsKey(md.name)) return;
        }

        methodName.methodName = getUniqueObfuscatedString();


        mMapping.from = md.name;
        mMapping.to = methodName.toString();
        mMapping.node = md;
        mMapping.desc = md.desc;

        addMethodsToAllChildren(chn, mapping, mMapping);

        cMapping.addMethodMapping(mMapping);
    }

    private static void addMethodsToAllChildren(ClassHierarchyNode node, JarMapping mapping, MethodMapping mMapping) {
        for (ClassHierarchyNode child : node.children) {
            ClassMapping childCMapping = mapping.resolveClass(child.classNode.name);
            childCMapping.methods.put(mMapping.from, mMapping);

            if (!child.children.isEmpty()) addMethodsToAllChildren(child, mapping, mMapping);
        }
    }

    private static void doField(FieldNode fd, ClassMapping cMapping) {
        FieldName fieldName = new FieldName(new ClassName(cMapping.to), fd.name);
        fieldName.fieldName = getUniqueObfuscatedString();

        FieldMapping fMapping = new FieldMapping();

        fMapping.from = fd.name;
        fMapping.to = fieldName.toString();
        fMapping.node = fd;

        cMapping.addFieldMapping(fMapping);
    }

    private static void doClass(ObfuscatorOptions.RemapOptions options, ClassNode cls, JarMapping mapping) {
        ClassMapping cMapping = new ClassMapping();
        ClassName className = new ClassName(cls.name);

        if (options.isRemapClassNames()) {
            className.className = getUniqueObfuscatedString();
            cMapping.from = cls.name;
            cMapping.to = className.toString();
        }

        cMapping.node = cls;
        mapping.addClassMapping(cMapping);
    }
}
