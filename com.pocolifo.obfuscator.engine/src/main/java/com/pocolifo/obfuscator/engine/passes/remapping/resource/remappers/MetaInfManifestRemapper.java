package com.pocolifo.obfuscator.engine.passes.remapping.resource.remappers;

import com.pocolifo.obfuscator.engine.ObfuscatorEngine;
import com.pocolifo.obfuscator.engine.passes.remapping.mapping.ClassMapping;
import com.pocolifo.obfuscator.engine.passes.remapping.resource.RemapResourceNamesArchivePass;
import com.pocolifo.obfuscator.engine.passes.remapping.resource.ResourceRemapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class MetaInfManifestRemapper implements ResourceRemapper {
    @Override
    public boolean isApplicable(Path path, BasicFileAttributes basicFileAttributes, RemapResourceNamesArchivePass.Options options, ObfuscatorEngine engine) {
        return options.metaInfManifest && path.toString().equalsIgnoreCase("/meta-inf/manifest.mf");
    }

    @Override
    public byte[] remapFile(Path path, BasicFileAttributes basicFileAttributes, ObfuscatorEngine engine) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();

        for (String line : Files.readAllLines(path)) {
            for (ClassMapping classMapping : engine.getMapping().classes.values()) {
                line = line.replaceAll(classMapping.from, classMapping.to);
                line = line.replaceAll(classMapping.from.replaceAll("/", "."), classMapping.to.replaceAll("/", "."));
            }

            contentBuilder.append(line).append(System.lineSeparator());
        }

        return contentBuilder.toString().getBytes(StandardCharsets.UTF_8);
    }
}
