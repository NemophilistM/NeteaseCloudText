package com.example.neteasecloudmusictext.model;

import android.util.Log;

import com.example.neteasecloudmusictext.util.HttpUtil;
import com.example.neteasecloudmusictext.view.ViewConstants;
import com.example.neteasecloudmusictext.view.ViewConstants;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterModel {
    public static void requestNetWork(String phone) {
        RequestBody requestBody = new FormBody.Builder()
                .add("phone", phone)
                .build();
        Request request = new Request.Builder()
                .url(ViewConstants.PREFIX_URL + ViewConstants.CAPTCHA_SEND)
                .post(requestBody)
                .build();
        HttpUtil.getThreadPoolExecutor().execute(() -> {
            try {
                HttpUtil.getOkHttpClient().newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }
    public static  void  requestRegister(String userName,String password,String phone,String captcha){
        RequestBody requestBody = new FormBody.Builder()
                .add("captcha",captcha)
                .add("phone",phone)
                .add("password",password)
                .add("nickname",userName)
                .build();
        Request request = new Request.Builder()
                .url(ViewConstants.PREFIX_URL+ViewConstants.REGISTER)
                .post(requestBody)
                .build();
        HttpUtil.getThreadPoolExecutor().execute(()->{
            try {
                Response response =  HttpUtil.getOkHttpClient().newCall(request).execute();
                assert response.body() != null;
                String responseData = response.body().string();
                Log.d(ViewConstants.TAG,responseData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
