package com.pocolifo.obfuscator.engine.passes.remapping.remapper;

import com.pocolifo.obfuscator.engine.passes.remapping.mapping.ClassMapping;
import com.pocolifo.obfuscator.engine.passes.remapping.mapping.JarMapping;
import com.pocolifo.obfuscator.engine.passes.remapping.mapping.MethodMapping;
import com.pocolifo.obfuscator.engine.passes.remapping.mapping.ParameterMapping;
import com.pocolifo.obfuscator.engine.passes.remapping.name.NameType;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.MethodRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class ParameterEnhancedMethodRemapper extends MethodRemapper {
    private final MethodMapping methodMapping;

    public ParameterEnhancedMethodRemapper(MethodVisitor methodVisitor, Remapper remapper, JarMapping mapping, ClassVisitor cv) {
        super(methodVisitor, remapper);

        ClassMapping cMapping = mapping.resolveClass(((ClassNode) cv).name, NameType.TO);
        MethodNode mn = (MethodNode) mv;

        methodMapping = cMapping.resolveMethod(mn.name, mn.desc, remapper, NameType.TO);
    }

    @Override
    public void visitParameter(String name, int access) {
        if (methodMapping == null) return;
        ParameterMapping parameterMapping = methodMapping.resolveParameter(name, NameType.FROM);

        if (parameterMapping != null) {
            super.visitParameter(parameterMapping.to, access);
        }
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        if (methodMapping == null) return;
        ParameterMapping parameterMapping = methodMapping.resolveParameter(name, NameType.FROM);

        if (parameterMapping != null) {
            super.visitLocalVariable(parameterMapping.to, descriptor, signature, start, end, index);
        }
    }
}
