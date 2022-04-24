package com.pocolifo.obfuscator.testproject;

import com.pocolifo.obfuscator.annotations.Pass;
import com.pocolifo.obfuscator.annotations.PassOption;

@Pass(value = "GarbageMembersPass", options = {
        @PassOption(key = "addFields", value = "true"),
        @PassOption(key = "addMethods", value = "false")
})
@Pass(value = "RemapNamesPass", options = {
        @PassOption(key = "remapClassNames", value = "false")
})
public class AnotherClassChild extends AnotherClass {
    @Override
    public void overrideMe() {
        System.out.println("I've been overridden");
    }

    public void sayHello() {
        System.out.println("Hello!");
    }
}
