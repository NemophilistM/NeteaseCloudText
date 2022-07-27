package com.example.neteasecloudmusictext.vm;

import android.content.Context;

import com.example.neteasecloudmusictext.model.RegisterModel;
import com.example.neteasecloudmusictext.view.RegisterActivity;

import okhttp3.OkHttpClient;

public class RegisterViewModel implements RegisterActivity.PastInformation {
    @Override
    public void pastPhone(String phone) {
        RegisterModel.requestNetWork(phone);
    }

    @Override
    public void pastCaptcha(String phone, String captcha, String password, String userName) {
        RegisterModel.requestRegister(userName,password,phone,captcha);

    }


}
