package com.pocolifo.obfuscator.passes.remapping;

public class ClassName {
    // com/pocolifo/pocolifoclient/mods/config/types/StringConfigurationType
    // com/pocolifo/pocolifoclient/mods/config/types/StringConfigurationType$0
    // com/pocolifo/pocolifoclient/mods/config/types/StringConfigurationType$sdfjOSIOFJ

    public boolean anonymousClass;
    public ClassName parentClass;
    public String className;
    public String originalFullPath;
    public String parentPath;

    public ClassName(String fullPath) {
        if (fullPath.contains("$")) {
            className = fullPath.substring(fullPath.lastIndexOf("$") + 1);
            parentClass = new ClassName(fullPath.substring(0, fullPath.lastIndexOf("$")));

            try {
                Integer.parseInt(className);
                anonymousClass = true;
            } catch (NumberFormatException ignored) {}
        } else {
            className = fullPath.substring(fullPath.lastIndexOf("/") + 1);
        }

        originalFullPath = fullPath;
        parentPath = fullPath.substring(0, fullPath.lastIndexOf("/"));
    }

    @Override
    public String toString() {
        if (parentClass == null) {
            return joinName(parentPath, className);
        } else {
            return parentClass + "$" + className;
        }
    }

    public static String joinName(String s1, String s2) {
        return s1.endsWith("/") ? s1 + s2 : s1 + "/" + s2;
    }
}
