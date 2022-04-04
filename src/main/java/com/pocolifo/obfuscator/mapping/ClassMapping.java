package com.pocolifo.obfuscator.mapping;

import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.List;

public class ClassMapping extends Mapping {
    public ClassNode node;

    public List<FieldMapping> fields = new ArrayList<>();
    public List<MethodMapping> methods = new ArrayList<>();
}
