package com.pocolifo.obfuscator.engine.passes.remapping.remapper;

import com.pocolifo.obfuscator.engine.passes.remapping.mapping.*;
import com.pocolifo.obfuscator.engine.passes.remapping.name.NameType;
import com.pocolifo.obfuscator.engine.util.Logging;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AnnotationRemapper;
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

    @Override
    public String mapAnnotationAttributeName(String descriptor, String name) {
        String fromClassName = Type.getType(descriptor).getClassName().replaceAll("\\.", "/");
        ClassMapping cls = mapping.resolveClass(fromClassName, NameType.FROM);
        Mapping member = cls == null ? null : cls.resolveMember(name, NameType.FROM);

        return member == null ? super.mapAnnotationAttributeName(descriptor, name) : member.to;
    }


}
