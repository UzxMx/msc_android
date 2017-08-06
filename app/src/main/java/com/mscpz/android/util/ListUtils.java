package com.mscpz.android.util;

import java.util.List;

/**
 * Created by xuemingxiang on 16-11-13.
 */

public class ListUtils {

    public static boolean isEmpty(List<?> list) {
        return list == null || list.size() == 0;
    }

    public static int size(List<?> list) {
        return list == null ? 0 : list.size();
    }

    public static String toString(List<?> list) {
        if (list == null) {
            return "list is null";
        } else if (list.size() == 0) {
            return "list size is zero";
        } else {
            StringBuilder builder = new StringBuilder("[");
            boolean first = true;
            for (Object o : list) {
                if (!first) {
                    builder.append(", ");
                } else {
                    first = false;
                }
                if (o == null) {
                    builder.append("null");
                } else {
                    builder.append(o.toString());
                }
            }
            builder.append("]");
            return builder.toString();
        }
    }
}
