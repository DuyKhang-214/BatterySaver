package com.khang.batterysaver.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.khang.batterysaver.R;
import com.khang.batterysaver.model.BatteryInfo;

/** Các hàm tiện ích đọc và chuyển thông tin pin thành chữ dễ đọc. */
public final class BatteryUtils {

    private BatteryUtils() {
    }

    /** Đọc nhanh trạng thái pin hiện tại (broadcast sticky nên không cần receiver). */
    public static BatteryInfo read(Context context) {
        Intent i = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        return BatteryInfo.fromBatteryIntent(i);
    }

    public static String statusText(Context c, BatteryInfo info) {
        if (info.isFull()) return c.getString(R.string.status_full);
        if (info.isCharging()) return c.getString(R.string.status_charging);
        if (info.status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
            return c.getString(R.string.status_discharging);
        }
        return c.getString(R.string.status_not_charging);
    }

    public static String sourceText(Context c, BatteryInfo info) {
        if ((info.plugged & BatteryManager.BATTERY_PLUGGED_AC) != 0) return c.getString(R.string.src_ac);
        if ((info.plugged & BatteryManager.BATTERY_PLUGGED_USB) != 0) return c.getString(R.string.src_usb);
        if ((info.plugged & BatteryManager.BATTERY_PLUGGED_WIRELESS) != 0) {
            return c.getString(R.string.src_wireless);
        }
        return c.getString(R.string.src_none);
    }

    public static String healthText(Context c, int health) {
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_GOOD:
                return c.getString(R.string.health_good);
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                return c.getString(R.string.health_overheat);
            case BatteryManager.BATTERY_HEALTH_DEAD:
                return c.getString(R.string.health_dead);
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                return c.getString(R.string.health_over_voltage);
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                return c.getString(R.string.health_failure);
            case BatteryManager.BATTERY_HEALTH_COLD:
                return c.getString(R.string.health_cold);
            default:
                return c.getString(R.string.health_unknown);
        }
    }

    public static String adviceText(Context c, int health) {
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_GOOD:
                return c.getString(R.string.advice_good);
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                return c.getString(R.string.advice_overheat);
            case BatteryManager.BATTERY_HEALTH_COLD:
                return c.getString(R.string.advice_cold);
            case BatteryManager.BATTERY_HEALTH_DEAD:
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                return c.getString(R.string.advice_bad);
            default:
                return c.getString(R.string.advice_unknown);
        }
    }

    /** Một dòng tóm tắt dùng để sao chép. */
    public static String summary(Context c, BatteryInfo info) {
        return c.getString(R.string.info_clip,
                info.level,
                statusText(c, info),
                info.temperature,
                info.voltage / 1000f,
                healthText(c, info.health));
    }

    /** Nội dung chia sẻ tình trạng pin. */
    public static String shareText(Context c, BatteryInfo info) {
        return c.getString(R.string.share_text, info.level, statusText(c, info));
    }
}
