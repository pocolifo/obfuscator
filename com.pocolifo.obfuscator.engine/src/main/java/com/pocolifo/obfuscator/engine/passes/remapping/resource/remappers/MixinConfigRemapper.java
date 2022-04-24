package com.pocolifo.obfuscator.engine.passes.remapping.resource.remappers;

import com.google.gson.*;
import com.pocolifo.obfuscator.engine.ObfuscatorEngine;
import com.pocolifo.obfuscator.engine.passes.remapping.mapping.ClassMapping;
import com.pocolifo.obfuscator.engine.passes.remapping.mapping.JarMapping;
import com.pocolifo.obfuscator.engine.passes.remapping.name.ClassName;
import com.pocolifo.obfuscator.engine.passes.remapping.name.NameType;
import com.pocolifo.obfuscator.engine.passes.remapping.resource.RemapResourceNamesArchivePass;
import com.pocolifo.obfuscator.engine.passes.remapping.resource.ResourceRemapper;
import com.pocolifo.obfuscator.engine.util.Logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class MixinConfigRemapper implements ResourceRemapper {
    @Override
    public boolean isApplicable(Path path, BasicFileAttributes basicFileAttributes, RemapResourceNamesArchivePass.Options options, ObfuscatorEngine engine) {
        return options.mixinConfig && path.toString().contains("mixin") && path.toString().endsWith(".json") && engine.getMapping() != null;
    }

    @Override
    public byte[] remapFile(Path path, BasicFileAttributes basicFileAttributes, ObfuscatorEngine engine) throws IOException {
        JsonObject mixinConfig = JsonParser.parseString(new String(Files.readAllBytes(path))).getAsJsonObject();

        if (!mixinConfig.has("package")) {
            Logging.warn("Mixin config %s%s%s is missing the \"package\" declaration! Cannot remap the config.", Logging.ANSI_CYAN, path.toString(), Logging.ANSI_YELLOW);
            throw new IOException("mixin config is missing package declaration");
        }

        String pkg = ClassName.normalizeSeparators(mixinConfig.get("package").getAsString());

        boolean mixinsChanged = remapArray(mixinConfig, "mixins", engine.getMapping(), pkg);
        boolean clientChanged = remapArray(mixinConfig, "client", engine.getMapping(), pkg);
        boolean serverChanged = remapArray(mixinConfig, "server", engine.getMapping(), pkg);

        if (!(mixinsChanged || clientChanged || serverChanged)) {
            Logging.warn("Could not find any mixins in the config to remap.");
        }

        return new Gson().toJson(mixinConfig).getBytes(StandardCharsets.UTF_8);
    }

    private static boolean remapArray(JsonObject object, String prop, JarMapping mapping, String pkg) {
        if (!object.has(prop)) return false;

        JsonArray array = object.get(prop).getAsJsonArray();

        for (int i = 0; array.size() > i; i++) {
            String simpleClassName = array.get(i).getAsString();

            ClassMapping classMapping = mapping.resolveClass(ClassName.joinName(pkg, simpleClassName), NameType.FROM);

            array.set(i, new JsonPrimitive(classMapping.to.replace(pkg + "/", "")));
        }

        return true;
    }
}
