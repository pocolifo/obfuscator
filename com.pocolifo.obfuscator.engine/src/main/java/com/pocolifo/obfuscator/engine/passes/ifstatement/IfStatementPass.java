package com.pocolifo.obfuscator.engine.passes.ifstatement;

import com.pocolifo.obfuscator.engine.ObfuscatorEngine;
import com.pocolifo.obfuscator.engine.passes.AbstractMethodPass;
import com.pocolifo.obfuscator.engine.passes.PassOptions;
import lombok.Getter;
import me.tongfei.progressbar.ProgressBar;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public class IfStatementPass extends AbstractMethodPass<PassOptions> implements Opcodes {
    @Getter
    public PassOptions options = new PassOptions();

    @Override
    public String getPassName() {
        return "Mangling if statements";
    }

    @Override
    public void doMethod(ObfuscatorEngine engine, Collection<ClassNode> inClasses, ClassNode classNode, MethodNode methodNode, ProgressBar bar, PassOptions options) {

    }
}
