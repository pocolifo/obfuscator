package com.pocolifo.obfuscator.engine.passes.antitamper;

import com.pocolifo.obfuscator.engine.ObfuscatorEngine;
import com.pocolifo.obfuscator.engine.passes.ClassPass;
import com.pocolifo.obfuscator.engine.passes.PassOptions;
import lombok.Getter;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collection;

public class AntiTamperPass implements ClassPass<PassOptions> {
    @Getter public PassOptions options = new PassOptions();

    @Override
    public Collection<ClassNode> run(ObfuscatorEngine engine, Collection<ClassNode> inClasses) {
        // todo
        // checks class name
        // checks method name
        return null;
    }
}
