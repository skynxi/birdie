/* Birdie web app: works offline, and picks up new versions the next time you open it. */
const CACHE = "birdie-web-v1";
const SHELL = ["./", "index.html", "app.js", "config.js", "capacitor.js", "manifest.webmanifest",
  "icons/icon-192.png", "icons/apple-touch-icon.png",
  "fonts/Nunito-600.woff2", "fonts/Nunito-700.woff2", "fonts/Nunito-800.woff2", "fonts/Nunito-900.woff2"];
self.addEventListener("install", e => { e.waitUntil(caches.open(CACHE).then(c => c.addAll(SHELL)).then(() => self.skipWaiting())); });
self.addEventListener("activate", e => { e.waitUntil(caches.keys().then(ks => Promise.all(ks.filter(k => k !== CACHE).map(k => caches.delete(k)))).then(() => self.clients.claim())); });
self.addEventListener("fetch", e => {
  const u = new URL(e.request.url);
  if (e.request.method !== "GET" || u.origin !== location.origin) return;
  // network first, so a new deploy shows up straight away; cache when offline
  e.respondWith(fetch(e.request).then(r => { const c = r.clone(); caches.open(CACHE).then(x => x.put(e.request, c)); return r; })
    .catch(() => caches.match(e.request).then(r => r || caches.match("index.html"))));
});
