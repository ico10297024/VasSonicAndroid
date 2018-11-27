/*
 * Tencent is pleased to support the open source community by making VasSonic available.
 *
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 *
 */

package com.tencent.sonic.sdk;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Interacts with the overall SonicSessions running in the system.
 * Instances of this class can be used to query or fetch the information, such as SonicSession SonicRuntime.
 */
public class SonicEngine {

    /**
     * Log filter
     */
    private final static String TAG = SonicConstants.SONIC_SDK_LOG_PREFIX + "SonicEngine";

    /**
     * SonicRuntime
     */
    private final SonicRuntime runtime;

    /**
     * Global config
     */
    private final SonicConfig config;

    /**
     * Single instance
     */
    private static SonicEngine sInstance;

    /**
     * Map containing preload session with capacity limits.
     */
    private final ConcurrentHashMap<String, SonicSession> preloadSessionPool = new ConcurrentHashMap<String, SonicSession>(5);

    /**
     * Map containing weak reference of running sessions.
     */
    private final ConcurrentHashMap<String, SonicSession> runningSessionHashMap = new ConcurrentHashMap<String, SonicSession>(5);


    private SonicEngine(SonicRuntime runtime, SonicConfig config) {
        this.runtime = runtime;
        this.config = config;
    }

    /**
     * Returns a SonicEngine instance
     * <p>
     * Make sure {@link #createInstance(SonicRuntime, SonicConfig)} has been called.
     *
     * @return SonicEngine instance
     * @throws IllegalStateException if {@link #createInstance(SonicRuntime, SonicConfig)} hasn't been called
     */
    public static synchronized SonicEngine getInstance() {
        if (null == sInstance) {
            throw new IllegalStateException("SonicEngine::createInstance() needs to be called before SonicEngine::getInstance()");
        }
        return sInstance;
    }

    /**
     * Check if {@link #getInstance()} is ready or not.
     * <p><b>Note: {@link #createInstance(SonicRuntime, SonicConfig)} must be called if {@code false} is returned.</b></p>
     *
     * @return Return <code>true</code> if {@link #sInstance} is not null, <code>false</code> otherwise
     */
    public static synchronized boolean isGetInstanceAllowed() {
        return null != sInstance;
    }

    /**
     * Create SonicEngine instance. Meanwhile it will initialize engine and SonicRuntime.
     *
     * @param runtime SonicRuntime
     * @param config  SonicConfig
     * @return SonicEngine object
     */
    public static synchronized SonicEngine createInstance(@NonNull SonicRuntime runtime, @NonNull SonicConfig config) {
        log.d("===Sonic初始化","total1");
        if (null == sInstance) {
            sInstance = new SonicEngine(runtime, config);
            if (config.AUTO_INIT_DB_WHEN_CREATE) {
                sInstance.initSonicDB();
            }
        }

        log.d("===Sonic初始化完成","total1");
        return sInstance;
    }

    /**
     * Init sonic DB which will upgrade to new version of database.
     */
    public void initSonicDB() {
        SonicDBHelper.createInstance(getRuntime().getContext()).getWritableDatabase(); // init and update db
    }

    /**
     * @return SonicRuntime object
     */
    public SonicRuntime getRuntime() {
        return runtime;
    }

    /**
     * @return SonicConfig object
     */
    public SonicConfig getConfig() {
        return config;
    }


    /**
     * Whether Sonic Service is available or not
     *
     * @return return true if Sonic Service is available , false else others.
     */
    public boolean isSonicAvailable() {
        return !SonicDBHelper.getInstance().isUpgrading();
    }

    /**
     * Create session ID
     *
     * @param url              session url
     * @param isAccountRelated Session Id will contain {@link com.tencent.sonic.sdk.SonicRuntime#getCurrentUserAccount()}  if {@code isAccountRelated } is true.
     * @return String Object of session ID
     */
    public static String makeSessionId(String url, boolean isAccountRelated) {
        return getInstance().getRuntime().makeSessionId(url, isAccountRelated);
    }

