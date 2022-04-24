package com.pocolifo.obfuscator.engine.passes.remapping.resource;

import com.pocolifo.obfuscator.engine.ObfuscatorEngine;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public interface ResourceRemapper {
    boolean isApplicable(Path path, BasicFileAttributes basicFileAttributes, RemapResourceNamesArchivePass.Options options, ObfuscatorEngine engine);

    byte[] remapFile(Path path, BasicFileAttributes basicFileAttributes, ObfuscatorEngine engine) throws IOException;
}
