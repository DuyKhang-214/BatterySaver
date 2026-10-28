package com.khang.batterysaver.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.khang.batterysaver.R;

/** Các Intent ngầm định (implicit): mở cài đặt hệ thống, chia sẻ nội dung. */
public final class IntentHelper {

    private IntentHelper() {
    }

    /** Mở một màn hình cài đặt của hệ thống, ví dụ Settings.ACTION_WIFI_SETTINGS. */
    public static void openSystemSettings(Context context, String action) {
        try {
            context.startActivity(new Intent(action));
        } catch (ActivityNotFoundException | SecurityException e) {
            Toast.makeText(context, R.string.no_app, Toast.LENGTH_SHORT).show();
        }
    }

    /** Chia sẻ một đoạn văn bản qua các ứng dụng khác (Zalo, Messenger, ...). */
    public static void shareText(Context context, String text) {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, text);
        try {
            context.startActivity(Intent.createChooser(send, context.getString(R.string.share_title)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.no_app, Toast.LENGTH_SHORT).show();
        }
    }
}
