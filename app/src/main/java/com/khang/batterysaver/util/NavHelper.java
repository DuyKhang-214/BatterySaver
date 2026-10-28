package com.khang.batterysaver.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.ComponentActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.khang.batterysaver.MainActivity;
import com.khang.batterysaver.R;
import com.khang.batterysaver.SettingsActivity;
import com.khang.batterysaver.TipsActivity;

/** Xử lý thanh điều hướng dưới (Navigation bar) và giao diện tràn viền. */
public final class NavHelper {

    private NavHelper() {
    }

    /** Bật chế độ tràn viền, icon thanh trạng thái màu sáng trên nền đen. */
    public static void enableEdgeToEdge(ComponentActivity activity) {
        EdgeToEdge.enable(activity,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.dark(Color.TRANSPARENT));
    }

    /** Chừa khoảng trống cho thanh trạng thái. BottomNavigationView tự xử lý phần dưới. */
    public static void applyInsets(View root, boolean padBottom) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, padBottom ? bars.bottom : 0);
            return insets;
        });
    }

    /** Gắn sự kiện chuyển màn hình cho BottomNavigationView bằng Intent tường minh. */
    public static void setupBottomNav(Activity activity, BottomNavigationView nav, int selectedId) {
        nav.setSelectedItemId(selectedId);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedId) return true;

            Intent intent;
            if (id == R.id.nav_home) {
                intent = new Intent(activity, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            } else if (id == R.id.nav_tips) {
                intent = new Intent(activity, TipsActivity.class);
            } else {
                intent = new Intent(activity, SettingsActivity.class);
            }
            activity.startActivity(intent);
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            // Từ màn hình phụ sang màn hình phụ khác thì đóng màn cũ để Back luôn về Trang chủ
            if (selectedId != R.id.nav_home && id != R.id.nav_home) {
                activity.finish();
            }
            return true;
        });
    }
}
