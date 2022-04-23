package com.pocolifo.obfuscator.classes;

import org.objectweb.asm.ClassWriter;

public class ObfuscationClassWriter extends ClassWriter {
    public ObfuscationClassWriter(int i) {
        super(i);
    }

    @Override
    protected ClassLoader getClassLoader() {
        return ObfuscationClassKeeper.classLoader;
    }
}
