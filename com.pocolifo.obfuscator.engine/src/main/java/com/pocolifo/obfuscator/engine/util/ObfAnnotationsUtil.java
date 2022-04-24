package com.pocolifo.obfuscator.engine.util;

import com.pocolifo.obfuscator.engine.passes.PassOptions;
import lombok.Data;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ObfAnnotationsUtil {
    public static PassOptions getOptions(ClassNode node, Class<?> passClass, PassOptions optionsInstance) {
        return getOptions(ListUtil.join(node.visibleAnnotations, node.invisibleAnnotations), passClass, optionsInstance);
    }

    public static PassOptions getOptions(FieldNode node, ClassNode clsNode, Class<?> passClass, PassOptions optionsInstance) {
        return getOptions(ListUtil.join(node.visibleAnnotations, node.invisibleAnnotations), passClass, getOptions(clsNode, passClass, optionsInstance));
    }

    public static PassOptions getOptions(MethodNode node, ClassNode clsNode, Class<?> passClass, PassOptions optionsInstance) {
        return getOptions(ListUtil.join(node.visibleAnnotations, node.invisibleAnnotations), passClass, getOptions(clsNode, passClass, optionsInstance));
    }

    @SuppressWarnings("unchecked")
    private static PassOptions getOptions(List<AnnotationNode> annotations, Class<?> passClass, PassOptions optionsInstance) {
        if (annotations != null) {
            for (AnnotationNode a : annotations) {
                if (a.desc.equals("Lcom/pocolifo/obfuscator/annotations/Passes;")) {
                    List<AnnotationNode> nodes = (List<AnnotationNode>) a.values.get(1);

                    for (AnnotationNode annotationNode : nodes) {
                        AnnotationPassOptions aops = getOptions(passClass.getSimpleName(), annotationNode, optionsInstance);

                        if (aops != null) return aops.options;
                    }
                } else if (a.desc.equals("Lcom/pocolifo/obfuscator/annotations/Pass;")) {
                    AnnotationPassOptions aops = getOptions(passClass.getSimpleName(), a, optionsInstance);

                    if (aops != null) return aops.options;
                }
            }
        }

        return optionsInstance;
    }

    @SuppressWarnings("unchecked")
    private static AnnotationPassOptions getOptions(String expectedPassName, AnnotationNode passAnnotation, PassOptions optionsInstance) {
        AnnotationPassOptions aops = new AnnotationPassOptions();
        aops.setOptions(optionsInstance);

        Map<String, Object> map = toMap(passAnnotation.values);
        aops.setPassName((String) map.get("value"));

        if (!aops.passName.equals(expectedPassName)) return null;

        for (AnnotationNode option : (List<AnnotationNode>) map.get("options")) {
            Map<String, Object> optionMap = toMap(option.values);

            String optionName = (String) optionMap.get("key");
            List<String> optionValue = (List<String>) optionMap.get("value");

            Class<? extends PassOptions> optsClass = optionsInstance.getClass();

            try {
                Field field = optsClass.getField(optionName);

                if (optionValue.size() == 1) {
                    String val = optionValue.get(0);

                    switch (field.getType().getSimpleName().toLowerCase()) {
                        case "boolean":
                            field.set(optionsInstance, Boolean.valueOf(val));
                            break;

                        case "int":
                            field.set(optionsInstance, Integer.parseInt(val));
                            break;
                    }
                } else {
                    field.set(optionsInstance, optionValue);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return aops;
    }

    private static Map<String, Object> toMap(List<Object> list) {
        HashMap<String, Object> map = new HashMap<>();

        for (int i = 0; list.size() > i; i += 2) {
            map.put((String) list.get(i), list.get(i + 1));
        }

        return map;
    }

    @Data
    private static class AnnotationPassOptions {
        public String passName;
        public PassOptions options;
    }
}
