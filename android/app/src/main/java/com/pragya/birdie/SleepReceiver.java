package com.pragya.birdie;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Handles the I'm awake button, and puts the sleep timer back after a reboot. */
public class SleepReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        String a = i.getAction();
        if (SleepNotify.ACTION_AWAKE.equals(a)) { SleepNotify.awake(c); return; }
        if (Intent.ACTION_BOOT_COMPLETED.equals(a) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(a) || "android.intent.action.LOCKED_BOOT_COMPLETED".equals(a)) {
            android.content.SharedPreferences p = SleepNotify.prefs(c);
            long st = p.getLong("start", 0);
            if (st > 0 && !p.contains("end")) SleepNotify.show(c, st);
        }
    }
}
