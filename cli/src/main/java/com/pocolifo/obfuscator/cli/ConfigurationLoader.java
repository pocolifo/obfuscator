package com.pocolifo.obfuscator.cli;

import com.google.gson.*;
import com.pocolifo.obfuscator.ObfuscatorOptions;
import com.pocolifo.obfuscator.passes.ClassPass;
import com.pocolifo.obfuscator.passes.PassOptions;
import com.pocolifo.obfuscator.util.DynamicOption;
import com.pocolifo.obfuscator.util.FileUtil;
import com.pocolifo.obfuscator.util.Logging;
import com.pocolifo.obfuscator.util.NotConfigOption;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

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
                options.libraryJars.addAll(FileUtil.recursivelyFindFiles(file, f -> true, new ArrayList<>()));
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

    public static ObfuscatorOptions loadConfiguration(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            return loadConfiguration(reader);
        }
    }

    public static ObfuscatorOptions loadDefaultConfiguration() {
        return new ObfuscatorOptions();
    }

    public static void dumpDefault(File to) throws IllegalAccessException, IOException {
        JsonObject obj = new JsonObject();
        ObfuscatorOptions def = loadDefaultConfiguration();

        obj.add("libraryJars", new JsonArray());
        obj.addProperty("outJar", def.outJar.toString());
        obj.addProperty("dumpHierarchy", def.dumpHierarchy);

        JsonObject passes = new JsonObject();
        for (ClassPass<?> pass : def.passes) {
            passes.add(pass.getClass().getSimpleName(), getClassAsObject(pass.getOptions()));
        }
        obj.add("passes", passes);


        String json = new GsonBuilder().setPrettyPrinting().create().toJson(obj);
        Files.write(to.toPath(), json.getBytes(StandardCharsets.UTF_8));
    }

    private static JsonObject getClassAsObject(Object instance) throws IllegalAccessException {
        JsonObject object = new JsonObject();

        for (Field field : instance.getClass().getFields()) {
            if (field.isAnnotationPresent(NotConfigOption.class) || field.isAnnotationPresent(DynamicOption.class)) continue;
            field.setAccessible(true);

            addFieldToObject(instance, field, object);
        }

        return object;
    }

    private static void addFieldToObject(Object instance, Field field, JsonObject object) throws IllegalAccessException {
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

    private static JsonElement getObjectAsElement(Object val) throws IllegalAccessException {
        JsonElement e = null;

        // hacky way to detect if class is a primitive. .isPrimitive did not give me any luck
        if (val.getClass().getCanonicalName().startsWith("java.lang.")) {
            if (val instanceof String) {
                e = new JsonPrimitive((String) val);
            } else if (val instanceof Number) {
                e = new JsonPrimitive((Number) val);
            } else if (val instanceof Character) {
                e = new JsonPrimitive((Character) val);
            } else if (val instanceof Boolean) {
                e = new JsonPrimitive((boolean) val);
            }
        } else {
            e = new JsonPrimitive(val.toString());
        }


        return e;
    }
}
