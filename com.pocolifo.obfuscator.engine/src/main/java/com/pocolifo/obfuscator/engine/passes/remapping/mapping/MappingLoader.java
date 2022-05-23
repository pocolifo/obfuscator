package com.pocolifo.obfuscator.engine.passes.remapping.mapping;

import com.pocolifo.obfuscator.engine.classes.ClassHierarchy;
import com.pocolifo.obfuscator.engine.classes.ClassHierarchyNode;
import com.pocolifo.obfuscator.engine.classes.ObfuscationClassKeeper;
import com.pocolifo.obfuscator.engine.passes.remapping.RemapNamesPass;
import com.pocolifo.obfuscator.engine.passes.remapping.name.ClassName;
import com.pocolifo.obfuscator.engine.passes.remapping.name.NameType;
import com.pocolifo.obfuscator.engine.util.MixinUtil;
import com.pocolifo.obfuscator.engine.util.ObfAnnotationsUtil;
import com.pocolifo.obfuscator.engine.util.obfstring.ObfuscatedStringSupplier;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static com.pocolifo.obfuscator.engine.util.RemappingUtil.*;

public class MappingLoader implements Serializable {
    private final List<String> obfuscatedStrings = new ArrayList<>();
    private final ObfuscatedStringSupplier obfuscatedStringProvider;

    public MappingLoader(ObfuscatedStringSupplier obfuscatedStringProvider) {
        this.obfuscatedStringProvider = obfuscatedStringProvider;
    }

    public String getUniqueObfuscatedString() {
        while (true) {
            String s = this.obfuscatedStringProvider.get();

            if (!obfuscatedStrings.contains(s)) {
                obfuscatedStrings.add(s);
                return s;
            }
        }
    }

    private static <T> Stream<T> getAppropriateStreamForSize(Collection<T> ts) {
        // parallelStream only if big
        return ts.size() > 100 ? ts.parallelStream() : ts.stream();
    }

