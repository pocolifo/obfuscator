package com.pocolifo.obfuscator.engine.passes.synthetic;

import com.pocolifo.obfuscator.engine.ObfuscatorEngine;
import com.pocolifo.obfuscator.engine.passes.ClassPass;
import com.pocolifo.obfuscator.engine.passes.PassOptions;
import com.pocolifo.obfuscator.engine.util.ProgressUtil;
import lombok.Getter;
import me.tongfei.progressbar.ProgressBar;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;

public class SyntheticAccChanger implements ClassPass<SyntheticAccChanger.Options> {
    @Getter public Options options = new Options();

    @Override
    public Collection<ClassNode> run(ObfuscatorEngine engine, Collection<ClassNode> inClasses) {
        try (ProgressBar bar = ProgressUtil.bar("Making classes & members synthetic", inClasses.size())) {
            for (ClassNode cls : inClasses) {
                if (options.addForClasses) {
                    cls.access |= Opcodes.ACC_SYNTHETIC;
                }

                if (options.addForMethods) {
                    for (MethodNode method : cls.methods) {
                        method.access |= Opcodes.ACC_SYNTHETIC;
                    }
                }

                if (options.addForFields) {
                    for (FieldNode field : cls.fields) {
                        field.access |= Opcodes.ACC_SYNTHETIC;
                    }
                }

                bar.step();
            }
        }

        return inClasses;
    }

    public static class Options extends PassOptions {
        public boolean addForClasses = true;
        public boolean addForFields = true;
        public boolean addForMethods = true;
    }
}
