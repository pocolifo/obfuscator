package com.pocolifo.obfuscator.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Repeatable(Passes.class)
public @interface Pass {
    /**
     * Pass to change by name.
     * Default is all passes.
     *
     * @return The name of the pass to skip.
     */
    String value();

    /**
     * Change certain options of the pass.
     * Default value disables the pass altogether.
     *
     * @return An array of PassOptions to modify the options for the pass.
     */
    PassOption[] options();
}