    private void createMappingBase(JarMapping mapping, ObfuscationClassKeeper classKeeper) {
        // create the mapping base
        // basically set up for mapping obfuscation
        getAppropriateStreamForSize(classKeeper.inputClasses).forEach(cls -> {
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
    }

    private void obfuscateField(FieldMapping fieldMapping, ClassHierarchyNode classHierarchyNode, JarMapping mapping, RemapNamesPass.Options options) {
        // can only be obfuscated if:
        //  - not declared in parent (mapping will be propagated later)
        //  - remapFieldNames is enabled
        //  - field isn't excluded
        //  - isn't a mixin field
        boolean canBeObfuscated = fieldNotDeclaredInParent(fieldMapping, classHierarchyNode) &&
                options.remapFieldNames &&
                !options.excludedFields.contains(fieldMapping.from) &&
                !MixinUtil.isMixinField(fieldMapping.node); // todo add check if the method is inside the mapping

        // obfuscate it
        if (canBeObfuscated) {
            fieldMapping.to = getUniqueObfuscatedString();
            propagateFieldNamesToChildren(classHierarchyNode, fieldMapping, mapping);
        } else {
            fieldMapping.to = fieldMapping.from;
        }
    }

    private void obfuscateMethod(MethodMapping methodMapping, ClassHierarchyNode classHierarchyNode, JarMapping mapping, RemapNamesPass.Options options) {
        boolean excludedFromObfuscation =
                options.excludedMethods.contains(methodMapping.from) || // from mapping
                        options.excludedMethods.contains(methodMapping.desc) || // descriptor
                        options.excludedMethods.contains(methodMapping.from + " " + methodMapping.desc); // name + descriptor

        // can only be obfuscated if:
        //  - not declared in parent (mapping will be propagated later)
        //  - remapMethodNames is enabled
        //  - not excluded
        //  - isn't a mixin method
        boolean canBeObfuscated = methodNotDeclaredInParent(methodMapping, classHierarchyNode) &&
                options.remapMethodNames &&
                !excludedFromObfuscation &&
                !MixinUtil.isMixinMethod(methodMapping.node); // todo add check if the method is inside the mapping

        // obfuscate it
        if (canBeObfuscated) {
            methodMapping.to = getUniqueObfuscatedString();
            propagateMethodNamesToChildren(classHierarchyNode, methodMapping, mapping);
        } else {
            methodMapping.to = methodMapping.from;
        }

        // obfuscate parameter names if enabled
        for (ParameterMapping parameterMapping : methodMapping.parameterMappings) {
            parameterMapping.to = options.remapMethodParameterNames ? getUniqueObfuscatedString() : parameterMapping.from;
        }
    }

    private void obfuscateClass(ClassHierarchyNode classHierarchyNode, RemapNamesPass.Options options, JarMapping mapping, ClassMapping classMapping, List<ClassMapping> fixNestedClasses) {
        boolean canBeObfuscated = options.remapClassNames && !options.excludedClasses.contains(classMapping.from);

        if (canBeObfuscated) {
            // obfuscate the class name
            ClassName className = new ClassName(classMapping.from);

            // fix anonymous class stuff
            if (className.anonymousClass) {
                fixNestedClasses.add(classMapping);
            } else {
                className.className = getUniqueObfuscatedString();

                // parent class stuff fix
                if (className.parentClass != null) {
                    fixNestedClasses.add(classMapping);
                }
            }

            classMapping.to = className.toString();
        } else {
            classMapping.to = classMapping.from;
        }

        // obfuscate fields inside the class
        for (FieldMapping fieldMapping : classMapping.fields.values()) {
            // precondition to make sure that the field hasn't been obfuscated already
            if (fieldMapping.to == null)
                obfuscateField(fieldMapping,
                        classHierarchyNode,
                        mapping,
                        (RemapNamesPass.Options) ObfAnnotationsUtil.getOptions(fieldMapping.node, classMapping.node, RemapNamesPass.class, options));
        }

        // obfuscate methods inside the class
        for (MethodMapping methodMapping : classMapping.methods.values()) {
            // precondition to make sure that the method hasn't been obfuscated already
            if (methodMapping.to == null)
                obfuscateMethod(methodMapping,
                        classHierarchyNode,
                        mapping,
                        (RemapNamesPass.Options) ObfAnnotationsUtil.getOptions(methodMapping.node, classMapping.node, RemapNamesPass.class, options));
        }
    }

    private void fixNestedClasses(JarMapping mapping, List<ClassMapping> fixNestedClasses) {
        for (ClassMapping classMapping : fixNestedClasses) {
            ClassName className = new ClassName(classMapping.to);
            ClassMapping parentClass = mapping.resolveClass(className.parentClass.toString(), NameType.FROM);

            if (parentClass == null) throw new RuntimeException("null parent: " + className);

            className.parentClass = new ClassName(parentClass.to);
            classMapping.to = className.toString();
        }
    }

    private void obfuscateMapping(ClassHierarchy hierarchy, RemapNamesPass.Options defaultOptions, JarMapping mapping) {
        final List<ClassMapping> fixNestedClasses = new ArrayList<>();

        getAppropriateStreamForSize(mapping.classes.values()).forEach(classMapping -> {
            RemapNamesPass.Options options = (RemapNamesPass.Options) ObfAnnotationsUtil.getOptions(classMapping.node, RemapNamesPass.class, defaultOptions);
            System.out.println(classMapping.from + " : " + options.remapClassNames);

            ClassHierarchyNode classHierarchyNode = hierarchy.find(classMapping.from);

            obfuscateClass(classHierarchyNode, options, mapping, classMapping, fixNestedClasses);
        });

        // update nested class names
        fixNestedClasses(mapping, fixNestedClasses);
    }

    public JarMapping generateMapping(ClassHierarchy hierarchy, ObfuscationClassKeeper classKeeper, RemapNamesPass.Options defaultOptions) {
        JarMapping mapping = new JarMapping();

        createMappingBase(mapping, classKeeper);
        obfuscateMapping(hierarchy, defaultOptions, mapping);

        return mapping;
    }
}
