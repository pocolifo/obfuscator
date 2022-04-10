package com.pocolifo.obfuscator.passes;

import com.pocolifo.obfuscator.ObfuscatorEngine;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collection;

public interface ClassPass {
    Collection<ClassNode> run(ObfuscatorEngine engine, Collection<ClassNode> inClasses);
}
