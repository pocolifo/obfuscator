package com.pocolifo.obfuscator.testproject;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AnotherClass.class)
public interface ExampleAccessor {
    @Accessor("myField")
    String getMyField();

    @Invoker
    void invokeOverrideMe();
}
