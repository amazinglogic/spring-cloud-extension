package cn.amazing.logic.sleuth.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author dengxiaolin
 * @since 2021/01/14
 */
public class ReflectUtils {
    private static final String PROXY = "$Proxy";
    private static final LoadingCache<FieldCacheKey, Field> fieldCache = CacheBuilder.newBuilder()
            .expireAfterWrite(24, TimeUnit.HOURS)
            .maximumSize(2000)
            .build(new CacheLoader<FieldCacheKey, Field>() {
                @Override
                public Field load(FieldCacheKey key) throws Exception {
                    return getFieldByName(key);
                }
            });

    public static Field getFieldByName(Object obj, String fieldName) {
        FieldCacheKey fieldCacheKey = FieldCacheKey.of(obj.getClass(), fieldName);

        // 动态代理不能使用缓存
        Class<?> clazz = obj.getClass();
        if (clazz.getName().contains(PROXY)) {
            return getFieldByName(fieldCacheKey);
        }

        try {
            return fieldCache.get(fieldCacheKey);
        }
        catch (Exception e) {
            throw new RuntimeException("there is not field named " + fieldName);
        }
    }

    private static Field getFieldByName(FieldCacheKey fieldCacheKey) {
        Class<?> clazz = fieldCacheKey.getClazz();
        String fieldName = fieldCacheKey.getFieldName();

        for (Class<?> superClass = clazz; superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                return superClass.getDeclaredField(fieldName);
            }
            catch (NoSuchFieldException e) {
                // ignore
            }
        }
        throw new RuntimeException("there is not field named " + fieldName);
    }

    public static Object getFieldValueByName(Object obj, String fieldName) {
        Field field = getFieldByName(obj, fieldName);
        if (field.isAccessible()) {
            return getValue(obj, field);
        }
        else {
            field.setAccessible(true);
            try {
                return getValue(obj, field);
            }
            finally {
                field.setAccessible(false);
            }
        }
    }

    public static Object getValue(Object obj, Field field) {
        try {
            return field.get(obj);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setValue(Field field, Object targetObj, Object value) {
        try {
            if (field.isAccessible()) {
                field.set(targetObj, value);
            }
            else {
                field.setAccessible(true);
                try {
                    field.set(targetObj, value);
                }
                finally {
                    field.setAccessible(false);
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class FieldCacheKey {
        private Class<?> clazz;
        private String fieldName;

        public static FieldCacheKey of(Class<?> clazz, String fieldName) {
            FieldCacheKey fieldCacheKey = new FieldCacheKey();
            fieldCacheKey.setClazz(clazz);
            fieldCacheKey.setFieldName(fieldName);

            return fieldCacheKey;
        }

        public Class<?> getClazz() {
            return clazz;
        }

        public void setClazz(Class<?> clazz) {
            this.clazz = clazz;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            FieldCacheKey that = (FieldCacheKey) o;
            return Objects.equals(clazz, that.clazz) &&
                    Objects.equals(fieldName, that.fieldName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clazz, fieldName);
        }
    }
}
