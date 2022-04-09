package com.pocolifo.obfuscator;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ObfuscatorEngineTest {

    @Test
    void obfuscate() throws IOException {
        new ObfuscatorEngine(new ObfuscatorOptions().setInJar(new File("pocolifoclient-reobfuscated.jar"))).obfuscate();
    }
}