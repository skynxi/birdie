package com.pragya.birdie;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import java.util.List;
import org.json.JSONObject;

/** Home-screen widget: today's to-dos, tap one to cross it off. */
public class TodayWidget extends AppWidgetProvider {
    static final String TOGGLE = "com.pragya.birdie.TOGGLE_TODO";

    @Override
    public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        updateAll(c, m, ids);
        m.notifyAppWidgetViewDataChanged(ids, R.id.w_list);
    }

    @Override
    public void onReceive(Context c, Intent intent) {
        super.onReceive(c, intent);
        if (TOGGLE.equals(intent.getAction())) {
            long id = intent.getLongExtra("id", -1);
            if (id != -1) { WidgetStore.toggle(c, id); WidgetStore.refreshAll(c); }
        }
    }

    static void updateAll(Context c, AppWidgetManager m, int[] ids) {
        List<JSONObject> list = WidgetStore.todayTodos(c);
        int done = 0;
        for (JSONObject x : list) if (x.optBoolean("done")) done++;
        for (int id : ids) {
            RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget_today);
            v.setTextViewText(R.id.w_count, list.isEmpty() ? "" : done + " of " + list.size() + " done");
            Intent svc = new Intent(c, TodayWidgetService.class);
            svc.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
            svc.setData(Uri.parse(svc.toUri(Intent.URI_INTENT_SCHEME)));
            v.setRemoteAdapter(R.id.w_list, svc);
            v.setEmptyView(R.id.w_list, R.id.w_empty);
            Intent t = new Intent(c, TodayWidget.class).setAction(TOGGLE);
            v.setPendingIntentTemplate(R.id.w_list, PendingIntent.getBroadcast(c, 1, t, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE));
            PendingIntent open = PendingIntent.getActivity(c, 2, WidgetStore.openApp(c), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            v.setOnClickPendingIntent(R.id.w_head, open);
            v.setOnClickPendingIntent(R.id.w_empty, open);
            m.updateAppWidget(id, v);
        }
    }
}
