package com.tencent.sonic.demo.mine;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 安卓系统日志打印
 *
 * @author tongxu_li
 * Copyright (c) 2015 Shanghai P&C Information Technology Co., Ltd.
 * <p>
 * 20181115 chenfangyi 新增了一系列的函数，方便日志中msg的编写，同时修复日志输出字符上限，增加日志统一前缀标识，方便与系统日志进行分离和查看
 */
public class Logs {

    public static final int VERBOSE = Log.VERBOSE;
    public static final int DEBUG = Log.DEBUG;
    public static final int INFO = Log.INFO;
    public static final int WARN = Log.WARN;
    public static final int ERROR = Log.ERROR;
    public static final int NONE = Log.ERROR + 1;

    // 当前日志等级
    public static int LOGLEVEL = Log.VERBOSE;

    /**
     * 关闭日志
     */
    public static void closeLogs() {
        LOGLEVEL = Logs.NONE;
    }

    /**
     * 设置日志等级.
     *
     * @param logLevel
     */
    public static void setLogLevel(int logLevel) {
        LOGLEVEL = logLevel;
    }

    /**
     * 判断某个等级日志能否被打印
     */
    public static boolean isLoggable(int logLevel) {
        return (logLevel >= LOGLEVEL);
    }

    /**
     * Verbose 日志.
     *
     * @param tag
     * @param s
     */
    public static void v(String tag, String s) {
        if (Logs.VERBOSE >= LOGLEVEL) Log.v(tag, s + "");
    }

    /**
     * Debug 日志.
     *
     * @param tag
     * @param s
     */
    public static void d(String tag, String s) {
        if (Logs.DEBUG >= LOGLEVEL) Log.d(tag, s + "");
    }

    /**
     * Info 日志.
     *
     * @param tag
     * @param s
     */
    public static void i(String tag, String s) {
        if (Logs.INFO >= LOGLEVEL) Log.i(tag, s + "");
    }

    /**
     * Warning 日志.
     *
     * @param tag
     * @param s
     */
    public static void w(String tag, String s) {
        if (Logs.WARN >= LOGLEVEL) Log.w(tag, s + "");
    }

    /**
     * Error 日志.
     *
     * @param tag
     * @param s
     */
    public static void e(String tag, String s) {
        if (Logs.ERROR >= LOGLEVEL) Log.e(tag, s + "");
    }

