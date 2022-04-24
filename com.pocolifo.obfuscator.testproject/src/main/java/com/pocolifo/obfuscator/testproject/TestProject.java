package com.pocolifo.obfuscator.testproject;

import com.pocolifo.obfuscator.annotations.Pass;
import com.pocolifo.obfuscator.annotations.PassOption;

@Pass(value = "StringManglerPass", options = {
        @PassOption(key = "enabled", value = "false")
})
public class TestProject {
    public static void main(String[] args) {
        System.out.println("Hello World");

        AnotherClass anotherClass = new AnotherClass();
        anotherClass.overrideMe();
        anotherClass.myMethod();

        AnotherClassChild child = new AnotherClassChild();
        child.myMethod();
        child.overrideMe();
        child.sayHello();
    }
}
