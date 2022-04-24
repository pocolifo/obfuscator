package com.pocolifo.obfuscator.engine.util;

import java.util.ArrayList;
import java.util.List;

public class ListUtil {
    @SafeVarargs
    public static <T> List<T> join(List<T>... lists) {
        List<T> l = new ArrayList<>();

        for (List<T> list : lists) {
            if (list != null) l.addAll(list);
        }

        return l;
    }
}
