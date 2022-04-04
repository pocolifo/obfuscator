package com.pocolifo.obfuscator;

import lombok.Builder;
import lombok.Data;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

@Data
@Builder
public class ObfuscatorOptions {
    private InputStream inJar;
    private OutputStream outJar;
}
