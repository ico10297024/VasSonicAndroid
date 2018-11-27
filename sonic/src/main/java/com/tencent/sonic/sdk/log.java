package com.tencent.sonic.sdk;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2015/4/21.
 */
public class log {
    final static String COMMON_TAG = "ico_";
    /**
     * 日志等级,从e到v依次为1到5，若输出全关则设置0
     * out等同i，err等同e
     */
    final static int LEVEL = 5;
    /**
     * 每次日志输入的最大长度,如果太大将分段输出
     */
    final static int MAX_SIZE = 200;

    public static void v(String msg, String... tags) {
        if (LEVEL < 5) {
            return;
        }
        String tag = COMMON_TAG + "v_" + concat("_", tags);
        List<String> data = split(msg, MAX_SIZE);
        for (int i = 0; i < data.size(); i++) {
            Log.v(tag, data.get(i));
        }
    }

    public static void d(String msg, String... tags) {
        if (LEVEL < 4) {
            return;
        }
        String tag = COMMON_TAG + "d_" + concat("_", tags);
        List<String> data = split(msg, MAX_SIZE);
        for (int i = 0; i < data.size(); i++) {
            Log.d(tag, data.get(i));
        }
    }

    public static void i(String msg, String... tags) {
        if (LEVEL < 3) {
            return;
        }
        String tag = COMMON_TAG + "i_" + concat("_", tags);
        List<String> data = split(msg, MAX_SIZE);
        for (int i = 0; i < data.size(); i++) {
            Log.i(tag, data.get(i));
        }
    }

    public static void w(String msg, String... tags) {
        if (LEVEL < 2) {
            return;
        }
        String tag = COMMON_TAG + "w_" + concat("_", tags);
        List<String> data = split(msg, MAX_SIZE);
        for (int i = 0; i < data.size(); i++) {
            Log.w(tag, data.get(i));
        }
    }

    public static void e(String msg, String... tags) {
        if (LEVEL < 1) {
            return;
        }
        String tag = COMMON_TAG + "e_" + concat("_", tags);
        List<String> data = split(msg, MAX_SIZE);
        for (int i = 0; i < data.size(); i++) {
            Log.e(tag, data.get(i));
        }
    }

    public static void out(String msg, String... tags) {
        if (LEVEL < 3) {
            return;
        }
        String tag = COMMON_TAG + concat("_", tags);
        System.out.println(tag + "," + msg);
    }

    public static void err(String msg, String... tags) {
        if (LEVEL < 1) {
            return;
        }
        String tag = COMMON_TAG + concat("_", tags);
        System.err.println(tag + "," + msg);
    }

    public static void vv(String[] msgs, String... tags) {
        if (LEVEL < 5) {
            return;
        }
        String tag = COMMON_TAG + "v_" + concat("_", tags);
        String msg = concat("_", msgs);
        List<String> data = split(msg, MAX_SIZE);
        for (int i = 0; i < data.size(); i++) {
            Log.v(tag, data.get(i));
        }
    }

    public static void dd(String[] msgs, String... tags) {
        if (LEVEL < 4) {
            return;
        }
        String tag = COMMON_TAG + "d_" + concat("_", tags);
        String msg = concat("_", msgs);
        List<String> data = split(msg, MAX_SIZE);
        for (int i = 0; i < data.size(); i++) {
            Log.d(tag, data.get(i));
        }
    }

    public static void ii(String[] msgs, String... tags) {
        if (LEVEL < 3) {
            return;
        }
        String tag = COMMON_TAG + "i_" + concat("_", tags);
        String msg = concat("_", msgs);
        List<String> data = split(msg, MAX_SIZE);
        for (int i = 0; i < data.size(); i++) {
            Log.i(tag, data.get(i));
        }
    }

    public static void ww(String[] msgs, String... tags) {
        if (LEVEL < 2) {
            return;
        }
        String tag = COMMON_TAG + "w_" + concat("_", tags);
        String msg = concat("_", msgs);
        List<String> data = split(msg, MAX_SIZE);
        for (int i = 0; i < data.size(); i++) {
            Log.w(tag, data.get(i));
        }
    }

    public static void ee(String[] msgs, String... tags) {
        if (LEVEL < 1) {
            return;
        }
        String tag = COMMON_TAG + "e_" + concat("_", tags);
        String msg = concat("_", msgs);
        List<String> data = split(msg, MAX_SIZE);
        for (int i = 0; i < data.size(); i++) {
            Log.e(tag, data.get(i));
        }
    }

    public static void outt(String[] msgs, String... tags) {
        if (LEVEL < 3) {
            return;
        }
        String tag = COMMON_TAG + concat("_", tags);
        String msg = concat("_", msgs);
        System.out.println(tag + "," + msg);
    }

    public static void errr(String[] msgs, String... tags) {
        if (LEVEL < 1) {
            return;
        }
        String tag = COMMON_TAG + concat("_", tags);
        String msg = concat("_", msgs);
        System.err.println(tag + "," + msg);
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
}
