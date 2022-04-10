package com.pocolifo.obfuscator.passes.remapping;

public enum NameType {
    TO,
    FROM,
    EITHER;

    public static boolean compare(NameType type, Mapping mapping, String name) {
        switch (type) {
            case TO:
                return mapping.to.equals(name);

            case FROM:
                return mapping.from.equals(name);

            case EITHER:
                return mapping.to.equals(name) || mapping.from.equals(name);
        }

        return false;
    }
}