    /**
     * This method will preCreate sonic session .
     * And maps the specified session id to the specified value in this table {@link #preloadSessionPool} if there is no same sonic session.
     * At the same time, if the number of {@link #preloadSessionPool} exceeds {@link SonicConfig#MAX_PRELOAD_SESSION_COUNT},
     * preCreateSession will return false and not create any sonic session.
     *
     * <p><b>Note: this method is intended for preload scene.</b></p>
     *
     * @param url           url for preCreate sonic session
     * @param sessionConfig SonicSession config
     * @return If this method preCreate sonic session and associated with {@code sessionId} in this table {@link #preloadSessionPool} successfully,
     * it will return true,
     * <code>false</code> otherwise.
     */
    public synchronized boolean preCreateSession(@NonNull String url, @NonNull SonicSessionConfig sessionConfig) {
        if (isSonicAvailable()) {
            String sessionId = makeSessionId(url, sessionConfig.IS_ACCOUNT_RELATED);
            if (!TextUtils.isEmpty(sessionId)) {
                SonicSession sonicSession = lookupSession(sessionConfig, sessionId, false);
                if (null != sonicSession) {
                    runtime.log(TAG, Log.ERROR, "preCreateSession：sessionId(" + sessionId + ") is already in preload pool.");
                    return false;
                }
                if (preloadSessionPool.size() < config.MAX_PRELOAD_SESSION_COUNT) {
                    if (isSessionAvailable(sessionId) && runtime.isNetworkValid()) {
                        sonicSession = internalCreateSession(sessionId, url, sessionConfig);
                        if (null != sonicSession) {
                            preloadSessionPool.put(sessionId, sonicSession);
                            return true;
                        }
                    }
                } else {
                    runtime.log(TAG, Log.ERROR, "create id(" + sessionId + ") fail for preload size is bigger than " + config.MAX_PRELOAD_SESSION_COUNT + ".");
                }
            }
        } else {
            runtime.log(TAG, Log.ERROR, "preCreateSession fail for sonic service is unavailable!");
        }
        return false;
    }

    /**
     * 根据指定的url创建一个session对象
     * <p>
     * 内部先根据url创建出sessionId，然后通过这个sessionId查看有没有 preload 的session对象
     * <p>
     * 如果没有preload的session对象，则检查sessionId对应的老数据是否已失效
     * <p>
     * 如果老数据已失效，则调用internalCreateSession进行创建
     *
     * @param url           url for SonicSession Object
     * @param sessionConfig SSonicSession config
     * @return This method will create and return SonicSession Object when url is legal.
     */
    public synchronized SonicSession createSession(@NonNull String url, @NonNull SonicSessionConfig sessionConfig) {
        if (isSonicAvailable()) {
            log.w(String.format("makeSessionId"), "sonic1");
            //通过url首先创建一个sessionid，makeSessionId内部调用sonicruntime.makeSessionId
            //也就是说，我们可以通过重写makeSessionId来控制创建出来的sessionId
            String sessionId = makeSessionId(url, sessionConfig.IS_ACCOUNT_RELATED);
            if (!TextUtils.isEmpty(sessionId)) {
                //查看是否有预创建的session，或者说缓存的session
                SonicSession sonicSession = lookupSession(sessionConfig, sessionId, true);
                if (null != sonicSession) {
                    //设置该session为预创建的session
                    //TODO ICO 但是为什么预创建的时候不设置呢？
                    sonicSession.setIsPreload(url);
                } else if (isSessionAvailable(sessionId)) { // 缓存中未存在
                    log.w(String.format("internalCreateSession"), "sonic1");
                    //根据sessionId创建SonicSession对象
                    sonicSession = internalCreateSession(sessionId, url, sessionConfig);
                }
                return sonicSession;
            }
        } else {
            runtime.log(TAG, Log.ERROR, "createSession fail for sonic service is unavailable!");
        }
        return null;
    }


