package com.pocolifo.obfuscator.passes;

public interface Options<T extends PassOptions> {
    T getOptions();
}
