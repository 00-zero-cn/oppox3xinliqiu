package com.example.watchrecorder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecorderService extends Service {

    public static volatile boolean running = false;
    public static volatile long totalRecordedMs = 0;
    public static volatile long segmentStartMs = 0;
    public static volatile String currentFileName = "-";

    private static final String CH = "recorder";
    private static final long SEGMENT_MS = 15 * 60 * 1000L;

    private MediaRecorder recorder;
    private Handler handler;
    private PowerManager.WakeLock wakeLock;
    private boolean chargingPaused = false;

    private final Runnable rotate = new Runnable() {
        @Override
        public void run() {
            if (!chargingPaused) {
                startSegment();
            }
            handler.postDelayed(this, SEGMENT_MS);
        }
    };

    private final BroadcastReceiver power = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
                chargingPaused = true;
                stopSegmentAndSettle();
                updateNotif("充电中，已自动暂停");
            } else if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
                chargingPaused = false;
                startSegment();
                updateNotif("正在录音 · 每15分钟分段保存");
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, notif("正在录音 · 每15分钟分段保存"));
        wakeLock = ((PowerManager) getSystemService(POWER_SERVICE))
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "watchrec:lock");
        wakeLock.acquire(10 * 60 * 60 * 1000L);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(power, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(power, filter);
        }

        handler = new Handler(Looper.getMainLooper());
        running = true;
        if (!chargingPaused) {
            startSegment();
        }
        handler.post(rotate);
        return START_STICKY;
    }

    private void startSegment() {
        stopSegmentQuiet();
        File dir = new File(getExternalFilesDir(null), "recordings");
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();
        String name = "rec_" + new SimpleDateFormat("MMdd_HHmmss", Locale.US).format(new Date()) + ".m4a";
        try {
            MediaRecorder r;
            if (Build.VERSION.SDK_INT >= 31) {
                r = new MediaRecorder(this);
            } else {
                r = new MediaRecorder();
            }
            r.setAudioSource(MediaRecorder.AudioSource.MIC);
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            r.setAudioChannels(1);
            r.setAudioSamplingRate(16000);
            r.setAudioEncodingBitRate(32000);
            r.setOutputFile(new File(dir, name).getAbsolutePath());
            r.prepare();
            r.start();
            recorder = r;
            segmentStartMs = System.currentTimeMillis();
            currentFileName = name;
        } catch (Exception e) {
            currentFileName = "启动失败: " + e.getMessage();
        }
    }

    private void stopSegmentAndSettle() {
        if (segmentStartMs > 0) {
            totalRecordedMs += System.currentTimeMillis() - segmentStartMs;
            segmentStartMs = 0;
        }
        stopSegmentQuiet();
    }

    private void stopSegmentQuiet() {
        if (recorder != null) {
            try { recorder.stop(); } catch (Exception ignored) { }
            try { recorder.release(); } catch (Exception ignored) { }
            recorder = null;
        }
    }

    private void updateNotif(String text) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(1, notif(text));
    }

    private Notification notif(String text) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.createNotificationChannel(new NotificationChannel(CH, "录音", NotificationManager.IMPORTANCE_LOW));
        Notification.Builder builder = (Build.VERSION.SDK_INT >= 26)
                ? new Notification.Builder(this, CH)
                : new Notification.Builder(this);
        return builder.setContentTitle("24h 录音")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .build();
    }

    @Override
    public void onDestroy() {
        if (handler != null) handler.removeCallbacks(rotate);
        try { unregisterReceiver(power); } catch (Exception ignored) { }
        stopSegmentAndSettle();
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        running = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
