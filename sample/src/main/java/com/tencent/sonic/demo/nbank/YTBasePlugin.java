package com.tencent.sonic.demo.nbank;

import android.webkit.WebView;

/**
 * 插件基类，项目中插件需要继承此类，防止js注入
 *
 * @author tongxu_li
 * Copyright (c) 2014 Shanghai P&C Information Technology Co., Ltd.
 * <p>
 * 20181115 chenfangyi 防止js注入，使用统一的函数来构建js代码和执行js代码
 */
public class YTBasePlugin {
    //fw_gen_10002216
    //fw_gen_100038931
    private static final String TAG = YTBasePlugin.class.getSimpleName();
    protected WebView mWebView;
    /** 用于执行js代码的前缀 */
    public final static String JAVASCRIPT = "javascript:";

    public YTBasePlugin(WebView webView) {
        this.mWebView = webView;
    }

    public void setWebView(WebView webView) {
        this.mWebView = webView;
    }

    /**
     * 防止js注入
     */
    public Object getClass(Object o) {
        return null;
    }


    /**
     * 对(字符串/对象)使用单引号包裹
     *
     * @param param 要包裹的字符串
     * @return String 返回包裹后的字符串
     */
    public static String wrapParam(Object param) {
        return WebViewHelper.wrapParam(param);
    }

    /**
     * 执行js函数
     *
     * @param function 要执行的js函数名，格式限定为XXX或者XXX()
     * @param params   要执行的js函数的参数列表，js函数参数不做处理直接拼接
     * @return String 返回构造完成并已执行的js代码
     */
    protected String execJsFunction(String function, Object... params) {
        Logs.dd("构造js函数，" + function, "total1");
        String js;
        try {
            js = WebViewHelper.execJsFunction(mWebView, function, params);
            Logs.dd("构造js函数，" + js, TAG);
            return js;
        } catch (Exception e) {
            e.printStackTrace();
            Logs.ee("执行js函数时出现异常，" + e.toString(), TAG);
            return null;
        }
    }

    /**
     * 构建js函数
     *
     * @param function 要构建的js函数名,格式限定为XXX或XXX()
     * @param params   要构建的js函数的参数列表，js函数参数不做处理直接拼接
     * @return String 返回构造完成并已执行的js代码
     */
    protected String genJsCode(String function, Object... params) {
        Logs.dd("构造js函数，" + function, "total1");
        try {
            String js = WebViewHelper.genJsCode(function, params);
            Logs.dd("构造js函数，" + js, TAG);
            return js;
        } catch (Exception e) {
            e.printStackTrace();
            Logs.ee("构造js函数时出现异常，" + e.toString(), TAG);
            return null;
        }
    }

    /** 加载url */
    protected void loadNetHtml(String url) {
        WebViewHelper.loadNetHtml(mWebView, url);
    }


}
