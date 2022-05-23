package com.pocolifo.obfuscator.webservice.cli;

import com.pocolifo.obfuscator.cli.Bootstrapper;
import com.pocolifo.obfuscator.cli.ConfigurationLoaderFactory;

public class WebServiceCliMain {
    public static void main(String[] args) {
        ConfigurationLoaderFactory.impl = ModifiedConfigurationLoader.class;
        Bootstrapper.main(args);
    }
}
