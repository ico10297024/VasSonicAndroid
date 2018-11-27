package com.tencent.sonic.demo.nbank;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * native与webjs交互插件插件名称"SysClientJs"
 * <p>
 * Lewis(lgs@yitong.com.cn) 2014年7月3日 上午9:23:33
 * JsPlugin Copyright (c) 2014 Shanghai P&C Information Technology
 * Co.,Ltd. All rights reserved.
 */
public class NativePlugin extends YTBasePlugin {
    private static final String TAG = "NativePlugin";

    public static final String NAME = "SysClientJs";

    /** 网络请求失败后需要传递给js的msg，用于弹出框的msg */
    private String MSG_HTTP_FAIL = "网络请求失败";
    private String JS_HTTP_FAIL_PARAM = "{MSG:\"" + MSG_HTTP_FAIL + "\"}";

    private Activity activity;

    public AlertDialog dialog;
    /** 等待层超时关闭的时间 */
    private int diaTimeOut = 60 * 1000;

    private static char ER_SPLIT_CHAR = 29;
    private static String ER_CODE_MESSAGE = "MOBILE" + ER_SPLIT_CHAR + "type" + ER_SPLIT_CHAR + "cardId";
    private String jsonStrIds;
    //    private String IdNo;//身份证号码
//    private String index;//判定身份证正反面
    private JSONObject object;
    private String datas;
    private String url;
    private Builder builder1;
    private String LOTTO_ID;

    private static long timeStamp;

    public NativePlugin(Activity activity, WebView webView) {
        super(webView);
        this.activity = activity;
    }

    //region dialog等待层
    long dialogStartTime;