    /**
     * 获取preload的session，并判断其有效性，如果有效则返回，无效则返回null
     *
     * @param sessionId possible sessionId
     * @param pick      When {@code pick} is true and there is SonicSession in {@link #preloadSessionPool},
     *                  it will remove from {@link #preloadSessionPool}
     *                  代表取的同时是否移除
     * @return Return valid SonicSession Object from {@link #preloadSessionPool} if the specified sessionId is a key in {@link #preloadSessionPool}.
     */
    private SonicSession lookupSession(SonicSessionConfig config, String sessionId, boolean pick) {
        if (!TextUtils.isEmpty(sessionId) && config != null) {
            //通过preloadSessionPool来获取preload的sonicSession
            //preloadSessionPool可以通过preCreateSession函数来对url进行 提前加载 来加速加载时间
            //比如某个页面我们知道用户一定会去打开，就可以通过这个函数来预先加载这个url，来达到加速的目的
            //TODO ICO 这里我先跳过，后面再回头过来看preloadSessionPool和preCreateSession
            SonicSession sonicSession = preloadSessionPool.get(sessionId);
            if (sonicSession != null) {
                //判断session缓存是否过期,以及sessionConfig是否发生变化

                //这里总共3个判断条件，从第一个判断条件可以知道每一个session对应一个config
                //sessionConfig.equals来判断config是否有变化，通过sessionMode和SUPPORT_LOCAL_SERVER进行判断
                //预加载的session有效时间，保存在SonicSessionConfig中，默认为3*60*1000,可以通过config.setPreloadSessionExpiredTimeMillis来更改有效时间
                //判断session的存续时长是否大于有效时长，大于则失效
                if (!config.equals(sonicSession.config)
                        || sonicSession.config.PRELOAD_SESSION_EXPIRED_TIME > 0
                        && System.currentTimeMillis() - sonicSession.createdTime > sonicSession.config.PRELOAD_SESSION_EXPIRED_TIME) {
                    //获取是否允许 ERROR 级别日志，默认为true，我们可以重写该函数
                    if (runtime.shouldLog(Log.ERROR)) {
                        runtime.log(TAG, Log.ERROR, "lookupSession error:sessionId(" + sessionId + ") is expired.");
                    }
                    /* preload的session失效啦 */
                    preloadSessionPool.remove(sessionId);
                    sonicSession.destroy();
                    return null;
                }

                //pick标识该session是否要从缓存中移除
                //preCreateSession是预编译，也是通过lookupSession来进行缓存的，所以传的是false，用以正式使用
                //而createSession是正式创建并使用，为了保证页面是最新的，所以传的是true
                if (pick) {
                    preloadSessionPool.remove(sessionId);
                }
            }
            return sonicSession;
        }
        return null;
    }

    /**
     * Create sonic session internal
     *
     * @param sessionId     session id
     * @param url           origin url
     * @param sessionConfig session config
     * @return Return new SonicSession if there was no mapping for the sessionId in {@link #runningSessionHashMap}
     */
    private SonicSession internalCreateSession(String sessionId, String url, SonicSessionConfig sessionConfig) {
        //每个运行中的session会被加入到runningSessionHashMap中
        //在session.destroy函数中将会从map中移除掉
        // 注：session.destroy的调用时机将会影响到下次session能否创建
        //TODO WIZ [session创建返回null]
        if (!runningSessionHashMap.containsKey(sessionId)) {
            SonicSession sonicSession;
            //根据配置的session模式创建对应的SonicSession
            //TODO ICO 后面再反过来研究两种模式的区别
            if (sessionConfig.sessionMode == SonicConstants.SESSION_MODE_QUICK) {
                sonicSession = new QuickSonicSession(sessionId, url, sessionConfig);
            } else {
                sonicSession = new StandardSonicSession(sessionId, url, sessionConfig);
            }
            //给session添加回调
            //该回调主要接受session状态，然后对runningSessionHashMap进行put和remove操作
            sonicSession.addSessionStateChangedCallback(sessionCallback);

            //根据标记是否立即开始运行session，默认为true
            if (sessionConfig.AUTO_START_WHEN_CREATE) {
                log.w(String.format("sonicSession.start()"), "sonic1");
                sonicSession.start();
            }
            return sonicSession;
        }
        if (runtime.shouldLog(Log.ERROR)) {
            runtime.log(TAG, Log.ERROR, "internalCreateSession error:sessionId(" + sessionId + ") is running now.");
        }
        return null;
    }

