package com.pocolifo.obfuscator.passes.antidecompile;

import com.pocolifo.obfuscator.passes.PassOptions;

public class AntiDecompileOptions extends PassOptions {
    public boolean fakeZipDirectory = true;
    public boolean randomCrcSignatures = true;
    public boolean invalidZipEntryTime = true;
    public boolean randomCompressedSize = true;
}
