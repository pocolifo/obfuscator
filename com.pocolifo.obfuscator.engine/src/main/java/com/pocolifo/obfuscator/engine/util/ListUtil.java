package com.pocolifo.obfuscator.engine.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListUtil {
    @SafeVarargs
    public static <T> List<T> join(List<T>... lists) {
        List<T> l = new ArrayList<>();

        for (List<T> list : lists) {
            if (list != null) l.addAll(list);
        }

        return l;
    }

    public static Map<Object, Object> toMap(List<Object> list) {
        if (list.size() % 2 != 0) throw new IllegalArgumentException("list size is not even");
        Map<Object, Object> m = new HashMap<>();

        for (int i = 0; list.size() > i; i += 2) {
            Object key = list.get(i);
            Object val = list.get(i + 1);

            m.put(key, val);
        }

        return m;
    }
}
