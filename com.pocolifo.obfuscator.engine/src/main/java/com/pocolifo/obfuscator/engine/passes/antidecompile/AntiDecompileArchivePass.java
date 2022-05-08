package com.pocolifo.obfuscator.engine.passes.antidecompile;

import com.pocolifo.obfuscator.engine.passes.ArchivePassRunTime;
import com.pocolifo.obfuscator.engine.passes.ArchivePass;
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
            entry.setCrc(ThreadLocalRandom.current().nextLong(4294967295L));
        }

        if (options.garbageZipEntryTime) {
            entry.setTime(ThreadLocalRandom.current().nextLong());
        }

        if (options.randomCompressedSize) {
            entry.setCompressedSize(ThreadLocalRandom.current().nextLong());
        }

        return entry;
    }
}
