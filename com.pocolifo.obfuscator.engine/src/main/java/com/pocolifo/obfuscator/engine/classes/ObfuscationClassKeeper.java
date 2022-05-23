package com.pocolifo.obfuscator.engine.classes;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ObfuscationClassKeeper {
    public static List<URL> jarUrls = new ArrayList<>();
    public static URLClassLoader classLoader;

    public Queue<ClassNode> inputClasses = new ConcurrentLinkedQueue<>();
    public Queue<ClassNode> allClasses = new ConcurrentLinkedQueue<>();
    public Queue<ClassNode> dependencyClasses = new ConcurrentLinkedQueue<>();

    private void loadClassesFromArchive(Path path, Collection<ClassNode> classes, int parseOptions) throws IOException {
        try (FileSystem fs = FileSystems.newFileSystem(path, ClassLoader.getSystemClassLoader())) {
            Path root = fs.getRootDirectories().iterator().next();

            Files.walk(root).filter(p -> p.getFileName() != null && p.getFileName().toString().endsWith(".class")).forEach(p -> {
                try (InputStream stream = Files.newInputStream(p, StandardOpenOption.READ)) {
                    ClassNode node = new ClassNode();

                    ClassReader reader = new ClassReader(stream);
                    reader.accept(node, parseOptions);

                    classes.add(node);
                    allClasses.add(node);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void loadInputJar(Path inJar) throws IOException {
        loadClassesFromArchive(inJar, inputClasses, ClassReader.EXPAND_FRAMES);
    }

    public void loadDependencyClasses(Path dependency) throws IOException {
        loadClassesFromArchive(dependency, dependencyClasses, ClassReader.SKIP_CODE);
    }

    public static void prepareJarOnToClasspath(File jar) throws MalformedURLException {
        jarUrls.add(jar.toURI().toURL());
    }

    public static void initializeClassLoader() {
        classLoader = new URLClassLoader(jarUrls.toArray(new URL[0]), ClassLoader.getSystemClassLoader());
    }
}
