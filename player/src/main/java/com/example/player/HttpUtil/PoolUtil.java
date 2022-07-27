package com.example.player.HttpUtil;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PoolUtil {
    public static ThreadPoolExecutor threadPoolExecutor;

    @SuppressWarnings("AlibabaThreadShouldSetName")
    public static void buildThreadPool() {
        threadPoolExecutor = new ThreadPoolExecutor(
                8, 15, 8, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10));
    }

    public static ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

}
