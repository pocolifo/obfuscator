package com.pocolifo.obfuscator.engine.test;

import com.pocolifo.obfuscator.engine.ObfuscatorEngine;
import com.pocolifo.obfuscator.engine.ObfuscatorOptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ObfuscatorEngineTest {
    // @Test
    void testObfuscatorEngine() throws IOException {
        ObfuscatorOptions opts = new ObfuscatorOptions();
        opts.inJar = TestUtility.getTestingJar();
        opts.prepare();

        new ObfuscatorEngine(opts).obfuscate();
    }
}
