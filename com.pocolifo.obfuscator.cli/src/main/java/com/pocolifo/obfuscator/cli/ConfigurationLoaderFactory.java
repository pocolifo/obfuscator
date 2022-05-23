package com.pocolifo.obfuscator.cli;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ConfigurationLoaderFactory {
    public static Class<? extends ConfigurationLoader> impl = ConfigurationLoader.class;

    public static ConfigurationLoader getConfigurationLoader() {
        try {
            Constructor<? extends ConfigurationLoader> constructor = impl.getConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("could not create instance of ConfigurationLoader implementation " + impl.getCanonicalName());
        }
    }
}
