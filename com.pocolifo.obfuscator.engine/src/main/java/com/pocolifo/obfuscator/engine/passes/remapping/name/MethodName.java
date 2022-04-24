package com.pocolifo.obfuscator.engine.passes.remapping.name;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MethodName {
    public ClassName parentClass;
    public String methodName;
    public String methodDescriptor;

    @Override
    public String toString() {
        return methodName;
    }

    public String getFullName() {
        return ClassName.joinName(parentClass.toString(), methodName);
    }
}
