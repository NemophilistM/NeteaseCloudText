package com.example.neteasecloudmusictext.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class HttpUtil {
    public static ThreadPoolExecutor threadPoolExecutor;
    private static OkHttpClient client;

    @SuppressWarnings("AlibabaThreadShouldSetName")
    public static void buildThreadPool() {
        threadPoolExecutor = new ThreadPoolExecutor(
                8, 15, 8, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10));
    }

    public static ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

    public static  OkHttpClient getOkHttpClient (){
        if(client==null){
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            client =  builder.cookieJar(new CookieJarManager())
                    .build();

        }
        return client;

    }
}
