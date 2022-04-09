package com.pocolifo.obfuscator.passes.remapping;

import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassMapping extends Mapping {
    public ClassNode node;

    public Map<String, FieldMapping> fields = new HashMap<>();
    public Map<String, MethodMapping> methods = new HashMap<>();

    public void addFieldMapping(FieldMapping mapping) {
        if (fields.containsKey(mapping.from)) throw new RuntimeException("mapping already for field: " + mapping.from);
        fields.put(mapping.from, mapping);
    }

    public void addMethodMapping(MethodMapping mapping) {
        if (methods.containsKey(mapping.from)) throw new RuntimeException("mapping already for method: " + mapping.from);
        methods.put(mapping.from, mapping);
    }

    // todo add descriptor
    public FieldMapping resolveField(String fromName) {
        for (FieldMapping fdm : fields.values()) {
            if (fdm.from.equals(fromName)) {
                return fdm;
            }
        }

        return null;
    }

    public MethodMapping resolveMethod(String fromName, String descriptor) {
        for (MethodMapping mdm : methods.values()) {
            if (mdm.from.equals(fromName) && mdm.desc.equals(descriptor)) {
                return mdm;
            }
        }

        return null;
    }
}
