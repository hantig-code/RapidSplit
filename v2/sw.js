/* Selbstauflösender Service Worker: Die App ist nach /app/ umgezogen.
   Diese Version räumt alte Caches ab, meldet sich selbst ab und lädt
   offene Fenster neu, damit die Weiterleitung greift. */
self.addEventListener("install", (e) => { self.skipWaiting(); });
self.addEventListener("activate", (e) => {
  e.waitUntil((async () => {
    try { const ks = await caches.keys(); await Promise.all(ks.map((k) => caches.delete(k))); } catch (err) {}
    try { await self.registration.unregister(); } catch (err) {}
    try {
      const cs = await self.clients.matchAll({ type: "window" });
      cs.forEach((c) => { try { c.navigate(c.url); } catch (err) {} });
    } catch (err) {}
  })());
});
self.addEventListener("fetch", () => {});
