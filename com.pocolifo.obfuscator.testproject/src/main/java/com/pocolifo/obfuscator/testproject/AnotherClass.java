package com.pocolifo.obfuscator.testproject;

import com.pocolifo.obfuscator.annotations.Pass;
import com.pocolifo.obfuscator.annotations.PassOption;

@Pass(value = "RemapNamesPass", options = {
        @PassOption(key = "remapMethodNames", value = "false")
})
public class AnotherClass {
    public void myMethod() {
        System.out.println("myMethod called");
    }

    @Pass(value = "RemapNamesPass", options = {
            @PassOption(key = "remapMethodNames", value = "true")
    })
    public void overrideMe() {
        System.out.println("I haven't been overridden");
    }
}
