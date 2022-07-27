package com.example.neteasecloudmusictext.model;

import android.content.Context;

import com.example.neteasecloudmusictext.view.ViewConstants;

public class SharedPreferencesAction {
    public static void setSharedPreferencesCookie(Context context, String cookie) {
        android.content.SharedPreferences sharedPreferences = context.getSharedPreferences(ViewConstants.COOKIE_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        android.content.SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(ViewConstants.COOKIE, cookie);
        edit.apply();

    }

    /**
     * 用于获取Cookie
     *
     * @param context 当前活动
     */
    public static String getSharedPreferencesCookie(Context context) {
        android.content.SharedPreferences sharedPreferences = context.getSharedPreferences(ViewConstants.COOKIE_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(ViewConstants.COOKIE, null);

    }

    public static void clearCookie(Context context) {
        android.content.SharedPreferences sharedPreferences = context.getSharedPreferences(ViewConstants.COOKIE_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
    }
}
