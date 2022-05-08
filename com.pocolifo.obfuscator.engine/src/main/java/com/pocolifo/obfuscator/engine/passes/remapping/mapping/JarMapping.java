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

    public boolean isFieldInMapping(String className, String fieldName, NameType type) {
        ClassMapping classMapping = resolveClass(className, type);

        if (classMapping == null) return false;

        return classMapping.resolveField(fieldName, type) != null;
    }

    public boolean isMethodInMapping(String className, String methodName, String descriptor, NameType type) {
        ClassMapping classMapping = resolveClass(className, type);

        if (classMapping == null) return false;

        return classMapping.resolveMethod(methodName, descriptor, type) != null;
    }
}
