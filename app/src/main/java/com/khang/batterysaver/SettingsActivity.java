package com.khang.batterysaver;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import android.widget.TextView;
import com.khang.batterysaver.util.NavHelper;
import com.khang.batterysaver.util.PrefsHelper;

public class SettingsActivity extends AppCompatActivity {

    private PrefsHelper prefs;
    private View root;
    private TextView tvThreshold;
    private Slider sliderThreshold;
    private MaterialSwitch switchAlert, switchAuto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavHelper.enableEdgeToEdge(this);
        setContentView(R.layout.activity_settings);
        root = findViewById(R.id.main);
        NavHelper.applyInsets(root, false);

        prefs = new PrefsHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvThreshold = findViewById(R.id.tvThreshold);
        sliderThreshold = findViewById(R.id.sliderThreshold);
        switchAlert = findViewById(R.id.switchAlert);
        switchAuto = findViewById(R.id.switchAuto);
        MaterialButton btnReset = findViewById(R.id.btnReset);

        loadValues();

        // Các sự kiện: thay đổi giá trị là lưu ngay
        sliderThreshold.addOnChangeListener((slider, value, fromUser) -> {
            int t = (int) value;
            tvThreshold.setText(getString(R.string.fmt_threshold, t));
            if (fromUser) prefs.setThreshold(t);
        });
        switchAlert.setOnCheckedChangeListener((button, checked) -> prefs.setAlertEnabled(checked));
        switchAuto.setOnCheckedChangeListener((button, checked) -> prefs.setAutoSaver(checked));

        btnReset.setOnClickListener(v -> {
            prefs.resetSettings();
            loadValues();
            Snackbar.make(root, R.string.settings_reset, Snackbar.LENGTH_SHORT).show();
        });

        NavHelper.setupBottomNav(this, findViewById(R.id.bottomNav), R.id.nav_settings);
    }

    private void loadValues() {
        int threshold = prefs.getThreshold();
        sliderThreshold.setValue(threshold);
        tvThreshold.setText(getString(R.string.fmt_threshold, threshold));
        switchAlert.setChecked(prefs.isAlertEnabled());
        switchAuto.setChecked(prefs.isAutoSaver());
    }
}
