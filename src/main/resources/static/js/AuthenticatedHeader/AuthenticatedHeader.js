document.addEventListener("DOMContentLoaded", function() {

    /* ==============================
       1. 다크모드 및 기본 설정
       ============================== */
    const toggleBtn = document.getElementById('darkModeBtn');
    const icon = document.getElementById('darkModeIcon');
    const body = document.body;

    // 로컬 스토리지 테마 적용
    const currentTheme = localStorage.getItem('theme');
    if (currentTheme === 'dark') {
        body.classList.add('dark-mode');
        if(icon) {
            icon.classList.replace('fa-regular', 'fa-solid');
            icon.classList.replace('fa-sun', 'fa-moon');
        }
    }

    if (toggleBtn) {
        toggleBtn.addEventListener('click', () => {
            body.classList.toggle('dark-mode');
            const isDark = body.classList.contains('dark-mode');
            localStorage.setItem('theme', isDark ? 'dark' : 'light');

            if(icon) {
                if (isDark) {
                    icon.classList.replace('fa-sun', 'fa-moon');
                    icon.classList.replace('fa-regular', 'fa-solid');
                } else {
                    icon.classList.replace('fa-moon', 'fa-sun');
                    icon.classList.replace('fa-solid', 'fa-regular');
                }
            }
        });
    }

    /* ==============================
       2. 드롭다운 통합 관리
       ============================== */
    const dropdownConfigs = [
        { btnId: 'langBtn', menuId: 'langMenu' },
        { btnId: 'profileBtn', menuId: 'profileMenu' },
        { btnId: 'notifyBtn', menuId: 'notifyMenu' }
    ];

    dropdownConfigs.forEach(config => {
        const btn = document.getElementById(config.btnId);
        const menu = document.getElementById(config.menuId);

        if (btn && menu) {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const isAlreadyOpen = menu.classList.contains('show');

                // 모든 드롭다운 닫기
                document.querySelectorAll('.notify-dropdown, .lang-dropdown, .profile-dropdown')
                        .forEach(m => m.classList.remove('show'));

                // 방금 클릭한 게 닫혀있었다면 열기
                if (!isAlreadyOpen) {
                    menu.classList.add('show');
                    if (config.btnId === 'notifyBtn') {
                        loadNotifications(); // 알림창 열 때 데이터 로드
                    }
                }
            });
        }
    });

    // 화면 클릭 시 닫기
    document.addEventListener('click', () => {
        document.querySelectorAll('.notify-dropdown, .lang-dropdown, .profile-dropdown')
                .forEach(m => m.classList.remove('show'));
    });

    // 메뉴 내부 클릭 시 닫힘 방지
    document.querySelectorAll('.notify-dropdown, .lang-dropdown, .profile-dropdown')
            .forEach(menu => {
                menu.addEventListener('click', (e) => e.stopPropagation());
            });


    /* ==============================
       3. 알림 시스템 로직
       ============================== */
    const notifyList = document.getElementById('notifyList');
    const notifyBadge = document.querySelector('.notify-badge');
    const expandBtn = document.getElementById('expandBtn');
    const markAllReadBtn = document.getElementById('markAllReadBtn');
    const deleteAllBtn = document.getElementById('deleteAllBtn');

    // "알림 없음" 요소를 미리 복제해둠 (innerHTML 초기화 시 삭제 방지)
    let emptyTemplate = null;
    const originalEmpty = document.getElementById('notifyEmpty');
    if (originalEmpty) {
        emptyTemplate = originalEmpty.cloneNode(true);
    }

    // 초기 뱃지 로드
    updateBadgeCount();

    // [3-1] 더 보기 / 접기 버튼
    if (expandBtn) {
        expandBtn.addEventListener('click', function(e) {
            e.stopPropagation();

            // CSS 클래스 토글
            notifyList.classList.toggle('expanded');

            const span = this.querySelector('span');
            const icon = this.querySelector('i');
            const moreTxt = this.getAttribute('data-more') || "더 보기";
            const foldTxt = this.getAttribute('data-fold') || "접기";

            if (notifyList.classList.contains('expanded')) {
                if(span) span.innerText = foldTxt;
                if(icon) icon.className = 'fa-solid fa-chevron-up';
            } else {
                if(span) span.innerText = moreTxt;
                if(icon) icon.className = 'fa-solid fa-chevron-down';
            }
        });
    }

    // [3-2] 모두 읽음
    if (markAllReadBtn) {
        markAllReadBtn.addEventListener('click', () => {
            fetch('/api/notifications/read-all', { method: 'PATCH' })
                .then(res => {
                    if (res.ok) {
                        document.querySelectorAll('.notify-item.unread').forEach(item => item.classList.remove('unread'));
                        updateBadgeCount();
                    }
                });
        });
    }

    // [3-3] 전체 삭제
    if (deleteAllBtn) {
        deleteAllBtn.addEventListener('click', () => {
            if(!confirm("모든 알림을 삭제하시겠습니까?")) return;
            fetch('/api/notifications', { method: 'DELETE' })
                .then(res => {
                    if (res.ok) {
                        renderEmptyState(); // 화면 비우고 알림 없음 표시
                        updateBadgeCount();
                    }
                });
        });
    }

    // [3-4] 알림 로드 및 렌더링
    function loadNotifications() {
        fetch('/api/notifications')
            .then(res => res.json())
            .then(data => {
                notifyList.innerHTML = ''; // 일단 비움

                if (!data || data.length === 0) {
                    renderEmptyState(); // 데이터 없으면 Empty 표시
                } else {
                    // 데이터 있으면 리스트 생성
                    data.forEach(n => {
                        notifyList.insertAdjacentHTML('beforeend', createNotificationHTML(n));
                    });
                }
                updateBadgeCount();
            })
            .catch(err => console.error("알림 로드 실패:", err));
    }

    // "알림 없음" 상태 그리기 함수
    function renderEmptyState() {
        notifyList.innerHTML = '';
        if (emptyTemplate) {
            const emptyClone = emptyTemplate.cloneNode(true);
            emptyClone.style.display = 'flex'; // 보이게 설정
            notifyList.appendChild(emptyClone);
        }
    }

    function createNotificationHTML(notif) {
        const isRead = notif.read || notif.isRead;
        const readClass = isRead ? '' : 'unread';
        const timeStr = timeAgo(notif.createdAt);

        return `
            <div class="notify-item ${readClass}" onclick="readAndGo('${notif.targetUrl}', ${notif.notificationId}, this)">
                <div class="notify-icon"><i class="fa-solid fa-bell"></i></div>
                <div class="notify-content">
                    <p class="notify-text"><strong>${notif.title}</strong><br>${notif.content}</p>
                    <span class="notify-time">${timeStr}</span>
                </div>
                <i class="fa-solid fa-xmark delete-btn" onclick="deleteNotification(event, ${notif.notificationId}, this)"></i>
            </div>
        `;
    }

    // [3-5] 뱃지 카운트
    function updateBadgeCount() {
        fetch('/api/notifications/unread-count')
            .then(res => res.json())
            .then(count => {
                if (count > 0) {
                    notifyBadge.style.display = 'flex';
                    notifyBadge.innerText = count > 99 ? '99+' : count;
                } else {
                    notifyBadge.style.display = 'none';
                }
            })
            .catch(() => { notifyBadge.style.display = 'none'; });
    }

    // 외부 호출용
    window.refreshBadge = updateBadgeCount;
});

