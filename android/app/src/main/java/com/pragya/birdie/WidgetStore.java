package com.pragya.birdie;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

/** Shared storage for widget data, plus to-dos ticked on the widget that the app hasn't picked up yet. */
final class WidgetStore {
    private static final String PREFS = "birdie_widgets";
    private WidgetStore() {}

    static SharedPreferences prefs(Context c) { return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE); }

    static JSONObject data(Context c) {
        try { return new JSONObject(prefs(c).getString("data", "{}")); } catch (Exception e) { return new JSONObject(); }
    }

    static JSONArray changes(Context c) {
        try { return new JSONArray(prefs(c).getString("changes", "[]")); } catch (Exception e) { return new JSONArray(); }
    }

    /** Saves the app's copy, keeping any widget ticks the app hasn't consumed yet. */
    static synchronized void saveFromApp(Context c, String json) {
        try {
            JSONObject d = new JSONObject(json);
            JSONArray ch = changes(c), todos = d.optJSONArray("todos");
            if (todos != null) for (int i = 0; i < ch.length(); i++) {
                JSONObject x = ch.getJSONObject(i);
                for (int j = 0; j < todos.length(); j++) {
                    JSONObject t = todos.getJSONObject(j);
                    if (t.optLong("id") == x.optLong("id")) t.put("done", x.optBoolean("done"));
                }
            }
            prefs(c).edit().putString("data", d.toString()).apply();
        } catch (Exception ignored) {}
    }

    static synchronized void toggle(Context c, long id) {
        try {
            JSONObject d = data(c);
            JSONArray todos = d.optJSONArray("todos");
            if (todos == null) return;
            boolean done = false;
            for (int j = 0; j < todos.length(); j++) {
                JSONObject t = todos.getJSONObject(j);
                if (t.optLong("id") == id) { done = !t.optBoolean("done"); t.put("done", done); }
            }
            JSONArray ch = changes(c), out = new JSONArray();
            for (int i = 0; i < ch.length(); i++) if (ch.getJSONObject(i).optLong("id") != id) out.put(ch.getJSONObject(i));
            JSONObject x = new JSONObject();
            x.put("id", id); x.put("done", done);
            x.put("at", new SimpleDateFormat("HH:mm", Locale.US).format(new Date()));
            out.put(x);
            prefs(c).edit().putString("data", d.toString()).putString("changes", out.toString()).apply();
        } catch (Exception ignored) {}
    }

    static synchronized String takeChanges(Context c) {
        String s = prefs(c).getString("changes", "[]");
        prefs(c).edit().putString("changes", "[]").apply();
        return s;
    }

    static String today() { return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()); }

    static Calendar midnight() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    static int daysUntil(String iso) {
        try {
            Calendar t = midnight();
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso);
            Calendar x = Calendar.getInstance(); x.setTime(d);
            return (int) Math.round((x.getTimeInMillis() - t.getTimeInMillis()) / 86400000.0);
        } catch (Exception e) { return 0; }
    }

    /** Today's list: today's tasks plus unfinished ones from earlier days. Open first. */
    static List<JSONObject> todayTodos(Context c) {
        List<JSONObject> open = new ArrayList<>(), done = new ArrayList<>();
        JSONArray todos = data(c).optJSONArray("todos");
        String t = today();
        if (todos != null) for (int i = 0; i < todos.length(); i++) {
            JSONObject x = todos.optJSONObject(i);
            if (x == null) continue;
            String d = x.optString("d");
            boolean isDone = x.optBoolean("done");
            if (d.equals(t)) (isDone ? done : open).add(x);
            else if (d.compareTo(t) < 0 && !isDone) open.add(x);
        }
        open.addAll(done);
        return open;
    }

    static void refreshAll(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        int[] a = m.getAppWidgetIds(new ComponentName(c, TodayWidget.class));
        if (a.length > 0) { TodayWidget.updateAll(c, m, a); m.notifyAppWidgetViewDataChanged(a, R.id.w_list); }
        int[] b = m.getAppWidgetIds(new ComponentName(c, ComingWidget.class));
        if (b.length > 0) ComingWidget.updateAll(c, m, b);
    }

    static Intent openApp(Context c) {
        Intent i = new Intent(c, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return i;
    }
}
