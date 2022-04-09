package com.pocolifo.obfuscator.passes.remapping;

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
