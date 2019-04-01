package com.venus.esb.lang;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.venus.esb.factory.ESBBeanFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lingminjun on 17/4/25.
 */
public final class ESBT {

    //字符串是否为空
    public static boolean isEmpty(final CharSequence value) {
        return value == null || value.length() == 0;
    }

    public static String dense(final CharSequence value) {
        if (value == null) {
            return null;
        }
        return value.toString().replaceAll("\\s*", "");
    }

    public static String string(final CharSequence value) {
        return string(value,"");
    }

    public static String string(final CharSequence value, final String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value.toString();
    }

    /**
     * YES,ON,1,TRUE,Y,T 统统返回 true,
     * NO,OFF,0,FALSE,N,F 统统返回 false,
     * 其他返回默认值
     * @param bool
     * @param defaultValue
     * @return
     */
    public static boolean bool(final CharSequence bool, final boolean defaultValue) {
        if (bool == null) {return defaultValue;}

        String b = bool.toString().trim();
        if (b == null || b.length() == 0) {
            return defaultValue;
        }

        if (b.equalsIgnoreCase("yes")
                || b.equalsIgnoreCase("on")
                || b.equalsIgnoreCase("1")
                || b.equalsIgnoreCase("true")
                || b.equalsIgnoreCase("y")
                || b.equalsIgnoreCase("t")) {
            return true;
        } else if (b.equalsIgnoreCase("no")
                || b.equalsIgnoreCase("off")
                || b.equalsIgnoreCase("0")
                || b.equalsIgnoreCase("false")
                || b.equalsIgnoreCase("n")
                || b.equalsIgnoreCase("f")) {
            return false;
        }

        return defaultValue;
    }

    /**
     * YES,ON,1,TRUE,Y 统统返回 true,
     * NO,OFF,0,FALSE,N 统统返回 false,
     * 其他返回默认值
     * @param bool
     * @return
     */
    public static boolean bool(final CharSequence bool) {
        return bool(bool,false);
    }

    public static boolean bool(final Boolean bool, final boolean defaultValue) {
        if (bool == null) {
            return defaultValue;
        }
        return bool.booleanValue();
    }

    public static boolean bool(final Boolean bool) {
        return bool(bool,false);
    }

    /**
     * value不合法时,返回defaultValue
     * @param value
     * @param defaultValue
     * @return
     */
    public static int integer(final CharSequence value, final int defaultValue) {
        if (value == null) {return defaultValue;}

        String b = value.toString().trim();
        if (b == null || b.length() == 0) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(b);
        } catch (Throwable e) {}

