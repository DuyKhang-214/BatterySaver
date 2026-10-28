package com.khang.batterysaver;

import android.os.Bundle;
import android.provider.Settings;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.khang.batterysaver.adapter.TipAdapter;
import com.khang.batterysaver.model.Tip;
import com.khang.batterysaver.util.IntentHelper;
import com.khang.batterysaver.util.NavHelper;
import com.khang.batterysaver.util.PrefsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TipsActivity extends AppCompatActivity {

    private static final String[] TIP_ACTIONS = {
            Settings.ACTION_DISPLAY_SETTINGS,
            Settings.ACTION_DISPLAY_SETTINGS,
            Settings.ACTION_WIFI_SETTINGS,
            Settings.ACTION_BLUETOOTH_SETTINGS,
            Settings.ACTION_LOCATION_SOURCE_SETTINGS,
            Settings.ACTION_BATTERY_SAVER_SETTINGS,
            Settings.ACTION_SYNC_SETTINGS,
            Settings.ACTION_APPLICATION_SETTINGS
    };
    private static final int[] TIP_ICONS = {
            R.drawable.ic_brightness,
            R.drawable.ic_timer,
            R.drawable.ic_wifi,
            R.drawable.ic_bluetooth,
            R.drawable.ic_location,
            R.drawable.ic_eco,
            R.drawable.ic_sync,
            R.drawable.ic_apps
    };

    private PrefsHelper prefs;
    private final List<Tip> tips = new ArrayList<>();
    private TipAdapter adapter;
    private ListView listTips;
    private TextView tvProgress;
    private LinearProgressIndicator progressTips;
    private View root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavHelper.enableEdgeToEdge(this);
        setContentView(R.layout.activity_tips);
        root = findViewById(R.id.main);
        NavHelper.applyInsets(root, false);

        prefs = new PrefsHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvProgress = findViewById(R.id.tvProgress);
        progressTips = findViewById(R.id.progressTips);
        listTips = findViewById(R.id.listTips);

        buildTips();
        adapter = new TipAdapter(this, tips);
        listTips.setAdapter(adapter);

        // Sự kiện bấm: mở màn hình cài đặt tương ứng bằng Intent ngầm định
        listTips.setOnItemClickListener((parent, view, position, id) -> openTip(tips.get(position)));
        registerForContextMenu(listTips); // nhấn giữ từng mẹo để mở Context Menu

        refreshProgress();
        NavHelper.setupBottomNav(this, findViewById(R.id.bottomNav), R.id.nav_tips);
    }

    private void buildTips() {
        String[] titles = getResources().getStringArray(R.array.tip_titles);
        String[] descs = getResources().getStringArray(R.array.tip_descs);
        Set<String> done = prefs.getDoneTips();
        tips.clear();
        for (int i = 0; i < titles.length; i++) {
            Tip tip = new Tip("tip_" + i, titles[i], descs[i], TIP_ICONS[i], TIP_ACTIONS[i]);
            tip.done = done.contains(tip.id);
            tips.add(tip);
        }
    }

    private void refreshProgress() {
        int done = 0;
        for (Tip t : tips) if (t.done) done++;
        tvProgress.setText(getString(R.string.fmt_tips_progress, done, tips.size()));
        int percent = tips.isEmpty() ? 0 : done * 100 / tips.size();
        progressTips.setProgressCompat(percent, true);
    }

    private void openTip(Tip tip) {
        IntentHelper.openSystemSettings(this, tip.action);
    }

    private void toggleDone(Tip tip) {
        tip.done = !tip.done;
        prefs.setTipDone(tip.id, tip.done);
        adapter.notifyDataSetChanged();
        refreshProgress();
    }

    // ----------------------------------------------------------- Options Menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tips, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_reset_tips) {
            prefs.clearDoneTips();
            for (Tip t : tips) t.done = false;
            adapter.notifyDataSetChanged();
            refreshProgress();
            Toast.makeText(this, R.string.tips_reset_done, Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_open_saver) {
            IntentHelper.openSystemSettings(this, Settings.ACTION_BATTERY_SAVER_SETTINGS);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ---------------------------------------------------------- Context Menu

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.listTips) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Tip tip = tips.get(info.position);
            menu.setHeaderTitle(tip.title);
            getMenuInflater().inflate(R.menu.menu_tip_context, menu);
            menu.findItem(R.id.tip_toggle_done).setTitle(
                    tip.done ? R.string.tip_mark_undone : R.string.tip_mark_done);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null) return super.onContextItemSelected(item);

        Tip tip = tips.get(info.position);
        int id = item.getItemId();
        if (id == R.id.tip_toggle_done) {
            toggleDone(tip);
        } else if (id == R.id.tip_open) {
            openTip(tip);
        } else if (id == R.id.tip_share) {
            IntentHelper.shareText(this, getString(R.string.share_tip, tip.title, tip.description));
        } else {
            return super.onContextItemSelected(item);
        }
        return true;
    }
}
