package com.pocolifo.obfuscator.cli;

import com.pocolifo.obfuscator.engine.ObfuscatorEngine;
import com.pocolifo.obfuscator.engine.ObfuscatorOptions;
import com.pocolifo.obfuscator.engine.util.Logging;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Bootstrapper {
    @SneakyThrows
    public static void main(String[] args) {
        ConfigurationLoader cl = ConfigurationLoaderFactory.getConfigurationLoader();
        Logging.fatalThrowsException = false;

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("--dump-default-config")) {
                dumpDefaultConfiguration(args, cl);
            } else {
                beginObfuscation(args, cl);
            }
        } else {
            improperUsage();
        }
    }

    private static void improperUsage() throws URISyntaxException {
        URI uri = Bootstrapper.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        String path = new File("").toURI().relativize(uri).getPath();

        Logging.welcome();
        Logging.fatal("improper usage; proper usage: java -jar %s [--dump-default-config] [path to JAR to obfuscate] [path of obfuscation config (optional)].config.json", path);
    }

    private static void beginObfuscation(String[] args, ConfigurationLoader cl) throws IOException {
        Logging.welcome();

        File configFile = null;
        File inputFile = null;

        for (String arg : args) {
            File file = new File(arg);

            if (file.exists()) {
                if (file.getName().endsWith(".config.json") && configFile == null) {
                    configFile = file;
                } else if (inputFile == null) {
                    inputFile = file;
                } else {
                    Logging.fatal("Too many arguments passed");
                }
            } else {
                Logging.fatal("file %s does not exist", file.getAbsolutePath());
            }
        }

        Logging.info("Input file: %s%s", Logging.ANSI_CYAN, inputFile.getAbsolutePath());
        Logging.info("Configuration: %s%s", Logging.ANSI_CYAN, (configFile == null ? "[default]" : configFile.getPath()));
        Logging.info("Loading configuration");

        ObfuscatorOptions options;

        if (configFile == null) {
            options = cl.loadDefaultConfiguration();
        } else {
            options = cl.loadConfiguration(configFile);
        }

        options.inJar = inputFile;

        Logging.info("Verifying configuration");
        options.prepare();

        Logging.info("Launching obfuscation engine");
        new ObfuscatorEngine(options).obfuscate();
    }

    private static void dumpDefaultConfiguration(String[] args, ConfigurationLoader cl) throws IOException, IllegalAccessException {
        String cfg = cl.dumpDefault();

        if (args.length > 1) {
            File config = new File(args[1]);
            Files.write(config.toPath(), cfg.getBytes(StandardCharsets.UTF_8));

            Logging.welcome();
            Logging.info("%sSuccessfully dumped default configuration to %s", Logging.ANSI_GREEN, config.getAbsolutePath());
        } else {
            System.out.println(cfg);
        }
    }
}
