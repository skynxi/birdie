package com.pragya.birdie;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.core.app.NotificationCompat;

/** Ongoing "sleeping" notification with an "I'm awake" button. */
public class SleepNotify {
    static final String CH = "birdie_sleep";
    static final String CH_MORNING = "birdie_morning";
    static final int ID = 7001, ID_MORNING = 7002;
    static final String ACTION_AWAKE = "com.pragya.birdie.AWAKE";
    static final int ACC = 0xFFFF8A2B;

    static SharedPreferences prefs(Context c) { return c.getSharedPreferences("birdie_sleep", Context.MODE_PRIVATE); }

    static void channels(Context c) {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        NotificationChannel a = new NotificationChannel(CH, "Sleep timer", NotificationManager.IMPORTANCE_LOW);
        a.setDescription("Shows while you sleep, with an I'm awake button");
        a.setShowBadge(false); a.setSound(null, null); a.enableVibration(false);
        nm.createNotificationChannel(a);
        NotificationChannel b = new NotificationChannel(CH_MORNING, "Good morning", NotificationManager.IMPORTANCE_DEFAULT);
        b.setDescription("Confirms how long you slept");
        nm.createNotificationChannel(b);
    }

    static PendingIntent openApp(Context c) {
        Intent i = new Intent(c, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(c, 1, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static void start(Context c, long at) {
        prefs(c).edit().putLong("start", at).remove("end").apply();
        show(c, at);
    }

    static void show(Context c, long at) {
        channels(c);
        Intent a = new Intent(c, SleepReceiver.class).setAction(ACTION_AWAKE);
        PendingIntent awake = PendingIntent.getBroadcast(c, 2, a, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification n = new NotificationCompat.Builder(c, CH)
            .setSmallIcon(R.drawable.ic_stat_birdie)
            .setColor(ACC)
            .setContentTitle("Sleeping…")
            .setContentText("Birdie is keeping time. Tap I’m awake when you get up.")
            .setWhen(at).setShowWhen(true).setUsesChronometer(true)
            .setOngoing(true).setOnlyAlertOnce(true).setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openApp(c))
            .addAction(0, "I’m awake", awake)
            .build();
        try { c.getSystemService(NotificationManager.class).notify(ID, n); } catch (SecurityException e) { }
    }

    static void stop(Context c) {
        prefs(c).edit().remove("start").remove("end").apply();
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        nm.cancel(ID);
    }

    /** Called from the I'm awake button. */
    static void awake(Context c) {
        SharedPreferences p = prefs(c);
        long st = p.getLong("start", 0), end = System.currentTimeMillis();
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        nm.cancel(ID);
        if (st == 0) return;
        p.edit().putLong("end", end).apply();
        long min = (end - st) / 60000;
        String dur = (min / 60) + "h " + (min % 60) + "m";
        channels(c);
        Notification n = new NotificationCompat.Builder(c, CH_MORNING)
            .setSmallIcon(R.drawable.ic_stat_birdie).setColor(ACC)
            .setContentTitle(min < 180 ? "Nice nap!" : "Good morning ☀")
            .setContentText("You slept " + dur + ". Open Birdie to see your streak.")
            .setAutoCancel(true).setContentIntent(openApp(c))
            .build();
        try { nm.notify(ID_MORNING, n); } catch (SecurityException e) { }
    }
}
