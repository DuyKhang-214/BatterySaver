package com.khang.batterysaver;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.khang.batterysaver.util.BatteryUtils;
import com.khang.batterysaver.model.BatteryInfo;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Battery insights: locally collected history, foreground-usage ranking, and estimated remaining time. */
public class BatteryInsightsActivity extends AppCompatActivity {
    private static final String PREFS = "battery_history";
    private static final String KEY = "levels";
    private LinearLayout content;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        com.khang.batterysaver.util.NavHelper.enableEdgeToEdge(this);
        setContentView(R.layout.activity_battery_insights);
        com.khang.batterysaver.util.NavHelper.applyInsets(findViewById(R.id.main), true);
        ((MaterialToolbar)findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> finish());
        content = findViewById(R.id.content);
        recordBatteryLevel();
        showInsights();
    }

    private void recordBatteryLevel() {
        int level = BatteryUtils.read(this).level;
        String old = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY, "");
        String now = System.currentTimeMillis() + "," + level;
        String[] lines = old.isEmpty() ? new String[0] : old.split(";");
        long lastTime = 0;
        if (lines.length > 0) try { lastTime = Long.parseLong(lines[lines.length-1].split(",")[0]); } catch(Exception ignored) {}
        // Avoid duplicate samples on repeated quick opens; sample at most once every 10 minutes.
        if (System.currentTimeMillis() - lastTime >= 10 * 60 * 1000L) {
            String value = old.isEmpty() ? now : old + ";" + now;
            String[] all = value.split(";");
            StringBuilder trimmed = new StringBuilder();
            for (int i=Math.max(0, all.length-48); i<all.length; i++) { if (trimmed.length()>0) trimmed.append(';'); trimmed.append(all[i]); }
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY, trimmed.toString()).apply();
        }
    }

    private List<Integer> levels() {
        List<Integer> out = new ArrayList<>();
        String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY, "");
        if (!raw.isEmpty()) for (String item : raw.split(";")) try { out.add(Integer.parseInt(item.split(",")[1])); } catch(Exception ignored) {}
        return out;
    }

    private void showInsights() {
        int level = BatteryUtils.read(this).level;
        addTitle("Ước tính thời gian pin còn lại");
        TextView estimate = addText("Đang tính...");
        List<Integer> history = levels();
        long minutes = 0;
        String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY, "");
        String[] samples = raw.isEmpty() ? new String[0] : raw.split(";");
        if (samples.length >= 2) try {
            String[] first = samples[0].split(","), last = samples[samples.length-1].split(",");
            long elapsed = Long.parseLong(last[0]) - Long.parseLong(first[0]);
            int drop = Integer.parseInt(first[1]) - Integer.parseInt(last[1]);
            if (elapsed > 0 && drop > 0) minutes = (long) level * elapsed / drop / 60000L;
        } catch(Exception ignored) {}
        if (minutes <= 0) minutes = level * 2L; // coarse fallback until enough real history exists
        estimate.setText(minutes >= 60 ? String.format(Locale.getDefault(), "Khoảng %d giờ %d phút còn lại\nĐây là ước tính; mức tiêu thụ thực tế thay đổi theo cách sử dụng.", minutes/60, minutes%60) : "Khoảng " + minutes + " phút còn lại\nĐây là ước tính ban đầu; mở ứng dụng định kỳ để tích lũy lịch sử chính xác hơn.");

        addTitle("Lịch sử pin");
        addText("Biểu đồ ghi lại mức pin mỗi 10 phút khi bạn mở màn hình này. Dữ liệu được lưu trên thiết bị.");
        BatteryChart chart = new BatteryChart(this, history);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, dp(190)); cp.setMargins(0, dp(8), 0, dp(12)); content.addView(chart, cp);
        addText(history.size() + " mẫu đã ghi nhận (tối đa 48 mẫu). Lịch sử sẽ đầy dần theo thời gian.");

        addTitle("Ứng dụng sử dụng nhiều nhất");
        if (!hasUsageAccess()) {
            addText("Để xem ứng dụng dùng màn hình nhiều nhất, cần cấp quyền Truy cập dữ liệu sử dụng. Thời gian sử dụng là chỉ báo tham khảo, không phải phép đo trực tiếp lượng pin tiêu hao.");
            com.google.android.material.button.MaterialButton grant = new com.google.android.material.button.MaterialButton(this);
            grant.setText("Cấp quyền truy cập dữ liệu sử dụng");
            grant.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));
            content.addView(grant);
        } else {
            List<UsageStats> stats = new ArrayList<>();
            UsageStatsManager manager = (UsageStatsManager)getSystemService(USAGE_STATS_SERVICE);
            long end = System.currentTimeMillis();
            stats.addAll(manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, end - 24*60*60*1000L, end));
            Collections.sort(stats, (a,b) -> Long.compare(b.getTotalTimeInForeground(), a.getTotalTimeInForeground()));
            int shown = 0;
            for (UsageStats s : stats) {
                if (s.getTotalTimeInForeground() < 60_000 || s.getPackageName().equals(getPackageName())) continue;
                String name = s.getPackageName();
                try { name = getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(s.getPackageName(), 0)).toString(); } catch(Exception ignored) {}
                addText((shown+1) + ". " + name + " — " + formatDuration(s.getTotalTimeInForeground()) + " dùng trong 24 giờ qua");
                if (++shown >= 8) break;
            }
            if (shown == 0) addText("Chưa có dữ liệu sử dụng đủ lớn trong 24 giờ qua.");
            addText("Lưu ý: Android không cung cấp cho ứng dụng thông thường số pin tiêu hao chính xác theo từng ứng dụng trên mọi thiết bị. Danh sách này xếp theo thời gian dùng màn hình, không khẳng định ứng dụng nào tiêu hao pin nhiều nhất tuyệt đối.");
        }
    }

    private boolean hasUsageAccess() {
        try { AppOpsManager appOps = (AppOpsManager)getSystemService(Context.APP_OPS_SERVICE); int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName()); return mode == AppOpsManager.MODE_ALLOWED; }
        catch(Exception e) { return false; }
    }
    private String formatDuration(long ms) { long m=ms/60000; return m>=60 ? (m/60)+" giờ "+(m%60)+" phút" : m+" phút"; }
    private int dp(float n) { return (int)(n*getResources().getDisplayMetrics().density+0.5f); }
    private void addTitle(String text) { TextView t=new TextView(this); t.setText(text); t.setTextColor(getColor(R.color.gold)); t.setTextSize(19); t.setTypeface(null, 1); t.setPadding(0,dp(16),0,dp(6)); content.addView(t); }
    private TextView addText(String text) { TextView t=new TextView(this); t.setText(text); t.setTextColor(getColor(R.color.text_primary)); t.setTextSize(14); t.setLineSpacing(dp(3),1f); t.setPadding(dp(14),dp(12),dp(14),dp(12)); t.setBackgroundResource(R.drawable.bg_card); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=dp(8); content.addView(t,p); return t; }

    private class BatteryChart extends View {
        private final List<Integer> data; private final Paint paint = new Paint(3);
        BatteryChart(Context c, List<Integer> d) { super(c); data=d; setBackgroundResource(R.drawable.bg_card); }
        @Override protected void onDraw(Canvas canvas) { super.onDraw(canvas); float w=getWidth(), h=getHeight(), pad=dp(22); paint.setColor(Color.GRAY); paint.setStrokeWidth(dp(1)); paint.setStyle(Paint.Style.STROKE); for(int i=0;i<4;i++){float y=pad+(h-2*pad)*i/3f; canvas.drawLine(pad,y,w-pad,y,paint);} if(data.isEmpty()){paint.setStyle(Paint.Style.FILL);paint.setTextSize(dp(14));canvas.drawText("Chưa có lịch sử — dữ liệu sẽ xuất hiện sau",pad,h/2,paint);return;} Path path=new Path(); for(int i=0;i<data.size();i++){float x=data.size()==1?w/2:pad+(w-2*pad)*i/(data.size()-1f);float y=h-pad-(h-2*pad)*Math.max(0,Math.min(100,data.get(i)))/100f;if(i==0)path.moveTo(x,y);else path.lineTo(x,y);} paint.setColor(getColor(R.color.gold));paint.setStrokeWidth(dp(3));paint.setStyle(Paint.Style.STROKE);canvas.drawPath(path,paint);paint.setStyle(Paint.Style.FILL);paint.setTextSize(dp(11));paint.setColor(getColor(R.color.text_secondary));canvas.drawText("100%",dp(5),pad+dp(4),paint);canvas.drawText("0%",dp(5),h-pad,paint); }
    }
}
