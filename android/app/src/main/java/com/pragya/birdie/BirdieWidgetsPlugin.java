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
}
