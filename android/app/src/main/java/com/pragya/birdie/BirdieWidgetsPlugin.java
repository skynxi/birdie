package com.pragya.birdie;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/** Bridge between the Birdie web app and its home-screen widgets. */
@CapacitorPlugin(name = "BirdieWidgets")
public class BirdieWidgetsPlugin extends Plugin {
    @PluginMethod
    public void update(PluginCall call) {
        String data = call.getString("data", "{}");
        WidgetStore.saveFromApp(getContext(), data);
        WidgetStore.refreshAll(getContext());
        call.resolve();
    }

    @PluginMethod
    public void consume(PluginCall call) {
        JSObject r = new JSObject();
        r.put("changes", WidgetStore.takeChanges(getContext()));
        call.resolve(r);
    }

    @PluginMethod
    public void sleepStart(PluginCall call) {
        Double at = call.getDouble("at");
        SleepNotify.start(getContext(), at == null ? System.currentTimeMillis() : at.longValue());
        call.resolve();
    }

    @PluginMethod
    public void sleepStop(PluginCall call) {
        SleepNotify.stop(getContext());
        call.resolve();
    }

    @PluginMethod
    public void sleepConsume(PluginCall call) {
        android.content.SharedPreferences p = SleepNotify.prefs(getContext());
        JSObject r = new JSObject();
        if (p.contains("end")) {
            r.put("start", p.getLong("start", 0));
            r.put("end", p.getLong("end", 0));
            p.edit().remove("start").remove("end").apply();
        }
        call.resolve(r);
    }

    /** Keeps a copy of all Birdie data in the app's private files, so an update or a cleared WebView never loses it. */
    @PluginMethod
    public void saveData(PluginCall call) {
        String data = call.getString("data", "");
        if (data == null || data.length() < 20) { call.resolve(); return; }
        try {
            java.io.File dir = getContext().getFilesDir();
            java.io.File main = new java.io.File(dir, "birdie-data.json"), tmp = new java.io.File(dir, "birdie-data.tmp"), prev = new java.io.File(dir, "birdie-data-prev.json");
            java.io.FileOutputStream o = new java.io.FileOutputStream(tmp);
            o.write(data.getBytes("UTF-8")); o.getFD().sync(); o.close();
            if (main.exists() && (!prev.exists() || System.currentTimeMillis() - prev.lastModified() > 86400000L)) {
                prev.delete(); main.renameTo(prev);
            }
            if (!tmp.renameTo(main)) { main.delete(); tmp.renameTo(main); }
            call.resolve();
        } catch (Exception e) { call.reject(e.getMessage()); }
    }

    @PluginMethod
    public void loadData(PluginCall call) {
        JSObject r = new JSObject();
        java.io.File dir = getContext().getFilesDir();
        for (String n : new String[]{"birdie-data.json", "birdie-data-prev.json"}) {
            java.io.File f = new java.io.File(dir, n);
            if (!f.exists() || f.length() < 20) continue;
            try {
                byte[] b = new byte[(int) f.length()];
                java.io.FileInputStream in = new java.io.FileInputStream(f);
                int off = 0, k; while (off < b.length && (k = in.read(b, off, b.length - off)) > 0) off += k; in.close();
                String s = new String(b, 0, off, "UTF-8");
                new org.json.JSONObject(s);
                r.put("data", s); break;
            } catch (Exception e) { }
        }
        call.resolve(r);
    }

