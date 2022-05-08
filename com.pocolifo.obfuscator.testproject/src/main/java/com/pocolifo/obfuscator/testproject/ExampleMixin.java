package com.pocolifo.obfuscator.testproject;

import com.pocolifo.obfuscator.annotations.Pass;
import com.pocolifo.obfuscator.annotations.PassOption;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Pass(value = "RemapNamesPass", options = {
        @PassOption(key = "remapMethodNames", value = "true")
})
@Mixin(AnotherClass.class)
public abstract class ExampleMixin {
    @Shadow @Final
    private String myField;

    @Shadow public abstract void iShouldBeExcluded();

    /**
     * @author
     */
    @Overwrite
    public void myMethod() {
        System.out.println("myMethod called");
    }
}