        return defaultValue;
    }

    /**
     * 默认值为0
     * @param value
     * @return
     */
    public static int integer(final CharSequence value) {
        return integer(value,0);
    }

    public static int integer(final Integer value, final int defaultValue) {
        if (value == null) {
            return value;
        }
        return value.intValue();
    }
    public static int integer(final Integer value) {
        return integer(value,0);
    }

    /**
         * value不合法时,返回defaultValue
         * @param value
         * @param defaultValue
         * @return
         */
    public static short shortInteger(final CharSequence value, final short defaultValue) {
        if (value == null) {return defaultValue;}

        String b = value.toString().trim();
        if (b == null || b.length() == 0) {
            return defaultValue;
        }

        try {
            return Short.parseShort(b);
        } catch (Throwable e) {}

        return defaultValue;
    }

    /**
     * 默认值为0
     * @param value
     * @return
     */
    public static short shortInteger(final CharSequence value) {
        return shortInteger(value,(short) 0);
    }

    public static short shortInteger(final Short value, final short defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value.shortValue();
    }

    public static short shortInteger(final Short value) {
        return shortInteger(value,(short) 0);
    }

    /**
     * value不合法时,返回defaultValue
     * @param value
     * @param defaultValue
     * @return
     */
    public static long longInteger(final CharSequence value, final long defaultValue) {
        if (value == null) {return defaultValue;}

        String b = value.toString().trim();
        if (b == null || b.length() == 0) {
            return defaultValue;
        }

        try {
            return Long.parseLong(b);
        } catch (Throwable e) {}

        return defaultValue;
    }

    /**
     * 默认值为0
     * @param value
     * @return
     */
    public static long longInteger(final CharSequence value) {
        return longInteger(value,0l);
    }

    public static long longInteger(final Long value, final long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value.longValue();
    }

    public static long longInteger(final Long value) {
        return longInteger(value,0l);
    }


    /**
     * value不合法时,返回defaultValue
     * @param value
     * @param defaultValue
     * @return
     */
    public static char character(final CharSequence value, final char defaultValue) {
        if (value == null) {return defaultValue;}

        String b = value.toString().trim();
        if (b == null || b.length() != 0) {
            return defaultValue;
        }

        try {
            return b.charAt(0);
        } catch (Throwable e) {}

        return defaultValue;
    }

    /**
     * 默认值为0
     * @param value
     * @return
     */
    public static char character(final CharSequence value) {
        return character(value,(char) 0);
    }

    public static char character(final Character value, final char defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value.charValue();
    }

    /**
     * 默认值为0
     * @param value
     * @return
     */
    public static char character(final Character value) {
        return character(value,(char) 0);
    }


    /**
     * value不合法时,返回defaultValue
     * @param value
     * @param defaultValue
     * @return
     */
    public static float floatDecimal(final CharSequence value, final float defaultValue) {
        if (value == null) {return defaultValue;}

        String b = value.toString().trim();
        if (b == null || b.length() == 0) {
            return defaultValue;
        }

        try {
            return Float.parseFloat(b);
        } catch (Throwable e) {}

        return defaultValue;
    }

    /**
     * 默认值为0
     * @param value
     * @return
     */
    public static float floatDecimal(final CharSequence value) {
        return floatDecimal(value,(float) 0);
    }

    public static float floatDecimal(final Float value, final float defaultValue) {
        if (value == null) {
            return value;
        }
        return value.floatValue();
    }

    /**
     * 默认值为0
     * @param value
     * @return
     */
    public static float floatDecimal(final Float value) {
        return floatDecimal(value,(float) 0);
    }

    /**
     * value不合法时,返回defaultValue
     * @param value
     * @param defaultValue
     * @return
     */
    public static double doubleDecimal(final CharSequence value, final double defaultValue) {
        if (value == null) {return defaultValue;}

        String b = value.toString().trim();
        if (b == null || b.length() == 0) {
            return defaultValue;
        }

        try {
            return Double.parseDouble(b);
        } catch (Throwable e) {}

        return defaultValue;
    }

    /**
     * 默认值为0
     * @param value
     * @return
     */
    public static double doubleDecimal(final CharSequence value) {
        return doubleDecimal(value,(double) 0);
    }

    public static double doubleDecimal(final Double value, final double defaultValue) {
        if (value == null) {
            return value;
        }
        return value.doubleValue();
    }

    /**
     * 默认值为0
     * @param value
     * @return
     */
    public static double doubleDecimal(final Double value) {
        return doubleDecimal(value,(double) 0);
    }

    public static double doubleDecimal(final Float value, final double defaultValue) {
        if (value == null) {
            return value;
        }
        return value.doubleValue();
    }

    /**
     * 默认值为0
     * @param value
     * @return
     */
    public static double doubleDecimal(final Float value) {
        return doubleDecimal(value,(double) 0);
    }

    public static byte byteNumber(final Byte value, final byte defaultValue) {
        if (value == null) {
            return value;
        }
        return value.byteValue();
    }

    /**
     * 默认值为0
     * @param value
     * @return
     */
    public static byte byteNumber(final Byte value) {
        return byteNumber(value,(byte) 0);
    }

    /**
     * 数组转换
     * @param array
     * @return
     */
    public static boolean[] bools(final Boolean[] array) {
        if (array == null) {return null;}
        boolean[] ary = new boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            ary[i] = bool(array[i]);
        }
        return ary;
    }
    public static Boolean[] bools(final boolean[] array) {
        if (array == null) {return null;}
        Boolean[] ary = new Boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            ary[i] = array[i];
        }
        return ary;
    }
    public static boolean[] bools(final List array) {
        if (array == null) {return null;}
        boolean[] ary = new boolean[array.size()];
        for (int i = 0; i < array.size(); i++) {
            Object obj = array.get(i);
            if (obj instanceof CharSequence) {
                ary[i] = bool((CharSequence)obj);
            } else if (obj instanceof Boolean) {
                ary[i] = ((Boolean) obj).booleanValue();
            } else if (obj instanceof Number) {
                ary[i] = ((Number) obj).longValue() != 0l;
            }
        }
        return ary;
    }

    public static char[] chars(final Character[] array) {
        if (array == null) {return null;}
        char[] ary = new char[array.length];
        for (int i = 0; i < array.length; i++) {
            ary[i] = character(array[i]);
        }
        return ary;
    }
    public static Character[] chars(final char[] array) {
        if (array == null) {return null;}
        Character[] ary = new Character[array.length];
        for (int i = 0; i < array.length; i++) {
            ary[i] = array[i];
        }
        return ary;
    }
    public static char[] chars(final List array) {
        if (array == null) {return null;}
        char[] ary = new char[array.size()];
        for (int i = 0; i < array.size(); i++) {
            Object obj = array.get(i);
            if (obj instanceof CharSequence) {
                ary[i] = character((CharSequence) obj);
            } else if (obj instanceof Character) {
                ary[i] = ((Character) obj).charValue();
            } else if (obj instanceof Number) {
                ary[i] = (char) ((Number) obj).shortValue();
            }
        }
        return ary;
    }

    public static byte[] bytes(final Byte[] array) {
        if (array == null) {return null;}
        byte[] ary = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            ary[i] = byteNumber(array[i]);
        }
        return ary;
    }
    public static Byte[] bytes(final byte[] array) {
        if (array == null) {return null;}
        Byte[] ary = new Byte[array.length];
        for (int i = 0; i < array.length; i++) {
            ary[i] = array[i];
        }
        return ary;
    }
    public static byte[] bytes(final List array) {
        if (array == null) {return null;}
        byte[] ary = new byte[array.size()];
        for (int i = 0; i < array.size(); i++) {
            Object obj = array.get(i);
            if (obj instanceof Number) {
                ary[i] = ((Number) obj).byteValue();
            }
        }
        return ary;
    }

    public static short[] shorts(final Short[] array) {
        if (array == null) {return null;}
        short[] ary = new short[array.length];
        for (int i = 0; i < array.length; i++) {
            ary[i] = shortInteger(array[i]);
        }
        return ary;
    }
    public static Short[] shorts(final short[] array) {
        if (array == null) {return null;}
        Short[] ary = new Short[array.length];
        for (int i = 0; i < array.length; i++) {
            ary[i] = array[i];
        }
        return ary;
    }
    public static short[] shorts(final List array) {
        if (array == null) {return null;}
        short[] ary = new short[array.size()];
        for (int i = 0; i < array.size(); i++) {
            Object obj = array.get(i);
            if (obj instanceof CharSequence) {
                ary[i] = shortInteger((CharSequence)obj);
            } else if (obj instanceof Number) {
                ary[i] = ((Number) obj).shortValue();
            }
        }
        return ary;
    }

    public static int[] integers(final Integer[] array) {
        if (array == null) {return null;}
        int[] ary = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            ary[i] = integer(array[i]);
        }
        return ary;
    }
    public static Integer[] integers(final int[] array) {
        if (array == null) {return null;}
        Integer[] ary = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            ary[i] = array[i];
        }
        return ary;
    }
    public static int[] integers(final List array) {
        if (array == null) {return null;}
        int[] ary = new int[array.size()];
        for (int i = 0; i < array.size(); i++) {
            Object obj = array.get(i);
            if (obj instanceof CharSequence) {
                ary[i] = integer((CharSequence)obj);
            } else if (obj instanceof Number) {
                ary[i] = ((Number) obj).intValue();
            }
        }
        return ary;
    }

    public static long[] longs(final Long[] array) {
        if (array == null) {return null;}
        long[] ary = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            ary[i] = longInteger(array[i]);
        }
        return ary;
    }
    public static Long[] longs(final long[] array) {
        if (array == null) {return null;}
        Long[] ary = new Long[array.length];
        for (int i = 0; i < array.length; i++) {
            ary[i] = array[i];
        }
        return ary;
    }
    public static long[] longs(final List array) {
        if (array == null) {return null;}
        long[] ary = new long[array.size()];
        for (int i = 0; i < array.size(); i++) {
            Object obj = array.get(i);
            if (obj instanceof CharSequence) {
                ary[i] = longInteger((CharSequence)obj);
            } else if (obj instanceof Number) {
                ary[i] = ((Number) obj).longValue();
            }
        }
        return ary;
    }

    public static float[] floats(final Float[] array) {
        if (array == null) {return null;}
        float[] ary = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            ary[i] = floatDecimal(array[i]);
        }
        return ary;
    }
    public static Float[] floats(final float[] array) {
        if (array == null) {return null;}
        Float[] ary = new Float[array.length];
        for (int i = 0; i < array.length; i++) {
            ary[i] = array[i];
        }
        return ary;
    }
    public static float[] floats(final List array) {
        if (array == null) {return null;}
        float[] ary = new float[array.size()];
        for (int i = 0; i < array.size(); i++) {
            Object obj = array.get(i);
            if (obj instanceof CharSequence) {
                ary[i] = floatDecimal((CharSequence)obj);
            } else if (obj instanceof Number) {
                ary[i] = ((Number) obj).floatValue();
            }
        }
        return ary;
    }

    public static double[] doubles(final Double[] array) {
        if (array == null) {return null;}
        double[] ary = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            ary[i] = doubleDecimal(array[i]);
        }
        return ary;
    }
    public static Double[] doubles(final double[] array) {
        if (array == null) {return null;}
        Double[] ary = new Double[array.length];
        for (int i = 0; i < array.length; i++) {
            ary[i] = array[i];
        }
        return ary;
    }
    public static double[] doubles(final List array) {
        if (array == null) {return null;}
        double[] ary = new double[array.size()];
        for (int i = 0; i < array.size(); i++) {
            Object obj = array.get(i);
            if (obj instanceof CharSequence) {
                ary[i] = doubleDecimal((CharSequence)obj);
            } else if (obj instanceof Number) {
                ary[i] = ((Number) obj).doubleValue();
            }
        }
        return ary;
    }


    /**
     * value不合法返回null,仅仅支持isBaseType()为true的类型,数组必须以[]包裹,号分割
     * 字符串类型,可以用''或者""包裹,以防止内容含逗号,
     * 注意字符串数组形式请防止嵌套符号串,此处简单分割,并不做完整性校验
     * @param value
     * @param type
     * @return
     */
    public static Object value(final CharSequence value, Class<?> type) {
        if (value == null) {return null;}

        if (type == String.class) {
            return value.toString();
        } else if (type == int.class || type == Integer.class) {
            return integer(value);
        } else if (type == byte.class || type == Byte.class) {
            return integer(value);
        } else if (type == short.class || type == Short.class) {
            return shortInteger(value);
        } else if (type == long.class || type == Long.class) {
            return longInteger(value);
        } else if (type == float.class || type == Float.class) {
            return floatDecimal(value);
        } else if (type == double.class || type == Double.class) {
            return doubleDecimal(value);
        } else if (type == char.class || type == Character.class) {
            return character(value);
        } else if (type == boolean.class || type == Boolean.class) {
            return bool(value);
        } else if (type.isArray()) {//逗号分割也有问题
            String str = value.toString().trim();
            if (!str.startsWith("[") || !str.endsWith("]")) {
                return null;
            }
            int length = str.length();
            str = str.substring(1, length - 1);
            if (type == String[].class) {
                length = str.length();
                if (str.startsWith("'") && str.endsWith("'")) {
                    return str.substring(1,length-1).split("','");
                } else if (str.startsWith("\"") && str.endsWith("\"")) {
                    return str.substring(1,length-1).split("\",\"");
                } else {
                    return str.split(",");
                }
            }

            String[] strs = str.split(",");
            List<Object> list = new ArrayList<Object>();
            for (String s : strs) {
                if (s.trim().length() == 0) {
                    continue;
                }
                if (type == int[].class || type == Integer[].class) {
                    list.add(integer(s));
                } else if (type == byte[].class || type == Byte[].class) {
                    list.add(integer(s));
                } else if (type == short[].class || type == Short[].class) {
                    list.add(shortInteger(s));
                } else if (type == long[].class || type == Long[].class) {
                    list.add(longInteger(s));
                } else if (type == float[].class || type == Float[].class) {
                    list.add(floatDecimal(s));
                } else if (type == double[].class || type == Double[].class) {
                    list.add(doubleDecimal(s));
                } else if (type == char[].class || type == Character[].class) {
                    list.add(character(s));
                } else if (type == boolean[].class || type == Boolean[].class) {
                    list.add(bool(s));
                }
            }

            if (type == int[].class ) {
                return integers(list);
            } else if (type == Integer[].class) {
                return list.toArray(new Integer[0]);
            } else if (type == byte[].class) {
                return bytes(list);
            } else if (type == Byte[].class) {
                return list.toArray(new Byte[0]);
            } else if (type == short[].class) {
                return shorts(list);
            } else if (type == Short[].class) {
                return list.toArray(new Short[0]);
            } else if (type == long[].class) {
                return longs(list);
            } else if (type == Long[].class) {
                return list.toArray(new Long[0]);
            } else if (type == float[].class) {
                return floats(list);
            } else if (type == Float[].class) {
                return list.toArray(new Float[0]);
            } else if (type == double[].class) {
                return doubles(list);
            } else if (type == Double[].class) {
                return list.toArray(new Double[0]);
            } else if (type == char[].class) {
                return chars(list);
            } else if (type == Character[].class) {
                return list.toArray(new Character[0]);
            } else if (type == boolean[].class) {
                return bools(list);
            } else if (type == Boolean[].class) {
                return list.toArray(new Boolean[0]);
            }
        }

        return null;
    }

    /**
     * 基础类型
     *
     * @see     java.lang.Boolean#TYPE
     * @see     java.lang.Character#TYPE
     * @see     java.lang.Byte#TYPE
     * @see     java.lang.Short#TYPE
     * @see     java.lang.Integer#TYPE
     * @see     java.lang.Long#TYPE
     * @see     java.lang.Float#TYPE
     * @see     java.lang.Double#TYPE
     * @see     java.lang.Void#TYPE
     *
     * @param clazz
     * @return
     */
    public static boolean isBaseType(Class<?> clazz) {
        if (clazz==null) {return false;}
        if (boolean.class == clazz
                || char.class == clazz
                || byte.class == clazz
                || short.class == clazz
                || int.class == clazz
                || long.class == clazz
                || float.class == clazz
                || double.class == clazz
                || String.class == clazz
                || boolean[].class == clazz
                || byte[].class == clazz
                || char[].class == clazz
                || short[].class == clazz
                || int[].class == clazz
                || long[].class == clazz
                || float[].class == clazz
                || double[].class == clazz
                || String[].class == clazz) {
            return true;
        }

        //Byte,Integer,Short,Long,Float,Double
        if (clazz.isPrimitive()
                || Boolean.class == clazz
                || Character.class == clazz
                || Byte.class == clazz
                || Short.class == clazz
                || Integer.class == clazz
                || Long.class == clazz
                || Float.class == clazz
                || Double.class == clazz
                || Boolean[].class == clazz
                || Character[].class == clazz
                || Byte[].class == clazz
                || Short[].class == clazz
                || Integer[].class == clazz
                || Long[].class == clazz
                || Float[].class == clazz
                || Double[].class == clazz ) {
            return true;
        }

        // 日期支持
//        if (Date.class == clazz || Date[].class == clazz) {
//            return true;
//        }

        return false;
    }

    /**
     * 基础类型
     *
     * @see     java.lang.Boolean#TYPE
     * @see     java.lang.Character#TYPE
     * @see     java.lang.Byte#TYPE
     * @see     java.lang.Short#TYPE
     * @see     java.lang.Integer#TYPE
     * @see     java.lang.Long#TYPE
     * @see     java.lang.Float#TYPE
     * @see     java.lang.Double#TYPE
     * @see     java.lang.Void#TYPE
     *
     * @param finalType
     * @return
     */
    public static boolean isBaseType(String finalType) {
        if (finalType==null || finalType.length() == 0) {return false;}
        if (finalType.equals("boolean")
                || finalType.equals("bool")
                || finalType.equals("char")
                || finalType.equals("byte")
                || finalType.equals("short")
                || finalType.equals("int")
                || finalType.equals("long")
                || finalType.equals("float")
                || finalType.equals("double")
                || finalType.equals("Boolean")
                || finalType.equals("Character")
                || finalType.equals("Byte")
                || finalType.equals("Short")
                || finalType.equals("Integer")
                || finalType.equals("Long")
                || finalType.equals("Float")
                || finalType.equals("Double")
                || finalType.equals("String")
                || finalType.equals("java.lang.Boolean")
                || finalType.equals("java.lang.Character")
                || finalType.equals("java.lang.Byte")
                || finalType.equals("java.lang.Short")
                || finalType.equals("java.lang.Integer")
                || finalType.equals("java.lang.Long")
                || finalType.equals("java.lang.Float")
                || finalType.equals("java.lang.Double")
                || finalType.equals("java.lang.String")
                || finalType.equals("[Z")
                || finalType.equals("[C")
                || finalType.equals("[B")
                || finalType.equals("[S")
                || finalType.equals("[I")
                || finalType.equals("[J")
                || finalType.equals("[F")
                || finalType.equals("[D")
                || finalType.equals("[Ljava.lang.String;")
                || finalType.equals("[Ljava.lang.Boolean;")
                || finalType.equals("[Ljava.lang.Character;")
                || finalType.equals("[Ljava.lang.Byte;")
                || finalType.equals("[Ljava.lang.Short;")
                || finalType.equals("[Ljava.lang.Integer;")
                || finalType.equals("[Ljava.lang.Long;")
                || finalType.equals("[Ljava.lang.Float;")
                || finalType.equals("[Ljava.lang.Double;")
                ) {
            return true;
        }

        // 日期支持
//        if (finalType.equals("java.util.Date") || finalType.equals("[Ljava.util.Date;")) {
//            return true;
//        }

        return false;
    }

    /**
     * 基础数据类型 isPrimitive
     *
     * @see     boolean
     * @see     char
     * @see     byte
     * @see     short
     * @see     int
     * @see     long
     * @see     float
     * @see     double
     *
     * @param finalType
     * @return
     */
    public static boolean isPrimitiveType(String finalType) {
        if (finalType==null || finalType.length() == 0) {return false;}
        if (finalType.equals("boolean")
                || finalType.equals("bool")
                || finalType.equals("char")
                || finalType.equals("byte")
                || finalType.equals("short")
                || finalType.equals("int")
                || finalType.equals("long")
                || finalType.equals("float")
                || finalType.equals("double")
                ) {
            return true;
        }

        return false;
    }

    /**
     * 基础数据类型 isPrimitive
     *
     * @see     boolean
     * @see     char
     * @see     byte
     * @see     short
     * @see     int
     * @see     long
     * @see     float
     * @see     double
     *
     * @param clazz
     * @return
     */
    public static boolean isPrimitiveType(Class<?> clazz) {
        if (clazz==null) {return false;}
        if (boolean.class == clazz
                || char.class == clazz
                || byte.class == clazz
                || short.class == clazz
                || int.class == clazz
                || long.class == clazz
                || float.class == clazz
                || double.class == clazz
                ) {
            return true;
        }
        return false;
    }

    /**
     * 基础数据类型，初始化默认值
     * @param finalType
     * @return
     */
    public static String defaultPrimitiveValue(String finalType) {
        if (finalType==null || finalType.length() == 0) {return null;}
        if (finalType.equals("boolean")) {return "false"; }
        else if (finalType.equals("bool")) {return "false"; }
        else if (finalType.equals("char")) {return "0"; }
        else if (finalType.equals("byte")) {return "0"; }
        else if (finalType.equals("short")) {return "0"; }
        else if (finalType.equals("int")) {return "0"; }
        else if (finalType.equals("long")) {return "0"; }
        else if (finalType.equals("float")) {return "0"; }
        else if (finalType.equals("double")) {return "0"; }
        else {return null;}
    }

    /**
     * 基础数据类型，初始化默认值
     * @param clazz
     * @return
     */
    public static String defaultPrimitiveValue(Class<?> clazz) {
        if (clazz==null) {return null;}
        if (clazz == boolean.class) {return "false"; }
        else if (clazz == char.class) {return "0"; }
        else if (clazz == byte.class) {return "0"; }
        else if (clazz == short.class) {return "0"; }
        else if (clazz == int.class) {return "0"; }
        else if (clazz == long.class) {return "0"; }
        else if (clazz == float.class) {return "0"; }
        else if (clazz == double.class) {return "0"; }
        else {return null;}
    }


    /**
     * 包装成Array类型 , 若已经是array则不发生变化,若list将拆包
     * @param finalType
     * @return
     */
    public static String packArrayType(String finalType) {
        if (finalType == null) {
            return finalType;
        }
        if (finalType.startsWith("[")) {
            return finalType;
        }

        if ("int".equals(finalType)) {
            return "[I";
        } else if ("short".equals(finalType)) {
            return "[S";
        } else if ("long".equals(finalType)) {
            return "[J";
        } else if ("float".equals(finalType)) {
            return "[F";
        } else if ("double".equals(finalType)) {
            return "[D";
        } else if ("boolean".equals(finalType)) {
            return "[Z";
        } else if ("bool".equals(finalType)) {
            return "[Z";
        } else if ("byte".equals(finalType)) {
            return "[B";
        } else if ("char".equals(finalType)) {
            return "[C";
        }

        // 大写如何显示
        else if ("Integer".equals(finalType)) {
            return "[java.lang.Integer;";
        } else if ("Short".equals(finalType)) {
            return "[java.lang.Short;";
        } else if ("Long".equals(finalType)) {
            return "[java.lang.Long;";
        } else if ("Float".equals(finalType)) {
            return "[java.lang.Float;";
        } else if ("Double".equals(finalType)) {
            return "[java.lang.Double;";
        } else if ("Boolean".equals(finalType)) {
            return "[java.lang.Boolean;";
        } else if ("Byte".equals(finalType)) {
            return "[java.lang.Byte;";
        } else if ("Character".equals(finalType)) {
            return "[java.lang.Character;";
        }

        else if (finalType.startsWith("java.util.List<")
                || finalType.startsWith("java.util.ArrayList<")) {//list的时候,直接拼接
            int idx = finalType.indexOf("<");
            return "[L"+finalType.substring(idx+1,finalType.length()-1)+";";
        } else {
            return "[L"+finalType + ";";
        }
    }

    /**
     * 从name加载class
     * @param name
     * @return
     */
    public static Class<?> classForName(String name) {
        if (ESBT.isEmpty(name)) {
            return null;
        }
        try {
            if (name.equals("boolean")) {
                return boolean.class;
            } else if (name.equals("bool")) {
                return boolean.class;
            } else if (name.equals("byte")) {
                return byte.class;
            } else if (name.equals("char")) {
                return char.class;
            } else if (name.equals("int")) {
                return int.class;
            } else if (name.equals("long")) {
                return long.class;
            } else if (name.equals("float")) {
                return float.class;
            } else if (name.equals("double")) {
                return double.class;
            } else {
                return Class.forName(name, true, Thread.currentThread().getContextClassLoader());
            }
        } catch (Throwable e) {/*e.printStackTrace();*/}
        return null;
    }

    /**
     * 加载type类型的实例
     * @param className
     * @param type 类型限定
     * @param <T>
     * @return
     */
    public static <T extends Object> T createObject(String className, Class<T> type) {
        if (!ESBT.isEmpty(className)) {
            try {
                Class<?> clazz = ESBT.classForName(className);
                if (clazz != null && type.isAssignableFrom(clazz)) {
                    return (T)clazz.newInstance();
                }
            } catch (Throwable e) {}
        }
        return null;
    }

    /**
     * 取拆包类型,非array和list类型
     * @return
     */
    public static String convertCoreType(String finalType) {
        if (ESBT.isEmpty(finalType)) {
            return "";
        }

        if ("[I".equals(finalType)) {
            return "int";
        } else if ("[S".equals(finalType)) {
            return "short";
        } else if ("[J".equals(finalType)) {
            return "long";
        } else if ("[F".equals(finalType)) {
            return "float";
        } else if ("[D".equals(finalType)) {
            return "double";
        } else if ("[Z".equals(finalType)) {
            return "boolean";
        } else if ("[B".equals(finalType)) {
            return "byte";
        } else if ("[C".equals(finalType)) {
            return "char";
        } else if (finalType.startsWith("[L") && finalType.endsWith(";")) {//arry的需要拆掉类型
            return finalType.substring(2,finalType.length() - 1);
        } else if (finalType.startsWith("java.util.List<")
                || finalType.startsWith("java.util.ArrayList<")) {//list的时候,直接拼接
            int idx = finalType.indexOf("<");
            return finalType.substring(idx+1,finalType.length()-1);
        } else {
            return finalType;
        }
    }

    /**
     * ESB中对类型描述
     * @return
     */
    public static String convertFinalType(String type, boolean isList, boolean isArray) {
        if (ESBT.isEmpty(type)) {
            return "";
        }

        if (isList) {
            return "java.util.List<"+type+">";
        } else if (isArray && !type.startsWith("[")) {
            return packArrayType(type);
        } else {
            return type;
        }
    }

    /**
     * 方法申明时需要的类型描述
     * @return
     */
    public static String convertDeclareType(String finalType) {
        if (finalType == null || finalType.length() == 0) {
            return "";
        }

        //方法签名实际是不看泛型的
        int idx = finalType.indexOf("<");
        if (idx >= 0 && idx < finalType.length()) {
            return finalType.substring(0, idx);
        }

        return finalType;
    }

    /**
     * 展示的java类型描述
     * @return
     */
    public static String convertDisplayType(String finalType) {
        if ("[I".equals(finalType)) {
            return "int[]";
        } else if ("[S".equals(finalType)) {
            return "short[]";
        } else if ("[J".equals(finalType)) {
            return "long[]";
        } else if ("[F".equals(finalType)) {
            return "float[]";
        } else if ("[D".equals(finalType)) {
            return "double[]";
        } else if ("[Z".equals(finalType)) {
            return "boolean[]";
        } else if ("[B".equals(finalType)) {
            return "byte[]";
        } else if ("[C".equals(finalType)) {
            return "char[]";
        } else if ("[Ljava.lang.String;".equals(finalType)) {
            return "String[]";
        } else if ("java.lang.String".equals(finalType)) {
            return "String";
        }

        else if ("[Ljava.lang.Boolean;".equals(finalType)) {
            return "Boolean[]";
        } else if ("java.lang.Boolean".equals(finalType)) {
            return "Boolean";
        } else if ("[Ljava.lang.Character;".equals(finalType)) {
            return "Character[]";
        } else if ("java.lang.Character".equals(finalType)) {
            return "Character";
        } else if ("[Ljava.lang.Byte;".equals(finalType)) {
            return "Byte[]";
        } else if ("java.lang.Byte".equals(finalType)) {
            return "Byte";
        } else if ("[Ljava.lang.Short;".equals(finalType)) {
            return "Short[]";
        } else if ("java.lang.Short".equals(finalType)) {
            return "Short";
        } else if ("[Ljava.lang.Integer;".equals(finalType)) {
            return "Integer[]";
        } else if ("java.lang.Integer".equals(finalType)) {
            return "Integer";
        } else if ("[Ljava.lang.Long;".equals(finalType)) {
            return "Long[]";
        } else if ("java.lang.Long".equals(finalType)) {
            return "Long";
        } else if ("[Ljava.lang.Float;".equals(finalType)) {
            return "Float[]";
        } else if ("java.lang.Float".equals(finalType)) {
            return "Float";
        } else if ("[Ljava.lang.Double;".equals(finalType)) {
            return "Double[]";
        } else if ("java.lang.Double".equals(finalType)) {
            return "Double";
        }

        else if (finalType.startsWith("[L") && finalType.endsWith(";")) {//arry的需要拆掉类型
            return finalType.substring(2,finalType.length() - 1) + "[]";
        } else if (finalType.startsWith("java.util.List<")
                || finalType.startsWith("java.util.ArrayList<")) {//list的时候,直接拼接
            int idx = finalType.indexOf("<");
            return "List<"+finalType.substring(idx+1,finalType.length()-1)+">";
        } else {
            return finalType;
        }
    }

    /**
     * 循环向上转型, 获取对象的 DeclaredField
     * 包含所有属性 private、protected、default、public
     * 说明:
     * 以下两个方法去属性范畴是所有(privte,protected,default,public),但是仅限于本类,不包含父类属性
     * public Field getDeclaredField (String name)
     * public Field[] getDeclaredFields ()
     *
     * 以下两个方法取值范围为(protected,default,public),包含父类继承的属性
     * public Field getField (String name)
     * public Field[] getFields ()
     *
     * @param clazz : 子类对象
     * @return 父类中的属性对象
     */
    public static Field[] getClassDeclaredFields(Class<?> clazz) {
        return getClassDeclaredFields(clazz,Object.class);
    }
    public static Field[] getClassDeclaredFields(Class<?> clazz, Class<?> root) {
        if (root == null) {
            root = Object.class;
        }

        //若父类和子类有同名属性,直接用子类的属性覆盖即可
        if (clazz == null || clazz == root) {
            return new Field[0];
        }

        ArrayList<Field> list = new ArrayList<Field>();
        HashSet<String> names = new HashSet<String>();
        for(; clazz != null && clazz != root; clazz = clazz.getSuperclass()) {
            try {
                Field[] flds = clazz.getDeclaredFields() ;
                for (Field fld : flds) {

                    //去掉static
                    if (Modifier.isStatic(fld.getModifiers())) {
                        continue;
                    }

                    //去重父类同名属性
                    if (names.contains(fld.getName())) {
                        continue;
                    }

                    names.add(fld.getName());
                    list.add(fld);
                }
            } catch (Throwable e) {
                //这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
                //如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了
            }
        }

        return list.toArray(new Field[0]);
    }

    /**
     * 获取某个对象的属性
     * @param object
     * @param fieldName
     * @return
     */
    public static Field getObjectDeclaredField(Object object, String fieldName){

        Class<?> clazz = object.getClass() ;
        for(; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                return clazz.getDeclaredField(fieldName) ;
            } catch (Throwable e) {
                //这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
                //如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了
            }
        }

        return null;
    }

    /**
     * 获取某个对象的属性值,路径为null或者数组,array越界,多无法取值
     * @param object
     * @param fieldPath 路径 this.person.name
     * @return
     */
    public static Object getValueForFieldPath(Object object, String fieldPath){
        if (object == null || fieldPath == null || fieldPath.length() == 0) {
            return object;
        }
        String[] paths = tidyFieldPaths(fieldPath);
        if (paths == null || paths.length == 0) {
            return object;
        }

        Object value = object;//
        for (int i = 0; i < paths.length; i++) {
            if (value == null) {
                return value;
            }

            String fieldName = paths[i];

            //对数组或者list的支持
            int begin = fieldName.indexOf("[");
            int idx = 0;
            if (begin >= 0 && begin < fieldName.length()) {
                idx = ESBT.integer(fieldName.substring(begin+1),fieldName.length() - 1);
                fieldName = fieldName.substring(0,begin);
            } else {
                begin = -1;
            }

            //处理 value本身是array或者list
            if (begin >= 0 && (fieldName == null || fieldName.length() == 0)) {
                return getSimilarArrayAtIndex(value,idx);
            }

            //字典类型取值或者反射取值
            if (value instanceof Map) {
                value = ((Map) value).get(fieldName);
            } else {//对于一般类型
                Field field = getObjectDeclaredField(value,fieldName);
                if (field == null) {
                    return null;
                }
                try {
                    field.setAccessible(true);
                    value = field.get(value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    value = null;
                }
            }

            if (value == null) {
                return null;
            }

            //数组取值
            if (begin >= 0) {
                return getSimilarArrayAtIndex(value,idx);
            }
        }
        return value;
    }

    /**
     * 设置对象的值,若路径是空,则不设置
     * @param object
     * @param fieldPath
     * @param value
     * @return
     */
    public static boolean setValueForFieldPath(Object object, String fieldPath, Object value){
        if (object == null || fieldPath == null || fieldPath.length() == 0) {
            return false;
        }
        String[] paths = tidyFieldPaths(fieldPath);
        if (paths == null || paths.length == 0) {
            return false;
        }

        //set很有趣,前面全部是get,故仅仅处理最后一个
        Object obj = object;//
        String fieldName = paths[0];
        if (paths.length > 1) {
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < paths.length - 1; i++) {
                if (i != 0) {
                    str.append(".");
                }
                str.append(paths[i]);
            }

            //处理最后一个
            fieldName = paths[paths.length - 1];
            obj = getValueForFieldPath(object,str.toString());
        }

        if (obj == null) {
            return false;
        }

        //对数组或者list的支持
        int begin = fieldName.indexOf("[");
        int idx = 0;
        if (begin >= 0 && begin < fieldName.length()) {
            idx = ESBT.integer(fieldName.substring(begin+1),fieldName.length() - 1);
            fieldName = fieldName.substring(0,begin);
        } else {
            begin = -1;
        }

        //去掉下标取值,然后get值
        if (begin >= 0 && fieldName.length() > 0) {
            obj = getValueForFieldPath(obj,fieldName);
        }

        //处理 array或者list
        if (begin >= 0) {
            return setSimilarArrayAtIndex(obj,value,idx);
        }

        //字典类型取值或者反射取值
        if (obj instanceof Map) {
            ((Map) obj).put(fieldName, value);
            return true;
        } else {//对于一般类型
            Field field = getObjectDeclaredField(obj,fieldName);
            if (field == null) {
                return false;
            }
            try {
                field.setAccessible(true);
                field.set(obj,value);
                return true;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    //判断是纯数字，空字符串将返回false
    public static boolean isDigit(CharSequence string) {
        if (ESBT.isEmpty(string)) {
            return false;
        }
        for (int idx = 0; idx < string.length(); idx++) {
            if (!Character.isDigit(string.charAt(idx))) {
                return false;
            }
        }
        return true;
    }

    private static final String THIS_PREFIX = "this";
    private static String[] tidyFieldPaths(String apath) {
        if (apath == null || apath.length() == 0) {
            return new String[0];
        }

        String path = apath;
        if (path.startsWith(THIS_PREFIX)) {
            path = path.substring(THIS_PREFIX.length());
        }

        if (path.startsWith(".")) {
            path = path.substring(1);
        }

        if (path.length() == 0) {
            return new String[0];
        }

        return path.split("\\.");
    }

    private static Object getSimilarArrayAtIndex(Object obj, int idx) {
        if (obj instanceof List) {
            List list = (List) obj;
            if (list.size() <= idx) {
                return null;
            }
            return list.get(idx);//越界将异常
        } else if (obj.getClass().isArray()) {
            if (Array.getLength(obj) <= idx) {
                return null;
            }
            return Array.get(obj,idx);
        } else {
            //对于一些类似array的类的支持,如:com.alibaba.dubbo.common.json.JSONArray、
            // com.google.gson.JsonArray、org.json.JSONArray等等
            try {
                Method method = obj.getClass().getMethod("get", new Class[] {int.class});
                if (method != null) {
                    return method.invoke(obj,idx);
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static boolean setSimilarArrayAtIndex(Object obj, Object value, int idx) {
        if (obj instanceof List) {
            List list = (List) obj;
            if (list.size() <= idx) {//支持add
                list.add(value);
            } else {
                list.set(idx, value);
            }
            return true;
        } else if (obj.getClass().isArray()) {
            if (Array.getLength(obj) <= idx) {
                return false;
            }
            Array.set(obj,idx,value);
        } else {//仅仅支持add方法
            //对于一些类似array的类的支持,
            // 如:com.alibaba.dubbo.common.json.JSONArray、
            // com.google.gson.JsonArray、org.json.JSONArray等等
            try {
                int size = -1;
                Method method = obj.getClass().getMethod("length", new Class[0]);
                if (method != null) {
                    Object o = method.invoke(obj);
                    if (o instanceof Integer) {
                        size = ((Integer) o).intValue();
                    }
                } else {
                    method = obj.getClass().getMethod("size", new Class[0]);
                    Object o = method.invoke(obj);
                    if (o instanceof Integer) {
                        size = ((Integer) o).intValue();
                    }
                }
                if (size < 0) {
                    return false;
                }

                if (size <= idx) {//支持add
                    Method mtd = obj.getClass().getMethod("add", new Class[] {Object.class});
                    if (mtd != null) {
                        mtd.invoke(obj,value);
                    }
                } else {
                    Method mtd = obj.getClass().getMethod("set", new Class[] {int.class, Object.class});
                    if (mtd != null) {
                        mtd.invoke(obj, idx, value);
                    }
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 获取服务名
     * @return
     */
    public static String getServiceName() {
        if (SERVICE_NAME != null) {
            return SERVICE_NAME;
        }

        synchronized(ESBT.class) {
            if (SERVICE_NAME != null) {
                return SERVICE_NAME;
            }

            String name = null;

            //先判断是否为web应用
            name = getWebAppName();

            if (name == null || name.length() == 0) {
                name = getDubboAppName();
            }

            if (name == null || name.length() == 0) {
                name = "service";
            }

            SERVICE_NAME = name;
        }

        return SERVICE_NAME;
    }
    private static String SERVICE_NAME = null;

    private static String getWebAppName() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        //只有WebappClassLoader才能断定是tomcat
        if (!loader.getClass().getSimpleName().contains("WebappClassLoader")) {
            return null;
        }

        //先去WebappClassLoader的contextName
        String str = null;//loader.getContextName();
        try {
            Method method = loader.getClass().getMethod("getContextName");
            if (method != null) {
                str = (String) method.invoke(loader,null);
            }
            if (str != null && str.length() > 0) {//取路径最后一段
                String ss[] = str.split("/");
                if (ss.length == 0) {
                    str = "";
                } else {
                    str = ss[ss.length - 1];
                }
            }
        } catch (Throwable e) {e.printStackTrace();}
        if (str != null && str.length() > 0) {
            return str;
        }

        //取web配置
        InputStream in = null;
        try {

            //WebappClassLoader默认目录:WEB-INF/classes/;所以取上一级的web目录
            in = loader.getResourceAsStream("../web.xml");

            //直接借助spring load方法, xml加载顺序并不一致
//            ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
//            Resource[] resources = resourcePatternResolver.getResources("*/web.xml");
//            if (resources != null && resources.length > 0) {
//                in = resources[0].getInputStream();
//            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        //没有取到,直接返回WebServer
        if (in == null) {
            return "WebServer";
        }

        //最后读取目录的
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document = null;
        try {
            //DOM parser instance
            DocumentBuilder builder = factory.newDocumentBuilder();
            //parse an XML file into a DOM tree
            document = builder.parse(in);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (document == null) {
            return null;
        }


//        DOMParser parser = new DOMParser();
        Element rootElement = document.getDocumentElement();

        //root is web-app
        if (!"web-app".equals(rootElement.getNodeName())) {
            return null;
        }

        //直接取id
        String webName = rootElement.getAttribute("id");
        if (webName != null || webName.length() > 0) {
            return webName;
        }

        //traverse child elements
        NodeList nodes = rootElement.getChildNodes();
        for (int i=0; i < nodes.getLength(); i++)
        {
            Node node = nodes.item(i);
            if (node == null) {
                continue;
            }
            if ("display-name".equals(node.getNodeName())) {
                return node.getTextContent();
            }
        }

        return null;
    }

    private static String getDubboAppName() {
        Properties prop = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();//ESBConfigCenter.class.getClassLoader();

        try {
            prop.load(loader.getResourceAsStream("config.properties"));
        } catch (Throwable e) {
            return null;
        }

        return getDubboAppName(prop);
    }

//    public static void main(String[] vars) throws IOException {
//        Pattern pattern1 = Pattern.compile("(?!<\\!--)\\s*<bean[\\s\\S]+class=\\s*\"com.alibaba.dubbo.config.ApplicationConfig\"\\s*>\\s+");
//        Pattern pattern2 = Pattern.compile("(?!<\\!--)\\s*<dubbo:application[\\s\\S]+[\\s/]*>\\s+");
//        Pattern pattern3 = Pattern.compile("(?!<\\!--)\\s*<property[\\s]+name=\\s*\"name\"\\s+value=\\s*\"\\S+\"[\\s/]*>\\s+");
//
//        File s = new File("/Users/lingminjun/work/work_code/server/haitao-dubbo-rpc/dubbo-demo/dubbo-demo-consumer/src/main/resources/META-INF/spring/dubbo-demo-application.xml");
//        String content = readFile(s,ESBConsts.UTF8_STR);
//
//        Matcher matcher1 = pattern1.matcher(content);
//        Matcher matcher2 = pattern2.matcher(content);
//
//        if (matcher1.find()) {
//            int idx = matcher1.end();
//            content = content.substring(idx);
//            //在下面的内容中找到
//            Matcher matcher3 = pattern3.matcher(content);
//            if (matcher3.find()) {
//                String group = matcher3.group();
//                int b = group.indexOf("value=");
//                b = group.indexOf("\"",b);
//                int e = group.indexOf("\"",b+1);
//                String name =  group.substring(b+1,e);
//                System.out.println("name:" + name);
//            }
//        } else if (matcher2.find()) {
//            String group = matcher2.group();
//            int b = group.indexOf("name=");
//            b = group.indexOf("\"",b);
//            int e = group.indexOf("\"",b+1);
//            String name =  group.substring(b+1,e);
//            System.out.println("name:" + name);
//        }
//    }

    public static String getDubboAppName(Properties prop) {
        if (prop.getProperty("application.name") != null) {
            return prop.getProperty("application.name");
        } else if (prop.getProperty("dubbo.application.name") != null) {
            return prop.getProperty("dubbo.application.name");
        } else if (prop.getProperty("spring.dubbo.application.name") != null) {
            return prop.getProperty("spring.dubbo.application.name");
        }

        ApplicationConfig config = ESBBeanFactory.getBean(ApplicationConfig.class);
        if (config != null) {
            return config.getName();
        }

        //说明此时还未创建Bean,另一个方法就是去默认的目录中查找
//        Enumeration<URL> urls = null;
        Resource[] resources = null;
        try {
//            urls = Thread.currentThread().getContextClassLoader().getResources("classpath*:META-INF/spring/*.xml");

            //直接借助spring load方法, xml加载顺序并不一致
            ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
            resources = resourcePatternResolver.getResources("classpath*:META-INF/spring/*.xml");
        } catch (Throwable e) {
            return null;
        }

        //一种 <dubbo:application name="hehe_consumer" />
        //另一种
        // <bean id="dubboApplicationConfig" class="com.alibaba.dubbo.config.ApplicationConfig">
        //      <property name="name" value="demo-provider"/>
        // </bean>
        //针对注释的存在问题
        Pattern pattern1 = Pattern.compile("(?!<\\!--)\\s*<bean[\\s\\S]+class=\\s*\"com.alibaba.dubbo.config.ApplicationConfig\"\\s*>\\s+");
        Pattern pattern2 = Pattern.compile("(?!<\\!--)\\s*<dubbo:application[\\s\\S]+[\\s/]*>\\s+");
        Pattern pattern3 = Pattern.compile("(?!<\\!--)\\s*<property[\\s]+name=\\s*\"name\"\\s+value=\\s*\"\\S+\"[\\s/]*>\\s+");
        for (Resource resource : resources) {

            //不能取到文件 resource无法取到文件
            InputStream s = null;
            try {
                s = resource.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            if (s == null) {
                continue;
            }

            try {
                    /*
                     Pattern p=Pattern.compile("\\d+");
                     Matcher m=p.matcher("aaa2223bb");
                    m.find();//匹配2223
                    m.start();//返回3
                    m.end();//返回7,返回的是2223后的索引号
                    m.group();//返回2223
                    */
                String content = readFile(s,ESBConsts.UTF8_STR);
                Matcher matcher1 = pattern1.matcher(content);
                Matcher matcher2 = pattern2.matcher(content);
                if (matcher1.find()) {
                    int idx = matcher1.end();
                    content = content.substring(idx);
                    //在下面的内容中找到
                    Matcher matcher3 = pattern3.matcher(content);
                    if (matcher3.find()) {
                        String group = matcher3.group();
                        int b = group.indexOf("value=");
                        b = group.indexOf("\"",b);
                        int e = group.indexOf("\"",b+1);
                        return  group.substring(b+1,e);
                    }
                } else if (matcher2.find()) {
                    String group = matcher2.group();
                    int b = group.indexOf("name=");
                    b = group.indexOf("\"",b);
                    int e = group.indexOf("\"",b+1);
                    return group.substring(b+1,e);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private static String readFile(InputStream in, String encoding) throws IOException {
        try {
            // 一次读多个字节
            byte[] tempbytes = new byte[1024];
            int byteread = 0;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // 读入多个字节到字节数组中，byteread为一次读入的字节数
            while ((byteread = in.read(tempbytes)) != -1) {
                out.write(tempbytes,0,byteread);
            }
            return new String(out.toByteArray(), encoding.toString());
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                }
            }
        }
        return null;
    }
}
