package com.pocolifo.obfuscator.test;

import com.pocolifo.obfuscator.ObfuscatorEngine;
import com.pocolifo.obfuscator.ObfuscatorOptions;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class ObfuscatorEngineTest {

    @Test
    void obfuscate() {
        try (FileInputStream input = new FileInputStream("in.jar"); FileOutputStream output = new FileOutputStream("out.jar")) {
            ObfuscatorOptions options = ObfuscatorOptions.builder()
                    .inJar(input)
                    .outJar(output)
                    .build();

            ObfuscatorEngine engine = new ObfuscatorEngine(options);
            engine.obfuscate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}