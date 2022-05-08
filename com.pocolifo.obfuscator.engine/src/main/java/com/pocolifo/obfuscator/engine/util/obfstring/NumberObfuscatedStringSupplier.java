package com.pocolifo.obfuscator.engine.util.obfstring;

import java.util.concurrent.ThreadLocalRandom;

public class NumberObfuscatedStringSupplier implements ObfuscatedStringSupplier {
    @Override
    public String get() {
        return String.valueOf(ThreadLocalRandom.current().nextLong());
    }
}
