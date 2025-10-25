// Service Worker 설치
self.addEventListener('install', function(event) {
    console.log('Service Worker 설치됨');
    self.skipWaiting();
});

// Service Worker 활성화
self.addEventListener('activate', function(event) {
    console.log('Service Worker 활성화됨');
    event.waitUntil(self.clients.claim());
});

// Push 이벤트 처리
self.addEventListener('push', function(event) {
    console.log('Push 알림 수신:', event);
    
    let data = {
        title: 'Web Push 알림',
        body: '새로운 알림이 도착했습니다.',
        icon: ''
    };

    if (event.data) {
        try {
            data = event.data.json();
            console.log('Push 데이터:', data);
        } catch (e) {
            console.error('Push 데이터 파싱 실패:', e);
            data.body = event.data.text();
        }
    }

    // 알림 옵션 구성 (유효한 값만 포함)
    const options = {
        body: data.body,
        vibrate: [200, 100, 200],
        tag: 'notification-tag',
        requireInteraction: false
    };

    // icon이 유효한 URL인 경우만 추가
    if (data.icon && isValidUrl(data.icon)) {
        options.icon = data.icon;
    }

    // 브라우저가 actions를 지원하는 경우만 추가
    if ('actions' in Notification.prototype) {
        options.actions = [
            {
                action: 'open',
                title: '열기'
            },
            {
                action: 'close',
                title: '닫기'
            }
        ];
    }

    console.log('알림 표시 시도:', data.title, options);

    // 알림 권한 확인
    if (Notification.permission !== 'granted') {
        console.error('알림 권한이 없습니다:', Notification.permission);
        return;
    }

    const notificationPromise = self.registration.showNotification(data.title, options)
        .then(() => {
            console.log('✅ 알림 표시 성공!');
        })
        .catch(err => {
            console.error('❌ 알림 표시 실패:', err);
            console.error('에러 상세:', err.message, err.stack);
        });

    event.waitUntil(notificationPromise);
});

// URL 유효성 검사 헬퍼 함수
function isValidUrl(string) {
    try {
        new URL(string);
        return true;
    } catch (_) {
        return false;
    }
}

// 알림 클릭 이벤트 처리
self.addEventListener('notificationclick', function(event) {
    console.log('알림 클릭됨:', event.action);
    
    event.notification.close();

    if (event.action === 'open') {
        event.waitUntil(
            clients.openWindow('/')
        );
    } else if (event.action === 'close') {
        // 알림 닫기 (이미 위에서 close() 호출됨)
    } else {
        // 알림 본문 클릭
        event.waitUntil(
            clients.openWindow('/')
        );
    }
});

// 알림 닫기 이벤트 처리
self.addEventListener('notificationclose', function(event) {
    console.log('알림 닫힘:', event.notification.tag);
});

// Fetch 이벤트 처리 (선택사항)
self.addEventListener('fetch', function(event) {
    // 네트워크 요청을 그대로 전달
    event.respondWith(fetch(event.request));
});

