package com.pocolifo.obfuscator.main;

import com.pocolifo.obfuscator.ObfuscatorEngine;
import com.pocolifo.obfuscator.ObfuscatorOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class Bootstrapper {
    public static void main(String[] args) {
        // pocolifoclient-reobfuscated.jar
        try {
            ObfuscatorOptions options = new ObfuscatorOptions()
                    .setInJar(new File("pocolifoclient-reobfuscated.jar"))
                    .setOutJar(new File("out.jar"))
                    .dumpHierarchy();

            Arrays.stream(new File("libraries").listFiles()).forEach(options::addLibraryJar);

            ObfuscatorEngine engine = new ObfuscatorEngine(options);
            engine.obfuscate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
