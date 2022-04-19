package com.pocolifo.obfuscator;

import com.pocolifo.obfuscator.classes.ClassHierarchy;
import com.pocolifo.obfuscator.classes.ClassHierarchyNode;
import com.pocolifo.obfuscator.classes.ObfuscationClassKeeper;
import com.pocolifo.obfuscator.logger.Logging;
import com.pocolifo.obfuscator.passes.ClassPass;
import com.pocolifo.obfuscator.passes.PassOptions;
import com.pocolifo.obfuscator.passes.remapping.RemapNamesPass;
import com.pocolifo.obfuscator.passes.shufflemembers.ShuffleMembersPass;
import com.pocolifo.obfuscator.passes.sourcehints.RemoveSourceHintsPass;
import com.pocolifo.obfuscator.passes.string.StringManglerPass;
import com.pocolifo.obfuscator.util.ProgressUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.tongfei.progressbar.ProgressBar;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@RequiredArgsConstructor
public class ObfuscatorEngine {
    @Getter private final ObfuscatorOptions options;
    @Getter private ObfuscationClassKeeper classKeeper;
    @Getter private ClassHierarchy hierarchy;

    public void obfuscate() throws IOException {
        // jar loading
        Logging.info("Loading jars");

        try {
            loadJars();
        } catch (Exception e) {
            e.printStackTrace();
            Logging.fatal("Could not load JARs! Exception: %s", e);
        }

        // hierarchy
        Logging.info("Creating class hierarchy");
        createHierarchy();

        // obfuscate
        Logging.info("Beginning obfuscation");
        Iterable<ClassNode> nodes = doClassObfuscation();

        Logging.info("Done obfuscating, writing out JAR");
        writeOutJar(nodes);

        // end
        Logging.info("Finished obfuscation in %fs", (System.currentTimeMillis() - options.initTimestamp) / 1000f);
    }

