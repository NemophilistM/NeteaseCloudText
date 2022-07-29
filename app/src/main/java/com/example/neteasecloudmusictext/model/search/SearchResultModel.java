package com.example.neteasecloudmusictext.model.search;


import com.example.neteasecloudmusictext.model.Formatter;
import com.example.neteasecloudmusictext.util.HttpUtil;
import com.example.neteasecloudmusictext.view.ViewConstants;
import com.example.player.entity.Song;

import java.io.IOException;

import java.util.List;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SearchResultModel {
    public static String responseData;

    /**
     * 请求歌单
     *
     * @param postWords 搜索关键词
     * @param curPage 页数
     * @param callBack 回调方法
     */
    public static void requestResultSongList(String postWords, int curPage, CallBack callBack) {

        RequestBody requestBody = new FormBody.Builder()
                .add(ViewConstants.KEY_WORD, postWords)
                .add(ViewConstants.LIMIT, String.valueOf(ViewConstants.REQUEST_PAGE_MAX))
                .add(ViewConstants.OFFSET, String.valueOf((curPage - 1) * ViewConstants.REQUEST_PAGE_MAX))
                .build();

        Request request = new Request.Builder()
                .url(ViewConstants.PREFIX_URL + ViewConstants.CLOUD_SEARCH_SONG + ViewConstants.TIMES_TAMP + System.currentTimeMillis())
                .post(requestBody)
                .build();
        HttpUtil.getThreadPoolExecutor().execute(() -> {
            Response response;
            try {
                response = HttpUtil.getOkHttpClient().newCall(request).execute();
                assert response.body() != null;
                responseData = response.body().string();
                List<Song> songs = Formatter.songLists(responseData);
                callBack.callBackWhetherTure(songs);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    /**
     * 回调给vm层设置数据
     */
    public interface CallBack {
        void callBackWhetherTure(List<Song> songs);
    }
}