/* ==============================
   4. 유틸리티 함수
   ============================== */
function changeLang(lang) {
    const url = new URL(window.location.href);
    url.searchParams.set('lang', lang);
    window.location.href = url.toString();
}

function deleteNotification(event, id, btn) {
    event.stopPropagation();
    fetch(`/api/notifications/${id}`, { method: 'DELETE' })
        .then(res => {
            if (res.ok) {
                const item = btn.closest('.notify-item');
                item.remove();

                // 다 지웠으면 Empty 표시
                const list = document.getElementById('notifyList');
                if (list.querySelectorAll('.notify-item').length === 0) {
                     if (window.refreshBadge) window.refreshBadge();
                }
                window.refreshBadge();
            }
        });
}

function readAndGo(url, id, el) {
    if (!el.classList.contains('unread')) {
        location.href = url;
        return;
    }
    fetch(`/api/notifications/${id}/read`, { method: 'PATCH' })
        .then(() => location.href = url)
        .catch(() => location.href = url);
}

// ★ [수정] 화면에 '通知'가 보이면 무조건 일본어로 표시하도록 강화
function timeAgo(dateString) {
    if (!dateString) return "";
    const diff = Math.floor((new Date() - new Date(dateString)) / 1000);

    // [감지 로직]
    // 1. HTML 태그 lang="ja"
    // 2. URL에 lang=ja 포함
    // 3. (가장 중요) 화면 헤더에 '通知' 텍스트가 있는지 확인
    const headerTitle = document.querySelector('.notify-header span');
    const isTextJA = headerTitle && headerTitle.innerText.trim() === '通知';

    const isJA = document.documentElement.lang === 'ja' ||
                 location.href.includes('lang=ja') ||
                 isTextJA;

    const i18n = isJA
        ? { now: "今", min: "分前", hr: "時間前", day: "日前" }
        : { now: "방금 전", min: "분 전", hr: "시간 전", day: "일 전" };

    if (diff < 60) return i18n.now;
    if (diff < 3600) return Math.floor(diff / 60) + i18n.min;
    if (diff < 86400) return Math.floor(diff / 3600) + i18n.hr;
    return Math.floor(diff / 86400) + i18n.day;
}