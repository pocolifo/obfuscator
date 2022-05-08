package com.pocolifo.obfuscator.testproject;

import com.pocolifo.obfuscator.annotations.Pass;
import com.pocolifo.obfuscator.annotations.PassOption;

import java.util.concurrent.ThreadLocalRandom;

@Pass(value = "RemapNamesPass", options = {
        @PassOption(key = "remapMethodNames", value = "false")
})
public class AnotherClass {
    private final String myField = "Hello world";

    public void myMethod() {
        System.out.println("myMethod called");
    }

    @Pass(value = "RemapNamesPass", options = {
            @PassOption(key = "remapMethodNames", value = "true")
    })
    public void overrideMe() {
        System.out.println("I haven't been overridden");
    }

    public void iShouldBeExcluded() {
        System.out.println("Am I remapped?");
    }

    public boolean isTrue() {
        return true;
    }
}
