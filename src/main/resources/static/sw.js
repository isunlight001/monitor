// Service Worker for 基金监控系统 PWA

const CACHE_NAME = 'fund-monitor-v1.0.0';
const urlsToCache = [
  '/',
  '/index.html',
  '/fund-monitor.html',
  '/alert-records.html',
  '/login.html',
  '/ai-test.html',
  '/css/header.css',
  '/components/header.html'
];

// 安装事件 - 缓存资源
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => {
        console.log('Opened cache');
        return cache.addAll(urlsToCache);
      })
  );
});

// 获取事件 - 拦截网络请求
self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request)
      .then(response => {
        // 如果在缓存中找到响应，则返回缓存的资源
        if (response) {
          return response;
        }
        return fetch(event.request);
      }
    )
  );
});

// 激活事件 - 清理旧缓存
self.addEventListener('activate', event => {
  const cacheWhitelist = [CACHE_NAME];
  event.waitUntil(
    caches.keys().then(cacheNames => {
      return Promise.all(
        cacheNames.map(cacheName => {
          if (cacheWhitelist.indexOf(cacheName) === -1) {
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
});