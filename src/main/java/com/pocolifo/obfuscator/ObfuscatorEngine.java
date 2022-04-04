package com.pocolifo.obfuscator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ObfuscatorEngine {
    @Getter private final ObfuscatorOptions options;
    private ObfuscationClassKeeper classKeeper; 

    public void obfuscate() {
        options.prepare();

        classKeeper = new ObfuscationClassKeeper();
        classKeeper.loadJar()
    }
}
