package com.pocolifo.obfuscator.engine.passes;

import com.pocolifo.obfuscator.engine.ObfuscatorEngine;
import org.objectweb.asm.tree.ClassNode;

import java.nio.file.FileSystem;
import java.util.zip.ZipEntry;

public interface ArchivePass<T extends PassOptions> extends Options<T> {
    default ZipEntry mutateZipEntry(ClassNode node, ZipEntry defaultEntry) {
        return defaultEntry;
    }
    default void finalRun(ObfuscatorEngine engine, FileSystem zipFileSystem) {}

    ArchivePassRunTime getRunTime();
}
