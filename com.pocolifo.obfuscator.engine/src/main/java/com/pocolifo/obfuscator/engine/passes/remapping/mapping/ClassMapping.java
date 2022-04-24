package com.pocolifo.obfuscator.engine.passes.remapping.mapping;

import com.pocolifo.obfuscator.engine.passes.remapping.name.NameType;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;

import java.util.HashMap;
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
        if (methods.containsKey(mapping.from + mapping.desc)) throw new RuntimeException("mapping already for method: " + mapping.from);
        methods.put(mapping.from + mapping.desc, mapping);
    }

    // todo add descriptor
    public FieldMapping resolveField(String name, NameType nameType) {
        for (FieldMapping fdm : fields.values()) {
            if (NameType.compare(nameType, fdm, name)) {
                return fdm;
            }
        }

        return null;
    }

    public MethodMapping resolveMethod(String name, String descriptor, NameType nameType) {
        for (MethodMapping mdm : methods.values()) {
            if (NameType.compare(nameType, mdm, name) && mdm.desc.equals(descriptor)) {
                return mdm;
            }
        }

        return null;
    }

    public MethodMapping resolveMethod(String name, String descriptor, Remapper remapper, NameType nameType) {
        for (MethodMapping mdm : methods.values()) {
            if (NameType.compare(nameType, mdm, name) && (mdm.desc.equals(descriptor) || remapper.mapMethodDesc(mdm.desc).equals(descriptor))) {
                return mdm;
            }
        }

        return null;
    }
}