    /**
     * Debug打印Map内容
     *
     * @param tag TAG
     * @param map 待打印Map
     */
    public static void d(String tag, HashMap<String, String> map) {
        if (Logs.ERROR >= LOGLEVEL && map != null) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                sb.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
            }
            Log.d(tag, sb.toString());
        }
    }

    /**
     * Verbose 日志.
     *
     * @param tag
     * @param s
     * @param e
     */
    public static void v(String tag, String s, Throwable e) {
        if (Logs.VERBOSE >= LOGLEVEL) Log.v(tag, s + "", e);
    }

    /**
     * Debug 日志.
     *
     * @param tag
     * @param s
     * @param e
     */
    public static void d(String tag, String s, Throwable e) {
        if (Logs.DEBUG >= LOGLEVEL) Log.d(tag, s + "", e);
    }

    /**
     * Info 日志.
     *
     * @param tag
     * @param s
     * @param e
     */
    public static void i(String tag, String s, Throwable e) {
        if (Logs.INFO >= LOGLEVEL) Log.i(tag, s + "", e);
    }

    /**
     * Warning 日志.
     *
     * @param tag
     * @param s
     * @param e
     */
    public static void w(String tag, String s, Throwable e) {
        if (Logs.WARN >= LOGLEVEL) Log.w(tag, s + "", e);
    }

    /**
     * Error 日志.
     *
     * @param tag
     * @param s
     * @param e
     */
    public static void e(String tag, String s, Throwable e) {
        if (Logs.ERROR >= LOGLEVEL) Log.e(tag, s + "", e);
    }

    /**
     * Verbose 格式化日志.
     *
     * @param tag
     * @param s
     * @param args
     */
    public static void v(String tag, String s, Object... args) {
        if (Logs.VERBOSE >= LOGLEVEL) Log.v(tag, String.format(s, args));
    }

    /**
     * Debug 格式化日志.
     *
     * @param tag
     * @param s
     * @param args
     */
    public static void d(String tag, String s, Object... args) {
        if (Logs.DEBUG >= LOGLEVEL) Log.d(tag, String.format(s, args));
    }

    /**
     * Info 格式化日志.
     *
     * @param tag
     * @param s
     * @param args
     */
    public static void i(String tag, String s, Object... args) {
        if (Logs.INFO >= LOGLEVEL) Log.i(tag, String.format(s, args));
    }

    /**
     * Warning 格式化日志.
     *
     * @param tag
     * @param s
     * @param args
     */
    public static void w(String tag, String s, Object... args) {
        if (Logs.WARN >= LOGLEVEL) Log.w(tag, String.format(s, args));
    }

    /**
     * Error 格式化日志.
     *
     * @param tag
     * @param s
     * @param args
     */
    public static void e(String tag, String s, Object... args) {
        if (Logs.ERROR >= LOGLEVEL) Log.e(tag, String.format(s, args));
    }


    //region ICO  自增加的日志，方便进行日志的输出，同时增加一个公共的tag头，用于快速筛选出自己所有的日志，另外由于日志长度原因，做了分割输出的处理
    final static String COMMON_TAG = "ico_";
    final static int MAX_SIZE = 200;


    public static void v(String msg, String... tags) {
        if (Logs.VERBOSE < LOGLEVEL) return;
        String tag = COMMON_TAG + "v_" + concat("_", tags);
        List<String> data = split(msg, MAX_SIZE);
        for (int i = 0; i < data.size(); i++) {
            Log.v(tag, data.get(i));
        }
    }

    public static void dd(String msg, String... tags) {
        if (Logs.DEBUG < LOGLEVEL) return;
        String tag = COMMON_TAG + "d_" + concat("_", tags);
        List<String> data = split(msg, MAX_SIZE);
        for (int i = 0; i < data.size(); i++) {
            Log.d(tag, data.get(i));
        }
    }

    public static void ii(String msg, String... tags) {
        if (Logs.INFO < LOGLEVEL) return;
        String tag = COMMON_TAG + "i_" + concat("_", tags);
        List<String> data = split(msg, MAX_SIZE);
        for (int i = 0; i < data.size(); i++) {
            Log.i(tag, data.get(i));
        }
    }

    public static void ww(String msg, String... tags) {
        if (Logs.WARN < LOGLEVEL) return;
        String tag = COMMON_TAG + "w_" + concat("_", tags);
        List<String> data = split(msg, MAX_SIZE);
        for (int i = 0; i < data.size(); i++) {
            Log.w(tag, data.get(i));
        }
    }

    public static void ee(String msg, String... tags) {
        if (Logs.ERROR < LOGLEVEL) return;
        String tag = COMMON_TAG + "e_" + concat("_", tags);
        List<String> data = split(msg, MAX_SIZE);
        for (int i = 0; i < data.size(); i++) {
            Log.e(tag, data.get(i));
        }
    }


    public static void vv(String[] msgs, String... tags) {
        if (Logs.VERBOSE < LOGLEVEL) return;
        String tag = COMMON_TAG + "v_" + concat("_", tags);
        String msg = concat("_", msgs);
        List<String> data = split(msg, MAX_SIZE);
        for (int i = 0; i < data.size(); i++) {
            Log.v(tag, data.get(i));
        }
    }

    public static void dd(String[] msgs, String... tags) {
        if (Logs.DEBUG < LOGLEVEL) return;
        String tag = COMMON_TAG + "d_" + concat("_", tags);
        String msg = concat("_", msgs);
        List<String> data = split(msg, MAX_SIZE);
        for (int i = 0; i < data.size(); i++) {
            Log.d(tag, data.get(i));
        }
    }

    public static void ii(String[] msgs, String... tags) {
        if (Logs.INFO < LOGLEVEL) return;
        String tag = COMMON_TAG + "i_" + concat("_", tags);
        String msg = concat("_", msgs);
        List<String> data = split(msg, MAX_SIZE);
        for (int i = 0; i < data.size(); i++) {
            Log.i(tag, data.get(i));
        }
    }

    public static void ww(String[] msgs, String... tags) {
        if (Logs.WARN < LOGLEVEL) return;
        String tag = COMMON_TAG + "w_" + concat("_", tags);
        String msg = concat("_", msgs);
        List<String> data = split(msg, MAX_SIZE);
        for (int i = 0; i < data.size(); i++) {
            Log.w(tag, data.get(i));
        }
    }

    public static void ee(String[] msgs, String... tags) {
        if (Logs.ERROR < LOGLEVEL) return;
        String tag = COMMON_TAG + "e_" + concat("_", tags);
        String msg = concat("_", msgs);
        List<String> data = split(msg, MAX_SIZE);
        for (int i = 0; i < data.size(); i++) {
            Log.e(tag, data.get(i));
        }
    }

    /**
     * 将一个字符串数组根据某个字符串连接
     *
     * @param str   要插入的字符串
     * @param texts 要被拼接的字符串数组
     * @return
     */
    public static String concat(String str, String... texts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < texts.length; i++) {
            String tmp = texts[i];
            sb.append(tmp);
            if (i < texts.length - 1) {
                sb.append(str);
            }
        }
        return sb.toString();
    }

    /**
     * 将一个字符串根据指定长度进行分割
     *
     * @return
     */
    public static List<String> split(String str, int size) {
        List<String> data = new ArrayList<>();
        if (str.length() <= size) {
            data.add(str);
            return data;
        } else {
            while (true) {
                if (str.length() > size) {
                    data.add(str.substring(0, size));
                } else {
                    data.add(str.substring(0));
                    break;
                }
                str = str.substring(size);
            }
            return data;
        }
    }
    //endregion
}
