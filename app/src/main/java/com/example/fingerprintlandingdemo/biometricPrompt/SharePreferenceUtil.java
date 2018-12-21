package com.example.fingerprintlandingdemo.biometricPrompt;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by yudoudou on 2018/12/21.
 */
class SharePreferenceUtil {
    private Context mContext;
    private String mFileName;
    private SharedPreferences mSharedPreferences;

    public SharePreferenceUtil(Context context, String fileName) {
        this.mContext = context;
        this.mFileName = fileName;
        this.mSharedPreferences = context.getSharedPreferences(fileName, context.MODE_PRIVATE);
    }

    public String readStringValue(String key, String defValue) {
        return mSharedPreferences.getString(key, defValue);
    }

    public boolean writeStringValue(String key, String value) {
        return mSharedPreferences.edit().putString(key, value).commit();
    }

    public boolean readBooleanValue(String key, boolean defValue) {
        return mSharedPreferences.getBoolean(key, defValue);
    }

    public boolean writeBooleanValue(String key, boolean value) {
        return mSharedPreferences.edit().putBoolean(key, value).commit();
    }
}
