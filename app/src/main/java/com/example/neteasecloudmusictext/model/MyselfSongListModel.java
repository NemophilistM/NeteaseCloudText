package com.example.neteasecloudmusictext.model;

import com.example.neteasecloudmusictext.util.HttpUtil;
import com.example.player.entity.Song;

import java.util.List;

public class MyselfSongListModel {
    public static void requestSongList(callBack callBack){
//        RequestBody requestBody = new FormBody.Builder()
//                .add("phone", phone)
//                .build();
//        Request request = new Request.Builder()
//                .url(ViewConstants.PREFIX_URL + ViewConstants.CAPTCHA_SEND)
//                .post(requestBody)
//                .build();
        HttpUtil.getThreadPoolExecutor().execute(()->{

        });

    }

    public interface callBack{
        public void CallBackData(List<Song> list);
    }
}
