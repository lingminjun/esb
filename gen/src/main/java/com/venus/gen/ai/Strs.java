package com.venus.gen.ai;

import com.venus.esb.lang.ESBT;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2019-05-06
 * Time: 10:36 PM
 */
public final class Strs {

    public final static class Range {
        public final int begin;
        public final int end;
        public final int length() {
            return end >= begin ? end - begin : begin - end;
        }

        public Range(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }

        @Override
        public int hashCode() {
            return ("(" + begin + "," + end + ")").hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof Range) {
                return this.begin == ((Range) obj).begin && this.end == ((Range) obj).end;
            }
            return false;
        }
    }

    public static final List<String> substrings(String string) {
        List<String> strings = new ArrayList<String>();
        findsubs(string,null,strings,false);
        return strings;
    }

    public static final List<String> substrings(String string, boolean tail) {
        List<String> strings = new ArrayList<String>();
        findsubs(string,null,strings,tail);
        return strings;
    }


    public static final List<Range> substringRanges(String string) {
        List<Range> ranges = new ArrayList<Range>();
        findsubs(string,ranges,null,false);
        return ranges;
    }

    public static final List<Range> substringRanges(String string, boolean tail) {
        List<Range> ranges = new ArrayList<Range>();
        findsubs(string,ranges,null,tail);
        return ranges;
    }


    public static int hitCount(String string, String substring){
        if (ESBT.isEmpty(string) || ESBT.isEmpty(substring)) {
            return 0;
        }

        int len = string.length();
        int space = substring.length();
        // 判断字符串中,是否包含指定的字符
        int count = -1;
        int idx = -space;
        do {
            count++;
            idx = string.indexOf(substring,idx+space);
        } while (idx >= 0 && idx < len);
        return count;
    }


    private static void findsubs(String string, List<Range> ranges, List<String> list, boolean tail) {
        if (ESBT.isEmpty(string)) {
            return;
        }
        int len = string.length();
        String target = string;

        target = target.replaceAll("\\\\\\\"", "##");
        target = target.replaceAll("\\\\\\\'", "##");

        int flag = 0; //1表示单引号，2表示双引号
        int begin = 0;
        for (int i = 0; i < len; i++) {
            int idx = tail ? len - i - 1: i;
            char c = target.charAt(idx);
            if (c == '\"') {
                if (flag == 0) {
                    begin = idx;
                    flag = 2;
                } else if (flag == 2) {
                    if (ranges != null) {
                        if (tail) {
                            ranges.add(0,new Range(idx + 1, begin));
                        } else {
                            ranges.add(new Range(begin + 1, idx));
                        }
                    }
                    if (list != null) {
                        if (tail) {
                            list.add(0,string.substring(idx + 1, begin));
                        } else {
                            list.add(string.substring(begin + 1, idx));
                        }
                    }
                    begin = idx;
                    flag = 0;
                }
            } else if (c == '\'') {
                if (flag == 0) {
                    begin = idx;
                    flag = 1;
                } else if (flag == 1) {
                    if (ranges != null) {
                        if (tail) {
                            ranges.add(0, new Range(idx + 1, begin));
                        } else {
                            ranges.add(new Range(begin + 1, idx));
                        }
                    }
                    if (list != null) {
                        if (tail) {
                            list.add(0, string.substring(idx + 1, begin));
                        } else {
                            list.add(string.substring(begin + 1, idx));
                        }
                    }
                    begin = idx;
                    flag = 0;
                }
            }
        }
    }
}
