package com.pocolifo.obfuscator.passes.remapping;

import com.pocolifo.obfuscator.ObfuscatorOptions;
import com.pocolifo.obfuscator.classes.ClassHierarchy;
import com.pocolifo.obfuscator.classes.ClassHierarchyNode;
import com.pocolifo.obfuscator.classes.ObfuscationClassKeeper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class MappingLoader {
    private static final List<String> OBFUSCATED_STRINGS = new ArrayList<>();
    private static final List<String> EXCLUDED_METHODS = Arrays.asList(
            "main([Ljava/lang/String;)V",
            "<init>",
            "<clinit>"
    );

    private static final List<String> EXCLUDED_FIELDS = Collections.singletonList(
            "serialVersionUID"
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

    public static JarMapping generateMapping(ClassHierarchy hierarchy, ObfuscationClassKeeper classKeeper, RemapNamesPass.Options options) {
        JarMapping mapping = new JarMapping();

        // create the mapping
        for (ClassNode cls : classKeeper.inputClasses) {
            ClassMapping classMapping = new ClassMapping();

            classMapping.node = cls;
            classMapping.from = cls.name;
            classMapping.to = null; // it's null because it hasn't been remapped yet

            for (MethodNode method : cls.methods) {
                MethodMapping methodMapping = new MethodMapping();

                methodMapping.node = method;
                methodMapping.desc = method.desc;
                methodMapping.from = method.name;
                methodMapping.to = null; // it's null because it hasn't been remapped yet

                if (method.parameters != null) {
                    for (ParameterNode parameter : method.parameters) {
                        ParameterMapping parameterMapping = new ParameterMapping();

                        parameterMapping.node = parameter;
                        parameterMapping.methodNode = method;
                        parameterMapping.from = parameter.name;
                        parameterMapping.to = null; // it's null because it hasn't been remapped yet

                        methodMapping.parameterMappings.add(parameterMapping);
                    }
                }

                classMapping.addMethodMapping(methodMapping);
            }

            for (FieldNode field : cls.fields) {
                FieldMapping fieldMapping = new FieldMapping();

                fieldMapping.node = field;
                fieldMapping.from = field.name;
                fieldMapping.to = null; // it's null because it hasn't been remapped yet

                classMapping.addFieldMapping(fieldMapping);
            }

            mapping.addClassMapping(classMapping);
        }


        // obfuscate the mapping
        List<ClassMapping> fixNestedClasses = new ArrayList<>();

        for (ClassMapping classMapping : mapping.classes.values()) {
            ClassHierarchyNode classHierarchyNode = hierarchy.find(classMapping.from);

            if (options.remapClassNames) {
                ClassName className = new ClassName(classMapping.from);

                if (className.anonymousClass) {
                    fixNestedClasses.add(classMapping);
                } else {
                    className.className = getUniqueObfuscatedString();

                    if (className.parentClass != null) {
                        fixNestedClasses.add(classMapping);
                    }
                }

                classMapping.to = className.toString();
            } else {
                classMapping.to = classMapping.from;
            }

            for (FieldMapping fieldMapping : classMapping.fields.values()) {
                if (fieldMapping.to != null) continue;

                if (fieldNotDeclaredInParent(fieldMapping, classHierarchyNode) && options.remapFieldNames && !EXCLUDED_FIELDS.contains(fieldMapping.from)) {
                    fieldMapping.to = getUniqueObfuscatedString();
                    propagateFieldNamesToChildren(classHierarchyNode, fieldMapping, mapping);
                } else {
                    fieldMapping.to = fieldMapping.from;
                }
            }

            for (MethodMapping methodMapping : classMapping.methods.values()) {
                if (methodMapping.to != null) continue;
                boolean excludedFromObfuscation = EXCLUDED_METHODS.contains(methodMapping.from) || EXCLUDED_METHODS.contains(methodMapping.from + methodMapping.desc);

                if (methodNotDeclaredInParent(methodMapping, classHierarchyNode) && options.remapMethodNames && !excludedFromObfuscation) {
                    methodMapping.to = getUniqueObfuscatedString();
                    propagateMethodNamesToChildren(classHierarchyNode, methodMapping, mapping);
                } else {
                    methodMapping.to = methodMapping.from;
                }

                for (ParameterMapping parameterMapping : methodMapping.parameterMappings) {
                    parameterMapping.to = options.remapMethodParameterNames ? getUniqueObfuscatedString() : parameterMapping.from;
                }
            }
        }

        // update nested class names
        for (ClassMapping classMapping : fixNestedClasses) {
            ClassName className = new ClassName(classMapping.to);
            ClassMapping parentClass = mapping.resolveClass(className.parentClass.toString(), NameType.FROM);

            className.parentClass = new ClassName(parentClass.to);
            classMapping.to = className.toString();
        }

        return mapping;
    }

    private static boolean methodNotDeclaredInParent(MethodMapping mapping, ClassHierarchyNode classHierarchyNode) {
        AtomicBoolean declaredInParent = new AtomicBoolean(false);

        iterateRecursiveParentClasses(classHierarchyNode, node -> {
            if (node.classNode.methods.stream().anyMatch(mm -> mm.name.equals(mapping.from) && mm.desc.equals(mapping.desc))) {
                declaredInParent.set(true);
                // todo break
            }
        });

        return !declaredInParent.get();
    }

    private static boolean fieldNotDeclaredInParent(FieldMapping mapping, ClassHierarchyNode classHierarchyNode) {
        AtomicBoolean declaredInParent = new AtomicBoolean(false);

        iterateRecursiveParentClasses(classHierarchyNode, node -> {
            if (node.classNode.methods.stream().anyMatch(fm -> fm.name.equals(mapping.from))) {
                declaredInParent.set(true);
                // todo break
            }
        });

        return !declaredInParent.get();
    }

    private static void propagateMethodNamesToChildren(ClassHierarchyNode classHierarchyNode, MethodMapping methodMapping, JarMapping mapping) {
        iterateRecursiveChildClasses(classHierarchyNode, childNode -> {
            ClassMapping classMapping = mapping.resolveClass(childNode.classNode.name, NameType.FROM);
            classMapping.methods.put(methodMapping.from + methodMapping.desc, methodMapping);
        });
    }

    private static void propagateFieldNamesToChildren(ClassHierarchyNode classHierarchyNode, FieldMapping fieldMapping, JarMapping mapping) {
        iterateRecursiveChildClasses(classHierarchyNode, childNode -> {
            ClassMapping classMapping = mapping.resolveClass(childNode.classNode.name, NameType.FROM);
            classMapping.fields.put(fieldMapping.from, fieldMapping);
        });
    }

    private static void iterateRecursiveParentClasses(ClassHierarchyNode starting, Consumer<ClassHierarchyNode> nodeSupplier) {
        if (starting == null) return;

        for (ClassHierarchyNode parent : starting.parents) {
            if (parent == null) continue;
            nodeSupplier.accept(parent);

            iterateRecursiveParentClasses(parent, nodeSupplier);
        }
    }

    private static void iterateRecursiveChildClasses(ClassHierarchyNode starting, Consumer<ClassHierarchyNode> nodeSupplier) {
        if (starting == null) return;

        for (ClassHierarchyNode child : starting.children) {
            if (child == null) continue;
            nodeSupplier.accept(child);

            iterateRecursiveChildClasses(child, nodeSupplier);
        }
    }

//    private static void doMethod(ClassMapping cMapping, MethodNode md, ClassHierarchy hierarchy, JarMapping mapping) {
//        boolean shouldObfuscate = EXCLUDED_METHODS.stream().noneMatch(s -> (md.name + " " + md.desc).startsWith(s));
//
//        ClassHierarchyNode parentClassNode = hierarchy.find(cMapping.from);
//        MethodMapping methodMapping = new MethodMapping();
//        MethodName methodName = new MethodName(new ClassName(cMapping.to), md.name, md.desc);
//
//        boolean existsInParent = false;
//        boolean existsInChild = false;
//
//        if (parentClassNode == null) {
//            Logging.fatal(cMapping.from + " not found on hierarchy! Did you include all of your JARs (dependencies, libraries)?");
//            return;
//        } else if (shouldObfuscate) {
//            // search for method
//            MethodMapping methodInParent = searchAllParentsForMethod(parentClassNode, md.name, md.desc, mapping);
//
//            existsInParent = methodInParent != null;
//            existsInChild = searchAllChildrenForMethod(parentClassNode, md.name, md.desc, mapping);
//
//            System.out.println(cMapping.from + " " + md.name  + md.desc + ": c=" + existsInChild + " p=" + existsInParent);
//
//            if (!existsInParent && existsInChild) {
//                methodName.methodName = getUniqueObfuscatedString();
//            }
//
////            if (existsInParent) {
////                methodName.methodName = methodInParent.to;
////            } else {
////
////            }
//        }
//
//        methodMapping.from = md.name;
//        methodMapping.to = methodName.toString();
//        methodMapping.desc = md.desc;
//        methodMapping.node = md;
//
//        if (!existsInParent && existsInChild) {
//            propagateMethodNameToChildren(parentClassNode, mapping, methodMapping);
//        } else {
//            cMapping.methods.remove(methodMapping.from + methodMapping.desc);
//            cMapping.addMethodMapping(methodMapping);
//        }
//    }
//
//    private static MethodMapping searchAllParentsForMethod(ClassHierarchyNode node, String name, String desc, JarMapping mapping) {
//        for (ClassHierarchyNode parent : node.parents) {
//            MethodMapping methodMapping;
//
//            for (MethodNode method : parent.classNode.methods) {
//                if (node.classNode.name.equals("me/youngermax/javachess/pieces/AbstractPiece")) {
//                    System.out.println("looking in " + parent.classNode.name + " @ " + method.name + method.desc);
//                }
//
//                methodMapping = checkMethod(method, parent, name, desc, mapping);
//
//                if (methodMapping != null) return methodMapping;
//            }
//
//            methodMapping = searchAllParentsForMethod(parent, name, desc, mapping);
//
//            if (methodMapping != null) return methodMapping;
//        }
//
//        return null;
//    }
//
//    private static boolean searchAllChildrenForMethod(ClassHierarchyNode node, String name, String desc, JarMapping mapping) {
//        for (ClassHierarchyNode child : node.children) {
//            if (child.classNode.methods.stream().anyMatch(methodNode -> checkMethod(methodNode, child, name, desc, mapping) != null)) {
//                return true;
//            } else {
//                return searchAllChildrenForMethod(child, name, desc, mapping);
//            }
//        }
//
//        return false;
//    }
//
//    private static MethodMapping checkMethod(MethodNode m, ClassHierarchyNode node, String name, String desc, JarMapping mapping) {
//        ClassMapping classMapping = mapping.resolveClass(node.classNode.name, NameType.EITHER);
//
//        if (classMapping == null) {
//            return null;
//        }
//
//        if (m.name.equals(name) && m.desc.equals(desc)) {
//            return classMapping.resolveMethod(name, desc, NameType.EITHER);
//        }
//
//        return classMapping.methods.get(name + desc);
//    }
//
//    private static void propagateMethodNameToChildren(ClassHierarchyNode node, JarMapping mapping, MethodMapping methodMapping) {
//        ClassMapping classMapping = mapping.resolveClass(node.classNode.name, NameType.FROM);
//
//        while (classMapping.methods.containsKey(methodMapping.from + methodMapping.desc))
//            classMapping.methods.remove(methodMapping.from + methodMapping.desc);
//
//        classMapping.addMethodMapping(methodMapping);
//
//        for (ClassHierarchyNode child : node.children) {
//            propagateMethodNameToChildren(child, mapping, methodMapping);
//        }
//    }
//
//    private static void doField(FieldNode fd, ClassMapping cMapping) {
//        FieldName fieldName = new FieldName(new ClassName(cMapping.to), fd.name);
//        fieldName.fieldName = getUniqueObfuscatedString();
//
//        FieldMapping fMapping = new FieldMapping();
//
//        fMapping.from = fd.name;
//        fMapping.to = fieldName.toString();
//        fMapping.node = fd;
//
//        cMapping.addFieldMapping(fMapping);
//    }
//
//    private static void doClass(ObfuscatorOptions.RemapOptions options, ClassNode cls, JarMapping mapping) {
//        ClassMapping cMapping = new ClassMapping();
//        ClassName className = new ClassName(cls.name);
//
//        if (options.isRemapClassNames()) {
//            className.className = getUniqueObfuscatedString();
//            cMapping.from = cls.name;
//            cMapping.to = className.toString();
//        }
//
//        cMapping.node = cls;
//        mapping.addClassMapping(cMapping);
//    }
}
