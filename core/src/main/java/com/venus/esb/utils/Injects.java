package com.venus.esb.utils;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by lingminjun on 2017/12/28.
 */

public final class Injects {
    /**
     * 填充数据
     * @param source 数据源
     * @param target 填充目标
     */
    public static void fill(Object source,Object target) {
        fill(source,target,true);
    }

    /**
     * 填充数据
     * @param source 数据源
     * @param target 填充目标
     * @param implicit 处理下划线开头属性，如: _name,_id,_age
     */
    public static void fill(Object source, Object target, boolean implicit) {
        fill(source,target,implicit,Object.class);
    }

    /**
     * 填充数据
     * @param source 数据源
     * @param target 填充目标
     * @param implicit 处理下划线开头属性，如: _name,_id,_age
     * @param root
     */
    public static void fill(Object source, Object target, boolean implicit, Class root) {
        if (source == null){
            return;
        }

        Class<?> objC = null;//

        try {
            objC = source.getClass();
        } catch (Throwable e) {
            objC = null;
            e.printStackTrace();
        }

        if (objC == null) {
            return;
        }

        if (source instanceof Map) {
            Map map = (Map)source;
            Iterator entries = map.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry entry = (Map.Entry) entries.next();
                Object key = entry.getKey();
                if (!(key instanceof String)) {
                    continue;
                }
                Object value = entry.getValue();
                setFieldValueForName(target,(String)key,value.getClass(),value,implicit,root);
            }
        } else {
            Field[] fV = objC.getDeclaredFields();
            for (Field field : fV) {

                //去掉静态属性
                if ((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC) {
                    continue;
                }

                //1、类型相等直接赋值，2、基本类型强行赋值
                Object value = null;
                try {
                    field.setAccessible(true);
                    value = field.get(source);
                } catch (Throwable e) {
                    continue;
                }

                setFieldValueForName(target,field.getName(),field.getType(),value,implicit,root);
            }
        }
    }

    private static void setFieldValueForName(Object target, String fieldName, Class<?> type, Object value, boolean implicit, Class root) {
        if (value == null) {
            return;
        }

        Field fieldToSet1 = getDeclaredField(target, fieldName, root);
        Field fieldToSet2 = null;

        if (implicit && !fieldName.startsWith("_")) {
            try {
                fieldToSet2 = getDeclaredField(target, "_" + fieldName, root);
            } catch (Throwable e) { }
        }

        if (fieldToSet1 == null && fieldToSet2 == null) {
            return;
        }

        if (fieldToSet1 != null) {
            setFieldValue(target, fieldToSet1, type, value);
        }

        if (fieldToSet2 != null) {
            setFieldValue(target, fieldToSet2, type, value);
        }
    }

    //1、类型相等直接赋值，2、基本类型强行赋值
    private static void setFieldValue(Object target, Field set, Class<?> type, Object value) {

        //支持基础类型的互换兼容
        if (!(type.equals(set.getType()))) {

            //隐士转换基础类型
            if (isBaseType(set.getType()) && (value instanceof String || isBaseType(value.getClass()))) {
                String string = value.toString();
                if (string.length() == 0) {return;}

                //byte,char,short,int,long,float,double，boolean
                if (set.getType() == int.class || set.getType() == Integer.class) {
                    try {
                        set.setInt(target, Integer.parseInt(string));
                    } catch (Throwable e) {}
                } else if (set.getType() == long.class || set.getType() == Long.class) {
                    try {
                        set.setLong(target, Long.parseLong(string));
                    } catch (Throwable e) {}
                } else if (set.getType() == short.class || set.getType() == Short.class) {
                    try {
                        set.setShort(target, Short.parseShort(string));
                    } catch (Throwable e) {}
                } else if (set.getType() == float.class || set.getType() == Float.class) {
                    try {
                        set.setFloat(target, Float.parseFloat(string));
                    } catch (Throwable e) {}
                } else if (set.getType() == double.class || set.getType() == Double.class) {
                    try {
                        set.setDouble(target, Double.parseDouble(string));
                    } catch (Throwable e) {}
                } else if (set.getType() == char.class || set.getType() == Character.class) {
                    if (string.length() == 1) {
                        try {
                            set.setChar(target, string.charAt(0));
                        } catch (Throwable e) {}
                    }
                } else if (set.getType() == byte.class || set.getType() == Byte.class) {
                    try {
                        short v = Short.parseShort(string);
                        if (v >= -127 && v <= 127) {
                            byte b = (byte)v;
                            set.setByte(target,b);
                        }
                    } catch (Throwable e) {}
                } else if (set.getType() == boolean.class || set.getType() == Boolean.class) {
                    try {
                        Boolean b = bool(string);
                        if (b != null) {
                            set.setBoolean(target, b);
                        }
                    } catch (Throwable e) {}
                }

                return;
            }
        }

        try {
            set.set(target, value);
        } catch (Throwable e) {}
    }

    private static boolean isBaseType(Class<?> type) {
        if (type.isPrimitive()) {
            return true;
        }

        if (type == Integer.class
                || type == Long.class
                || type == Short.class
                || type == Float.class
                || type == Character.class
                || type == Double.class
                || type == Boolean.class
                || type == Byte.class) {
            return true;
        }

        if (type == int.class
                || type == long.class
                || type == short.class
                || type == float.class
                || type == char.class
                || type == double.class
                || type == boolean.class
                || type == byte.class) {
            return true;
        }

        return false;
    }

    /**
     * 循环向上转型, 获取对象的 DeclaredField
     * @param object : 子类对象
     * @param fieldName : 父类中的属性名
     * @return 父类中的属性对象
     */

    public static Field getDeclaredField(Object object, String fieldName, Class root){

        Class<?> clazz = object.getClass() ;
        for(; clazz != Object.class; clazz = clazz.getSuperclass()) {

            if (clazz == root || clazz == Object.class) {//若到了基类则直接返回
                return null;
            }

            try {
                Field field = clazz.getDeclaredField(fieldName);
                if ((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC) {
                    return null;
                }
                return field;
            } catch (Throwable e) {
                //这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
                //如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了
            }
        }
        return null;
    }

    private final static Boolean bool(String v) {
        if (v == null || v.length() == 0) {return null;}
        if ("1".equalsIgnoreCase(v)
                || "yes".equalsIgnoreCase(v)
                || "true".equalsIgnoreCase(v)
                || "on".equalsIgnoreCase(v)
                || "y".equalsIgnoreCase(v)
                || "t".equalsIgnoreCase(v)) {
            return true;
        } else if ("0".equalsIgnoreCase(v)
                || "no".equalsIgnoreCase(v)
                || "false".equalsIgnoreCase(v)
                || "off".equalsIgnoreCase(v)
                || "n".equalsIgnoreCase(v)
                || "f".equalsIgnoreCase(v)) {
            return false;
        }
        return null;
    }
}
