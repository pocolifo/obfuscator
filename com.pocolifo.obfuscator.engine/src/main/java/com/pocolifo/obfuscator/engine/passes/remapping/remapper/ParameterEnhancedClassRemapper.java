package com.pocolifo.obfuscator.engine.passes.remapping.remapper;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.ClassRemapper;

public class ParameterEnhancedClassRemapper extends ClassRemapper {
    public ParameterEnhancedClassRemapper(ClassVisitor classVisitor, JarMappingRemapper remapper) {
        super(classVisitor, remapper);
    }

    @Override
    protected MethodVisitor createMethodRemapper(MethodVisitor methodVisitor) {
        return new ParameterEnhancedMethodRemapper(methodVisitor, remapper, ((JarMappingRemapper) remapper).mapping, cv);
    }
}
