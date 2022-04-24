package com.pocolifo.obfuscator.engine.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class TestUtility {
    public static File getTestingJar() {
        try {
            Path tempFile = Files.createTempFile("test-pocolifo-obfuscator", "engine");

            File originalTestJar = new File("../com.pocolifo.obfuscator.testproject/build/libs/testproject.jar");
            Files.copy(originalTestJar.toPath(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            return originalTestJar;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
