package com.pocolifo.obfuscator.engine.passes.remapping.mapping;

import com.pocolifo.obfuscator.engine.passes.remapping.name.NameType;

import java.util.HashMap;
import java.util.Map;

public class JarMapping {
    public Map<String, ClassMapping> classes = new HashMap<>();

    public void addClassMapping(ClassMapping mapping) {
        if (classes.containsKey(mapping.from)) throw new RuntimeException("mapping already for class: " + mapping.from);
        classes.put(mapping.from, mapping);
    }

    public ClassMapping resolveClass(String name, NameType nameType) {
        for (ClassMapping cls : classes.values()) {
            if (NameType.compare(nameType, cls, name)) {
                return cls;
            }
        }

        return null;
    }
}
