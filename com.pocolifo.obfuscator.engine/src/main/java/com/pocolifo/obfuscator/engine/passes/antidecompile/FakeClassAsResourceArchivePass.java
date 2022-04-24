package com.pocolifo.obfuscator.engine.passes.antidecompile;

import com.pocolifo.obfuscator.engine.ObfuscatorEngine;
import com.pocolifo.obfuscator.engine.passes.ArchivePass;
import com.pocolifo.obfuscator.engine.passes.ArchivePassRunTime;
import com.pocolifo.obfuscator.engine.util.Logging;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.ZipEntry;

public class FakeClassAsResourceArchivePass implements ArchivePass<FakeClassAsResourceOptions> {
    @Getter
    public FakeClassAsResourceOptions options = new FakeClassAsResourceOptions();

    public void finalRun(ObfuscatorEngine engine, FileSystem zipFileSystem) {
        for (int i = 0; ThreadLocalRandom.current().nextInt(options.fakeClassCountMin, options.fakeClassCountMax + 1) > i; i++) {
            insertFakeClass(zipFileSystem);
        }
    }

    private void insertFakeClass(FileSystem zipFileSystem) {
        ZipEntry entry = new ZipEntry(UUID.randomUUID() + ".class");
        try {
            Path path = zipFileSystem.getPath(entry.getName());

            byte[] randomBytes = new byte[options.fakeClassSizeBytes];
            ThreadLocalRandom.current().nextBytes(randomBytes);

            Files.write(path, randomBytes);
        } catch (IOException e) {
            Logging.warn("Could not include fake class as resource: %s", e);
        }
    }

    @Override
    public ArchivePassRunTime getRunTime() {
        return ArchivePassRunTime.FINAL;
    }
}
