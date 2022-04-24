package com.pocolifo.obfuscator.engine.passes.remapping.name;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FieldName {
    public ClassName parentClass;
    public String fieldName;

    @Override
    public String toString() {
        return fieldName;
    }

    public String getFullName() {
        return ClassName.joinName(parentClass.toString(), fieldName);
    }
}
