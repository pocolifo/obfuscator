package com.pocolifo.obfuscator.cli;

import com.pocolifo.obfuscator.ObfuscatorEngine;
import com.pocolifo.obfuscator.ObfuscatorOptions;
import com.pocolifo.obfuscator.util.Logging;
import lombok.SneakyThrows;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public class Bootstrapper {
    @SneakyThrows
    public static void main(String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("--dump-default-config")) {
                String cfg = ConfigurationLoader.dumpDefault();

                if (args.length > 1) {
                    File config = new File(args[1]);
                    Files.write(config.toPath(), cfg.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);

                    Logging.welcome();
                    Logging.info("%sSuccessfully dumped default configuration to %s", Logging.ANSI_GREEN, config.getAbsolutePath());
                } else {
                    System.out.println(cfg);
                }
            } else {
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
                    options = ConfigurationLoader.loadDefaultConfiguration();
                } else {
                    options = ConfigurationLoader.loadConfiguration(configFile);
                }

                options.inJar = inputFile;

                Logging.info("Verifying configuration");
                options.prepare();

                Logging.info("Launching obfuscation engine");
                new ObfuscatorEngine(options).obfuscate();
            }
        } else {
            URI uri = Bootstrapper.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            String path = new File("").toURI().relativize(uri).getPath();

            Logging.welcome();
            Logging.fatal("improper usage; proper usage: java -jar %s [--dump-default-config] [path to JAR to obfuscate] [path of obfuscation config (optional)].config.json", path);
        }
    }
}
