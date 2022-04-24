package com.pocolifo.obfuscator.engine.passes.remapping.resource;

import com.pocolifo.obfuscator.engine.ObfuscatorEngine;
import com.pocolifo.obfuscator.engine.passes.ArchivePass;
import com.pocolifo.obfuscator.engine.passes.ArchivePassRunTime;
import com.pocolifo.obfuscator.engine.passes.PassOptions;
import com.pocolifo.obfuscator.engine.passes.remapping.resource.remappers.MetaInfManifestRemapper;
import com.pocolifo.obfuscator.engine.passes.remapping.resource.remappers.MixinConfigRemapper;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;

public class RemapResourceNamesArchivePass implements ArchivePass<RemapResourceNamesArchivePass.Options> {
    @Getter public Options options = new Options();

    public List<ResourceRemapper> resourceRemappers = Arrays.asList(
            new MetaInfManifestRemapper(),
            new MixinConfigRemapper()
    );

    @Override
    public void finalRun(ObfuscatorEngine engine, FileSystem zipFileSystem) {
        if (engine.getMapping() == null) return;
        Path root = zipFileSystem.getRootDirectories().iterator().next();

        try {
            Files.walkFileTree(root, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                    if (options.excludeResources.contains(path.toString())) return FileVisitResult.CONTINUE;

                    for (ResourceRemapper resourceRemapper : resourceRemappers) {
                        if (resourceRemapper.isApplicable(path, basicFileAttributes, options, engine)) {
                            Files.write(path, resourceRemapper.remapFile(path, basicFileAttributes, engine));
                        }
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path path, IOException e) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ArchivePassRunTime getRunTime() {
        return ArchivePassRunTime.FINAL;
    }

    public static class Options extends PassOptions {
        public boolean metaInfManifest = true;
        public boolean mixinConfig = true;

        public List<String> excludeResources = Arrays.asList(
                "/example.txt",
                "/path/to/some/resource.contents"
        );
    }
}
