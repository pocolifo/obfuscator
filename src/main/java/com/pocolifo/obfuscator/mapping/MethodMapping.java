package com.pocolifo.obfuscator.mapping;

import org.objectweb.asm.tree.MethodNode;

public class MethodMapping extends Mapping {
    public String fromDesc;
    public String toDesc;
    public MethodNode node;
}
