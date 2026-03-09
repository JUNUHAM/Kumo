// ==========================================================
// 🌟 [추가] 부모 창(메인 사이트) 다크모드 실시간 동기화
// ==========================================================
function syncDarkMode() {
    try {
        if (window.parent.document.body.classList.contains('dark-mode')) {
            document.body.classList.add('dark-mode');
        } else {
            document.body.classList.remove('dark-mode');
        }
    } catch (e) {
        console.log("iframe 다크모드 동기화 대기 중...");
    }
}

syncDarkMode();

try {
    const observer = new MutationObserver(syncDarkMode);
    observer.observe(window.parent.document.body, { attributes: true, attributeFilter: ['class'] });
} catch (e) {
    console.log("MutationObserver 연결 실패");
}

// 방 입장 함수
function enterRoom(roomId) {
    const userId = window.MY_USER_ID;
    const currentLang = window.CURRENT_LANG || 'kr';

    if (userId) {
        location.href = `/chat/room/${roomId}?userId=${userId}&lang=${currentLang}`;
    } else {
        const msg = (window.CHAT_LANG && window.CHAT_LANG.noLogin) ? window.CHAT_LANG.noLogin : '로그인이 필요합니다.';
        alert(msg);
    }
}

document.addEventListener("DOMContentLoaded", function () {
    const searchIcon = document.getElementById('searchIcon');
    const searchBar = document.getElementById('searchBar');
    const searchInput = document.getElementById('searchInput');

    if (searchIcon) {
        searchIcon.onclick = function () {
            searchBar.classList.toggle('active');
            if (searchBar.classList.contains('active')) {
                setTimeout(() => searchInput.focus(), 200);
            } else {
                searchInput.value = '';
                filterRooms('all');
            }
        };
    }

    if (searchInput) {
        searchInput.addEventListener('input', function () {
            const keyword = this.value.trim().toLowerCase();
            const rooms = document.querySelectorAll('.chat-item');
            rooms.forEach(room => {
                const name = room.querySelector('.chat-name').innerText.toLowerCase();
                room.style.display = name.includes(keyword) ? '' : 'none';
            });
        });
    }

    const tabBtns = document.querySelectorAll('.tab-btn');
    tabBtns.forEach(btn => {
        btn.addEventListener('click', function () {
            tabBtns.forEach(b => b.classList.remove('active'));
            this.classList.add('active');
            filterRooms(this.getAttribute('data-filter'));
        });
    });

    initializeUnreadBadges();
    connectChatList();
});

function initializeUnreadBadges() {
    document.querySelectorAll('.chat-item').forEach(room => {
        toggleUnreadBadge(room, room.getAttribute('data-unread') === 'true');
    });
}

function filterRooms(filterType) {
    const rooms = document.querySelectorAll('.chat-item');
    rooms.forEach(room => {
        const isUnread = room.getAttribute('data-unread') === 'true';
        if (filterType === 'all') room.style.display = '';
        else if (filterType === 'unread') room.style.display = isUnread ? '' : 'none';
        else if (filterType === 'read') room.style.display = !isUnread ? '' : 'none';
    });
}

function toggleOptionsMenu(event, iconElement) {
    event.stopPropagation();
    const dropdown = iconElement.nextElementSibling;
    document.querySelectorAll('.options-dropdown.show').forEach(menu => {
        if (menu !== dropdown) menu.classList.remove('show');
    });
    dropdown.classList.toggle('show');
}

document.addEventListener('click', () => {
    document.querySelectorAll('.options-dropdown.show').forEach(m => m.classList.remove('show'));
});

function togglePinRoom(event, element) {
    event.stopPropagation();
    const chatItem = element.closest('.chat-item');
    const pinText = element.querySelector('.pin-text');
    if (!chatItem) return;

    const isPinned = chatItem.classList.contains('is-pinned');
    if (isPinned) {
        chatItem.classList.remove('is-pinned');
        pinText.innerText = window.CHAT_LANG.pin;
    } else {
        chatItem.classList.add('is-pinned');
        pinText.innerText = window.CHAT_LANG.unpin;
    }
    location.reload(); // 간단히 새로고침으로 정렬 반영
}

// 🌟 [수정] 채팅 삭제 (서버 연동)
function deleteRoom(event, element) {
    event.stopPropagation();
    const chatItem = element.closest('.chat-item');
    const roomId = chatItem.getAttribute('data-room-id');
    const userId = window.MY_USER_ID;
    
    const confirmMsg = (window.CHAT_LANG && window.CHAT_LANG.deleteConfirm) 
        ? window.CHAT_LANG.deleteConfirm 
        : '정말 이 채팅방을 나가시겠습니까?';

    if (confirm(confirmMsg)) {
        // 서버에 삭제(방 나가기) 요청
        fetch(`/chat/room/exit/${roomId}?userId=${userId}`, {
            method: 'POST'
        }).then(res => {
            if (res.ok) {
                chatItem.style.transition = 'all 0.3s ease';
                chatItem.style.opacity = '0';
                chatItem.style.transform = 'translateX(20px)';
                setTimeout(() => chatItem.remove(), 300);
            } else {
                alert("삭제 실패");
            }
        }).catch(err => {
            console.error(err);
            alert("Network Error");
        });
    }
}

var stompListClient = null;

function connectChatList() {
    const myUserId = window.MY_USER_ID;
    if (!myUserId) return;

    var socket = new SockJS('/ws-stomp');
    stompListClient = Stomp.over(socket);
    stompListClient.debug = null;

    stompListClient.connect({}, function () {
        stompListClient.subscribe('/sub/chat/user/' + myUserId, function (messageOutput) {
            updateChatListUI(JSON.parse(messageOutput.body));
        });
    }, function () {
        setTimeout(connectChatList, 3000);
    });
}

function updateChatListUI(newMsg) {
    const targetRoom = document.querySelector(`.chat-item[data-room-id="${newMsg.roomId}"]`);
    if (targetRoom) {
        const preview = targetRoom.querySelector('.chat-preview');
        const timeSpan = targetRoom.querySelector('.chat-time');

        if (preview) {
            if (newMsg.messageType === 'IMAGE') preview.innerText = window.CHAT_LANG.image;
            else if (newMsg.messageType === 'FILE') preview.innerText = window.CHAT_LANG.file;
            else preview.innerText = newMsg.content;
        }

        if (timeSpan && newMsg.createdAt) {
            let timeStr = newMsg.createdAt.includes(' ') ? newMsg.createdAt.split(' ')[1].substring(0, 5) : newMsg.createdAt.substring(0, 5);
            timeSpan.innerText = timeStr;
        }

        const isUnread = (String(newMsg.senderId) !== String(window.MY_USER_ID));
        targetRoom.setAttribute('data-unread', String(isUnread));
        toggleUnreadBadge(targetRoom, isUnread);
        
        // 맨 위로 올리기
        const container = document.querySelector('.chat-list');
        if (!targetRoom.classList.contains('is-pinned')) {
            container.prepend(targetRoom);
        }
    }
}

function toggleUnreadBadge(roomElement, isUnread) {
    const infoDiv = roomElement.querySelector('.chat-info');
    if (!infoDiv) return;
    let badge = infoDiv.querySelector('.unread-dot');

    if (isUnread) {
        if (!badge) {
            badge = document.createElement('div');
            badge.className = 'unread-dot';
            badge.style = "width:10px; height:10px; background-color:#fa5252; border-radius:50%; position:absolute; right:20px; top:30px;";
            infoDiv.appendChild(badge);
        }
    } else if (badge) {
        badge.remove();
    }
}