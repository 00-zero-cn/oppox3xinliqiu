package com.example.watchrecorder;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private TextView status;
    private Button btn;
    private final Handler h = new Handler(Looper.getMainLooper());

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            refresh();
            h.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);

        status = new TextView(this);
        status.setTextSize(13);
        btn = new Button(this);
        btn.setOnClickListener(v -> {
            if (RecorderService.running) {
                stopService(new Intent(this, RecorderService.class));
            } else {
                startForegroundService(new Intent(this, RecorderService.class));
            }
            refresh();
        });
        root.addView(status);
        root.addView(btn);
        setContentView(root);

        List<String> need = new ArrayList<>();
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            need.add(Manifest.permission.RECORD_AUDIO);
        }
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            need.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        if (!need.isEmpty()) {
            requestPermissions(need.toArray(new String[0]), 1);
        }
    }

    private void refresh() {
        if (RecorderService.running) {
            btn.setText("停止录音");
            long total = RecorderService.totalRecordedMs
                    + (System.currentTimeMillis() - RecorderService.segmentStartMs);
            status.setText("状态：录音中
今日已录：" + fmt(total)
                    + "
当前段：" + RecorderService.currentFileName);
        } else {
            btn.setText("开始录音");
            status.setText("状态：停止
今日已录：" + fmt(RecorderService.totalRecordedMs));
        }
    }

    private String fmt(long ms) {
        long s = Math.max(0, ms) / 1000;
        return String.format(Locale.US, "%02d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        h.post(tick);
    }

    @Override
    protected void onPause() {
        super.onPause();
        h.removeCallbacks(tick);
    }
}
