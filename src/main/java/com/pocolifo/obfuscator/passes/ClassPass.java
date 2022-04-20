package com.pocolifo.obfuscator.passes;

import com.pocolifo.obfuscator.ObfuscatorEngine;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collection;
import java.util.Map;

public interface ClassPass<T extends PassOptions> extends Options<T> {
    Collection<ClassNode> run(ObfuscatorEngine engine, Collection<ClassNode> inClasses);
}
