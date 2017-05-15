package com.backdoor.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;

/**
 * Copyright 2017 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class ObjectUtil {

    public static String getObjectPrint(Object o, Class<?> clazz) {
        List<Field> fields = Arrays.asList(clazz.getDeclaredFields());
        String toString = clazz.getName() + " -> ";
        for (Field f : fields) {
            f.setAccessible(true);
            if (!Modifier.isStatic(f.getModifiers())) {
                toString += f.getName() + ": ";
                try {
                    toString += getValue(f, o) + ", ";
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return toString;
    }

    private static String getValue(Field field, Object clazz) throws IllegalAccessException {
        Class<?> type = field.getType();
        if (!(field.getGenericType() instanceof ParameterizedType)) {
            if (type == Float.TYPE) {
                return field.getFloat(clazz) + "";
            } else if (type == Integer.TYPE) {
                return field.getInt(clazz) + "";
            } else if (type == Double.TYPE) {
                return field.getDouble(clazz) + "";
            } else if (type == java.lang.Long.TYPE) {
                return field.getLong(clazz) + "";
            } else if (type == Boolean.TYPE) {
                return field.getBoolean(clazz) + "";
            } else if (type.isEnum()) {
                try {
                    return field.get(clazz).toString();
                } catch (ClassCastException | NullPointerException e) {
                    return "enum";
                }
            } else {
                try {
                    return (String) field.get(clazz);
                } catch (ClassCastException | NullPointerException e) {
                    return "array";
                }
            }
        } else {
            String res = "{ ";
            res += field.get(clazz) + " }";
            return res;
        }
    }
}
