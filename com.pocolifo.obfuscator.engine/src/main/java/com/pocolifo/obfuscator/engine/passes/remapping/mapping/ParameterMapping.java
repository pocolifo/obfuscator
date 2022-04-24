package com.pocolifo.obfuscator.engine.passes.remapping.mapping;

import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;

public class ParameterMapping extends Mapping {
    public ParameterNode node;
    public MethodNode methodNode;
}
