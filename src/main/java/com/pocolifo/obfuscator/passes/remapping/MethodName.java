package com.pocolifo.obfuscator.passes.remapping;

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
