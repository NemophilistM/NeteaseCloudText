package com.example.neteasecloudmusictext.model.search;

import com.example.neteasecloudmusictext.util.HttpUtil;
import com.example.neteasecloudmusictext.view.ViewConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;
import okhttp3.Response;

public class SearchMainModel {
    public static void requestHotSongList(CallBack callBack){
        Request request = new Request.Builder()
                .url(ViewConstants.PREFIX_URL + ViewConstants.HOT_SONG_LIST)
                .get()
                .build();
        HttpUtil.getThreadPoolExecutor().execute(()->{
            Response response = null;
            try {
                response = HttpUtil.getOkHttpClient().newCall(request).execute();
                String responseData= response.body().string();
                int code = 0;
                String errorMsg = null;
                List<String> hotSong = new ArrayList<>();
                try {
                    JSONObject root = new JSONObject(responseData);
                    code = root.getInt(ViewConstants.CODE_JSON);
                    if(code == ViewConstants.SUCCESS_CODE_JSON){
                        JSONObject result = root.getJSONObject(ViewConstants.RESULT_JSON);
                        JSONArray hots = result.getJSONArray(ViewConstants.HOTS_JSON);
                        for (int i = 0; i < hots.length(); i++) {
                            JSONObject song = hots.getJSONObject(i);
                            hotSong.add(song.getString(ViewConstants.FIRST_JSON));
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
                callBack.CallBackWhetherTrue(hotSong);
            } catch (IOException e) {
                e.printStackTrace();
            }


        });
    }
    public interface CallBack{
        public void CallBackWhetherTrue(List<String> hotSong);
    }
}
