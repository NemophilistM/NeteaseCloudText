package com.example.neteasecloudmusictext.model;

import android.util.Log;
import com.example.neteasecloudmusictext.util.HttpUtil;
import com.example.neteasecloudmusictext.view.ViewConstants;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginModel {

    public static void requestPhone(String phone,CallBack callBack) {
        RequestBody requestBody = new FormBody.Builder()
                .add("phone", phone)
                .build();
        Request request = new Request.Builder()
                .url(ViewConstants.PREFIX_URL + ViewConstants.CAPTCHA_SEND)
                .post(requestBody)
                .build();

        HttpUtil.getThreadPoolExecutor().execute(() -> {
            try {
                Response response = HttpUtil.getOkHttpClient().newCall(request).execute();

                String responseData= response.body().string();
                if(!responseData.isEmpty()){
                    callBack.CallBackWhetherTrue(responseData);
                }
                Log.d(ViewConstants.TAG,responseData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


    }

    public static void requestCaptcha(String captcha, String phone,CallBack callBack) {
        RequestBody requestBody = new FormBody.Builder()
                .add("phone",phone)
                .add("captcha",captcha)
                .build();
        Request request = new Request.Builder()
                .url(ViewConstants.PREFIX_URL+ViewConstants.CAPTCHA_CHECK)
                .post(requestBody)
                .build();
        HttpUtil.getThreadPoolExecutor().execute(()->{
            try {
                Response response = HttpUtil.getOkHttpClient().newCall(request).execute();
                assert response.body() != null;
                String responseData = response.body().string();
                if(!responseData.isEmpty()){
                    Request requestUserData = new Request.Builder()
                            .url(ViewConstants.PREFIX_URL+ViewConstants.GET_USER_DATA)
                            .get()
                            .build();
                    Call call = HttpUtil.getOkHttpClient().newCall(requestUserData);
                    Response response1 = call.execute();
                    String responseUserData=response1.body().string();
                    Log.e(ViewConstants.TAG,responseUserData);
                    callBack.CallBackWhetherTrue(responseData);
                }
                Log.d(ViewConstants.TAG,responseData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    public interface CallBack{
        public void CallBackWhetherTrue(String information );
    }



}
