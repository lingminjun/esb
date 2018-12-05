package com.venus.esb.utils;


import com.venus.esb.lang.ESBT;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by lingminjun on 2017/12/28.
 * 支持基础类型互相转换
 * 支持枚举和字符串转换
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

        // map情况
        if (source instanceof Map) {//从map填充，查看属性
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
        }
        else if (target.getClass().isArray()) {//是容器，则不再遍历属性
            fillArray(source,target,implicit,root);
        }
        else if (Collection.class.isAssignableFrom(target.getClass())) {//是容器，则不再遍历属性
            return;//无法支持，由于没有类型定义
//            Type genericType;
//            try {
//                genericType = ((ParameterizedTypeImpl) f.getGenericType()).getActualTypeArguments()[0];
//            } catch (Throwable throwable) {
//                throw new RuntimeException("can not get generic type of list in " + groupName + " " + clazz.getName() + " " + f.getName(),
//                        throwable);
//            }
//            try {
//                type = Class.forName(((Class) genericType).getName(), true, Thread.currentThread().getContextClassLoader());
//            }
//                fillCollection(source,target,implicit,root);
        }
        else {
            Field[] fV = ESBT.getClassDeclaredFields(objC);//.getDeclaredFields();
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

    private static void fillArray(Object source, Object target, boolean implicit, Class root) {
        int size = Array.getLength(target);
        if (size == 0) {//直接不需填充
            return;
        }

        int tsize = 0;
        Object sourceArray = null;
        if (source.getClass().isArray()) {
            tsize = Array.getLength(source);
            sourceArray = source;
        } else if (source instanceof Collection) {
            tsize = ((Collection) source).size();
            sourceArray = ((Collection) source).toArray();
        }

        if (tsize == 0) {//无内容，不需填充
            return;
        }

        size = Math.min(size,tsize);

        Class<?> targetType  = target.getClass();
        if (targetType == boolean[].class) {
            boolean[] varray = (boolean[])target;
            for (int i = 0; i < size; i++) {
                Object vobj = Array.get(sourceArray,i);
                if (vobj == null) {continue;}

                if (isNumericalValue(vobj)) {//隐士转换基础类型
                    String string = vobj.toString();
                    if (string.length() == 0) {
                        continue;
                    }
                    varray[i] = ESBT.bool(string);
                } else {//没有意义
                    return;
                }
            }
        } else if (targetType == char[].class) {
            char[] varray = (char[])target;
            for (int i = 0; i < size; i++) {
                Object vobj = Array.get(sourceArray,i);
                if (vobj == null) {continue;}

                if (isNumericalValue(vobj)) {//隐士转换基础类型
                    String string = vobj.toString();
                    if (string.length() == 1) {
                        varray[i] = string.charAt(0);
                    }
                } else {//没有意义
                    return;
                }
            }
        } else if (targetType == byte[].class) {
            byte[] varray = (byte[])target;
            for (int i = 0; i < size; i++) {
                Object vobj = Array.get(sourceArray,i);
                if (vobj == null) {continue;}
                if (isNumericalValue(vobj)) {//隐士转换基础类型
                    String string = vobj.toString();
                    try {
                        short v = Short.parseShort(string);
                        if (v >= -127 && v <= 127) {
                            varray[i] = (byte)v;
                        }
                    } catch (Throwable e) {}
                } else {//没有意义
                    return;
                }
            }
        } else if (targetType == int[].class) {
            int[] varray = (int[])target;
            for (int i = 0; i < size; i++) {
                Object vobj = Array.get(sourceArray,i);
                if (vobj == null) {continue;}

                if (isNumericalValue(vobj)) {//隐士转换基础类型
                    String string = vobj.toString();
                    if (string.length() == 0) {
                        continue;
                    }
                    varray[i] = ESBT.integer(string);
                } else {//没有意义
                    return;
                }
            }
        } else if (targetType == short[].class) {
            short[] varray = (short[])target;
            for (int i = 0; i < size; i++) {
                Object vobj = Array.get(sourceArray,i);
                if (vobj == null) {continue;}

                if (isNumericalValue(vobj)) {//隐士转换基础类型
                    String string = vobj.toString();
                    if (string.length() == 0) {
                        continue;
                    }
                    varray[i] = ESBT.shortInteger(string);
                } else {//没有意义
                    return;
                }
            }
        } else if (targetType == long[].class) {
            long[] varray = (long[])target;
            for (int i = 0; i < size; i++) {
                Object vobj = Array.get(sourceArray,i);
                if (vobj == null) {continue;}

                if (isNumericalValue(vobj)) {//隐士转换基础类型
                    String string = vobj.toString();
                    if (string.length() == 0) {
                        continue;
                    }
                    varray[i] = ESBT.longInteger(string);
                } else {//没有意义
                    return;
                }
            }
        } else if (targetType == float[].class) {
            float[] varray = (float[])target;
            for (int i = 0; i < size; i++) {
                Object vobj = Array.get(sourceArray,i);
                if (vobj == null) {continue;}

                if (isNumericalValue(vobj)) {//隐士转换基础类型
                    String string = vobj.toString();
                    if (string.length() == 0) {
                        continue;
                    }
                    varray[i] = ESBT.floatDecimal(string);
                } else {//没有意义
                    return;
                }
            }
        } else if (targetType == double[].class) {
            double[] varray = (double[])target;
            for (int i = 0; i < size; i++) {
                Object vobj = Array.get(sourceArray,i);
                if (vobj == null) {continue;}

                if (isNumericalValue(vobj)) {//隐士转换基础类型
                    String string = vobj.toString();
                    if (string.length() == 0) {
                        continue;
                    }
                    varray[i] = ESBT.doubleDecimal(string);
                } else {//没有意义
                    return;
                }
            }
        }
        // 大写情况
        else if (targetType == Boolean[].class) {
            Boolean[] varray = (Boolean[])target;
            for (int i = 0; i < size; i++) {
                Object vobj = Array.get(sourceArray,i);
                if (vobj == null) {continue;}

                if (isNumericalValue(vobj)) {//隐士转换基础类型
                    String string = vobj.toString();
                    if (string.length() == 0) {
                        continue;
                    }
                    varray[i] = ESBT.bool(string);
                } else {//没有意义
                    return;
                }
            }
        } else if (targetType == Character[].class) {
            Character[] varray = (Character[])target;
            for (int i = 0; i < size; i++) {
                Object vobj = Array.get(sourceArray,i);
                if (vobj == null) {continue;}

                if (isNumericalValue(vobj)) {//隐士转换基础类型
                    String string = vobj.toString();
                    if (string.length() == 1) {
                        varray[i] = string.charAt(0);
                    }
                } else {//没有意义
                    return;
                }
            }
        } else if (targetType == Byte[].class) {
            Byte[] varray = (Byte[])target;
            for (int i = 0; i < size; i++) {
                Object vobj = Array.get(sourceArray,i);
                if (vobj == null) {continue;}
                if (isNumericalValue(vobj)) {//隐士转换基础类型
                    String string = vobj.toString();
                    try {
                        short v = Short.parseShort(string);
                        if (v >= -127 && v <= 127) {
                            varray[i] = (byte)v;
                        }
                    } catch (Throwable e) {}
                } else {//没有意义
                    return;
                }
            }
        } else if (targetType == Integer[].class) {
            Integer[] varray = (Integer[])target;
            for (int i = 0; i < size; i++) {
                Object vobj = Array.get(sourceArray,i);
                if (vobj == null) {continue;}

                if (isNumericalValue(vobj)) {//隐士转换基础类型
                    String string = vobj.toString();
                    if (string.length() == 0) {
                        continue;
                    }
                    varray[i] = ESBT.integer(string);
                } else {//没有意义
                    return;
                }
            }
        } else if (targetType == Short[].class) {
            Short[] varray = (Short[])target;
            for (int i = 0; i < size; i++) {
                Object vobj = Array.get(sourceArray,i);
                if (vobj == null) {continue;}

                if (isNumericalValue(vobj)) {//隐士转换基础类型
                    String string = vobj.toString();
                    if (string.length() == 0) {
                        continue;
                    }
                    varray[i] = ESBT.shortInteger(string);
                } else {//没有意义
                    return;
                }
            }
        } else if (targetType == Long[].class) {
            Long[] varray = (Long[])target;
            for (int i = 0; i < size; i++) {
                Object vobj = Array.get(sourceArray,i);
                if (vobj == null) {continue;}

                if (isNumericalValue(vobj)) {//隐士转换基础类型
                    String string = vobj.toString();
                    if (string.length() == 0) {
                        continue;
                    }
                    varray[i] = ESBT.longInteger(string);
                } else {//没有意义
                    return;
                }
            }
        } else if (targetType == Float[].class) {
            Float[] varray = (Float[])target;
            for (int i = 0; i < size; i++) {
                Object vobj = Array.get(sourceArray,i);
                if (vobj == null) {continue;}

                if (isNumericalValue(vobj)) {//隐士转换基础类型
                    String string = vobj.toString();
                    if (string.length() == 0) {
                        continue;
                    }
                    varray[i] = ESBT.floatDecimal(string);
                } else {//没有意义
                    return;
                }
            }
        } else if (targetType == Double[].class) {
            Double[] varray = (Double[])target;
            for (int i = 0; i < size; i++) {
                Object vobj = Array.get(sourceArray,i);
                if (vobj == null) {continue;}

                if (isNumericalValue(vobj)) {//隐士转换基础类型
                    String string = vobj.toString();
                    if (string.length() == 0) {
                        continue;
                    }
                    varray[i] = ESBT.doubleDecimal(string);
                } else {//没有意义
                    return;
                }
            }
        }
        //字符串数组
        else if (targetType == String[].class) {
            String[] varray = (String[])target;
            for (int i = 0; i < size; i++) {
                Object vobj = Array.get(sourceArray,i);
                if (vobj == null) {continue;}

                if (isNumericalValue(vobj)) {//隐士转换基础类型
                    String string = vobj.toString();
                    if (string.length() == 0) {
                        continue;
                    }
                    varray[i] = string;
                } else if (vobj.getClass().isEnum()) {//枚举转换
                    varray[i] = ((Enum)vobj).name();
                } else {//没有意义
                    return;
                }
            }
        } else {//取元素类型

            String eleTypeString = ESBT.convertCoreType(targetType.getName());
            Class<?> eleType = ESBT.classForName(eleTypeString);
            if (eleType == null) {
                return;
            }

            for (int i = 0; i < size; i++) {
                Object fobj = Array.get(sourceArray,i);
                //忽略null的存在
                if (fobj == null) {
                    continue;
                }

                //支持枚举类型转换
                if (eleType.isEnum()) {
                    if (fobj.getClass() == eleType) {
                        Array.set(target,i,fobj);
                    } else if (fobj instanceof CharSequence) {
                        String strName = fobj.toString();
                        Object[] enumValues = eleType.getEnumConstants();
                        for (Object enumV : enumValues){
                            if (strName.equals(enumV.toString())) {
                                Array.set(target,i,enumV);
                                break;
                            }
                        }
                    } else {//无法支持
                        return;
                    }
                    continue;
                }

                Object tobj = null;
                try {
                    tobj = eleType.newInstance();
                } catch (Throwable e) {
                    e.printStackTrace();
                }

                if (tobj == null) {
                    return;//没有必要继续
                }

                fill(fobj,tobj,implicit,root);

                Array.set(target,i,tobj);
            }
        }
    }

    /**
     * 填充 Collection
     * @param source
     * @param target
     * @param targetElementClass
     * @param implicit
     * @param root
     */
    public static void fillCollection(Object source, Collection<?> target, Class<?> targetElementClass, boolean implicit, Class root) {
        //无法实例化出来，无意义
        if (targetElementClass == null) {
            return;
        }

        int size = 0;
        Object array = null;
        if (source.getClass().isArray()) {
            size = Array.getLength(source);
            array = source;
        } else if (source instanceof Collection) {
            size = ((Collection) source).size();
            array = ((Collection) source).toArray();
        }

        //只允许这两类目标设置
        if (size == 0 || array == null) {
            return;
        }

        Collection targetCollection = (Collection)target;

        for (int i = 0; i < size; i++) {
            Object vobj = Array.get(array,i);
            //忽略null的存在
            if (vobj == null) {
                continue;
            }

            //隐士转换基础类型
            if (isBaseType(targetElementClass)) {
                //基础类型
                if (!isNumericalValue(vobj)) {
                    return;
                }
                String string = vobj.toString();
                if (targetElementClass == boolean.class || targetElementClass == Boolean.class) {
                    targetCollection.add(ESBT.bool(string));
                } else if (targetElementClass == char.class || targetElementClass == Character.class) {
                    if (string.length() == 1) {
                        targetCollection.add(string.charAt(0));
                    }
                } else if (targetElementClass == byte.class || targetElementClass == Byte.class) {
                    try {
                        short v = Short.parseShort(string);
                        if (v >= -127 && v <= 127) {
                            targetCollection.add((byte)v);
                        }
                    } catch (Throwable e) {}
                } else if (targetElementClass == int.class || targetElementClass == Integer.class) {
                    targetCollection.add(ESBT.integer(string));
                } else if (targetElementClass == short.class || targetElementClass == Short.class) {
                    targetCollection.add(ESBT.shortInteger(string));
                } else if (targetElementClass == long.class || targetElementClass == Long.class) {
                    targetCollection.add(ESBT.longInteger(string));
                } else if (targetElementClass == float.class || targetElementClass == Float.class) {
                    targetCollection.add(ESBT.floatDecimal(string));
                } else if (targetElementClass == double.class || targetElementClass == Double.class) {
                    targetCollection.add(ESBT.doubleDecimal(string));
                }
                //字符串数组
                else if (targetElementClass == String.class) {
                    targetCollection.add(string);
                }

            } else if (targetElementClass.isEnum()) {//枚举支持
                if (targetElementClass == vobj.getClass()) {//直接加入
                    targetCollection.add(vobj);
                } else if (vobj instanceof CharSequence) {
                    String strName = vobj.toString();
                    Object[] enumValues = targetElementClass.getEnumConstants();
                    for (Object enumV : enumValues){
                        if (strName.equals(enumV.toString())) {
                            targetCollection.add(enumV);
                            break;
                        }
                    }
                } else {
                    return;
                }
            } else {//都是复杂结构体
                Object tobj = null;
                try {
                    tobj = targetElementClass.newInstance();
                } catch (Throwable e) {
                    e.printStackTrace();
                }

                if (tobj == null) {
                    return;//没有必要继续
                }
                fill(tobj, vobj, implicit, root);
                targetCollection.add(tobj);
            }
        }
    }

    private static void setFieldValueForName(Object target, String fieldName, Class<?> type, Object value, boolean implicit, Class root) {
        if (value == null) {
            return;
        }

        Field fieldToSet1 = getDeclaredField(target, fieldName, root);
        Field fieldToSet2 = null;

        if (fieldToSet1 == null && implicit && !fieldName.startsWith("_")) {
            try {
                fieldToSet2 = getDeclaredField(target, "_" + fieldName, root);
            } catch (Throwable e) { }
        }

        if (fieldToSet1 == null && fieldToSet2 == null) {
            return;
        }

        if (fieldToSet1 != null) {
            setFieldValue(target, fieldToSet1, type, value, implicit, root);
        }

        if (fieldToSet2 != null) {
            setFieldValue(target, fieldToSet2, type, value, implicit, root);
        }
    }

    //1、类型相等直接赋值，2、基本类型强行赋值
    private static void setFieldValue(Object target, Field set, Class<?> type, Object value, boolean implicit, Class root) {

        //1、Array处理
        if (set.getType().isArray()) {
            setArrayFieldValue(target,set,type,value,implicit,root);
            return;
        }

        //2、泛型处理
        if (set.getGenericType() != set.getType()) {
            setGenericFieldValue(target,set,type,value,implicit,root);
            return;
        }

        //3、相同类型，直接赋值(非泛型情况,非数组情况)----此处存在浅拷贝问题（忽略）
        if (type.equals(set.getType())) {
            try {
                set.setAccessible(true);
                set.set(target, value);
            } catch (Throwable e) {}
            return;
        }

        //4、如果set是父类的情况(非泛型情况,非数组情况)----此处存在浅拷贝问题（忽略）
        if (set.getType().isAssignableFrom(type)) {
            try {
                set.setAccessible(true);
                set.set(target, value);
            } catch (Throwable e) {}
            return;
        }

        //5、基础类型，相互转化情况
        //5.1 支持时间转换
        if (type == Date.class && (set.getType() == long.class || set.getType() == Long.class)) {
            try {
                set.setAccessible(true);
                if (set.getType() == Long.class) {
                    set.set(target, ((Date) value).getTime());
                } else {
                    set.setLong(target, ((Date) value).getTime());
                }
            } catch (Throwable e) {}
            return;
        }
        if ((type == long.class || type == Long.class) && set.getType() == Date.class) {
            try {
                set.setAccessible(true);
                set.set(target, new Date(Long.parseLong(value.toString())));
            } catch (Throwable e) {}
            return;
        }

        // 5.2 隐士转换基础类型
        if (isBaseType(set.getType()) && isNumericalValue(value)) {//隐士转换基础类型
            String string = value.toString();
            if (string.length() == 0) {return;}

            //byte,char,short,int,long,float,double，boolean
            if (set.getType() == int.class || set.getType() == Integer.class) {
                try {
                    set.setAccessible(true);
                    if (set.getType() == Integer.class) {
                        set.set(target, Integer.parseInt(string));
                    } else {
                        set.setInt(target, Integer.parseInt(string));
                    }
                } catch (Throwable e) {}
            } else if (set.getType() == long.class || set.getType() == Long.class) {
                try {
                    set.setAccessible(true);
                    if (set.getType() == Long.class) {
                        set.set(target, Long.parseLong(string));
                    } else {
                        set.setLong(target, Long.parseLong(string));
                    }
                } catch (Throwable e) {}
            } else if (set.getType() == short.class || set.getType() == Short.class) {
                try {
                    set.setAccessible(true);
                    if (set.getType() == Short.class) {
                        set.set(target, Short.parseShort(string));
                    } else {
                        set.setShort(target, Short.parseShort(string));
                    }
                } catch (Throwable e) {}
            } else if (set.getType() == float.class || set.getType() == Float.class) {
                try {
                    set.setAccessible(true);
                    if (set.getType() == Float.class) {
                        set.set(target, Float.parseFloat(string));
                    } else {
                        set.setFloat(target, Float.parseFloat(string));
                    }
                } catch (Throwable e) {}
            } else if (set.getType() == double.class || set.getType() == Double.class) {
                try {
                    set.setAccessible(true);
                    if (set.getType() == Double.class) {
                        set.set(target, Double.parseDouble(string));
                    } else {
                        set.setDouble(target, Double.parseDouble(string));
                    }
                } catch (Throwable e) {}
            } else if (set.getType() == char.class || set.getType() == Character.class) {
                if (string.length() == 1) {
                    try {
                        set.setAccessible(true);
                        if (set.getType() == Character.class) {
                            set.set(target, string.charAt(0));
                        } else {
                            set.setChar(target, string.charAt(0));
                        }
                    } catch (Throwable e) {}
                }
            } else if (set.getType() == byte.class || set.getType() == Byte.class) {
                try {
                    short v = Short.parseShort(string);
                    if (v >= -127 && v <= 127) {
                        set.setAccessible(true);
                        byte b = (byte)v;
                        if (set.getType() == Byte.class) {
                            set.set(target, b);
                        } else {
                            set.setByte(target, b);
                        }
                    }
                } catch (Throwable e) {}
            } else if (set.getType() == boolean.class || set.getType() == Boolean.class) {
                try {
                    Boolean b = bool(string);
                    if (b != null) {
                        set.setAccessible(true);
                        if (set.getType() == Boolean.class) {
                            set.set(target, b);
                        } else {
                            set.setBoolean(target, b);
                        }
                    }
                } catch (Throwable e) {}
            }

            return;
        }

        //6、枚举支持
        // 6.1 字符串到枚举
        if (set.getType().isEnum()) {
            if (value.getClass() == set.getType()) {
                try {
                    set.setAccessible(true);
                    set.set(target, value);
                } catch (Throwable e) {}
            } else if (value instanceof CharSequence) {
                String strName = value.toString();
                Object[] enumValues = set.getType().getEnumConstants();
                for (Object enumV : enumValues){
                    if (strName.equals(enumV.toString())) {
                        try {
                            set.setAccessible(true);
                            set.set(target, enumV);
                        } catch (Throwable e) {}
                        break;
                    }
                }
            }

            return;
        }
        // 6.2 枚举到字符串
        else if (set.getType() == String.class && value.getClass().isEnum()) {
            try {
                set.setAccessible(true);
                set.set(target, ((Enum)value).name());
            } catch (Throwable e) {}
            return;
        }

        // 7、其他复杂类型(非泛型)
        if (set.getGenericType() == set.getType()) {
            Object fvalue = null;
            try {
                fvalue = set.getType().newInstance();
            } catch (Throwable e) {e.printStackTrace();}

            if (fvalue != null) {
                fill(value,fvalue,implicit,root);
                try {
                    set.setAccessible(true);
                    set.set(target, fvalue);
                } catch (Throwable e) {}
            }
            return;
        }

        // 最后尝试一下
        try {
            set.setAccessible(true);
            set.set(target, value);
        } catch (Throwable e) {}
    }

    private static void setArrayFieldValue(Object target, Field set, Class<?> type, Object value, boolean implicit, Class root) {
        // array类型一致，直接赋值 (存在问题，数据未经过深拷贝)---（忽略）
        if (set.getType() == value.getClass()) {
            try {
                set.setAccessible(true);
                set.set(target, value);
            } catch (Throwable e) {}
            return;
        }

        int size = 0;
        Object souceArray = null;
        if (value.getClass().isArray()) {
            size = Array.getLength(value);
            souceArray = value;
        } else if (value instanceof Collection) {
            size = ((Collection) value).size();
            souceArray = ((Collection) value).toArray();
        }

        if (size == 0) {//不支持情况
            return;
        }

        Class<?> setType = set.getType();
        Class<?> eleType = setType.getComponentType();
        Object valueArray = null;
        // 基础数据类型array
        if (setType == boolean[].class) {
            valueArray = new boolean[size];
        } else if (setType == char[].class) {
            valueArray = new char[size];
        } else if (setType == byte[].class) {
            valueArray = new byte[size];
        } else if (setType == int[].class) {
            valueArray = new int[size];
        } else if (setType == short[].class) {
            valueArray = new short[size];
        } else if (setType == long[].class) {
            valueArray = new long[size];
        } else if (setType == float[].class) {
            valueArray = new float[size];
        } else if (setType == double[].class) {
            valueArray = new double[size];
        }
        // 大写情况
        else if (setType == Boolean[].class) {
            valueArray = new Boolean[size];
        } else if (setType == Character[].class) {
            valueArray = new Character[size];
        } else if (setType == Byte[].class) {
            valueArray = new Byte[size];
        } else if (setType == Integer[].class) {
            valueArray = new Integer[size];
        } else if (setType == Short[].class) {
            valueArray = new Short[size];
        } else if (setType == Long[].class) {
            valueArray = new Long[size];
        } else if (setType == Float[].class) {
            valueArray = new Float[size];
        } else if (setType == Double[].class) {
            valueArray = new Double[size];
        }
        //字符串数组
        else if (setType == String[].class) {
            valueArray = new String[size];
        } else {//其他为对象类型的array,先用list装载
            Object varray = null;
            try {
                varray = Array.newInstance(eleType,size);
            } catch (Throwable e) {e.printStackTrace();}
            if (varray == null) {//无法创建array
                return;
            }
            valueArray = varray;
        }

        if (souceArray != null && valueArray != null) {
            fillArray(souceArray,valueArray,implicit,root);
            try {
                set.setAccessible(true);
                set.set(target, valueArray);
            } catch (Throwable e) {}
        }
    }

    private static void setGenericFieldValue(Object target, Field set, Class<?> type, Object value, boolean implicit, Class root) {
        // 输入源进行要求
        int size = 0;
        Object array = null;
        if (value.getClass().isArray()) {
            size = Array.getLength(value);
            array = value;
        } else if (value instanceof Collection) {
            size = ((Collection) value).size();
            array = ((Collection) value).toArray();
        }

        if (size == 0) {
            return;
        }

        Class<?> setType = set.getType();
        Class<?> eleType = null;
        Type genericType;
        try {
            genericType = ((ParameterizedTypeImpl) set.getGenericType()).getActualTypeArguments()[0];
        } catch (Throwable throwable) {
            return;
        }
        try {
            eleType = Class.forName(((Class) genericType).getName(), true, Thread.currentThread().getContextClassLoader());
        } catch (Exception e) {
            return;
        }

        //无法实例化出来，无意义
        if (eleType == null) {
            return;
        }

        //1、支持简单容器
        Collection valueCollection = null;
        if (ArrayList.class.isAssignableFrom(setType)) {
            valueCollection = new ArrayList();
        } else if (LinkedList.class.isAssignableFrom(setType)) {
            valueCollection = new LinkedList();
        } else if (Stack.class.isAssignableFrom(setType)) {
            valueCollection = new Stack();
        } else if (Vector.class.isAssignableFrom(setType)) {
            valueCollection = new Vector();
        } else if (List.class.isAssignableFrom(setType)) {
            valueCollection = new ArrayList();
        } else if (HashSet.class.isAssignableFrom(setType)) {
            valueCollection = new HashSet();
        } else if (TreeSet.class.isAssignableFrom(setType)) {
            valueCollection = new TreeSet();
        } else if (Set.class.isAssignableFrom(setType)) {
            valueCollection = new HashSet();
        }

        //其他不支持（自定义泛型自求多福泛型，强制设置一遍）
        if (valueCollection == null) {
            try {
                set.setAccessible(true);
                set.set(target, value);
            } catch (Throwable e) {}
            return;
        }

        fillCollection(array,valueCollection,eleType,implicit,root);

        try {
            set.setAccessible(true);
            set.set(target, valueCollection);
        } catch (Throwable e) {}
    }

    private static boolean isNumericalValue(Object value) {
        if (value instanceof CharSequence || isBaseType(value.getClass())) {
            return true;
        }
        return false;
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
