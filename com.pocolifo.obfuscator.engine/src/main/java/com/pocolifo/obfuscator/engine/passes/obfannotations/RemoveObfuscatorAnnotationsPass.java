package com.pocolifo.obfuscator.engine.passes.obfannotations;

import com.pocolifo.obfuscator.annotations.Pass;
import com.pocolifo.obfuscator.annotations.PassOption;
import com.pocolifo.obfuscator.annotations.Passes;
import com.pocolifo.obfuscator.engine.ObfuscatorEngine;
import com.pocolifo.obfuscator.engine.passes.ClassPass;
import com.pocolifo.obfuscator.engine.passes.PassOptions;
import com.pocolifo.obfuscator.engine.util.ProgressUtil;
import lombok.Getter;
import me.tongfei.progressbar.ProgressBar;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class RemoveObfuscatorAnnotationsPass implements ClassPass<PassOptions> {
    @Getter public PassOptions options = new PassOptions();

    @Override
    public Collection<ClassNode> run(ObfuscatorEngine engine, Collection<ClassNode> inClasses) {
        try (ProgressBar bar = ProgressUtil.bar("Removing obfuscator annotations", inClasses.size())) {
            for (ClassNode cls : inClasses) {
                doAnnotationList(cls.visibleAnnotations);
                doAnnotationList(cls.invisibleAnnotations);

                for (FieldNode field : cls.fields) {
                    doAnnotationList(field.visibleAnnotations);
                    doAnnotationList(field.invisibleAnnotations);
                }

                for (MethodNode method : cls.methods) {
                    doAnnotationList(method.visibleAnnotations);
                    doAnnotationList(method.invisibleAnnotations);
                }
            }

            bar.step();
        }

        return inClasses;
    }

    private static void doAnnotationList(List<AnnotationNode> list) {
        if (list == null) return;

        for (Iterator<AnnotationNode> iterator = list.iterator(); iterator.hasNext(); ) {
            AnnotationNode a = iterator.next();

            if (a.desc.equals(getDescriptorName(Pass.class)) || a.desc.equals(getDescriptorName(Passes.class))) {
                iterator.remove();
            }
        }
    }

    private static String getDescriptorName(Class<?> cls) {
        return "L" + cls.getCanonicalName().replaceAll("\\.", "/") + ";";
    }
}
