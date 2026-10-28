package com.khang.batterysaver.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/** Lưu cài đặt của ứng dụng bằng SharedPreferences. */
public class PrefsHelper {

    private static final String FILE = "battery_saver_prefs";
    private static final String KEY_THRESHOLD = "threshold";
    private static final String KEY_ALERT = "alert_enabled";
    private static final String KEY_AUTO = "auto_saver";
    private static final String KEY_MODE = "mode";
    private static final String KEY_DONE_TIPS = "done_tips";

    public static final int DEFAULT_THRESHOLD = 20;

    private final SharedPreferences sp;

    public PrefsHelper(Context context) {
        sp = context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public int getThreshold() {
        return sp.getInt(KEY_THRESHOLD, DEFAULT_THRESHOLD);
    }

    public void setThreshold(int value) {
        sp.edit().putInt(KEY_THRESHOLD, value).apply();
    }

    public boolean isAlertEnabled() {
        return sp.getBoolean(KEY_ALERT, true);
    }

    public void setAlertEnabled(boolean value) {
        sp.edit().putBoolean(KEY_ALERT, value).apply();
    }

    public boolean isAutoSaver() {
        return sp.getBoolean(KEY_AUTO, false);
    }

    public void setAutoSaver(boolean value) {
        sp.edit().putBoolean(KEY_AUTO, value).apply();
    }

    public int getMode() {
        return sp.getInt(KEY_MODE, 0);
    }

    public void setMode(int mode) {
        sp.edit().putInt(KEY_MODE, mode).apply();
    }

    public Set<String> getDoneTips() {
        // Phải copy ra tập mới, không được sửa trực tiếp Set do SharedPreferences trả về
        return new HashSet<>(sp.getStringSet(KEY_DONE_TIPS, new HashSet<>()));
    }

    public void setTipDone(String tipId, boolean done) {
        Set<String> set = getDoneTips();
        if (done) set.add(tipId);
        else set.remove(tipId);
        sp.edit().putStringSet(KEY_DONE_TIPS, set).apply();
    }

    public void clearDoneTips() {
        sp.edit().remove(KEY_DONE_TIPS).apply();
    }

    public void resetSettings() {
        sp.edit()
                .putInt(KEY_THRESHOLD, DEFAULT_THRESHOLD)
                .putBoolean(KEY_ALERT, true)
                .putBoolean(KEY_AUTO, false)
                .apply();
    }
}
