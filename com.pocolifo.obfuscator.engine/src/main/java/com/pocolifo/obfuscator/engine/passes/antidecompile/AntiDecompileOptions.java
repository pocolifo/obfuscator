package com.pocolifo.obfuscator.engine.passes.antidecompile;

import com.pocolifo.obfuscator.engine.passes.PassOptions;

public class AntiDecompileOptions extends PassOptions {
    public boolean fakeZipDirectory = true;
    public boolean randomCrcSignatures = true;
    public boolean invalidZipEntryTime = true;
    public boolean randomCompressedSize = true;
}
