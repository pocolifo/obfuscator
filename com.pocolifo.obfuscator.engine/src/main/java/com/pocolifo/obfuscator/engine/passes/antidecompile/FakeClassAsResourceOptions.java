package com.pocolifo.obfuscator.engine.passes.antidecompile;

import com.pocolifo.obfuscator.engine.passes.PassOptions;

public class FakeClassAsResourceOptions extends PassOptions {
    public int fakeClassSizeBytes = 512;

    public int fakeClassCountMin = 8;
    public int fakeClassCountMax = 24;
}