    /**
     * 关闭等待层
     */
    @JavascriptInterface
    public void hideWaitPanel() {
        Logs.dd(new String[]{"hideWaitPanel", "等待层关闭"}, TAG);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialog != null && dialog.isShowing() && !activity.isFinishing()) {
                    Logs.dd(new String[]{"hideWaitPanel", (System.currentTimeMillis() - dialogStartTime) + ""}, "total1");

                    Logs.dd(new String[]{"hideWaitPanel", "等待层关闭"}, "dialog");
                    dialog.dismiss();
                }
            }
        });
    }


    /** 开启等待层 */
    public void showWaitPanel() {
        Logs.dd(new String[]{"showWaitPanel", "开启等待层"}, TAG);
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (dialog == null) {
//                    dialog = ProgressDialog.createProgress(activity, "");
                    dialog = new AlertDialog.Builder(activity).create();
                    dialog.setCanceledOnTouchOutside(false);
                }
                dialog.setMessage("");
                if (dialog != null && !dialog.isShowing() && !activity.isFinishing()) {

                    dialogStartTime = System.currentTimeMillis();
                    Logs.dd(new String[]{"showWaitPanel"}, "total1");
                    dialog.show();

                    /* 等待层超时关闭的延迟消息中增加时间戳标记，避免多等待层频繁出现时造成后续等待层被误杀的问题 */
                    timeStamp = System.currentTimeMillis();
                    Message msg = new Message();
                    msg.obj = timeStamp;
                    mHandler.sendMessageDelayed(msg, diaTimeOut);
                }
            }
        });
    }

    /**
     * 开启等待层
     * {"msg":"正在加载中。。。","touchable":"false"}
     */
    @JavascriptInterface
    public void showWaitPanel(String msg) {
        Logs.dd(new String[]{"showWaitPanel", "开启等待层", msg}, TAG);
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (dialog == null) {
//                    dialog = ProgressDialogTools.createProgress(activity, "");
                    dialog = new AlertDialog.Builder(activity).create();
                    dialog.setCanceledOnTouchOutside(false);
                }
                dialog.setMessage("");
                if (dialog != null && !dialog.isShowing() && !activity.isFinishing()) {
                    dialogStartTime = System.currentTimeMillis();
                    Logs.dd(new String[]{"showWaitPanel"}, "total1");
                    dialog.show();

                    /* 等待层超时关闭的延迟消息中增加时间戳标记，避免多等待层频繁出现时造成后续等待层被误杀的问题 */
                    timeStamp = System.currentTimeMillis();
                    Message msg = new Message();
                    msg.obj = timeStamp;
                    mHandler.sendMessageDelayed(msg, diaTimeOut);
                }
            }
        });
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // 等待层超时关闭时验证时间戳，防止等待层误杀
            if (msg.obj != null
                    && timeStamp == (long) msg.obj
                    && dialog != null && dialog.isShowing() && !activity.isFinishing()) {
                Logs.dd(new String[]{"hideWaitPanel", "mHandler"}, "dialog");
                dialog.dismiss();
            }
        }
    };
    //endregion


    long postStartTime;

    //region post

    /**
     * Ajax请求
     * {"url":"/common/sysdate.do",
     * "params":{"name":"zhangsan","sex":"F","mobile":"15609892332","REQ_REPLAY_CODE":"20150817145356"},
     * "success":"fw_gen_10001","failure":"fw_gen_10002"}
     */
    @JavascriptInterface
    public void post(final String json) {
        postStartTime = System.currentTimeMillis();
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                post(json, null);
            }
        });

    }

    private void post(String json, final String cacheKey) {
        JSONObject object;
        try {
            object = new JSONObject(json);
        } catch (JSONException e) {
            Logs.e(TAG, "非法的json格式：" + json, e);
            return;
        }
        final String url = "http://web01sit.mbank.nb/mobilebank/" + object.optString("url");

        Logs.dd(new String[]{"post", "Ajax请求", url, json}, TAG);

        final String params = object.optString("params");
        final String success = object.optString("success");
        final String failure = object.optString("failure");
        Logs.dd(new String[]{"post", "Ajax请求开始", url, success, failure}, "total1");
        //内网数据
        String _json = "{\"fixFlag1\":{\"ADD_PERSON\":\"admin\",\"FIXED_NAME\":\"望子成龙\",\"FILENAME1\":\"http://12.99.105.67/mobilebank/resources/banner/1531813882066@FILENAME1.png\",\"FILENAME2\":\"http://12.99.105.67/mobilebank/resources/banner/1531813863493@FILENAME2.png\",\"ID\":\"20180707145604\",\"ADD_DATE\":\"2018-07-1716:08:17\",\"FILENAME3\":\"http://12.99.105.67/mobilebank/resources/banner/1531813863594@FILENAME3.png\",\"SORT\":1},\"MSG\":\"1\",\"fixFlag2\":{\"ADD_PERSON\":\"admin\",\"FIXED_NAME\":\"温馨家园\",\"FILENAME1\":\"http://12.99.105.67/mobilebank/resources/banner/1531813966907@FILENAME1.png\",\"FILENAME2\":\"http://12.99.105.67/mobilebank/resources/banner/1531813966983@FILENAME2.png\",\"ID\":\"20180611110111\",\"ADD_DATE\":\"2018-07-31 14:38:55\",\"FILENAME3\":\"http://12.99.105.67/mobilebank/resources/banner/1531813967067@FILENAME3.png\",\"SORT\":2},\"STATUS\":\"1\",\"fixedInfo\":\"1\",\"fixFlag3\":{\"ADD_PERSON\":\"admin\",\"FIXED_NAME\":\"养老定投\",\"FILENAME1\":\"http://12.99.105.67/mobilebank/resources/banner/1530946836133@FILENAME1.png\",\"FILENAME2\":\"http://12.99.105.67/mobilebank/resources/banner/1530946836211@FILENAME2.png\",\"ID\":\"20180707150036\",\"ADD_DATE\":\"2018-07-17 15:02:40\",\"FILENAME3\":\"http://12.99.105.67/mobilebank/resources/banner/1530946836301@FILENAME3.png\",\"SORT\":3},\"iFundThemeListNew\":[{\"themeName\":\"人工智能\",\"themeCode\":\"884201.WI\",\"SYL_D\":\"21.8191\"},{\"themeName\":\"芯片国产化\",\"themeCode\":\"884160.WI\",\"SYL_D\":\"21.0458\"},{\"themeName\":\"去IOE\",\"themeCode\":\"884169.WI\",\"SYL_D\":\"20.757\"},{\"themeName\":\"IPV6\",\"themeCode\":\"884098.WI\",\"SYL_D\":\"20.1213\"},{\"themeName\":\"智能IC卡\",\"themeCode\":\"884123.WI\",\"SYL_D\":\"17.5361\"},{\"themeName\":\"软件\",\"themeCode\":\"886059.WI\",\"SYL_D\":\"17.4098\"},{\"themeName\":\"移动转售\",\"themeCode\":\"884158.WI\",\"SYL_D\":\"17.3708\"},{\"themeName\":\"海南旅游岛\",\"themeCode\":\"884050.WI\",\"SYL_D\":\"17.3048\"},{\"themeName\":\"核高基\",\"themeCode\":\"884068.WI\",\"SYL_D\":\"17.2761\"}],\"IS_WEB\":false,\"DYNAMIC_KEY\":\"A2jKACtl7u42fYBG\",\"fundBannerInfo\":[{\"PHOTO_PATH\":\"http://12.99.105.67/mobilebank/resources/banner/20180717154942.PNG\",\"ADD_USER\":\"admin\",\"ID\":\"20180717154942\",\"ADD_DATE\":\"2018-10-30 16:59:33\",\"FUND_CLICK_URL\":\"page/financeArea/palmFund-x/fundDetail/index.html?detailFlag=1&prodId=005711\"},{\"PHOTO_PATH\":\"http://12.99.105.67/mobilebank/resources/banner/20180717154931.PNG\",\"ADD_USER\":\"admin\",\"ID\":\"20180717154931\",\"ADD_DATE\":\"2018-10-30 17:11:34\",\"FUND_CLICK_URL\":\"http://12.99.153.239:8080/mobilebank/page/financeArea/palmFund-x/fundDetail/index.html?detailFlag=1&prodId=163412\"},{\"PHOTO_PATH\":\"http://12.99.105.67/mobilebank/resources/banner/20180608162021.png\",\"ADD_USER\":\"admin\",\"ID\":\"20180608162021\",\"ADD_DATE\":\"2018-08-08 15:04:51\",\"FUND_CLICK_URL\":\"http://12.99.105.67/mobilebank/page/financeArea/roboAdvisor/index.html\"},{\"PHOTO_PATH\":\"http://12.99.105.67/mobilebank/resources/banner/20180608161956.png\",\"ADD_USER\":\"admin\",\"ID\":\"20180608161956\",\"ADD_DATE\":\"2018-10-30 16:55:13\",\"FUND_CLICK_URL\":\"http://12.99.153.239:8080/mobilebank/page/financeArea/palmFund-x/fundDetail/index.html?detailFlag=1&prodId=005711\"}],\"funRecInfo\":[{\"FUNDCODE\":\"200002\",\"F_ID\":20180611093709,\"LABEL_SET\":\"人民热爱祖国花朵|人民热爱祖国花朵|人民热爱祖国花朵|\",\"iFundData\":{\"SYL_Y\":\"\",\"SYL_Z\":\"\",\"fundState\":\"0\",\"fundCode\":\"200002\",\"PrdAttr\":\"1\",\"SYL_2N\":\"\",\"tranTime\":\"20181031103759\",\"SYL_6Y\":\"\",\"fundType\":\"1\",\"riskLevelName\":\"高风险\",\"netValueDate\":\"08-02\",\"SYL_7D\":\"0.00\",\"fundManager\":\"长城基金\",\"SYL_D\":\"\",\"hotOrNew\":\"0\",\"fundName\":\"长城久泰\",\"RANK3Y\":\"★★★★★\",\"SYL_5N\":\"\",\"NVGRWTD\":\"\",\"SYL_3N\":\"\",\"riskLevel\":\"5\",\"fundManagerCode\":\"20\",\"IsAgio\":\"\",\"SYL_1N\":\"\",\"fundStateName\":\"开放期\",\"unitNetValue\":\"1.5023\",\"TACode\":\"20\",\"SYL_JN\":\"\",\"SYL_3Y\":\"\"},\"RISE\":\"4\",\"recData\":\"\",\"ADD_DATE\":\"2018-06-11 09:36:09\",\"recList\":[{\"recommend\":\"人民热爱祖国花朵\"},{\"recommend\":\"人民热爱祖国花朵\"},{\"recommend\":\"人民热爱祖国花朵\"}],\"SORT\":1,\"RECOMMENDSAY\":\"ky\"},{\"FUNDCODE\":\"161626\",\"F_ID\":20180613030630,\"LABEL_SET\":\"恶趣味恶趣味请|而威尔玩儿|我认为人味儿|\",\"iFundData\":{\"SYL_Y\":\"\",\"SYL_Z\":\"\",\"fundState\":\"0\",\"fundCode\":\"161626\",\"PrdAttr\":\"2\",\"SYL_2N\":\"\",\"tranTime\":\"20181031103757\",\"SYL_6Y\":\"\",\"fundType\":\"2\",\"riskLevelName\":\"中低风险\",\"netValueDate\":\"08-02\",\"SYL_7D\":\"0.00\",\"fundManager\":\"融通基金\",\"SYL_D\":\"\",\"hotOrNew\":\"0\",\"fundName\":\"融通通福债A\",\"RANK3Y\":\"\",\"SYL_5N\":\"\",\"NVGRWTD\":\"\",\"SYL_3N\":\"\",\"riskLevel\":\"2\",\"fundManagerCode\":\"98\",\"IsAgio\":\"\",\"SYL_1N\":\"\",\"fundStateName\":\"开放期\",\"unitNetValue\":\"1.0420\",\"TACode\":\"98\",\"SYL_JN\":\"\",\"SYL_3Y\":\"\"},\"RISE\":\"2\",\"recData\":\"\",\"ADD_DATE\":\"2018-06-13 15:03:40\",\"recList\":[{\"recommend\":\"恶趣味恶趣味请\"},{\"recommend\":\"而威尔玩儿\"},{\"recommend\":\"我认为人味儿\"}],\"SORT\":2,\"RECOMMENDSAY\":\"推荐推荐推荐推荐推荐推荐推荐推荐推荐推荐\"},{\"FUNDCODE\":\"1Y0038\",\"F_ID\":20180611093528,\"LABEL_SET\":\"饿地方|沃11位饿尔|饿饿饿而|\",\"iFundData\":{\"SYL_Y\":\"\",\"SYL_Z\":\"\",\"fundState\":\"4\",\"fundCode\":\"1Y0038\",\"PrdAttr\":\"3\",\"SYL_2N\":\"\",\"tranTime\":\"20181031103758\",\"SYL_6Y\":\"\",\"fundType\":\"3\",\"riskLevelName\":\"中风险\",\"netValueDate\":\"02-06\",\"SYL_7D\":\"0.00\",\"fundManager\":\"永赢资产管理\",\"SYL_D\":\"\",\"hotOrNew\":\"0\",\"fundName\":\"永赢资产甬泰三十八期\",\"RANK3Y\":\"\",\"SYL_5N\":\"\",\"NVGRWTD\":\"\",\"SYL_3N\":\"\",\"riskLevel\":\"3\",\"fundManagerCode\":\"1Y\",\"IsAgio\":\"\",\"SYL_1N\":\"\",\"fundStateName\":\"停止交易\",\"unitNetValue\":\"1.0000\",\"TACode\":\"1Y\",\"SYL_JN\":\"\",\"SYL_3Y\":\"\"},\"RISE\":\"2\",\"recData\":\"\",\"ADD_DATE\":\"2018-06-11 09:34:27\",\"recList\":[{\"recommend\":\"饿地方\"},{\"recommend\":\"沃11位饿尔\"},{\"recommend\":\"饿饿饿而\"}],\"SORT\":4,\"RECOMMENDSAY\":\"er儿童而特瑞特瑞他\"},{\"FUNDCODE\":\"200001\",\"F_ID\":20180611093607,\"LABEL_SET\":\"尔|威号个金号尔|配吗个发|\",\"iFundData\":{\"SYL_Y\":\"\",\"SYL_Z\":\"\",\"fundState\":\"0\",\"fundCode\":\"200001\",\"PrdAttr\":\"3\",\"SYL_2N\":\"\",\"tranTime\":\"20181031103759\",\"SYL_6Y\":\"\",\"fundType\":\"3\",\"riskLevelName\":\"中高风险\",\"netValueDate\":\"08-02\",\"SYL_7D\":\"0.00\",\"fundManager\":\"长城基金\",\"SYL_D\":\"\",\"hotOrNew\":\"0\",\"fundName\":\"长城久恒\",\"RANK3Y\":\"★★★★★\",\"SYL_5N\":\"\",\"NVGRWTD\":\"\",\"SYL_3N\":\"\",\"riskLevel\":\"4\",\"fundManagerCode\":\"20\",\"IsAgio\":\"\",\"SYL_1N\":\"\",\"fundStateName\":\"开放期\",\"unitNetValue\":\"1.1934\",\"TACode\":\"20\",\"SYL_JN\":\"\",\"SYL_3Y\":\"\"},\"RISE\":\"3\",\"recData\":\"\",\"ADD_DATE\":\"2018-06-11 09:35:07\",\"recList\":[{\"recommend\":\"尔\"},{\"recommend\":\"威号个金号尔\"},{\"recommend\":\"配吗个发\"}],\"SORT\":5}]}";
        //外网数据
//        String _json = "{\"fixFlag1\":{\"ADD_PERSON\":\"admin\",\"FIXED_NAME\":\"望子成龙\",\"FILENAME1\":\"http://12.99.83.14/mobilebank/resources/banner/1531206510069@FILENAME1.png\",\"FILENAME2\":\"http://12.99.83.14/mobilebank/resources/banner/1531206510176@FILENAME2.png\",\"ID\":\"20180705150456\",\"ADD_DATE\":\"2018-07-10 15:08:31\",\"FILENAME3\":\"http://12.99.83.14/mobilebank/resources/banner/1531206510285@FILENAME3.png\",\"SORT\":1},\"MSG\":\"1\",\"fixFlag2\":{\"ADD_PERSON\":\"admin\",\"FIXED_NAME\":\"温馨家园\",\"FILENAME1\":\"http://12.99.83.14/mobilebank/resources/banner/1531206574483@FILENAME1.png\",\"FILENAME2\":\"http://12.99.83.14/mobilebank/resources/banner/1531206574578@FILENAME2.png\",\"ID\":\"20180706103926\",\"ADD_DATE\":\"2018-07-10 15:09:35\",\"FILENAME3\":\"http://12.99.83.14/mobilebank/resources/banner/1531206574701@FILENAME3.png\",\"SORT\":2},\"STATUS\":\"1\",\"fixedInfo\":\"1\",\"fixFlag3\":{\"ADD_PERSON\":\"admin\",\"FIXED_NAME\":\"爱旅生活\",\"FILENAME1\":\"http://12.99.83.14/mobilebank/resources/banner/1534755169796@FILENAME1.png\",\"FILENAME2\":\"http://12.99.83.14/mobilebank/resources/banner/1534755169894@FILENAME2.png\",\"ID\":\"20180706104249\",\"ADD_DATE\":\"2018-07-11 16:52:57\",\"FILENAME3\":\"http://12.99.83.14/mobilebank/resources/banner/1531206679783@FILENAME3.png\",\"SORT\":3},\"iFundThemeListNew\":[{\"themeName\":\"工程机械\",\"themeCode\":\"886068.WI\",\"SYL_D\":\"1.3741\"},{\"themeName\":\"食品安全\",\"themeCode\":\"884127.WI\",\"SYL_D\":\"1.3537\"},{\"themeName\":\"芯片国产化\",\"themeCode\":\"884160.WI\",\"SYL_D\":\"-.0131\"},{\"themeName\":\"网络安全\",\"themeCode\":\"884133.WI\",\"SYL_D\":\"-1.5349\"},{\"themeName\":\"去IOE\",\"themeCode\":\"884169.WI\",\"SYL_D\":\"-3.1866\"},{\"themeName\":\"航天军工\",\"themeCode\":\"886015.WI\",\"SYL_D\":\"-3.2876\"},{\"themeName\":\"生物科技\",\"themeCode\":\"886050.WI\",\"SYL_D\":\"-3.4011\"},{\"themeName\":\"卫星导航\",\"themeCode\":\"884087.WI\",\"SYL_D\":\"-3.4964\"},{\"themeName\":\"征信\",\"themeCode\":\"884212.WI\",\"SYL_D\":\"-3.5564\"}],\"IS_WEB\":false,\"DYNAMIC_KEY\":\"zriOhVRrMMg9ryYo\",\"fundBannerInfo\":[{\"PHOTO_PATH\":\"http://12.99.83.14/mobilebank/resources/banner/20181228194202.png\",\"ADD_USER\":\"admin\",\"ID\":\"20181228194202\",\"ADD_DATE\":\"2018-11-08 10:14:02\",\"FUND_CLICK_URL\":\"page/financeArea/palmFund-x/fundDetail/index.html?detailFlag=1&prodId=110022\"},{\"PHOTO_PATH\":\"http://12.99.83.14/mobilebank/resources/banner/20181221155805.png\",\"ADD_USER\":\"admin\",\"ID\":\"20181221155805\",\"ADD_DATE\":\"2018-11-08 19:22:28\",\"FUND_CLICK_URL\":\"http://221.136.68.106:22680/mobilebank/page/financeArea/palmFund-x/fundDetail/index.html?detailFlag=1&prodId=162201\"},{\"PHOTO_PATH\":\"http://12.99.83.14/mobilebank/resources/banner/20180710152339.PNG\",\"ADD_USER\":\"l1233\",\"ID\":\"20180710152339\",\"ADD_DATE\":\"2018-07-10 15:23:41\",\"FUND_CLICK_URL\":\"http://221.136.68.106:22680/mobilebank/page/financeArea/palmFund-x/fixSuperMarket.html\"},{\"PHOTO_PATH\":\"http://12.99.83.14/mobilebank/resources/banner/20180705145951.PNG\",\"ADD_USER\":\"l1233\",\"ID\":\"20180705145951\",\"ADD_DATE\":\"2018-07-10 15:23:03\",\"FUND_CLICK_URL\":\"http://221.136.68.106:22680/mobilebank/page/financeArea/palmFund-x/myfund/salesModel.html \"}],\"funRecInfo\":[{\"FUNDCODE\":\"001417\",\"F_ID\":20180707012626,\"LABEL_SET\":\"推荐标签1|推荐标签2|\",\"iFundData\":{\"SYL_Y\":\"\",\"SYL_Z\":\"\",\"fundState\":\"0\",\"fundCode\":\"001417\",\"PrdAttr\":\"3\",\"SYL_2N\":\"\",\"tranTime\":\"20190414082435\",\"SYL_6Y\":\"\",\"fundType\":\"3\",\"riskLevelName\":\"中高风险\",\"netValueDate\":\"10-31\",\"SYL_7D\":\"0.00\",\"fundManager\":\"汇添富基金\",\"SYL_D\":\"\",\"hotOrNew\":\"0\",\"fundName\":\"汇添富医疗服务\",\"RANK3Y\":\"\",\"SYL_5N\":\"\",\"NVGRWTD\":\"\",\"SYL_3N\":\"\",\"riskLevel\":\"4\",\"fundManagerCode\":\"47\",\"IsAgio\":\"\",\"SYL_1N\":\"\",\"fundStateName\":\"开放期\",\"unitNetValue\":\"0.9000\",\"TACode\":\"47\",\"SYL_JN\":\"\",\"SYL_3Y\":\"\"},\"RISE\":\"3\",\"recData\":\"\",\"ADD_DATE\":\"2018-07-07 13:26:02\",\"recList\":[{\"recommend\":\"推荐标签1\"},{\"recommend\":\"推荐标签2\"}],\"SORT\":1,\"RECOMMENDSAY\":\"推荐话术，推荐话术\"},{\"FUNDCODE\":\"110022\",\"F_ID\":20180830033828,\"LABEL_SET\":\"推荐推荐推荐推荐|推荐推荐推荐推荐|推荐推荐推荐推荐|\",\"iFundData\":{\"SYL_Y\":\"\",\"SYL_Z\":\"\",\"fundState\":\"0\",\"fundCode\":\"110022\",\"PrdAttr\":\"1\",\"SYL_2N\":\"\",\"tranTime\":\"20190414082438\",\"SYL_6Y\":\"\",\"fundType\":\"1\",\"riskLevelName\":\"高风险\",\"netValueDate\":\"11-26\",\"SYL_7D\":\"0.00\",\"fundManager\":\"易方达基金\",\"SYL_D\":\"\",\"hotOrNew\":\"0\",\"fundName\":\"易方达消费行业\",\"RANK3Y\":\"★★★★★\",\"SYL_5N\":\"\",\"NVGRWTD\":\"\",\"SYL_3N\":\"\",\"riskLevel\":\"5\",\"fundManagerCode\":\"11\",\"IsAgio\":\"\",\"SYL_1N\":\"\",\"fundStateName\":\"开放期\",\"unitNetValue\":\"1.7523\",\"TACode\":\"11\",\"SYL_JN\":\"\",\"SYL_3Y\":\"\"},\"RISE\":\"1\",\"recData\":\"\",\"ADD_DATE\":\"2018-08-30 15:37:24\",\"recList\":[{\"recommend\":\"推荐推荐推荐推荐\"},{\"recommend\":\"推荐推荐推荐推荐\"},{\"recommend\":\"推荐推荐推荐推荐\"}],\"SORT\":1},{\"FUNDCODE\":\"110011\",\"F_ID\":20180705112532,\"LABEL_SET\":\"推荐标签1|推荐标签2|\",\"iFundData\":{\"SYL_Y\":\"\",\"SYL_Z\":\"\",\"fundState\":\"0\",\"fundCode\":\"110011\",\"PrdAttr\":\"3\",\"SYL_2N\":\"\",\"tranTime\":\"20190414082438\",\"SYL_6Y\":\"\",\"fundType\":\"3\",\"riskLevelName\":\"中高风险\",\"netValueDate\":\"11-26\",\"SYL_7D\":\"0.00\",\"fundManager\":\"易方达基金\",\"SYL_D\":\"\",\"hotOrNew\":\"0\",\"fundName\":\"易方达中小盘\",\"RANK3Y\":\"★★★★★\",\"SYL_5N\":\"\",\"NVGRWTD\":\"\",\"SYL_3N\":\"\",\"riskLevel\":\"4\",\"fundManagerCode\":\"11\",\"IsAgio\":\"\",\"SYL_1N\":\"\",\"fundStateName\":\"开放期\",\"unitNetValue\":\"3.2131\",\"TACode\":\"11\",\"SYL_JN\":\"\",\"SYL_3Y\":\"\"},\"RISE\":\"1\",\"recData\":\"\",\"ADD_DATE\":\"2018-07-05 11:25:00\",\"recList\":[{\"recommend\":\"推荐标签1\"},{\"recommend\":\"推荐标签2\"}],\"SORT\":2,\"RECOMMENDSAY\":\"推荐话术，推荐话术\"}]});execJsFunction,执行js函数，javascript:fw_gen_100025326({\"fixFlag1\":{\"ADD_PERSON\":\"admin\",\"FIXED_NAME\":\"望子成龙\",\"FILENAME1\":\"http://12.99.83.14/mobilebank/resources/banner/1531206510069@FILENAME1.png\",\"FILENA/1531206510176@FILENAME2.png\",\"ID\":\"20180705150456\",\"ADD_DATE\":\"2018-07-10 15:08:31\",\"FILENAME3\":\"http://12.99.83.14/mobilebank/resources/banner/1531206510285@FILENAME3.png\",\"SORT\":1},\"MSG\":\"1\",\"fixFlag2\":{\"ADD_PERSON\":\"admin\",\"FIXED_NAME\":\"温馨家园\",\"FILENAME1\":\"http://12.99.83.14/mobilebank/resources/banner/1531206574483@FILENAME1.png\",\"FILENAME2\":\"http://12.99.83.14/mobilebank/resources/banner/1531206574578@FILENAME2.png\",\"ID\":\"20180706103926\",\"ADD_DATE\":\"2018-07-10 15:09:35\",\"FILENAME3\":\"http://12.99.83.14/mobilebank/resources/banner/1531206574701@FILENAME3.png\",\"SORT\":2},\"STATUS\":\"1\",\"fixedInfo\":\"1\",\"fixFlag3\":{\"ADD_PERSON\":\"admin\",\"FIXED_NAME\":\"爱旅生活\",\"FILENAME1\":\"http://12.99.83.14/mobilebank/resources/banner/1534755169796@FILENAME1.png\",\"FILENAME2\":\"http://12.99.83.14/mobilebank/resources/banner/1534755169894@FILENAME2.png\",\"ID\":\"20180706104249\",\"ADD_DATE\":\"2018-07-11 16:52:57\",\"FILENAME3\":\"http://12.99.83.14/mobilebank/resources/banner/1531206679783@FILENAME3.png\",\"SORT\":3},\"iFundThemeListNew\":[{\"themeName\":\"工程机械\",\"themeCode\":\"886068.WI\",\"SYL_D\":\"1.3741\"},{\"themeName\":\"食品安全\",\"themeCode\":\"884127.WI\",\"SYL_D\":\"1.3537\"},{\"themeName\":\"芯片国产化\",\"themeCode\":\"884160.WI\",\"SYL_D\":\"-.0131\"},{\"themeName\":\"网络安全\",\"themeCode\":\"884133.WI\",\"SYL_D\":\"-1.5349\"},{\"themeName\":\"去IOE\",\"themeCode\":\"884169.WI\",\"SYL_D\":\"-3.1866\"},{\"themeName\":\"航天军工\",\"themeCode\":\"886015.WI\",\"SYL_D\":\"-3.2876\"},{\"themeName\":\"生物科技\",\"themeCode\":\"886050.WI\",\"SYL_D\":\"-3.4011\"},{\"themeName\":\"卫星导航\",\"themeCode\":\"884087.WI\",\"SYL_D\":\"-3.4964\"},{\"themeName\":\"征信\",\"themeCode\":\"884212.WI\",\"SYL_D\":\"-3.5564\"}],\"IS_WEB\":false,\"DYNAMIC_KEY\":\"zriOhVRrMMg9ryYo\",\"fundBannerInfo\":[{\"PHOTO_PATH\":\"http://12.99.83.14/mobilebank/resources/banner/20181228194202.png\",\"ADD_USER\":\"admin\",\"ID\":\"20181228194202\",\"ADD_DATE\":\"2018-11-08 10:14:02\",\"FUND_CLICK_URL\":\"page/financeArea/palmFund-x/fundDetail/index.html?detailFlag=1&prodId=110022\"},{\"PHOTO_PATH\":\"http://12.99.83.14/mobilebank/resources/banner/20181221155805.png\",\"ADD_USER\":\"admin\",\"ID\":\"20181221155805\",\"ADD_DATE\":\"2018-11-08 19:22:28\",\"FUND_CLICK_URL\":\"http://221.136.68.106:22680/mobilebank/page/financeArea/palmFund-x/fundDetail/index.html?detailFlag=1&prodId=162201\"},{\"PHOTO_PATH\":\"http://12.99.83.14/mobilebank/resources/banner/20180710152339.PNG\",\"ADD_USER\":\"l1233\",\"ID\":\"20180710152339\",\"ADD_DATE\":\"2018-07-10 15:23:41\",\"FUND_CLICK_URL\":\"http://221.136.68.106:22680/mobilebank/page/financeArea/palmFund-x/fixSuperMarket.html\"},{\"PHOTO_PATH\":\"http://12.99.83.14/mobilebank/resources/banner/20180705145951.PNG\",\"ADD_USER\":\"l1233\",\"ID\":\"20180705145951\",\"ADD_DATE\":\"2018-07-10 15:23:03\",\"FUND_CLICK_URL\":\"http://221.136.68.106:22680/mobilebank/page/financeArea/palmFund-x/myfund/salesModel.html \"}],\"funRecInfo\":[{\"FUNDCODE\":\"001417\",\"F_ID\":20180707012626,\"LABEL_SET\":\"推荐标签1|推荐标签2|\",\"iFundData\":{\"SYL_Y\":\"\",\"SYL_Z\":\"\",\"fundState\":\"0\",\"fundCode\":\"001417\",\"PrdAttr\":\"3\",\"SYL_2N\":\"\",\"tranTime\":\"20190414082435\",\"SYL_6Y\":\"\",\"fundType\":\"3\",\"riskLevelName\":\"中高风险\",\"netValueDate\":\"10-31\",\"SYL_7D\":\"0.00\",\"fundManager\":\"汇添富基金\",\"SYL_D\":\"\",\"hotOrNew\":\"0\",\"fundName\":\"汇添富医疗服务\",\"RANK3Y\":\"\",\"SYL_5N\":\"\",\"NVGRWTD\":\"\",\"SYL_3N\":\"\",\"riskLevel\":\"4\",\"fundManagerCode\":\"47\",\"IsAgio\":\"\",\"SYL_1N\":\"\",\"fundStateName\":\"开放期\",\"unitNetValue\":\"0.9000\",\"TACode\":\"47\",\"SYL_JN\":\"\",\"SYL_3Y\":\"\"},\"RISE\":\"3\",\"recData\":\"\",\"ADD_DATE\":\"2018-07-07 13:26:02\",\"recList\":[{\"recommend\":\"推荐标签1\"},{\"recommend\":\"推荐标签2\"}],\"SORT\":1,\"RECOMMENDSAY\":\"推荐话术，推荐话术\"},{\"FUNDCODE\":\"110022\",\"F_ID\":20180830033828,\"LABEL_SET\":\"推荐推荐推荐推荐|推荐推荐推荐推荐|推荐推荐推荐推荐|\",\"iFundData\":{\"SYL_Y\":\"\",\"SYL_Z\":\"\",\"fundState\":\"0\",\"fundCode\":\"110022\",\"PrdAttr\":\"1\",\"SYL_2N\":\"\",\"tranTime\":\"20190414082438\",\"SYL_6Y\":\"\",\"fundType\":\"1\",\"riskLevelName\":\"高风险\",\"netValueDate\":\"11-26\",\"SYL_7D\":\"0.00\",\"fundManager\":\"易方达基金\",\"SYL_D\":\"\",\"hotOrNew\":\"0\",\"fundName\":\"易方达消费行业\",\"RANK3Y\":\"★★★★★\",\"SYL_5N\":\"\",\"NVGRWTD\":\"\",\"SYL_3N\":\"\",\"riskLevel\":\"5\",\"fundManagerCode\":\"11\",\"IsAgio\":\"\",\"SYL_1N\":\"\",\"fundStateName\":\"开放期\",\"unitNetValue\":\"1.7523\",\"TACode\":\"11\",\"SYL_JN\":\"\",\"SYL_3Y\":\"\"},\"RISE\":\"1\",\"recData\":\"\",\"ADD_DATE\":\"2018-08-30 15:37:24\",\"recList\":[{\"recommend\":\"推荐推荐推荐推荐\"},{\"recommend\":\"推荐推荐推荐推荐\"},{\"recommend\":\"推荐推荐推荐推荐\"}],\"SORT\":1},{\"FUNDCODE\":\"110011\",\"F_ID\":20180705112532,\"LABEL_SET\":\"推荐标签1|推荐标签2|\",\"iFundData\":{\"SYL_Y\":\"\",\"SYL_Z\":\"\",\"fundState\":\"0\",\"fundCode\":\"110011\",\"PrdAttr\":\"3\",\"SYL_2N\":\"\",\"tranTime\":\"20190414082438\",\"SYL_6Y\":\"\",\"fundType\":\"3\",\"riskLevelName\":\"中高风险\",\"netValueDate\":\"11-26\",\"SYL_7D\":\"0.00\",\"fundManager\":\"易方达基金\",\"SYL_D\":\"\",\"hotOrNew\":\"0\",\"fundName\":\"易方达中小盘\",\"RANK3Y\":\"★★★★★\",\"SYL_5N\":\"\",\"NVGRWTD\":\"\",\"SYL_3N\":\"\",\"riskLevel\":\"4\",\"fundManagerCode\":\"11\",\"IsAgio\":\"\",\"SYL_1N\":\"\",\"fundStateName\":\"开放期\",\"unitNetValue\":\"3.2131\",\"TACode\":\"11\",\"SYL_JN\":\"\",\"SYL_3Y\":\"\"},\"RISE\":\"1\",\"recData\":\"\",\"ADD_DATE\":\"2018-07-05 11:25:00\",\"recList\":[{\"recommend\":\"推荐标签1\"},{\"recommend\":\"推荐标签2\"}],\"SORT\":2,\"RECOMMENDSAY\":\"推荐话术，推荐话术\"}]});";
        execJsFunction(success, _json);
    }

    /**
     * 登录session获取
     */
    @JavascriptInterface
    public void getSession(String obj) {
        Logs.dd(new String[]{"getSession", "登录session获取", obj}, TAG);

        try {
            JSONObject jsonObject = new JSONObject(obj);
            final String sessionFunc = jsonObject.getString("callback");

            final JSONObject jobj = new JSONObject();
            //内网数据
            execJsFunction(sessionFunc, wrapParam("{\"loginFlag\":0,\"SESSION_TOKEN\":\"79b51cf339b2c82c8109c6478a891ae5\"}"));
            //外网数据
//            execJsFunction(sessionFunc, wrapParam("{\"loginFlag\":0,\"SESSION_TOKEN\":\"d0722f98eaeb7c8522c9775e650318fa\"}"));
        } catch (JSONException e) {
            Logs.e(TAG, e.getMessage());
        }
    }


    /**
     * 初始化导航栏信息
     * {"title":"标题","leftButton":{"exist":"true","name":"按钮名称",
     * "func":"回调函数名称" },"rightButton":{"exist":"false","name":"",
     * "func":""}}"exist":"true"显示按钮
     */
    @JavascriptInterface
    public void initPageTitle(final String obj) {
        Logs.dd(new String[]{"initPageTitle", "初始化头部信息", obj}, TAG);
    }
    //endregion
}
