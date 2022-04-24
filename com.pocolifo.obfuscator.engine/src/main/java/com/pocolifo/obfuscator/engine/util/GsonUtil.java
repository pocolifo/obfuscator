package com.pocolifo.obfuscator.engine.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class GsonUtil {
    public static JsonElement getObjectAsElement(Object val) {
        JsonElement e = null;

        // hacky way to detect if class is a primitive. .isPrimitive did not give me any luck
        if (val.getClass().getCanonicalName().startsWith("java.lang.")) {
            if (val instanceof String) {
                e = new JsonPrimitive((String) val);
            } else if (val instanceof Number) {
                e = new JsonPrimitive((Number) val);
            } else if (val instanceof Character) {
                e = new JsonPrimitive((Character) val);
            } else if (val instanceof Boolean) {
                e = new JsonPrimitive((boolean) val);
            }
        } else {
            e = new JsonPrimitive(val.toString());
        }


        return e;
    }
}
