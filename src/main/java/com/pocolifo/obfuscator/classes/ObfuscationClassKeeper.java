package com.pocolifo.obfuscator.classes;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ObfuscationClassKeeper {
    public List<ClassNode> allClasses = new ArrayList<>();
    public List<ClassNode> inputClasses = new ArrayList<>();
    public List<ClassNode> dependencyClasses = new ArrayList<>();

    private void loadJar(InputStream stream, List<ClassNode> list, int parseOptions) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(stream)) {
            for (ZipEntry e; (e = zis.getNextEntry()) != null;) {
                if (!e.getName().endsWith(".class")) continue;

                ClassNode node = new ClassNode();
                ClassReader reader = new ClassReader(zis);
                reader.accept(node, parseOptions);

                list.add(node);
                allClasses.add(node);
            }
        }
    }

    public void loadInputJar(InputStream inJar) throws IOException {
        loadJar(inJar, inputClasses, ClassReader.EXPAND_FRAMES);
    }

    public void loadDependencyJar(InputStream dependencyJar) throws IOException {
        loadJar(dependencyJar, dependencyClasses, ClassReader.SKIP_CODE);
    }
}