    /**
     * If the server fails or specifies HTTP pattern, SonicSession won't use Sonic pattern Within {@link com.tencent.sonic.sdk.SonicConfig#SONIC_UNAVAILABLE_TIME} ms
     * <p>
     * 从数据库中获取对应sessionId的数据，获取其sonic不可用的时间
     * <p>
     * 该值在sonic的http请求响应标签cache-offline为http的时候会刷新，记录sonic服务端的不可用时间
     * <p>
     * 如果当前时间大于不可用时间，则代表sonic服务端可能已经好了，可以再创建session创建请求看看
     *
     * @param sessionId session id
     * @return Test if the sessionId is available.
     */
    private boolean isSessionAvailable(String sessionId) {
        long unavailableTime = SonicDataHelper.getLastSonicUnavailableTime(sessionId);
        if (System.currentTimeMillis() > unavailableTime) {
            Log.w("ico_w_sonic", "isSessionAvailable: true");
            return true;
        }
        if (runtime.shouldLog(Log.ERROR)) {
            runtime.log(TAG, Log.ERROR, "sessionId(" + sessionId + ") is unavailable and unavailable time until " + unavailableTime + ".");
        }
        Log.w("ico_w_sonic", "isSessionAvailable: false");
        return false;
    }

    /**
     * Removes all of the cache from {@link #preloadSessionPool} and deletes file caches from SDCard.
     *
     * @return Returns {@code false} if {@link #runningSessionHashMap} is not empty.
     * Returns {@code true} if all of the local file cache has been deleted, <code>false</code> otherwise
     */
    public synchronized boolean cleanCache() {
        if (!preloadSessionPool.isEmpty()) {
            runtime.log(TAG, Log.INFO, "cleanCache: remove all preload sessions, size=" + preloadSessionPool.size() + ".");
            Collection<SonicSession> sonicSessions = preloadSessionPool.values();
            for (SonicSession session : sonicSessions) {
                session.destroy();
            }
            preloadSessionPool.clear();
        }

        if (!runningSessionHashMap.isEmpty()) {
            runtime.log(TAG, Log.ERROR, "cleanCache fail, running session map's size is " + runningSessionHashMap.size() + ".");
            return false;
        }

        runtime.log(TAG, Log.INFO, "cleanCache: remove all sessions cache.");

        return SonicUtils.removeAllSessionCache();
    }

    /**
     * Removes the sessionId and its corresponding SonicSession from {@link #preloadSessionPool}.
     *
     * @param sessionId A unique session id
     * @return Return {@code true} If there is no specified sessionId in {@link #runningSessionHashMap}, <code>false</code> otherwise.
     */
    public synchronized boolean removeSessionCache(@NonNull String sessionId) {
        SonicSession sonicSession = preloadSessionPool.get(sessionId);
        if (null != sonicSession) {
            sonicSession.destroy();
            preloadSessionPool.remove(sessionId);
            runtime.log(TAG, Log.INFO, "sessionId(" + sessionId + ") removeSessionCache: remove preload session.");
        }

        if (!runningSessionHashMap.containsKey(sessionId)) {
            runtime.log(TAG, Log.INFO, "sessionId(" + sessionId + ") removeSessionCache success.");
            SonicUtils.removeSessionCache(sessionId);
            return true;
        }
        runtime.log(TAG, Log.ERROR, "sessionId(" + sessionId + ") removeSessionCache fail: session is running.");
        return false;
    }

    /**
     * It will Post a task to trim sonic cache
     * if the last time of check sonic cache exceed {@link SonicConfig#SONIC_CACHE_CHECK_TIME_INTERVAL}.
     */
    public void trimSonicCache() {
        SonicFileUtils.checkAndTrimCache();
        SonicFileUtils.checkAndTrimResourceCache();
    }

    /**
     * <p>A callback receives notifications from a SonicSession.
     * Notifications indicate session related events, such as the running or the
     * destroy of the SonicSession.
     * It is intended to handle cache of SonicSession correctly to avoid concurrent modification.
     * </p>
     */
    private final SonicSession.Callback sessionCallback = new SonicSession.Callback() {
        @Override
        public void onSessionStateChange(SonicSession session, int oldState, int newState, Bundle extraData) {
            SonicUtils.log(TAG, Log.DEBUG, "onSessionStateChange:session(" + session.sId + ") from state " + oldState + " -> " + newState);
            switch (newState) {
                case SonicSession.STATE_RUNNING:
                    runningSessionHashMap.put(session.id, session);
                    break;
                case SonicSession.STATE_DESTROY:
                    Log.w("ico_w_sonic", "runningSessionHashMap.remove: ");
                    runningSessionHashMap.remove(session.id);
                    break;
            }
        }
    };

}
