package com.pocolifo.obfuscator.engine.passes.bytecodescrambler;

import com.pocolifo.obfuscator.engine.passes.PassOptions;

public class BytecodeScramblerOptions extends PassOptions {
    public BytecodeScramblerOptions() {
        enabled = false;
    }

    public boolean gotoScrambling = true;
}
