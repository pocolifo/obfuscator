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
                    .setInJar(new File("java-chess-1.0-SNAPSHOT.jar"))
                    .setOutJar(new File("output.jar"))
                    .dumpHierarchy();

//            Arrays.stream(new File("libraries").listFiles()).forEach(options::addLibraryJar);

            ObfuscatorEngine engine = new ObfuscatorEngine(options);
            engine.obfuscate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
