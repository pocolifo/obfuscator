package com.pocolifo.obfuscator.gradleplugin.config;

import com.pocolifo.obfuscator.engine.ObfuscatorOptions;
import com.pocolifo.obfuscator.engine.passes.Options;
import com.pocolifo.obfuscator.engine.passes.PassOptions;
import lombok.Getter;
import org.gradle.api.Action;

import java.util.stream.Stream;

public class ObfuscatorExtension {
    @Getter
    private final ObfuscatorOptions obfuscatorOptions = new ObfuscatorOptions();

    public void pass(Class<Options<? extends PassOptions>> pass, Action<Object> action) {
        pass(pass.getSimpleName(), action);
    }

    public void pass(String passName, Action<Object> action) {
        Options<? extends PassOptions> options = Stream.concat(obfuscatorOptions.passes.stream(), obfuscatorOptions.archivePasses.stream())
                .filter(classPass -> classPass.getClass().getSimpleName().equals(passName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("%s does not exist or hasn't been added", passName)));

        action.execute(options.getOptions());
    }
}
