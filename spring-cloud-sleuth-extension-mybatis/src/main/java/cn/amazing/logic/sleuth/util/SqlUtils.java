package cn.amazing.logic.sleuth.util;

import java.sql.PreparedStatement;

/**
 * @author dengxiaolin
 * @since 2021/08/16
 */
public class SqlUtils {
    /**
     * sql 最大长度
     */
    private static final int MAX_LENGTH = 10000;

    public static String getTraceSql(PreparedStatement preparedStatement) {
        String sql = preparedStatement.toString();
        return getTraceSql(sql);
    }

    public static String getTraceSql(String sql) {
        sql = sql.substring(sql.indexOf(":") + 1);
        return cutLongSql(sql.replaceAll("[\\s]+", " "));
    }

    private static String cutLongSql(String sql) {
        if (sql.length() > MAX_LENGTH) {
            return sql.substring(0, MAX_LENGTH);
        }
        else {
            return sql;
        }
    }
}
