package com.tencent.sonic.demo.mine;

import android.content.Context;

import com.tencent.sonic.demo.SonicRuntimeImpl;
import com.tencent.sonic.sdk.SonicConfig;
import com.tencent.sonic.sdk.SonicEngine;
import com.tencent.sonic.sdk.SonicSession;
import com.tencent.sonic.sdk.SonicSessionClient;
import com.tencent.sonic.sdk.SonicSessionConfig;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class SonicSessionPool {
    private static final String TAG = SonicSessionPool.class.getSimpleName();
    private static volatile SonicSessionPool sonicSessionPool;

    /** 返回一个session池实例 */
    public static SonicSessionPool getInstance(int maxSize) {
        if (sonicSessionPool == null) {
            synchronized (SonicSessionPool.class) {
                if (sonicSessionPool == null) {
                    sonicSessionPool = new SonicSessionPool(maxSize);
                }
            }
        }
        return sonicSessionPool;
    }

    public SonicSessionPool(int maxSize) {
        this.mMaxSize = maxSize;
    }

    private int mMaxSize;
    private LinkedHashMap<String, SonicSession> sessionMap = new LinkedHashMap();

    public SonicSession createSession(Context context, String url, Class clazz) throws IllegalAccessException, InstantiationException {
        return createSession(context, url, clazz, new SonicSessionConfig.Builder().build());
    }


    public SonicSession createSession(Context context, String url, Class clazz, SonicSessionConfig sessionConfig) throws IllegalAccessException, InstantiationException {
        Logs.dd("url==" + url, "total1");
        SonicSession sonicSession;
        boolean needCreate = false;
        if (sessionMap.get(url) == null) {
            needCreate = true;
        } else if (sessionMap.get(url).isDestroyedOrWaitingForDestroy()) {
            needCreate = true;
            sessionMap.remove(url);
        }
        Logs.dd("needCreate==" + needCreate + "|" + url, "total1");
        if (!needCreate) {
            return sessionMap.get(url);
        }

        // step 1: Initialize sonic engine if necessary, or maybe u can do this when application created
        if (!SonicEngine.isGetInstanceAllowed()) {
            SonicEngine.createInstance(new SonicRuntimeImpl(context.getApplicationContext()), new SonicConfig.Builder().build());
        }

        // step 2: Create SonicSession
        sonicSession = SonicEngine.getInstance().createSession(url, sessionConfig);


        if (null != sonicSession) {
            sonicSession.bindClient((SonicSessionClient) clazz.newInstance());
            sessionMap.put(url, sonicSession);
        } else {
            Logs.ee("create session fail!", TAG);
            // this only happen when a same sonic session is already running,
            // u can comment following codes to feedback as a default mode.
//            throw new UnknownError("create session fail!");
//            return;
        }
        return sonicSession;
    }

    public void destroy(String url) {
        SonicSession session = sessionMap.remove(url);
        if (session == null) return;
        session.destroy();
    }

    public void destroyAll() {
        Iterator<Map.Entry<String, SonicSession>> iter = sessionMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, SonicSession> entry = iter.next();
            String key = entry.getKey();
            SonicSession value = entry.getValue();
            value.destroy();
            sessionMap.remove(key);
        }
    }
}
