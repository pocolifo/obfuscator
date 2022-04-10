package com.pocolifo.obfuscator.passes.remapping;

import lombok.RequiredArgsConstructor;
import org.objectweb.asm.commons.Remapper;

@RequiredArgsConstructor
public class JarMappingRemapper extends Remapper {
    public final JarMapping mapping;

    @Override
    public String map(String internalName) {
        ClassMapping cls = mapping.resolveClass(internalName, NameType.FROM);

        return cls == null ? super.map(internalName) : cls.to;
    }

    @Override
    public String mapFieldName(String owner, String name, String descriptor) {
        ClassMapping cls = mapping.resolveClass(owner, NameType.FROM);
        FieldMapping fdm = cls == null ? null : cls.resolveField(name, NameType.FROM);

        return fdm == null ? super.mapFieldName(owner, name, descriptor) : fdm.to;
    }

    @Override
    public String mapMethodName(String owner, String name, String descriptor) {
        ClassMapping cls = mapping.resolveClass(owner, NameType.FROM);
        MethodMapping mdm = cls == null ? null : cls.resolveMethod(name, descriptor, NameType.FROM);

        return mdm == null ? super.mapMethodName(owner, name, descriptor) : mdm.to;
    }
}
