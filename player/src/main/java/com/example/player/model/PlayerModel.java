package com.example.player.model;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.renderscript.Sampler;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.player.ViewConstants;
import com.example.player.entity.Song;
import com.example.player.util.HttpUtil;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PlayerModel {
//    HandlerThread netHandlerThread = new HandlerThread(ViewConstants.HANDLER_THREAD_NET);
//    private Handler handler;
    public void requestMusicUrl(Long id,Callback callback){
//        netHandlerThread.start();
//        handler = new Handler(netHandlerThread.getLooper()){
//            @Override
//            public void handleMessage(@NonNull Message msg) {
//                super.handleMessage(msg);
//                long id = (long) msg.obj;
//                RequestBody requestBody = new FormBody.Builder()
//                        .add(ViewConstants.ID, String.valueOf(id))
//                        .build();
//                Request request = new Request.Builder()
//                        .url(ViewConstants.GET_SONG_URL+"?"+ViewConstants.TIMESTAMP+ System.currentTimeMillis())
//                        .post(requestBody)
//                        .build();
//                Response response = null;
//                try {
//                    response = HttpUtil.getOkHttpClient().newCall(request).execute();
//                    assert response.body() != null;
//                    String responseData= response.body().string();
//                    try {
//                        JSONObject root = new JSONObject(responseData);
//                        JSONArray data = root.getJSONArray(ViewConstants.DATA_JSON);
//                        JSONObject song = data.getJSONObject(0);
//                        String url =song.getString(ViewConstants.URL);
//                        callback.getUrl(url);
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//        Message message = Message.obtain();
//        message.obj = id;
//        handler.sendMessage(message);
        HttpUtil.getThreadPoolExecutor().execute(()->{
            RequestBody requestBody = new FormBody.Builder()
                    .add(ViewConstants.ID, String.valueOf(id))
                    .build();
            Request request = new Request.Builder()
                    .url(ViewConstants.GET_SONG_URL+"?"+ViewConstants.TIMESTAMP+ System.currentTimeMillis())
                    .post(requestBody)
                    .build();
            Response response = null;
            try {
                response = HttpUtil.getOkHttpClient().newCall(request).execute();
                assert response.body() != null;
                String responseData= response.body().string();
                try {
                    JSONObject root = new JSONObject(responseData);
                    JSONArray data = root.getJSONArray(ViewConstants.DATA_JSON);
                    JSONObject song = data.getJSONObject(0);
                    String url =song.getString(ViewConstants.URL);
                    callback.getUrl(url);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    public interface  Callback{
        void getUrl(String url);
        void dispatch();
    }
}
