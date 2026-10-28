package com.khang.batterysaver;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.khang.batterysaver.model.BatteryInfo;
import com.khang.batterysaver.util.BatteryUtils;
import com.khang.batterysaver.util.IntentHelper;
import com.khang.batterysaver.util.NavHelper;

/** Màn hình chi tiết pin: nhận dữ liệu từ MainActivity qua Intent extras. */
public class BatteryDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavHelper.enableEdgeToEdge(this);
        setContentView(R.layout.activity_battery_detail);
        NavHelper.applyInsets(findViewById(R.id.main), true);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Đọc dữ liệu được gửi qua Intent
        BatteryInfo info = BatteryInfo.readFrom(getIntent());

        ((TextView) findViewById(R.id.tvDetailPercent)).setText(getString(R.string.fmt_percent, info.level));
        ((TextView) findViewById(R.id.tvDetailStatus)).setText(BatteryUtils.statusText(this, info));
        ((TextView) findViewById(R.id.tvAdvice)).setText(BatteryUtils.adviceText(this, info.health));

        ViewGroup container = findViewById(R.id.containerRows);
        addRow(container, R.string.label_status, BatteryUtils.statusText(this, info));
        addRow(container, R.string.label_source, BatteryUtils.sourceText(this, info));
        addRow(container, R.string.label_temp, getString(R.string.fmt_temp, info.temperature));
        addRow(container, R.string.label_voltage, getString(R.string.fmt_voltage, info.voltage / 1000f));
        addRow(container, R.string.label_health, BatteryUtils.healthText(this, info.health));
        addRow(container, R.string.label_tech, info.technology);

        findViewById(R.id.btnShare).setOnClickListener(v ->
                IntentHelper.shareText(this, BatteryUtils.summary(this, info)));
        findViewById(R.id.btnOpenSaver).setOnClickListener(v ->
                IntentHelper.openSystemSettings(this, Settings.ACTION_BATTERY_SAVER_SETTINGS));
    }

    private void addRow(ViewGroup container, int labelRes, String value) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_detail_row, container, false);
        ((TextView) row.findViewById(R.id.tvRowLabel)).setText(labelRes);
        ((TextView) row.findViewById(R.id.tvRowValue)).setText(value);
        container.addView(row);
    }
}
