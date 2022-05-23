package com.pocolifo.obfuscator.engine.passes.bytecodescrambler;

import com.pocolifo.obfuscator.engine.ObfuscatorEngine;
import com.pocolifo.obfuscator.engine.passes.ClassPass;
import com.pocolifo.obfuscator.engine.util.ProgressUtil;
import lombok.Getter;
import me.tongfei.progressbar.ProgressBar;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collection;

public class BytecodeScrambler implements ClassPass<BytecodeScramblerOptions>, Opcodes {
    @Getter private BytecodeScramblerOptions options = new BytecodeScramblerOptions();

    @Override
    public Collection<ClassNode> run(ObfuscatorEngine engine, Collection<ClassNode> inClasses) {
        try (ProgressBar bar = ProgressUtil.bar("Scrambling bytecode", inClasses.size())) {
            inClasses.forEach(cls -> {
                if (options.gotoScrambling) {
                    cls.methods.forEach(mtd -> BytecodeSequenceScrambler.process(mtd.instructions));
                }

                bar.step();
            });
        }

        return inClasses;
    }
}
