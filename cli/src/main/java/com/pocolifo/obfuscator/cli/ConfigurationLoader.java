package com.pocolifo.obfuscator.cli;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pocolifo.obfuscator.ObfuscatorOptions;
import com.pocolifo.obfuscator.logger.Logging;
import com.pocolifo.obfuscator.passes.ClassPass;
import com.pocolifo.obfuscator.passes.PassOptions;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationLoader {
    private static ObfuscatorOptions loadConfiguration(Reader reader) {
        JsonObject object = new Gson().fromJson(reader, JsonObject.class);

        ObfuscatorOptions options = new ObfuscatorOptions();

        options.outJar = new File(object.get("outJar").getAsString());
        options.dumpHierarchy = object.get("dumpHierarchy").getAsBoolean();

        if (object.has("javaHome")) options.javaHome = new File(object.get("javaHome").getAsString());

        object.get("libraryJars").getAsJsonArray().forEach(element -> {
            File file = new File(element.getAsString());

            if (file.isDirectory()) {
                options.libraryJars.addAll(recursivelyGetFiles(file, new ArrayList<>()));
            } else {
                options.libraryJars.add(file);
            }
        });

        // add rest
        object.get("passes").getAsJsonObject().entrySet().forEach(entry -> {
            String name = entry.getKey();
            JsonObject cfg = entry.getValue().getAsJsonObject();
            ClassPass<?> pass = null;

            for (ClassPass<?> p : options.passes) {
                if (p.getClass().getSimpleName().equals(name)) {
                    pass = p;
                    break;
                }
            }

            if (pass == null) {
                Logging.warn("Could not find pass \"%s\", skipping configuration entry", name);
            } else {
                PassOptions passOptions = pass.getOptions();
                Class<? extends PassOptions> opts = passOptions.getClass();

                cfg.entrySet().forEach(opt -> {
                    String key = opt.getKey();
                    JsonElement value = opt.getValue();

                    try {
                        Field field = opts.getField(key);
                        field.setAccessible(true);

                        if (value.isJsonPrimitive()) {
                            if (value.getAsJsonPrimitive().isBoolean()) {
                                field.set(passOptions, value.getAsBoolean());
                            }
                        }
                        // todo more options
                    } catch (NoSuchFieldException e) {
                        Logging.warn("Option doesn't exist for \"%s\": %s", name, key);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                });
            }
        });

        return options;
    }

    private static List<File> recursivelyGetFiles(File directory, List<File> files) {
        for (File file : directory.listFiles()) {
            files.add(file);

            if (file.isDirectory()) recursivelyGetFiles(file, files);
        }

        return files;
    }

    public static ObfuscatorOptions loadConfiguration(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            return loadConfiguration(reader);
        }
    }

    public static ObfuscatorOptions loadDefaultConfiguration() throws IOException {
        try (InputStreamReader reader = new InputStreamReader(ConfigurationLoader.class.getResourceAsStream("/default-config.config.json"))) {
            return loadConfiguration(reader);
        }
    }
}