    /** Downloads a new Birdie APK from GitHub and opens Android's installer. Data is kept because the signing key is the same. */
    @PluginMethod
    public void installUpdate(final PluginCall call) {
        final String url = call.getString("url");
        final android.content.Context c = getContext();
        if (url == null || !url.startsWith("https://")) { call.reject("Bad download link"); return; }
        if (android.os.Build.VERSION.SDK_INT >= 26 && !c.getPackageManager().canRequestPackageInstalls()) {
            android.content.Intent i = new android.content.Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, android.net.Uri.parse("package:" + c.getPackageName()));
            i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(i);
            JSObject r = new JSObject(); r.put("needsPermission", true); call.resolve(r); return;
        }
        new Thread(() -> {
            java.net.HttpURLConnection con = null;
            try {
                java.io.File dir = new java.io.File(c.getCacheDir(), "updates");
                dir.mkdirs();
                java.io.File[] old = dir.listFiles(); if (old != null) for (java.io.File o : old) o.delete();
                java.io.File f = new java.io.File(dir, "birdie-update.apk");
                String u = url;
                for (int hop = 0; hop < 6; hop++) {
                    con = (java.net.HttpURLConnection) new java.net.URL(u).openConnection();
                    con.setInstanceFollowRedirects(false);
                    con.setRequestProperty("User-Agent", "Birdie-updater");
                    con.setRequestProperty("Accept", "application/octet-stream");
                    con.setConnectTimeout(20000); con.setReadTimeout(30000);
                    int code = con.getResponseCode();
                    if (code >= 300 && code < 400) { String loc = con.getHeaderField("Location"); con.disconnect(); if (loc == null) throw new Exception("Bad redirect"); u = new java.net.URL(new java.net.URL(u), loc).toString(); continue; }
                    if (code != 200) throw new Exception("Download failed (" + code + ")");
                    break;
                }
                long total = con.getContentLengthLong(), got = 0; int lastPct = -1;
                java.io.InputStream in = con.getInputStream();
                java.io.FileOutputStream out = new java.io.FileOutputStream(f);
                byte[] buf = new byte[65536]; int n;
                while ((n = in.read(buf)) > 0) {
                    out.write(buf, 0, n); got += n;
                    if (total > 0) { int pct = (int) (got * 100 / total); if (pct != lastPct) { lastPct = pct; JSObject e = new JSObject(); e.put("pct", pct); notifyListeners("updateProgress", e); } }
                }
                out.close(); in.close();
                android.content.pm.PackageManager pm = c.getPackageManager();
                android.content.pm.PackageInfo info = pm.getPackageArchiveInfo(f.getAbsolutePath(), 0);
                if (info == null || !c.getPackageName().equals(info.packageName)) throw new Exception("That file isn’t a Birdie update");
                long cur = android.os.Build.VERSION.SDK_INT >= 28 ? pm.getPackageInfo(c.getPackageName(), 0).getLongVersionCode() : pm.getPackageInfo(c.getPackageName(), 0).versionCode;
                long nv = android.os.Build.VERSION.SDK_INT >= 28 ? info.getLongVersionCode() : info.versionCode;
                if (nv <= cur) throw new Exception("You already have this version or newer");
                android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(c, c.getPackageName() + ".fileprovider", f);
                android.content.Intent i = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                i.setDataAndType(uri, "application/vnd.android.package-archive");
                i.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                c.startActivity(i);
                JSObject r = new JSObject(); r.put("opened", true); call.resolve(r);
            } catch (Exception e) {
                call.reject(e.getMessage() == null ? "Download failed" : e.getMessage());
            } finally { if (con != null) con.disconnect(); }
        }).start();
    }

    /* ---------- automatic backups in Documents/Birdie (falls back to the app's own folder) ---------- */
    private java.io.File[] backupDirs() {
        java.io.File pub = new java.io.File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS), "Birdie");
        java.io.File own = new java.io.File(getContext().getExternalFilesDir(null), "Backups");
        return new java.io.File[]{pub, own};
    }

    @PluginMethod
    public void backupWrite(PluginCall call) {
        String name = call.getString("name", ""), data = call.getString("data", "");
        int keep = call.getInt("keep", 14);
        if (!name.matches("Birdie-backup-[0-9-]+\\.json") || data.length() < 20) { call.reject("Bad backup"); return; }
        Exception last = null;
        String[] labels = {"Documents/Birdie", "Birdie’s app folder"};
        java.io.File[] dirs = backupDirs();
        for (int d = 0; d < dirs.length; d++) {
            try {
                java.io.File dir = dirs[d];
                if (!dir.exists() && !dir.mkdirs()) throw new Exception("Can’t create " + dir);
                java.io.File tmp = new java.io.File(dir, name + ".tmp"), f = new java.io.File(dir, name);
                java.io.FileOutputStream o = new java.io.FileOutputStream(tmp);
                o.write(data.getBytes("UTF-8")); o.getFD().sync(); o.close();
                if (f.exists()) f.delete();
                if (!tmp.renameTo(f)) throw new Exception("Rename failed");
                java.io.File[] all = dir.listFiles((x, n) -> n.startsWith("Birdie-backup-") && n.endsWith(".json"));
                if (all != null && all.length > keep) {
                    java.util.Arrays.sort(all, (a, b) -> b.getName().compareTo(a.getName()));
                    for (int i = keep; i < all.length; i++) all[i].delete();
                }
                JSObject r = new JSObject(); r.put("where", labels[d]); r.put("name", name); call.resolve(r); return;
            } catch (Exception e) { last = e; }
        }
        call.reject(last == null ? "Backup failed" : last.getMessage());
    }

    @PluginMethod
    public void backupList(PluginCall call) {
        com.getcapacitor.JSArray arr = new com.getcapacitor.JSArray();
        java.util.HashSet<String> seen = new java.util.HashSet<>();
        String[] labels = {"Documents/Birdie", "Birdie’s app folder"};
        java.io.File[] dirs = backupDirs();
        for (int d = 0; d < dirs.length; d++) {
            java.io.File[] all = dirs[d].listFiles((x, n) -> n.startsWith("Birdie-backup-") && n.endsWith(".json"));
            if (all == null) continue;
            for (java.io.File f : all) {
                if (!f.canRead() || seen.contains(f.getName())) continue;
                seen.add(f.getName());
                JSObject o = new JSObject(); o.put("name", f.getName()); o.put("size", f.length()); o.put("mtime", f.lastModified()); o.put("where", labels[d]); o.put("dir", d);
                arr.put(o);
            }
        }
        JSObject r = new JSObject(); r.put("files", arr); call.resolve(r);
    }

    @PluginMethod
    public void backupRead(PluginCall call) {
        String name = call.getString("name", "");
        int d = call.getInt("dir", 0);
        if (!name.matches("Birdie-backup-[0-9-]+\\.json")) { call.reject("Bad name"); return; }
        try {
            java.io.File f = new java.io.File(backupDirs()[d == 1 ? 1 : 0], name);
            byte[] b = new byte[(int) f.length()];
            java.io.FileInputStream in = new java.io.FileInputStream(f);
            int off = 0, k; while (off < b.length && (k = in.read(b, off, b.length - off)) > 0) off += k; in.close();
            JSObject r = new JSObject(); r.put("data", new String(b, 0, off, "UTF-8")); call.resolve(r);
        } catch (Exception e) { call.reject("Couldn’t read that backup"); }
    }

    /* ---------- screen time (Usage access, the data Digital Wellbeing uses) ---------- */
    private boolean usageAllowed() {
        try {
            android.app.AppOpsManager ops = (android.app.AppOpsManager) getContext().getSystemService(android.content.Context.APP_OPS_SERVICE);
            int mode = android.os.Build.VERSION.SDK_INT >= 29
                ? ops.unsafeCheckOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getContext().getPackageName())
                : ops.checkOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getContext().getPackageName());
            return mode == android.app.AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) { return false; }
    }

    @PluginMethod
    public void usageStatus(PluginCall call) { JSObject r = new JSObject(); r.put("granted", usageAllowed()); call.resolve(r); }

    @PluginMethod
    public void usageOpenSettings(PluginCall call) {
        android.content.Intent i = new android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS);
        if (android.os.Build.VERSION.SDK_INT >= 29) i.setData(android.net.Uri.parse("package:" + getContext().getPackageName()));
        i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
        try { getContext().startActivity(i); }
        catch (Exception e) { android.content.Intent j = new android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS); j.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK); getContext().startActivity(j); }
        call.resolve();
    }

    private String appLabel(String pkg, java.util.HashMap<String, String> cache) {
        if (cache.containsKey(pkg)) return cache.get(pkg);
        String n = pkg;
        try { android.content.pm.PackageManager pm = getContext().getPackageManager(); n = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString(); } catch (Exception e) { }
        cache.put(pkg, n); return n;
    }

    /** Per day: total phone time, time per app, unlocks, first pickup and last use. */
    @PluginMethod
    public void usageDays(final PluginCall call) {
        if (!usageAllowed()) { call.reject("no-permission"); return; }
        final int days = Math.max(1, Math.min(14, call.getInt("days", 7)));
        new Thread(() -> {
            try {
                android.app.usage.UsageStatsManager um = (android.app.usage.UsageStatsManager) getContext().getSystemService(android.content.Context.USAGE_STATS_SERVICE);
                android.content.pm.PackageManager pm = getContext().getPackageManager();
                java.util.HashSet<String> skip = new java.util.HashSet<>();
                android.content.Intent home = new android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_HOME);
                for (android.content.pm.ResolveInfo ri : pm.queryIntentActivities(home, 0)) skip.add(ri.activityInfo.packageName);
                skip.add("com.android.systemui"); skip.add("android");
                java.util.HashMap<String, String> labels = new java.util.HashMap<>();
                java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
                com.getcapacitor.JSArray out = new com.getcapacitor.JSArray();
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0); cal.set(java.util.Calendar.MINUTE, 0); cal.set(java.util.Calendar.SECOND, 0); cal.set(java.util.Calendar.MILLISECOND, 0);
                long now = System.currentTimeMillis();
                for (int d = 0; d < days; d++) {
                    long start = cal.getTimeInMillis();
                    java.util.Calendar e = (java.util.Calendar) cal.clone(); e.add(java.util.Calendar.DATE, 1);
                    long end = Math.min(e.getTimeInMillis(), now);
                    java.util.HashMap<String, Long> per = new java.util.HashMap<>();
                    android.app.usage.UsageEvents ev = um.queryEvents(start, end);
                    android.app.usage.UsageEvents.Event x = new android.app.usage.UsageEvents.Event();
                    String cur = null; long since = 0; int unlocks = 0; long first = 0, last = 0;
                    while (ev.hasNextEvent()) {
                        ev.getNextEvent(x);
                        int t = x.getEventType(); long ts = x.getTimeStamp(); String pk = x.getPackageName();
                        if (t == 1) { // activity resumed
                            if (cur != null && !cur.equals(pk)) per.put(cur, per.getOrDefault(cur, 0L) + (ts - since));
                            if (cur == null || !cur.equals(pk)) { cur = pk; since = ts; }
                            if (!skip.contains(pk)) { if (first == 0) first = ts; last = ts; }
                        } else if (t == 2 || t == 23) { // paused / stopped
                            if (cur != null && cur.equals(pk)) { per.put(cur, per.getOrDefault(cur, 0L) + (ts - since)); last = Math.max(last, ts); cur = null; }
                        } else if (t == 16) { // screen off
                            if (cur != null) { per.put(cur, per.getOrDefault(cur, 0L) + (ts - since)); last = Math.max(last, ts); cur = null; }
                        } else if (t == 18) { unlocks++; } // keyguard hidden
                    }
                    if (cur != null) per.put(cur, per.getOrDefault(cur, 0L) + (end - since));
                    long total = 0;
                    java.util.ArrayList<java.util.Map.Entry<String, Long>> list = new java.util.ArrayList<>();
                    for (java.util.Map.Entry<String, Long> en : per.entrySet()) { if (skip.contains(en.getKey()) || en.getValue() < 60000) continue; total += en.getValue(); list.add(en); }
                    list.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
                    com.getcapacitor.JSArray apps = new com.getcapacitor.JSArray();
                    for (int i = 0; i < Math.min(12, list.size()); i++) { JSObject a = new JSObject(); a.put("pkg", list.get(i).getKey()); a.put("name", appLabel(list.get(i).getKey(), labels)); a.put("min", Math.round(list.get(i).getValue() / 60000.0)); apps.put(a); }
                    JSObject o = new JSObject();
                    o.put("d", fmt.format(new java.util.Date(start))); o.put("total", Math.round(total / 60000.0)); o.put("unlocks", unlocks); o.put("first", first); o.put("last", last); o.put("apps", apps);
                    out.put(o);
                    cal.add(java.util.Calendar.DATE, -1);
                }
                JSObject r = new JSObject(); r.put("days", out); call.resolve(r);
            } catch (Exception e) { call.reject(e.getMessage() == null ? "Couldn’t read screen time" : e.getMessage()); }
        }).start();
    }

    /** Last night: the longest stretch the screen stayed off between 8 PM and 2 PM. */
    @PluginMethod
    public void usageSleep(final PluginCall call) {
        if (!usageAllowed()) { call.reject("no-permission"); return; }
        new Thread(() -> {
            try {
                android.app.usage.UsageStatsManager um = (android.app.usage.UsageStatsManager) getContext().getSystemService(android.content.Context.USAGE_STATS_SERVICE);
                java.util.Calendar c = java.util.Calendar.getInstance();
                c.set(java.util.Calendar.HOUR_OF_DAY, 20); c.set(java.util.Calendar.MINUTE, 0); c.set(java.util.Calendar.SECOND, 0); c.set(java.util.Calendar.MILLISECOND, 0);
                c.add(java.util.Calendar.DATE, -1);
                long from = c.getTimeInMillis(), to = Math.min(from + 18L * 3600000L, System.currentTimeMillis());
                android.app.usage.UsageEvents ev = um.queryEvents(from, to);
                android.app.usage.UsageEvents.Event x = new android.app.usage.UsageEvents.Event();
                long offAt = from, bestStart = 0, bestEnd = 0; boolean on = false;
                while (ev.hasNextEvent()) {
                    ev.getNextEvent(x);
                    int t = x.getEventType(); long ts = x.getTimeStamp();
                    if (t == 15 || t == 18 || t == 1) { // screen on / unlock / app opened
                        if (!on) { if (ts - offAt > bestEnd - bestStart) { bestStart = offAt; bestEnd = ts; } on = true; }
                    } else if (t == 16) { on = false; offAt = ts; }
                }
                JSObject r = new JSObject();
                if (bestEnd - bestStart >= 3L * 3600000L) { r.put("bed", bestStart); r.put("wake", bestEnd); }
                call.resolve(r);
            } catch (Exception e) { call.reject("Couldn’t read screen time"); }
        }).start();
    }
}
