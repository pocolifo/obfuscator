package com.pocolifo.obfuscator.engine.util.obfstring;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
public class TwoLetterObfuscatedStringSupplier implements ObfuscatedStringSupplier {
    private final char c1;
    private final char c2;

    @Override
    public String get() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; DEFAULT_STRING_LENGTH > i; i++) {
            if (ThreadLocalRandom.current().nextBoolean()) {
                builder.append(c1);
            } else {
                builder.append(c2);
            }
        }

        return builder.toString();
    }

    public static class LetterA extends TwoLetterObfuscatedStringSupplier {
        public LetterA() {
            super('\u0430', 'a');
        }
    }

    public static class LetterI extends TwoLetterObfuscatedStringSupplier {
        public LetterI() {
            super('\u0406', 'I');
        }
    }
}
