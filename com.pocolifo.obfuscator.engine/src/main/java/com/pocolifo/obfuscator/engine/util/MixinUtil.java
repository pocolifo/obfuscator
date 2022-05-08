package com.pocolifo.obfuscator.engine.util;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.List;

public class MixinUtil {
    private static final String MIXIN_ANNOTATION_DESC = "Lorg/spongepowered/asm/mixin/Mixin;";

    private static final List<String> MIXIN_ANNOTATIONS_FIELD = Arrays.asList(
            "Lorg/spongepowered/asm/mixin/Shadow;",
            "Lorg/spongepowered/asm/mixin/Final;"
    );

    private static final List<String> MIXIN_ANNOTATIONS_METHOD = Arrays.asList(
            "Lorg/spongepowered/asm/mixin/Shadow;",
            "Lorg/spongepowered/asm/mixin/Final;",
            "Lorg/spongepowered/asm/mixin/Overwrite;",
            "Lorg/spongepowered/asm/mixin/gen/Accessor;",
            "Lorg/spongepowered/asm/mixin/gen/Invoker;"
    );

    public static boolean isAnnotatedWith(String desc, FieldNode node) {
        if (node == null) return false;

        return checkAnnotatedWithSafe(desc, node.visibleAnnotations) ||
                checkAnnotatedWithSafe(desc, node.invisibleAnnotations);
    }

    public static boolean isAnnotatedWith(String desc, MethodNode node) {
        if (node == null) return false;

        return checkAnnotatedWithSafe(desc, node.visibleAnnotations) ||
                checkAnnotatedWithSafe(desc, node.invisibleAnnotations);
    }

    public static boolean isMixinField(FieldNode node) {
        return MIXIN_ANNOTATIONS_FIELD.stream().anyMatch(s -> isAnnotatedWith(s, node));
    }

    public static boolean isMixinMethod(MethodNode node) {
        return MIXIN_ANNOTATIONS_METHOD.stream().anyMatch(s -> isAnnotatedWith(s, node));
    }

    private static boolean checkAnnotatedWithSafe(String desc, List<AnnotationNode> nodes) {
        if (nodes == null) return false;

        return isAnnotatedWith(desc, nodes);
    }

    private static boolean isAnnotatedWith(String desc, List<AnnotationNode> nodes) {
        for (AnnotationNode node : nodes) {
            if (node.desc.equals(desc)) return true;
        }

        return false;
    }

    private static String getMixingInClass(ClassNode node) {
        // todo
        AnnotationNode mixinAnnotation = node.visibleAnnotations.stream()
                .filter(annotationNode -> annotationNode.desc.equals(MIXIN_ANNOTATION_DESC))
                .findFirst()
                .orElseGet(() -> node.invisibleAnnotations.stream()
                        .filter(annotationNode -> annotationNode.desc.equals(MIXIN_ANNOTATION_DESC))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException(String.format("%s is not a mixin class", node.name))));

        mixinAnnotation.values.forEach(System.out::println);

        return "";
    }
}
