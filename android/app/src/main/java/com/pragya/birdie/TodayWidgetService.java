package com.pragya.birdie;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class TodayWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) { return new Factory(getApplicationContext()); }

    static class Factory implements RemoteViewsFactory {
        private final Context c;
        private List<JSONObject> items = new ArrayList<>();
        Factory(Context c) { this.c = c; }
        public void onCreate() {}
        public void onDataSetChanged() { items = WidgetStore.todayTodos(c); }
        public void onDestroy() { items.clear(); }
        public int getCount() { return items.size(); }
        public RemoteViews getLoadingView() { return null; }
        public int getViewTypeCount() { return 1; }
        public long getItemId(int i) { return i < items.size() ? items.get(i).optLong("id") : i; }
        public boolean hasStableIds() { return true; }

        public RemoteViews getViewAt(int i) {
            RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget_todo_item);
            if (i >= items.size()) return v;
            JSONObject x = items.get(i);
            boolean done = x.optBoolean("done");
            String t = x.optString("t");
            if (done) {
                SpannableString s = new SpannableString(t);
                s.setSpan(new StrikethroughSpan(), 0, t.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                v.setTextViewText(R.id.i_text, s);
                v.setTextColor(R.id.i_text, c.getColor(R.color.w_mut));
            } else {
                v.setTextViewText(R.id.i_text, t);
                v.setTextColor(R.id.i_text, c.getColor(R.color.w_ink));
            }
            v.setImageViewResource(R.id.i_box, done ? R.drawable.w_box_on : R.drawable.w_box);
            v.setContentDescription(R.id.i_root, (done ? "Done: " : "Not done: ") + t);
            Intent fill = new Intent();
            fill.putExtra("id", x.optLong("id"));
            v.setOnClickFillInIntent(R.id.i_root, fill);
            return v;
        }
    }
}
