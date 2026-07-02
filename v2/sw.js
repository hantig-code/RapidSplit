/* Kajak-Tour Kostenrechner — Service Worker
   Cache-first App-Shell, damit die App vollständig offline läuft.
   Bei jeder inhaltlichen Änderung CACHE_VERSION erhöhen. */
const CACHE_VERSION = "kajak-v2app-v34";
const APP_SHELL = [
  "./",
  "./index.html",
  "./manifest.webmanifest",
  "./icons/icon-192.png",
  "./icons/icon-dark-192.png",
  "./icons/favicon.png",
  "./icons/icon-512.png",
  "./icons/apple-touch-icon.png",
];

self.addEventListener("install", (event) => {
  // cache:"reload" umgeht den HTTP-Cache des Browsers, damit ein CACHE_VERSION-Bump
  // garantiert die NEUEN Assets vorlädt (nicht versehentlich veraltete).
  event.waitUntil(
    caches.open(CACHE_VERSION).then((cache) =>
      Promise.all(APP_SHELL.map((u) =>
        fetch(new Request(u, { cache: "reload" }))
          .then((res) => { if (res.ok) return cache.put(u, res); })
          .catch(() => {})
      ))
    )
  );
  self.skipWaiting();
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(keys.filter((k) => k !== CACHE_VERSION).map((k) => caches.delete(k)))
    )
  );
  self.clients.claim();
});

self.addEventListener("fetch", (event) => {
  const req = event.request;
  if (req.method !== "GET") return;

  // Navigationsanfragen: bei Offline auf index.html zurückfallen.
  if (req.mode === "navigate") {
    event.respondWith(
      fetch(req).catch(() => caches.match("./index.html"))
    );
    return;
  }

  // Sonst: Cache-first, danach Netzwerk. ignoreSearch, damit versionierte URLs
  // (z. B. icon.png?v=20) auf die vorgeladene Datei ohne Query treffen.
  event.respondWith(
    caches.match(req).then((c) => c || caches.match(req, { ignoreSearch: true })).then((cached) => {
      if (cached) return cached;
      return fetch(req).then((res) => {
        // Nur erfolgreiche, gleicher-Ursprung-Antworten cachen (keine 404/500/Opaque dauerhaft speichern).
        if (res && res.ok && res.type === "basic") {
          const copy = res.clone();
          caches.open(CACHE_VERSION).then((cache) => cache.put(req, copy));
        }
        return res;
      }).catch(() => cached);
    })
  );
});
