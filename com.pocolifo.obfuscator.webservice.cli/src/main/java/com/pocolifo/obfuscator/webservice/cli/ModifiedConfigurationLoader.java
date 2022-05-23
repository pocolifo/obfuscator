package com.pocolifo.obfuscator.webservice.cli;

import com.pocolifo.obfuscator.cli.ConfigurationLoader;
import com.pocolifo.obfuscator.engine.ObfuscatorOptions;

import java.io.File;
import java.io.Reader;

public class ModifiedConfigurationLoader extends ConfigurationLoader {
    @Override
    protected ObfuscatorOptions loadConfiguration(Reader reader) {
        ObfuscatorOptions obfuscatorOptions = super.loadConfiguration(reader);

        obfuscatorOptions.outJar = new File(obfuscatorOptions.inJar.getPath() + "-obfuscated.jar");
        obfuscatorOptions.dumpHierarchy = false;

        return obfuscatorOptions;
    }
}
