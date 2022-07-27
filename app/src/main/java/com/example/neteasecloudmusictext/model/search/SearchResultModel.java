package com.example.neteasecloudmusictext.model.search;

import android.view.View;

import com.example.neteasecloudmusictext.model.Formatter;
import com.example.neteasecloudmusictext.util.HttpUtil;
import com.example.neteasecloudmusictext.view.ViewConstants;
import com.example.player.entity.Song;

import java.io.IOException;

import java.util.List;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SearchResultModel {
    public static String responseData;
    public static void requestResultSongList(String postWords,int curPage,CallBack callBack){

            RequestBody requestBody = new FormBody.Builder()
                    .add(ViewConstants.KEY_WORD,postWords)
                    .add(ViewConstants.LIMIT, String.valueOf(ViewConstants.REQUEST_PAGE_MAX))
                    .add(ViewConstants.OFFSET, String.valueOf((curPage-1)* ViewConstants.REQUEST_PAGE_MAX))
//                    .add(ViewConstants.TIMES_TAMP_ADD, String.valueOf(System.currentTimeMillis()))
                    .build();

            Request request = new Request.Builder()
                    .url(ViewConstants.PREFIX_URL + ViewConstants.CLOUD_SEARCH_SONG+ViewConstants.TIMES_TAMP+ System.currentTimeMillis())
//                    .url(ViewConstants.PREFIX_URL + ViewConstants.CLOUD_SEARCH_SONG)
                    .post(requestBody)
                    .build();
            HttpUtil.getThreadPoolExecutor().execute(()->{
                Response response = null;
                try {
                    response = HttpUtil.getOkHttpClient().newCall(request).execute();
                    assert response.body() != null;
                    responseData= response.body().string();
                    List<Song> songs = Formatter.songLists(responseData);
                    callBack.callBackWhetherTure(songs);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            });
//        }else {
//            List<Song> songs = Formatter.songLists(responseData);
//            callBack.callBackWhetherTure(songs);
//        }

    }
    public static void requestAllSong (String postWords, CallBack callback){
        RequestBody requestBody = new FormBody.Builder()
                .add(ViewConstants.KEY_WORD,postWords)
                .build();
        Request request = new Request.Builder()
                .url(ViewConstants.PREFIX_URL + ViewConstants.CLOUD_SEARCH_SONG)
                .post(requestBody)
                .build();
        HttpUtil.getThreadPoolExecutor().execute(()->{
            Response response = null;
            try {
                response = HttpUtil.getOkHttpClient().newCall(request).execute();
                assert response.body() != null;
                responseData= response.body().string();
                List<Song> songs = Formatter.songLists(responseData);
                callback.callBackWhetherTure(songs);
            } catch (IOException e) {
                e.printStackTrace();
            }


        });
    }
    public interface  CallBack{
        public void callBackWhetherTure(List<Song> songs);
    }
}
