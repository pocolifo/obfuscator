package com.pocolifo.obfuscator.engine.util.obfstring;

import java.util.concurrent.ThreadLocalRandom;

public class AlphabetObfuscatedStringSupplier implements ObfuscatedStringSupplier {
    @Override
    public String get() {
        StringBuilder builder = new StringBuilder();
        final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz".toCharArray();

        for (int i = 0; DEFAULT_STRING_LENGTH > i; i++) builder.append(ALPHABET[ThreadLocalRandom.current().nextInt(0, ALPHABET.length)]);

        return builder.toString();
    }
}