    private void writeOutJar(Iterable<ClassNode> nodes) throws IOException {
        int count;

        try (ZipFile file = new ZipFile(options.inJar)) {
            count = file.size();
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(options.inJar));
             ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(options.outJar));
             ProgressBar bar = ProgressUtil.bar("Writing out JAR", count)) {
            for (ZipEntry e; (e = zis.getNextEntry()) != null;) {
                if (!e.getName().endsWith(".class")) {
                    zos.putNextEntry(new ZipEntry(e.getName()));

                    byte[] buffer = new byte[1024];
                    for (int length; (length = zis.read(buffer)) > 0;) {
                        zos.write(buffer, 0, length);
                    }

                    zos.closeEntry();
                    bar.step();
                }
            }

            for (ClassNode node : nodes) {
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                node.accept(writer);

                zos.putNextEntry(new ZipEntry(node.name + ".class"));
                zos.write(writer.toByteArray());
                zos.closeEntry();
                bar.step();
            }
        }
    }

    private Iterable<ClassNode> doClassObfuscation() {
        Logging.info("Loaded passes:");
        for (ClassPass<? extends PassOptions> classPass : options.passes) {
            Logging.info("%s * %s", classPass.getOptions().enabled ? Logging.ANSI_GREEN : Logging.ANSI_RED, classPass.getClass().getSimpleName());
        }

        Collection<ClassNode> outClasses = classKeeper.inputClasses;

        for (ClassPass<? extends PassOptions> classPass : options.passes) {
            if (!classPass.getOptions().enabled) continue;

            outClasses = classPass.run(this, outClasses);
        }

        return outClasses;
    }

    private void createHierarchy() {
        // init hierarchy
        hierarchy = new ClassHierarchy();

        try (ProgressBar bar = ProgressUtil.bar("Initializing class hierarchy", classKeeper.allClasses.size())) {
            for (ClassNode cls : classKeeper.allClasses) {
                if (cls.name.equals("module-info")) continue;

                if (cls.superName == null) {
                    if (hierarchy.root == null) {
                        hierarchy.root = new ClassHierarchyNode(cls);
                    } else {
                        bar.close();
                        Logging.fatal("There are more than one superclasses loaded!");
                    }
                }

                bar.step();
            }

            if (hierarchy == null) {
                bar.close();
                Logging.fatal("Could not find superclass (like java/lang/Object).");
            }
        }

        // load it
        Queue<ClassNode> classesToAdd = classKeeper.allClasses;

        Map<String, ClassHierarchyNode> nodes = new HashMap<>();

        try (ProgressBar bar = ProgressUtil.bar("Loading class hierarchy nodes", classesToAdd.size())) {
            // load
            for (ClassNode cls : classesToAdd) {
                nodes.put(cls.name, new ClassHierarchyNode(cls));

                bar.step();
            }
        }

        // add root
        nodes.put(hierarchy.root.classNode.name, hierarchy.root);

        // construct
        Set<String> missingClasses = new LinkedHashSet<>();

        try (ProgressBar bar = ProgressUtil.bar("Constructing class hierarchy", nodes.size())) {
            nodes.values().parallelStream().forEach(value -> {
                hierarchy.loadClassIntoHierarchy(value, nodes, missingClasses);
                bar.step();
            });
        }

        if (!missingClasses.isEmpty()) {
            Logging.warn("Missing classes! This can cause hierarchy construction problems, but not always.");

            for (String missingClass : missingClasses) {
                Logging.warn("* %s", missingClass);
            }
        }

        Logging.info("Constructed hierarchy");

        if (options.dumpHierarchy) {
            Logging.info("Dumping hierarchy to file");

            try (PrintStream stream = new PrintStream("hierarchy.txt")) {
                hierarchy.printOut(stream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private int getJavaVersionOfInput() throws IOException {
        AtomicInteger javaVersionOfCode = new AtomicInteger(-1);

        try (ZipFile zf = new ZipFile(options.inJar)) {
            zf.stream().filter(zipEntry -> zipEntry.getName().endsWith(".class")).findFirst().ifPresent(zipEntry -> {
                try (InputStream stream = zf.getInputStream(zipEntry)) {
                    ClassReader reader = new ClassReader(stream);
                    ClassNode node = new ClassNode();

                    reader.accept(node, ClassReader.SKIP_CODE);

                    Map<Integer, Integer> opcodeToJavaVersion = new HashMap<>();

                    for (Field field : Opcodes.class.getDeclaredFields()) {
                        if (field.getName().startsWith("V")) {
                            String javaVersion = field.getName().replace("V", "").replace("1_", "");

                            try {
                                field.setAccessible(true);
                                opcodeToJavaVersion.put(field.getInt(null), Integer.parseInt(javaVersion));
                            } catch (NumberFormatException | IllegalAccessException ignored) {}
                        }
                    }

                    javaVersionOfCode.set(opcodeToJavaVersion.get(node.version));
                } catch (IOException ignored) {}
            });
        }

        return javaVersionOfCode.get();
    }

    private void loadJars() throws Exception {
        classKeeper = new ObfuscationClassKeeper();

        Logging.info("Detecting input JAR Java version");
        int javaVersionOfCode = getJavaVersionOfInput();

        if (javaVersionOfCode == -1) {
            Logging.warn("Could not detect language level");
        } else {
            int currentVersion = Integer.parseInt(System.getProperty("java.specification.version").replace("1.", ""));

            if (currentVersion == javaVersionOfCode) {
                Logging.info("Detected language level matches runtime Java version: %sJava %d", Logging.ANSI_CYAN, javaVersionOfCode);
            } else {
                Logging.warn("Detected language level (Java %d) and runtime Java version (Java %d) do not match. This can cause JRE libraries to be missed and hierarchy construction issues unless you specify the Java %d home.", javaVersionOfCode, currentVersion, javaVersionOfCode);
            }
        }

        Logging.info("Adding JRE and runtime jars");

        File[] jreJars = new File(System.getProperty("java.home"), "lib").listFiles((file, s) -> s.endsWith(".jar"));
        File[] jreJmods = new File(System.getProperty("java.home"), "jmods").listFiles((file, s) -> s.endsWith(".jmod"));

        if (jreJars != null) {
            for (File library : jreJars) {
                Logging.info("Found runtime library: %s%s", Logging.ANSI_CYAN, library.getAbsolutePath());
                options.libraryJars.add(library);
            }
        }

        if (jreJmods != null) {
            for (File library : jreJmods) {
                Logging.info("Found runtime library: %s%s", Logging.ANSI_CYAN, library.getAbsolutePath());
                options.libraryJars.add(library);
            }
        }

        // count
        int jarsToLoad = 1 + options.libraryJars.size();
        Logging.info("About to load %d jars", jarsToLoad);

        try (ProgressBar bar = ProgressUtil.bar("Loading input JARs", jarsToLoad); FileInputStream fis = new FileInputStream(options.inJar)) {
            classKeeper.loadInputJar(fis);
            bar.step();

            AtomicReference<Exception> exception = new AtomicReference<>();

            // libraries
            options.libraryJars.parallelStream().forEach(file -> {
                try {
                    if (file.getName().endsWith(".jmod")) {
                        classKeeper.loadDependencyJmod(file.toPath());
                    } else {
                        try (FileInputStream depStream = new FileInputStream(file)) {
                            classKeeper.loadDependencyJar(depStream);
                        }
                    }

                    bar.step();
                } catch (IOException e) {
                    exception.set(e);
                }
            });

            if (exception.get() != null) {
                throw exception.get();
            }
        }
    }
}
