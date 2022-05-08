package com.pocolifo.obfuscator.engine.passes;

import java.io.Serializable;

public interface Options<T extends PassOptions> extends Serializable {
    T getOptions();
}
