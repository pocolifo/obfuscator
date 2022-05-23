package com.pocolifo.obfuscator.cli;

import com.google.gson.*;
import com.pocolifo.obfuscator.engine.ObfuscatorOptions;
import com.pocolifo.obfuscator.engine.passes.ClassPass;
import com.pocolifo.obfuscator.engine.passes.Options;
import com.pocolifo.obfuscator.engine.passes.PassOptions;
import com.pocolifo.obfuscator.engine.util.DynamicOption;
import com.pocolifo.obfuscator.engine.util.FileUtil;
import com.pocolifo.obfuscator.engine.util.Logging;
import com.pocolifo.obfuscator.engine.util.NotConfigOption;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static com.pocolifo.obfuscator.engine.util.GsonUtil.getObjectAsElement;

public class ConfigurationLoader {
    protected ObfuscatorOptions loadConfiguration(Reader reader) {
        JsonObject object = new Gson().fromJson(reader, JsonObject.class);

        ObfuscatorOptions options = new ObfuscatorOptions();

        options.outJar = new File(object.get("outJar").getAsString());
        options.dumpHierarchy = object.get("dumpHierarchy").getAsBoolean();

        if (object.has("javaHome")) options.javaHome = new File(object.get("javaHome").getAsString());

        object.get("libraryJars").getAsJsonArray().forEach(element -> {
            File file = new File(element.getAsString());

            if (file.isDirectory()) {
                options.libraryJars.addAll(FileUtil.recursivelyFindFiles(file, f -> true, new ArrayList<>()));
            } else {
                options.libraryJars.add(file);
            }
        });

        // add rest
        object.get("passes").getAsJsonObject().entrySet().forEach(entry -> {
            String name = entry.getKey();
            JsonObject cfg = entry.getValue().getAsJsonObject();
            Options<?> pass = null;

            for (ClassPass<?> p : options.passes) {
                if (p.getClass().getSimpleName().equals(name)) {
                    pass = p;
                    break;
                }
            }

            if (pass == null) {
                for (Options<?> p : options.archivePasses) {
                    if (p.getClass().getSimpleName().equals(name)) {
                        pass = p;
                        break;
                    }
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

    public ObfuscatorOptions loadConfiguration(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            return loadConfiguration(reader);
        }
    }

    public ObfuscatorOptions loadDefaultConfiguration() {
        return new ObfuscatorOptions();
    }

    public String dumpDefault() throws IllegalAccessException {
        JsonObject obj = new JsonObject();
        ObfuscatorOptions def = loadDefaultConfiguration();

        obj.add("libraryJars", new JsonArray());
        obj.addProperty("outJar", def.outJar.toString());
        obj.addProperty("dumpHierarchy", def.dumpHierarchy);

        JsonObject passes = new JsonObject();
        for (Options<?> pass : def.passes) {
            passes.add(pass.getClass().getSimpleName(), getClassAsObject(pass.getOptions()));
        }

        for (Options<?> pass : def.archivePasses) {
            passes.add(pass.getClass().getSimpleName(), getClassAsObject(pass.getOptions()));
        }
        obj.add("passes", passes);


        return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(obj);
    }

    protected static JsonObject getClassAsObject(Object instance) throws IllegalAccessException {
        JsonObject object = new JsonObject();

        for (Field field : instance.getClass().getFields()) {
            if (field.isAnnotationPresent(NotConfigOption.class) || field.isAnnotationPresent(DynamicOption.class)) continue;
            field.setAccessible(true);

            addFieldToObject(instance, field, object);
        }

        return object;
    }

    protected static void addFieldToObject(Object instance, Field field, JsonObject object) throws IllegalAccessException {
        Object val = field.get(instance);

        if (val instanceof Iterable) {
            JsonArray array = new JsonArray();

            for (Object o : (Iterable<?>) val) {
                array.add(getObjectAsElement(o));
            }

            object.add(field.getName(), array);
        } else {
            object.add(field.getName(), getObjectAsElement(val));
        }
    }
}
