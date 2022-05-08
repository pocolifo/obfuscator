package com.pocolifo.obfuscator.engine.util.obfstring;

import java.io.Serializable;

public interface ObfuscatedStringSupplier extends Serializable {
    int DEFAULT_STRING_LENGTH = 24;

    String get();
}
