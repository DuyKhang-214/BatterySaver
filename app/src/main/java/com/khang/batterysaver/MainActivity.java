package com.khang.batterysaver;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.khang.batterysaver.model.BatteryInfo;
import com.khang.batterysaver.util.BatteryUtils;
import com.khang.batterysaver.util.IntentHelper;
import com.khang.batterysaver.util.NavHelper;
import com.khang.batterysaver.util.PrefsHelper;

public class MainActivity extends AppCompatActivity {

    private static final int MODE_NORMAL = 0;
    private static final int MODE_SAVER = 1;
    private static final int MODE_ULTRA = 2;

    // Độ sáng (%) và mức tiết kiệm ước tính (%) của từng chế độ
    private static final int[] MODE_BRIGHTNESS = {100, 50, 25};
    private static final int[] MODE_GAIN = {0, 25, 60};
    private static final int[] MODE_TITLES = {R.string.mode_normal, R.string.mode_saver, R.string.mode_ultra};
    private static final int[] MODE_DESCS = {R.string.mode_desc_normal, R.string.mode_desc_saver, R.string.mode_desc_ultra};
    private static final int[] MODE_BUTTONS = {R.id.btnModeNormal, R.id.btnModeSaver, R.id.btnModeUltra};
    private static final int[] MODE_MENU_IDS = {R.id.mode_normal, R.id.mode_saver, R.id.mode_ultra};

    private PrefsHelper prefs;
    private BatteryInfo lastInfo;
    private int currentMode = MODE_NORMAL;
    private int shownPercent = 0;
    private boolean lowAlertShown = false;
    private boolean updatingUi = false;
    private ObjectAnimator boltPulse;

    // Views
    private View root;
    private View cardHero;
    private CircularProgressIndicator ringBattery;
    private TextView tvPercent, tvStatus, tvSource;
    private ImageView imgBolt;
    private TextView valueTemp, valueVoltage, valueHealth, valueTech;
    private MaterialButtonToggleGroup toggleMode;
    private View cardModeDesc;
    private TextView tvModeTitle, tvModeDesc, tvModeGain, tvBrightness;
    private Slider sliderBrightness;
    private BottomNavigationView bottomNav;

    // Sự kiện: nhận broadcast mỗi khi pin thay đổi
    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUi(BatteryInfo.fromBatteryIntent(intent));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavHelper.enableEdgeToEdge(this);
        setContentView(R.layout.activity_main);
        root = findViewById(R.id.main);
        NavHelper.applyInsets(root, false);

        prefs = new PrefsHelper(this);
        lastInfo = BatteryUtils.read(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bindViews();
        setupInfoCards();
        setupQuickTiles();
        setupModeToggle();
        setupBrightness();
        setupButtons();
        findViewById(R.id.btnBatteryInsights).setOnClickListener(v ->
                startActivity(new Intent(this, BatteryInsightsActivity.class)));

        registerForContextMenu(cardHero); // nhấn giữ thẻ pin để mở Context Menu

        selectMode(prefs.getMode(), false);
        playEntranceAnimation();
    }

    private void bindViews() {
        cardHero = findViewById(R.id.cardHero);
        ringBattery = findViewById(R.id.ringBattery);
        tvPercent = findViewById(R.id.tvPercent);
        tvStatus = findViewById(R.id.tvStatus);
        tvSource = findViewById(R.id.tvSource);
        imgBolt = findViewById(R.id.imgBolt);
        toggleMode = findViewById(R.id.toggleMode);
        cardModeDesc = findViewById(R.id.cardModeDesc);
        tvModeTitle = findViewById(R.id.tvModeTitle);
        tvModeDesc = findViewById(R.id.tvModeDesc);
        tvModeGain = findViewById(R.id.tvModeGain);
        tvBrightness = findViewById(R.id.tvBrightness);
        sliderBrightness = findViewById(R.id.sliderBrightness);
        bottomNav = findViewById(R.id.bottomNav);
    }

    // ---------------------------------------------------------------- setup

    private void setupInfoCards() {
        valueTemp = bindInfo(R.id.cardTemp, R.drawable.ic_thermo, R.string.label_temp);
        valueVoltage = bindInfo(R.id.cardVoltage, R.drawable.ic_bolt, R.string.label_voltage);
        valueHealth = bindInfo(R.id.cardHealth, R.drawable.ic_heart, R.string.label_health);
        valueTech = bindInfo(R.id.cardTech, R.drawable.ic_info, R.string.label_tech);
    }

    private TextView bindInfo(int cardId, int iconRes, int labelRes) {
        View card = findViewById(cardId);
        ((ImageView) card.findViewById(R.id.imgIcon)).setImageResource(iconRes);
        ((TextView) card.findViewById(R.id.tvLabel)).setText(labelRes);
        return card.findViewById(R.id.tvValue);
    }

