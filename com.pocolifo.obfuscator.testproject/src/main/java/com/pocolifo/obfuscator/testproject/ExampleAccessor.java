package com.pocolifo.obfuscator.testproject;

import com.pocolifo.obfuscator.annotations.Pass;
import com.pocolifo.obfuscator.annotations.PassOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pass(value = "RemapNamesPass", options = {
        @PassOption(key = "remapClassNames", value = "false")
})
@Mixin(AnotherClass.class)
public interface ExampleAccessor {
    @Accessor("myField")
    String getMyField();

    @Invoker
    void invokeOverrideMe();
}
