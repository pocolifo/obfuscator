package com.pocolifo.obfuscator;

import com.pocolifo.obfuscator.classes.ClassHierarchy;
import com.pocolifo.obfuscator.classes.ClassHierarchyNode;
import com.pocolifo.obfuscator.classes.ObfuscationClassKeeper;
import com.pocolifo.obfuscator.logger.Logging;
import com.pocolifo.obfuscator.passes.ClassPass;
import com.pocolifo.obfuscator.passes.remapping.RemapNamesPass;
import com.pocolifo.obfuscator.passes.sourcehints.RemoveSourceHintsPass;
import com.pocolifo.obfuscator.util.ProgressUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.tongfei.progressbar.ProgressBar;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@RequiredArgsConstructor
public class ObfuscatorEngine {
    private static final Iterable<ClassPass> CLASS_PASSES = Arrays.asList(
            new RemapNamesPass(),
            new RemoveSourceHintsPass()
    );

    @Getter private final ObfuscatorOptions options;
    @Getter private ObfuscationClassKeeper classKeeper;
    @Getter private ClassHierarchy hierarchy;

    public void obfuscate() throws IOException {
        Logging.welcome();

        // initialization
        Logging.info("Initializing");
        options.prepare();

        // jar loading
        Logging.info("Loading jars");
        loadJars();

        // hierarchy
        Logging.info("Creating class hierarchy");
        createHierarchy();

        // obfuscate
        Logging.info("Beginning obfuscation");

        Logging.info("Running class obfuscation");
        Iterable<ClassNode> nodes = doClassObfuscation();

        Logging.info("Done obfuscating, writing out JAR");
        writeOutJar(nodes);
    }

    private void writeOutJar(Iterable<ClassNode> nodes) throws IOException {
        int count;

        try (ZipFile file = new ZipFile(options.getInJar())) {
            count = file.size();
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(options.getInJar()));
             ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(options.getOutJar()));
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
        Collection<ClassNode> outClasses = classKeeper.inputClasses;

        for (ClassPass classPass : CLASS_PASSES) {
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

        if (options.isDumpHierarchy()) {
            Logging.info("Dumping hierarchy to file");

            try (PrintStream stream = new PrintStream("hierarchy.txt")) {
                hierarchy.printOut(stream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadJars() throws IOException {
        classKeeper = new ObfuscationClassKeeper();

        Logging.info("Adding JRE and runtime jars");

        File[] jreJars = new File(System.getProperty("java.home"), "lib").listFiles((file, s) -> s.endsWith(".jar"));

        for (File jreJar : jreJars) {
            Logging.info("Found runtime JAR: " + jreJar.getAbsolutePath());
            options.addLibraryJar(jreJar);
        }

        // count
        int jarsToLoad = 1 + options.getLibraryJars().size();
        Logging.info("About to load %d jars", jarsToLoad);

        try (ProgressBar bar = ProgressUtil.bar("Loading input JARs", jarsToLoad); FileInputStream fis = new FileInputStream(options.getInJar())) {
            classKeeper.loadInputJar(fis);
            bar.step();

            // libraries
            for (File file : options.getLibraryJars()) {
                try (FileInputStream depStream = new FileInputStream(file)) {
                    classKeeper.loadDependencyJar(depStream);
                    bar.step();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
