package com.khang.batterysaver.model;

import android.content.Intent;
import android.os.BatteryManager;

/** Chứa thông tin pin đọc từ broadcast ACTION_BATTERY_CHANGED. */
public class BatteryInfo {

    public static final String EXTRA_LEVEL = "extra_level";
    public static final String EXTRA_STATUS = "extra_status";
    public static final String EXTRA_PLUGGED = "extra_plugged";
    public static final String EXTRA_HEALTH = "extra_health";
    public static final String EXTRA_TEMP = "extra_temp";
    public static final String EXTRA_VOLTAGE = "extra_voltage";
    public static final String EXTRA_TECH = "extra_tech";

    public int level;
    public int status = BatteryManager.BATTERY_STATUS_UNKNOWN;
    public int plugged;
    public int health = BatteryManager.BATTERY_HEALTH_UNKNOWN;
    public float temperature;
    public int voltage;
    public String technology = "N/A";

    public boolean isCharging() {
        return status == BatteryManager.BATTERY_STATUS_CHARGING;
    }

    public boolean isFull() {
        return status == BatteryManager.BATTERY_STATUS_FULL;
    }

    /** true nếu máy chưa cắm sạc (đang chạy bằng pin). */
    public boolean isOnBattery() {
        return plugged == 0;
    }

    /** Phân tích Intent sticky của hệ thống thành đối tượng BatteryInfo. */
    public static BatteryInfo fromBatteryIntent(Intent i) {
        BatteryInfo b = new BatteryInfo();
        if (i == null) return b;

        int level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        b.level = (level < 0 || scale <= 0) ? 0 : Math.round(level * 100f / scale);
        b.status = i.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);
        b.plugged = i.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
        b.health = i.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN);
        b.temperature = i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f;
        b.voltage = i.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
        String tech = i.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
        b.technology = (tech == null || tech.isEmpty()) ? "N/A" : tech;
        return b;
    }

    /** Đóng gói dữ liệu vào Intent để gửi sang màn hình khác. */
    public void writeTo(Intent intent) {
        intent.putExtra(EXTRA_LEVEL, level);
        intent.putExtra(EXTRA_STATUS, status);
        intent.putExtra(EXTRA_PLUGGED, plugged);
        intent.putExtra(EXTRA_HEALTH, health);
        intent.putExtra(EXTRA_TEMP, temperature);
        intent.putExtra(EXTRA_VOLTAGE, voltage);
        intent.putExtra(EXTRA_TECH, technology);
    }

    /** Đọc lại dữ liệu từ Intent do màn hình khác gửi tới. */
    public static BatteryInfo readFrom(Intent intent) {
        BatteryInfo b = new BatteryInfo();
        if (intent == null) return b;
        b.level = intent.getIntExtra(EXTRA_LEVEL, 0);
        b.status = intent.getIntExtra(EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);
        b.plugged = intent.getIntExtra(EXTRA_PLUGGED, 0);
        b.health = intent.getIntExtra(EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN);
        b.temperature = intent.getFloatExtra(EXTRA_TEMP, 0f);
        b.voltage = intent.getIntExtra(EXTRA_VOLTAGE, 0);
        String tech = intent.getStringExtra(EXTRA_TECH);
        b.technology = tech == null ? "N/A" : tech;
        return b;
    }
}
