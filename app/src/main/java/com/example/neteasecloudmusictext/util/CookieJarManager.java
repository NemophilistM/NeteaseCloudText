package com.example.neteasecloudmusictext.util;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.neteasecloudmusictext.view.ViewConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class CookieJarManager implements CookieJar {

    List<Cookie> cookies;

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        this.cookies = cookies;
    }

    @NonNull
    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        if (cookies != null){
            return cookies;
        }

        return new ArrayList<Cookie>();
    }
}
