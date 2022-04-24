package com.pocolifo.obfuscator.engine.passes.remapping.mapping;

import com.pocolifo.obfuscator.engine.passes.remapping.name.NameType;
import org.objectweb.asm.tree.MethodNode;

import java.util.LinkedList;
import java.util.List;

public class MethodMapping extends Mapping {
    public String desc;
    public MethodNode node;

    public List<ParameterMapping> parameterMappings = new LinkedList<>();

    public ParameterMapping resolveParameter(String name, NameType nameType) {
        for (ParameterMapping parameterMapping : parameterMappings) {
            if (NameType.compare(nameType, parameterMapping, name)) return parameterMapping;
        }

        return null;
    }
}
