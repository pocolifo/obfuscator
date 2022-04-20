package com.pocolifo.obfuscator.passes;

import com.pocolifo.obfuscator.ArchivePassRunTime;
import com.pocolifo.obfuscator.ObfuscatorEngine;
import org.objectweb.asm.tree.ClassNode;

import java.util.zip.ZipEntry;

public interface ArchivePass<T extends PassOptions> extends Options<T> {
    default ZipEntry mutateZipEntry(ClassNode node, ZipEntry defaultEntry) {
        return defaultEntry;
    }

    ArchivePassRunTime getRunTime();

    void run(ObfuscatorEngine engine);
}
