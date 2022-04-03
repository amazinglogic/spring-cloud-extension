package cn.amazing.logic.sleuth.util;

import java.util.UUID;

/**
 * @author dengxiaolin
 * @since 2021/01/22
 */
public class TraceUtils {

    public static String getSimpleName(String name) {
        String[] names = name.split("\\.");
        int length = names.length;
        if (length > 2) {
            return names[length - 2] + "." + names[length - 1];
        }
        else {
            return name;
        }
    }

    public static String buildTraceId() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .toLowerCase();
    }
}
