package com.pocolifo.obfuscator.passes.antidecompile;

import com.pocolifo.obfuscator.ArchivePassRunTime;
import com.pocolifo.obfuscator.ObfuscatorEngine;
import com.pocolifo.obfuscator.passes.ArchivePass;
import lombok.Getter;
import org.objectweb.asm.tree.ClassNode;

import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.ZipEntry;

public class AntiDecompileArchivePass implements ArchivePass<AntiDecompileOptions> {
    @Getter private AntiDecompileOptions options = new AntiDecompileOptions();

    @Override
    public ArchivePassRunTime getRunTime() {
        return ArchivePassRunTime.AFTER_CLASSES;
    }

    @Override
    public ZipEntry mutateZipEntry(ClassNode node, ZipEntry entry) {
        if (options.fakeZipDirectory) {
            entry = new ZipEntry(node.name + ".class/");
        }

        if (options.randomCrcSignatures) {
            entry.setCrc(ThreadLocalRandom.current().nextLong(1));
        }

        if (options.invalidZipEntryTime) {
            entry.setTime(Long.MIN_VALUE);
        }

        if (options.randomCompressedSize) {
            entry.setCompressedSize(ThreadLocalRandom.current().nextLong());
        }

        return entry;
    }

    @Override
    public void run(ObfuscatorEngine engine) {

    }
}
