package com.paperfly.imageShare.common.utils;

import java.util.Collection;
import java.util.Map;

public class EmptyUtil {
    public static boolean empty(String str) {
        return str == null || "".equals(str);
    }

    public static boolean empty(Map map) {
        return map == null || map.isEmpty();
    }

    public static boolean empty(Collection col){
        return col == null || col.isEmpty();
    }

    public static boolean empty(Object obj){
        return obj == null;
    }

    public static boolean empty(Object[] objs){
        return objs==null || objs.length==0;
    }
}
