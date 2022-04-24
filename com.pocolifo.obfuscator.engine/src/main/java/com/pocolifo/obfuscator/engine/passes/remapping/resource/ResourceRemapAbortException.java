package com.pocolifo.obfuscator.engine.passes.remapping.resource;

import java.io.IOException;

public class ResourceRemapAbortException extends IOException {
    public ResourceRemapAbortException(String str) {
        super(str);
    }
}
