/*
 *
 *  * Tencent is pleased to support the open source community by making VasSonic available.
 *  *
 *  * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 *  * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  *
 *  * https://opensource.org/licenses/BSD-3-Clause
 *  *
 *  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *  *
 *  *
 *
 */

package com.tencent.sonic.sdk.download;

import android.text.TextUtils;
import android.util.Log;

import com.tencent.sonic.sdk.SonicConstants;
import com.tencent.sonic.sdk.SonicEngine;
import com.tencent.sonic.sdk.SonicFileUtils;
import com.tencent.sonic.sdk.SonicResourceDataHelper;
import com.tencent.sonic.sdk.SonicUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Sonic download cache manager
 */

public abstract class SonicDownloadCache {

    /**
     * get the cached content according to the url
     *
     * @param url the download url
     * @return bytes of cached content of the url
     */
    public abstract byte[] getResourceCache(String url);

    /**
     * get the cached response headers according to the url
     *
     * @param url the download url
     * @return cached headers of the url
     */
    public abstract Map<String, List<String>> getResourceCacheHeader(String url);

    /**
     * @return Sub resource cache
     */
    public static SonicDownloadCache getSubResourceCache() {
        return new SonicResourceCache();
    }

    /**
     * An sub resource cache implementation {@link SonicDownloadCache}
     */
    public static class SonicResourceCache extends SonicDownloadCache {

        /**
         * log filter
         */
        public static final String TAG = SonicConstants.SONIC_SDK_LOG_PREFIX + "SonicDownloadCache";

        /**
         * 获取url对应的缓存资源文件，并进行校验，如果一切通过，则返回文件的二进制流数据，否则返回null
         * 缓存目录：runtime.getSonicResourceCacheDir
         */
        public byte[] getResourceCache(String resourceUrl) {
            if (TextUtils.isEmpty(resourceUrl)) {
                return null;
            }
            String resourceId = SonicUtils.getMD5(resourceUrl);
            //从数据库中获取资源idDUIYINGDEXINXI
            SonicResourceDataHelper.ResourceData resourceData = SonicResourceDataHelper.getResourceData(resourceId);

            // the resource cache expired
            if (resourceData.expiredTime < System.currentTimeMillis()) {
                /* 失效则返回null */
                return null;
            }

            boolean verifyError;
            byte[] resourceBytes = null;
            // verify local data
            if (TextUtils.isEmpty(resourceData.resourceSha1)) {//资源信息有缺陷
                verifyError = true;
                SonicUtils.log(TAG, Log.INFO, "get resource data(" + resourceUrl + "): resource data is empty.");
            } else {
                /* 获取对应的资源缓存文件和其数据，并对其进行校验 */
                //获取资源文件的绝对路径，缓存资源以resourceId作为文件名，没有后缀，目录则是通过runtime.getSonicResourceCacheDir来获取
                String resourcePath = SonicFileUtils.getSonicResourcePath(resourceId);
                File resourceFile = new File(resourcePath);
                //读取文件的二进制流
                resourceBytes = SonicFileUtils.readFileToBytes(resourceFile);
                //如果文件二进制流是空的，说明资源出现了问题
                verifyError = resourceBytes == null || resourceBytes.length <= 0;
                if (verifyError) {
                    SonicUtils.log(TAG, Log.ERROR, "get resource data(" + resourceUrl + ") error:cache data is null.");
                } else {
                    if (SonicEngine.getInstance().getConfig().VERIFY_CACHE_FILE_WITH_SHA1) {
                        //校验sha1
                        if (!SonicFileUtils.verifyData(resourceBytes, resourceData.resourceSha1)) {
                            verifyError = true;
                            resourceBytes = null;
                            SonicUtils.log(TAG, Log.ERROR, "get resource data(" + resourceUrl + ") error:verify html cache with sha1 fail.");
                        } else {
                            SonicUtils.log(TAG, Log.INFO, "get resource data(" + resourceUrl + ") verify html cache with sha1 success.");
                        }
                    } else {
                        //校验文件大小
                        if (resourceData.resourceSize != resourceFile.length()) {
                            verifyError = true;
                            resourceBytes = null;
                            SonicUtils.log(TAG, Log.ERROR, "get resource data(" + resourceUrl + ") error:verify html cache with size fail.");
                        }
                    }
                }
            }
            // if the local data is faulty, delete it
            //校验失败，清除该缓存文件,清空数据库对应的数据
            if (verifyError) {
                long startTime = System.currentTimeMillis();
                SonicUtils.removeResourceCache(resourceId);
                resourceData.reset();
                SonicUtils.log(TAG, Log.INFO, "get resource data(" + resourceUrl + ") :verify error so remove session cache, cost " + +(System.currentTimeMillis() - startTime) + "ms.");
            }
            return resourceBytes;
        }

        public Map<String, List<String>> getResourceCacheHeader(String resourceUrl) {
            String resourceName = SonicUtils.getMD5(resourceUrl);
            String headerPath = SonicFileUtils.getSonicResourceHeaderPath(resourceName);
            return SonicFileUtils.getHeaderFromLocalCache(headerPath);
        }
    }
}
