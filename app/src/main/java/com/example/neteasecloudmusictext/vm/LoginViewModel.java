package com.example.neteasecloudmusictext.vm;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.neteasecloudmusictext.model.LoginModel;
import com.example.neteasecloudmusictext.view.LoginActivity;

public class LoginViewModel extends ViewModel implements LoginActivity.pastPhone {
    private final MutableLiveData<String> mutableLiveDataSendWhetherTrue = new MutableLiveData<>();
    public LiveData<String> liveDataSendWhetherTrue ;
    private final MutableLiveData<String> mutableLiveDataLoginWhetherTrue = new MutableLiveData<>();
    public LiveData<String> liveDataLoginWhetherTrue;

    public LoginViewModel() {
        liveDataSendWhetherTrue = mutableLiveDataSendWhetherTrue;
        liveDataLoginWhetherTrue = mutableLiveDataLoginWhetherTrue;
    }
    @Override
    public void pastPhone(String phone) {
        LoginModel.requestPhone(phone, information -> {
            mutableLiveDataSendWhetherTrue.postValue(information);
            liveDataSendWhetherTrue = mutableLiveDataSendWhetherTrue;
        });
    }

    @Override
    public void pastCaptcha(String phone, String captcha) {
        LoginModel.requestCaptcha(captcha,phone,information -> {
            mutableLiveDataLoginWhetherTrue.postValue(information);
            liveDataLoginWhetherTrue = mutableLiveDataLoginWhetherTrue;
        });
    }



}