    private void setupQuickTiles() {
        bindTile(R.id.tileWifi, R.drawable.ic_wifi, R.string.tile_wifi, Settings.ACTION_WIFI_SETTINGS);
        bindTile(R.id.tileBluetooth, R.drawable.ic_bluetooth, R.string.tile_bluetooth, Settings.ACTION_BLUETOOTH_SETTINGS);
        bindTile(R.id.tileDisplay, R.drawable.ic_brightness, R.string.tile_display, Settings.ACTION_DISPLAY_SETTINGS);
        bindTile(R.id.tileLocation, R.drawable.ic_location, R.string.tile_location, Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    }

    private void bindTile(int tileId, int iconRes, int labelRes, String action) {
        View tile = findViewById(tileId);
        ((ImageView) tile.findViewById(R.id.imgTile)).setImageResource(iconRes);
        ((TextView) tile.findViewById(R.id.tvTile)).setText(labelRes);
        tile.setOnClickListener(v -> IntentHelper.openSystemSettings(this, action));
    }

    private void setupModeToggle() {
        toggleMode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked || updatingUi) return;
            for (int i = 0; i < MODE_BUTTONS.length; i++) {
                if (MODE_BUTTONS[i] == checkedId) {
                    selectMode(i, true);
                    toastModeChanged(i);
                    break;
                }
            }
        });
    }

    private void setupBrightness() {
        sliderBrightness.addOnChangeListener((slider, value, fromUser) -> {
            int percent = (int) value;
            tvBrightness.setText(getString(R.string.fmt_brightness, percent));
            if (fromUser) applyBrightness(percent);
        });
    }

    private void setupButtons() {
        MaterialButton btnDetails = findViewById(R.id.btnDetails);
        MaterialButton btnSystem = findViewById(R.id.btnSystemSaver);
        btnDetails.setOnClickListener(v -> openDetails());
        btnSystem.setOnClickListener(v ->
                IntentHelper.openSystemSettings(this, Settings.ACTION_BATTERY_SAVER_SETTINGS));
    }

    /** Hiệu ứng: các khối trượt lên lần lượt khi mở màn hình. */
    private void playEntranceAnimation() {
        int[] ids = {R.id.cardHero, R.id.gridInfo, R.id.sectionMode, R.id.cardBrightness,
                R.id.rowQuick, R.id.rowButtons};
        for (int i = 0; i < ids.length; i++) {
            Animation a = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            a.setStartOffset(i * 90L);
            findViewById(ids[i]).startAnimation(a);
        }
    }

    // ------------------------------------------------------------ lifecycle

    @Override
    protected void onStart() {
        super.onStart();
        // ACTION_BATTERY_CHANGED là sticky nên onReceive được gọi ngay lần đầu
        ContextCompat.registerReceiver(this, batteryReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED), ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavHelper.setupBottomNav(this, bottomNav, R.id.nav_home);
        if (lastInfo != null) updateUi(lastInfo); // áp dụng lại ngưỡng pin yếu nếu vừa đổi cài đặt
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(batteryReceiver);
        if (boltPulse != null) boltPulse.cancel();
    }

    // ------------------------------------------------------------------ UI

    private void updateUi(BatteryInfo info) {
        lastInfo = info;

        boolean low = info.isOnBattery() && info.level <= prefs.getThreshold();
        int color = ContextCompat.getColor(this, low ? R.color.danger : R.color.gold);

        animatePercent(info.level);
        ringBattery.setIndicatorColor(color);
        ringBattery.setProgressCompat(info.level, true);
        tvPercent.setTextColor(color);

        tvStatus.setText(BatteryUtils.statusText(this, info));
        tvSource.setText(BatteryUtils.sourceText(this, info));
        valueTemp.setText(getString(R.string.fmt_temp, info.temperature));
        valueVoltage.setText(getString(R.string.fmt_voltage, info.voltage / 1000f));
        valueHealth.setText(BatteryUtils.healthText(this, info.health));
        valueTech.setText(info.technology);

        updateBoltAnimation(info.isCharging());
        checkLowBattery(info, low);
    }

    private void animatePercent(int target) {
        ValueAnimator animator = ValueAnimator.ofInt(shownPercent, target);
        animator.setDuration(700);
        animator.addUpdateListener(a ->
                tvPercent.setText(getString(R.string.fmt_percent, (int) a.getAnimatedValue())));
        animator.start();
        shownPercent = target;
    }

    /** Biểu tượng tia sét nhấp nháy khi đang sạc. */
    private void updateBoltAnimation(boolean charging) {
        if (charging) {
            imgBolt.setImageResource(R.drawable.ic_bolt);
            if (boltPulse == null) {
                boltPulse = ObjectAnimator.ofFloat(imgBolt, "alpha", 1f, 0.25f);
                boltPulse.setDuration(700);
                boltPulse.setRepeatCount(ValueAnimator.INFINITE);
                boltPulse.setRepeatMode(ValueAnimator.REVERSE);
            }
            if (!boltPulse.isStarted()) boltPulse.start();
        } else {
            imgBolt.setImageResource(R.drawable.ic_battery);
            if (boltPulse != null) boltPulse.cancel();
            imgBolt.setAlpha(1f);
        }
    }

    private void checkLowBattery(BatteryInfo info, boolean low) {
        if (!low) {
            lowAlertShown = false;
            return;
        }
        if (lowAlertShown) return;
        lowAlertShown = true;

        if (prefs.isAutoSaver() && currentMode == MODE_NORMAL) {
            selectMode(MODE_SAVER, true);
            Snackbar.make(root, R.string.auto_saver_on, Snackbar.LENGTH_LONG).show();
            return;
        }
        if (prefs.isAlertEnabled()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.low_title)
                    .setMessage(getString(R.string.low_msg, info.level))
                    .setPositiveButton(R.string.low_yes, (d, w) -> {
                        selectMode(MODE_SAVER, true);
                        toastModeChanged(MODE_SAVER);
                    })
                    .setNegativeButton(R.string.low_no, null)
                    .show();
        }
    }

    // ----------------------------------------------------------------- mode

    private void selectMode(int mode, boolean animate) {
        currentMode = mode;
        prefs.setMode(mode);

        updatingUi = true;
        toggleMode.check(MODE_BUTTONS[mode]);
        sliderBrightness.setValue(MODE_BRIGHTNESS[mode]);
        updatingUi = false;
        applyBrightness(MODE_BRIGHTNESS[mode]);

        tvModeTitle.setText(MODE_TITLES[mode]);
        tvModeDesc.setText(MODE_DESCS[mode]);
        tvModeGain.setText(MODE_GAIN[mode] == 0
                ? getString(R.string.mode_gain_none)
                : getString(R.string.mode_gain, MODE_GAIN[mode]));

        if (animate) cardModeDesc.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pop));
        invalidateOptionsMenu();
    }

    private void toastModeChanged(int mode) {
        Toast.makeText(this, getString(R.string.mode_changed, getString(MODE_TITLES[mode])),
                Toast.LENGTH_SHORT).show();
    }

    /** Đổi độ sáng của cửa sổ ứng dụng (không cần quyền đặc biệt). */
    private void applyBrightness(int percent) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = percent >= 100
                ? WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                : Math.max(percent, 5) / 100f;
        getWindow().setAttributes(lp);
        tvBrightness.setText(getString(R.string.fmt_brightness, percent));
    }

    // --------------------------------------------------------------- intents

    /** Intent tường minh + extras: gửi dữ liệu pin sang BatteryDetailActivity. */
    private void openDetails() {
        Intent intent = new Intent(this, BatteryDetailActivity.class);
        lastInfo.writeTo(intent);
        startActivity(intent);
    }

    private void copyInfo() {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText("battery", BatteryUtils.summary(this, lastInfo)));
            Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT).show();
        }
    }

    // ----------------------------------------------------------- Options Menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(MODE_MENU_IDS[currentMode]);
        if (item != null) item.setChecked(true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        for (int i = 0; i < MODE_MENU_IDS.length; i++) {
            if (id == MODE_MENU_IDS[i]) {
                selectMode(i, true);
                toastModeChanged(i);
                return true;
            }
        }

        if (id == R.id.action_refresh) {
            updateUi(BatteryUtils.read(this));
            Snackbar.make(root, R.string.refreshed, Snackbar.LENGTH_SHORT).show();
        } else if (id == R.id.action_details) {
            openDetails();
        } else if (id == R.id.action_share) {
            IntentHelper.shareText(this, BatteryUtils.shareText(this, lastInfo));
        } else if (id == R.id.action_system_saver) {
            IntentHelper.openSystemSettings(this, Settings.ACTION_BATTERY_SAVER_SETTINGS);
        } else if (id == R.id.action_about) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.about_title)
                    .setMessage(R.string.about_msg)
                    .setPositiveButton(R.string.close, null)
                    .show();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    // ---------------------------------------------------------- Context Menu

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.cardHero) {
            menu.setHeaderTitle(R.string.ctx_title);
            getMenuInflater().inflate(R.menu.menu_hero_context, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.ctx_copy) {
            copyInfo();
        } else if (id == R.id.ctx_share) {
            IntentHelper.shareText(this, BatteryUtils.shareText(this, lastInfo));
        } else if (id == R.id.ctx_details) {
            openDetails();
        } else {
            return super.onContextItemSelected(item);
        }
        return true;
    }
}
