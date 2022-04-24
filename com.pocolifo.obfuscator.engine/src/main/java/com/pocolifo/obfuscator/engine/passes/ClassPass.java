package com.pocolifo.obfuscator.engine.passes;

import com.pocolifo.obfuscator.engine.ObfuscatorEngine;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collection;

public interface ClassPass<T extends PassOptions> extends Options<T> {
    Collection<ClassNode> run(ObfuscatorEngine engine, Collection<ClassNode> inClasses);
}
