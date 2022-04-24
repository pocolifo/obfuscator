package com.pocolifo.obfuscator.engine.passes;

public interface Options<T extends PassOptions> {
    T getOptions();
}
