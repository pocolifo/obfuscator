package com.pocolifo.obfuscator.engine.util;

import com.pocolifo.obfuscator.annotations.Pass;
import com.pocolifo.obfuscator.annotations.Passes;
import com.pocolifo.obfuscator.engine.passes.PassOptions;
import lombok.Data;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/*
 * lord forgive me
 */
public class ObfAnnotationsUtil {
    private static final String PASSES_TYPE_DESCRIPTOR = Type.getDescriptor(Passes.class);
    private static final String PASS_TYPE_DESCRIPTOR = Type.getDescriptor(Pass.class);

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
        if (annotations == null) return optionsInstance;

        for (AnnotationNode annotation : annotations) {
            if (annotation.desc.equals(PASS_TYPE_DESCRIPTOR)) {
                Map<Object, Object> keyValMap = ListUtil.toMap(annotation.values);

                // make sure that the pass is the right one
                if (!keyValMap.get("value").equals(passClass.getSimpleName())) return optionsInstance;

                // get pass options
                return getAnnotationPassOptions(annotation, optionsInstance);
            } else if (annotation.desc.equals(PASSES_TYPE_DESCRIPTOR)) {
                Map<Object, Object> keyValMap = ListUtil.toMap(annotation.values);
                List<AnnotationNode> passOptions = (List<AnnotationNode>) keyValMap.get("value");

                return getOptions(passOptions, passClass, optionsInstance);
            }
        }

        return optionsInstance;
    }

    @SuppressWarnings("unchecked")
    private static PassOptions getAnnotationPassOptions(AnnotationNode annotation, PassOptions optionsInstance) {
        Map<Object, Object> keyValMap = ListUtil.toMap(annotation.values);
        List<AnnotationNode> optionPassAnnotations = (List<AnnotationNode>) keyValMap.get("options");

        optionPassAnnotations.forEach(annotationNode -> {
            Map<Object, Object> optionsMap = ListUtil.toMap(annotationNode.values);
            System.out.println(optionsMap);

            String key = (String) optionsMap.get("key");
            List<String> val = (List<String>) optionsMap.get("value");

            setPassOption(optionsInstance, key, val);
        });

        return optionsInstance;
    }

    private static void setPassOption(PassOptions optionsInstance, String key, List<String> value) {
        try {
            Field field = optionsInstance.getClass().getField(key);
            field.setAccessible(true);

            if (value.size() == 1) {
                setPrimitiveValue(field, optionsInstance, value.get(0));
            } else {
                field.set(optionsInstance, value);
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setPrimitiveValue(Field field, Object instance, Object val) throws IllegalAccessException {
        switch (field.getType().getSimpleName().toLowerCase()) {
            case "boolean":
                field.set(instance, Boolean.valueOf((String) val));
                break;

            case "int":
                field.set(instance, Integer.parseInt((String) val));
                break;
        }
    }
}
