package com.pocolifo.obfuscator.engine.passes.remapping.mapping;

import com.pocolifo.obfuscator.engine.classes.ClassHierarchy;
import com.pocolifo.obfuscator.engine.classes.ClassHierarchyNode;
import com.pocolifo.obfuscator.engine.classes.ObfuscationClassKeeper;
import com.pocolifo.obfuscator.engine.passes.remapping.RemapNamesPass;
import com.pocolifo.obfuscator.engine.passes.remapping.name.ClassName;
import com.pocolifo.obfuscator.engine.passes.remapping.name.NameType;
import com.pocolifo.obfuscator.engine.util.ObfAnnotationsUtil;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.pocolifo.obfuscator.engine.util.RemappingUtil.*;

public class MappingLoader {
    private static final List<String> OBFUSCATED_STRINGS = new ArrayList<>();

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
                builder.append('\u0406');
            }
        }

        return builder.toString();
    }

    public static JarMapping generateMapping(ClassHierarchy hierarchy, ObfuscationClassKeeper classKeeper, RemapNamesPass.Options defaultOptions) {
        JarMapping mapping = new JarMapping();

        // create the mapping
        classKeeper.inputClasses.parallelStream().forEach(cls -> {
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
        });


        // obfuscate the mapping
        final List<ClassMapping> fixNestedClasses = new ArrayList<>();

        mapping.classes.values().parallelStream().forEach(classMapping -> {
            RemapNamesPass.Options options;

            ClassHierarchyNode classHierarchyNode = hierarchy.find(classMapping.from);
            options = (RemapNamesPass.Options) ObfAnnotationsUtil.getOptions(classMapping.node, RemapNamesPass.class, defaultOptions);

            if (options.remapClassNames && !options.excludedClasses.contains(classMapping.from)) {
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

                options = (RemapNamesPass.Options) ObfAnnotationsUtil.getOptions(fieldMapping.node, classMapping.node, RemapNamesPass.class, defaultOptions);

                if (fieldNotDeclaredInParent(fieldMapping, classHierarchyNode) && options.remapFieldNames &&
                        !options.excludedFields.contains(fieldMapping.from)) {
                    fieldMapping.to = getUniqueObfuscatedString();
                    propagateFieldNamesToChildren(classHierarchyNode, fieldMapping, mapping);
                } else {
                    fieldMapping.to = fieldMapping.from;
                }
            }

            for (MethodMapping methodMapping : classMapping.methods.values()) {
                if (methodMapping.to != null) continue;

                options = (RemapNamesPass.Options) ObfAnnotationsUtil.getOptions(methodMapping.node, classMapping.node, RemapNamesPass.class, defaultOptions);

                boolean excludedFromObfuscation =
                        options.excludedMethods.contains(methodMapping.from) || // from mapping
                                options.excludedMethods.contains(methodMapping.desc) || // descriptor
                                options.excludedMethods.contains(methodMapping.from + " " + methodMapping.desc); // name + descriptor

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
        });

        // update nested class names
        for (ClassMapping classMapping : fixNestedClasses) {
            ClassName className = new ClassName(classMapping.to);
            ClassMapping parentClass = mapping.resolveClass(className.parentClass.toString(), NameType.FROM);

            if (parentClass == null) throw new RuntimeException("null parent: " + className.toString());

            className.parentClass = new ClassName(parentClass.to);
            classMapping.to = className.toString();
        }

        return mapping;
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
