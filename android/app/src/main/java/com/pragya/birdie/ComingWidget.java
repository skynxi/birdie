package com.pragya.birdie;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;
import java.util.Calendar;
import org.json.JSONArray;
import org.json.JSONObject;

/** Home-screen widget: the next birthday or date, and the next follow-up. */
public class ComingWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context c, AppWidgetManager m, int[] ids) { updateAll(c, m, ids); }

    private static final String[] MON = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

    static int until(int m, int d) {
        Calendar t = WidgetStore.midnight();
        Calendar x = (Calendar) t.clone();
        x.set(Calendar.MONTH, m); x.set(Calendar.DAY_OF_MONTH, d);
        if (x.before(t)) x.add(Calendar.YEAR, 1);
        return (int) Math.round((x.getTimeInMillis() - t.getTimeInMillis()) / 86400000.0);
    }

    static String when(int n) { return n == 0 ? "Today" : n == 1 ? "Tomorrow" : "In " + n + " days"; }

    static void updateAll(Context c, AppWidgetManager m, int[] ids) {
        JSONObject d = WidgetStore.data(c);
        JSONObject bestDate = null; int bestN = Integer.MAX_VALUE;
        JSONArray dates = d.optJSONArray("dates");
        if (dates != null) for (int i = 0; i < dates.length(); i++) {
            JSONObject x = dates.optJSONObject(i);
            if (x == null) continue;
            int n = until(x.optInt("m"), x.optInt("d"));
            if (n < bestN) { bestN = n; bestDate = x; }
        }
        JSONObject bestC = null; int bestF = Integer.MAX_VALUE;
        JSONArray cs = d.optJSONArray("contacts");
        if (cs != null) for (int i = 0; i < cs.length(); i++) {
            JSONObject x = cs.optJSONObject(i);
            if (x == null) continue;
            int n = WidgetStore.daysUntil(x.optString("next"));
            if (n < bestF) { bestF = n; bestC = x; }
        }
        for (int id : ids) {
            RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget_coming);
            if (bestDate != null) {
                boolean b = "b".equals(bestDate.optString("k"));
                v.setTextViewText(R.id.c_dlabel, b ? "Next birthday" : "Next date");
                v.setTextViewText(R.id.c_dname, bestDate.optString("n"));
                v.setTextViewText(R.id.c_dwhen, when(bestN) + " · " + bestDate.optInt("d") + " " + MON[Math.max(0, Math.min(11, bestDate.optInt("m")))]);
            } else {
                v.setTextViewText(R.id.c_dlabel, "Next date");
                v.setTextViewText(R.id.c_dname, "No dates yet");
                v.setTextViewText(R.id.c_dwhen, "Add birthdays in Birdie");
            }
            boolean hasF = bestC != null;
            v.setViewVisibility(R.id.c_sep, hasF ? View.VISIBLE : View.GONE);
            v.setViewVisibility(R.id.c_flabel, hasF ? View.VISIBLE : View.GONE);
            v.setViewVisibility(R.id.c_fname, hasF ? View.VISIBLE : View.GONE);
            v.setViewVisibility(R.id.c_fwhen, hasF ? View.VISIBLE : View.GONE);
            if (hasF) {
                v.setTextViewText(R.id.c_fname, bestC.optString("n"));
                v.setTextViewText(R.id.c_fwhen, bestF < 0 ? "Overdue " + (-bestF) + "d" : bestF == 0 ? "Due today" : "In " + bestF + " days");
                v.setTextColor(R.id.c_fwhen, c.getColor(bestF <= 0 ? R.color.w_bad : R.color.w_mut));
            }
            v.setOnClickPendingIntent(R.id.c_root, PendingIntent.getActivity(c, 3, WidgetStore.openApp(c), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
            m.updateAppWidget(id, v);
        }
    }
}
