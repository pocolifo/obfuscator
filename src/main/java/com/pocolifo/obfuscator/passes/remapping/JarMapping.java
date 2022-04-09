package com.pocolifo.obfuscator.passes.remapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JarMapping {
    public Map<String, ClassMapping> classes = new HashMap<>();

    public void addClassMapping(ClassMapping mapping) {
        if (classes.containsKey(mapping.from)) throw new RuntimeException("mapping already for class: " + mapping.from);
        classes.put(mapping.from, mapping);
    }

    public ClassMapping resolveClass(String fromName) {
        for (ClassMapping cls : classes.values()) {
            if (cls.from.equals(fromName)) {
                return cls;
            }
        }

        return null;
    }
}
