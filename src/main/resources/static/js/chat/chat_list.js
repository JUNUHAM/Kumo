/* --- src/main/resources/static/js/chat_list.js --- */

// 방 입장 함수
// 방 입장 함수
function enterRoom(roomId) {
    // ❌ (삭제) 더 이상 촌스럽게 주소창을 뒤지지 않습니다!
    // const urlParams = new URLSearchParams(window.location.search);
    // const userId = urlParams.get('userId');

    // 🟢 (추가) 아까 HTML에 몰래 숨겨둔 '진짜 ID'를 쏙 빼옵니다!
    const userIdInput = document.getElementById('currentUserId');
    const userId = userIdInput ? userIdInput.value : null;

    if (userId) {
        // ID를 무사히 찾았다면 채팅방으로 돌격!
        location.href = '/chat/room/' + roomId + '?userId=' + userId;
    } else {
        alert("로그인 정보(userId)가 없습니다! 다시 로그인해주세요.");
    }
}

// DOMContentLoaded 이벤트 (검색바 + 탭 메뉴 + 진짜 이름 검색 로직)
document.addEventListener("DOMContentLoaded", function () {
    const searchIcon = document.getElementById('searchIcon');
    const searchBar = document.getElementById('searchBar');
    const searchInput = document.getElementById('searchInput');

    // 1. 돋보기 아이콘 클릭 시 검색창 열기/닫기
    if (searchIcon) {
        searchIcon.onclick = function () {
            searchBar.classList.toggle('active');
            if (searchBar.classList.contains('active')) {
                setTimeout(() => searchInput.focus(), 200);
            } else {
                // 검색창 닫을 때 검색어 지우고 '전체' 탭으로 초기화
                searchInput.value = '';
                const allTabBtn = document.querySelector('.tab-btn');
                if (allTabBtn) filterRooms('all', allTabBtn);
            }
        };
    }

    // 2. 진짜 '이름 검색' 로직 복구
    if (searchInput) {
        searchInput.addEventListener('input', function () {
            const keyword = this.value.trim().toLowerCase();
            const rooms = document.querySelectorAll('.chat-item');

            // 검색할 때는 '전체' 탭 기준으로 모든 방을 보여준 상태에서 필터링
            document.querySelectorAll('.tab-btn').forEach(tab => tab.classList.remove('active'));
            const allTabBtn = document.querySelector('.tab-btn');
            if (allTabBtn) allTabBtn.classList.add('active');

            rooms.forEach(room => {
                const nameElement = room.querySelector('.chat-name');
                if (nameElement) {
                    const name = nameElement.innerText.toLowerCase();
                    // 이름에 검색어가 포함되어 있으면 보여주고, 없으면 숨김
                    if (name.startsWith(keyword)) {
                        room.style.display = '';
                    } else {
                        room.style.display = 'none';
                    }
                }
            });
        });
    }

    // 3. 탭 메뉴 토글 로직
    const tabBtns = document.querySelectorAll('.tab-btn');
    tabBtns.forEach(btn => {
        btn.addEventListener('click', function () {
            if (searchInput) searchInput.value = ''; // 탭 누르면 검색어 지우기
            tabBtns.forEach(b => b.classList.remove('active'));
            this.classList.add('active');
        });
    });
});

// 필터링 핵심 함수
function filterRooms(filterType, clickedTab) {
    if (clickedTab) {
        document.querySelectorAll('.tab-btn').forEach(tab => tab.classList.remove('active'));
        clickedTab.classList.add('active');
    }

    const rooms = document.querySelectorAll('.chat-item');

    rooms.forEach(room => {
        const unreadData = room.getAttribute('data-unread') || 'false';
        const isUnread = (unreadData.trim() === 'true');

        if (filterType === 'all' || !filterType) {
            room.style.display = '';
        } else if (filterType === 'unread') {
            room.style.display = isUnread ? '' : 'none';
        } else if (filterType === 'read') {
            room.style.display = !isUnread ? '' : 'none';
        }
    });
}

// 점 세 개 클릭 시 메뉴 열기/닫기
function toggleOptionsMenu(event, iconElement) {
    event.stopPropagation();
    document.querySelectorAll('.options-dropdown.show').forEach(menu => {
        if (menu !== iconElement.nextElementSibling) {
            menu.classList.remove('show');
        }
    });
    const dropdown = iconElement.nextElementSibling;
    dropdown.classList.toggle('show');
}

// 화면 밖 클릭 시 드롭다운 닫기
document.addEventListener('click', function (event) {
    if (!event.target.matches('.options-icon')) {
        document.querySelectorAll('.options-dropdown.show').forEach(menu => {
            menu.classList.remove('show');
        });
    }
});

// 고정/고정해제 버튼
function togglePinRoom(event, element) {
    event.stopPropagation();

    const chatItem = element.closest('.chat-item');
    const chatList = document.querySelector('.chat-list');
    const pinText = element.querySelector('.pin-text');

    if (!chatItem) return;

    const isPinned = chatItem.classList.contains('is-pinned');

    chatItem.style.transition = 'opacity 0.3s ease';
    chatItem.style.opacity = '0.5';
    setTimeout(() => chatItem.style.opacity = '1', 300);

    if (isPinned) {
        chatItem.classList.remove('is-pinned');
        pinText.innerText = '고정';

        const myTime = chatItem.querySelector('.chat-time').innerText;
        const otherRooms = Array.from(chatList.querySelectorAll('.chat-item:not(.is-pinned)'));
        let targetRoom = otherRooms.find(room => {
            const roomTime = room.querySelector('.chat-time').innerText;
            return roomTime < myTime;
        });

        if (targetRoom) {
            chatList.insertBefore(chatItem, targetRoom);
        } else {
            chatList.appendChild(chatItem);
        }

    } else {
        chatItem.classList.add('is-pinned');
        pinText.innerText = '해제';
        chatList.prepend(chatItem);
    }

    document.querySelectorAll('.options-dropdown.show').forEach(m => m.classList.remove('show'));
}

// 삭제 버튼 클릭
function deleteRoom(event, element) {
    event.stopPropagation();
    if (confirm("정말 이 채팅방을 나가시겠습니까?")) {
        const chatItem = element.closest('.chat-item');
        if (chatItem) {
            chatItem.style.transition = 'all 0.3s ease';
            chatItem.style.opacity = '0';
            chatItem.style.transform = 'translateX(20px)';
            setTimeout(() => {
                chatItem.remove();
            }, 300);
        }
    } else {
        document.querySelectorAll('.options-dropdown.show').forEach(m => m.classList.remove('show'));
    }
} // ★ 여기서 deleteRoom 함수가 완전히 끝납니다!


// ====================================================================
// ★★★ [LIVE] 채팅 리스트 실시간 동기화 (웹소켓 엔진) ★★★
// ====================================================================

var stompListClient = null;

// DOMContentLoaded 안에서 호출할 수 있도록 세팅
document.addEventListener("DOMContentLoaded", function () {
    connectChatList();
});

function connectChatList() {
    const urlParams = new URLSearchParams(window.location.search);
    const myUserId = urlParams.get('userId');

    if (!myUserId) return; // 유저 아이디 없으면 연결 안 함

    var socket = new SockJS('/ws-stomp');
    stompListClient = Stomp.over(socket);

    // 콘솔창 더러워지는 것 방지
    stompListClient.debug = null;

    stompListClient.connect({}, function (frame) {
        console.log('✅ [LIVE] 로비(목록) 웹소켓 연결 완료!');

        // ★ 핵심: 백엔드가 '나(myUserId)'에게 보내는 전용 알림 파이프를 구독합니다.
        const myLobbyTopic = '/sub/chat/user/' + myUserId;

        stompListClient.subscribe(myLobbyTopic, function (messageOutput) {
            var newMsg = JSON.parse(messageOutput.body);
            updateChatListUI(newMsg);
        });

    }, function (error) {
        console.error('❌ [LIVE] 로비 연결 끊김! 3초 후 재연결...', error);
        setTimeout(connectChatList, 3000);
    });
}

// ==========================================================
// ★ 완벽 조준 완료된 실시간 목록 갱신 함수 ★
// ==========================================================
function updateChatListUI(newMsg) {
    const chatItems = document.querySelectorAll('.chat-item');
    let targetRoom = null;

    chatItems.forEach(item => {
        const onclickAttr = item.getAttribute('onclick');
        if (onclickAttr && onclickAttr.includes(`enterRoom(${newMsg.roomId})`)) {
            targetRoom = item;
        }
    });

    if (targetRoom) {
        // ★ 범인 검거: .chat-recent-msg 가 아니라 .chat-preview 였습니다!
        const recentMsgSpan = targetRoom.querySelector('.chat-preview');
        const timeSpan = targetRoom.querySelector('.chat-time');

        // 1. 메시지 내용 찰칵! 덮어쓰기
        if (recentMsgSpan) {
            if (newMsg.messageType === 'IMAGE') recentMsgSpan.innerText = '(사진)';
            else if (newMsg.messageType === 'FILE') recentMsgSpan.innerText = '(파일)';
            else recentMsgSpan.innerText = newMsg.content;
        }

        // 2. 시간 찰칵! 덮어쓰기 (HH:mm 형태로 잘라서 예쁘게 넣기)
        if (timeSpan && newMsg.createdAt) {
            let timeStr = newMsg.createdAt;
            // 만약 서버 시간이 "2026-03-03 15:40" 처럼 길게 온다면 "15:40"만 자르기
            if (timeStr.includes(' ')) timeStr = timeStr.split(' ')[1].substring(0, 5);
            else if (timeStr.includes('T')) timeStr = timeStr.split('T')[1].substring(0, 5);

            timeSpan.innerText = timeStr;
        }

        // 3. 방을 맨 위로 훅! 끌어올리기 (고정핀 방이 아닐 때만)
        if (!targetRoom.classList.contains('is-pinned')) {
            const chatListContainer = document.querySelector('.chat-list');
            const firstUnpinned = chatListContainer.querySelector('.chat-item:not(.is-pinned)');

            // ★ 라이브의 맛: 새 메시지 오면 KUMO 스타일 연노랑 불빛이 1초간 켜집니다!
            targetRoom.style.transition = 'background-color 0.4s ease';
            targetRoom.style.backgroundColor = 'rgba(255, 184, 0, 0.15)';
            setTimeout(() => targetRoom.style.backgroundColor = '', 1000);

            if (firstUnpinned) {
                chatListContainer.insertBefore(targetRoom, firstUnpinned);
            } else {
                chatListContainer.appendChild(targetRoom);
            }
        }
    } else {
        console.log("목록에 없는 완전히 새로운 방입니다! (페이지 새로고침 필요)");
    }
}

// ==========================================================
// ★ [LIVE] 완벽 조준 완료된 실시간 목록 갱신 (탭 동기화 포함) ★
// ==========================================
function updateChatListUI(newMsg) {
    const chatItems = document.querySelectorAll('.chat-item');
    let targetRoom = null;

    chatItems.forEach(item => {
        const onclickAttr = item.getAttribute('onclick');
        if (onclickAttr && onclickAttr.includes(`enterRoom(${newMsg.roomId})`)) {
            targetRoom = item;
        }
    });

    if (targetRoom) {
        const recentMsgSpan = targetRoom.querySelector('.chat-preview');
        const timeSpan = targetRoom.querySelector('.chat-time');

        // 1. 메시지 내용 찰칵! 덮어쓰기
        if (recentMsgSpan) {
            if (newMsg.messageType === 'IMAGE') recentMsgSpan.innerText = '(사진)';
            else if (newMsg.messageType === 'FILE') recentMsgSpan.innerText = '(파일)';
            else recentMsgSpan.innerText = newMsg.content;
        }

        // 2. 시간 찰칵! 덮어쓰기 (HH:mm 형태로 잘라서 예쁘게 넣기)
        if (timeSpan && newMsg.createdAt) {
            let timeStr = newMsg.createdAt;
            if (timeStr.includes(' ')) timeStr = timeStr.split(' ')[1].substring(0, 5);
            else if (timeStr.includes('T')) timeStr = timeStr.split('T')[1].substring(0, 5);
            timeSpan.innerText = timeStr;
        }

        // 3. 방을 맨 위로 훅! 끌어올리기 (고정핀 방이 아닐 때만)
        if (!targetRoom.classList.contains('is-pinned')) {
            const chatListContainer = document.querySelector('.chat-list');
            const firstUnpinned = chatListContainer.querySelector('.chat-item:not(.is-pinned)');

            targetRoom.style.transition = 'background-color 0.4s ease';
            targetRoom.style.backgroundColor = 'rgba(255, 184, 0, 0.15)';
            setTimeout(() => targetRoom.style.backgroundColor = '', 1000);

            if (firstUnpinned) {
                chatListContainer.insertBefore(targetRoom, firstUnpinned);
            } else {
                chatListContainer.appendChild(targetRoom);
            }
        }

        // ==========================================================
        // ★ [추가] 4. 안읽음/읽음 데이터 실시간 업데이트 및 탭 즉시 갱신 ★
        // ==========================================================
        const urlParams = new URLSearchParams(window.location.search);
        const myUserId = urlParams.get('userId');

        // 메시지 발신자가 '나'인지 '상대방'인지 판별해서 데이터 속성 갱신
        if (String(newMsg.senderId) !== String(myUserId)) {
            targetRoom.setAttribute('data-unread', 'true'); // 상대방이 보냄 -> 안읽음
        } else {
            targetRoom.setAttribute('data-unread', 'false'); // 내가 보냄 -> 읽음
        }

        // 현재 켜져 있는 탭(전체/읽지않음/읽음)에 맞게 방을 즉시 보여주거나 숨기기
        const activeTab = document.querySelector('.tab-btn.active');
        if (activeTab) {
            const filterText = activeTab.innerText.trim();
            const isUnread = targetRoom.getAttribute('data-unread') === 'true';

            if (filterText === '읽지않음' && !isUnread) {
                targetRoom.style.display = 'none'; // '읽지않음' 탭인데 읽은 상태면 숨김
            } else if (filterText === '읽음' && isUnread) {
                targetRoom.style.display = 'none'; // '읽음' 탭인데 안 읽은 새 메시지가 오면 숨김
            } else {
                targetRoom.style.display = ''; // 조건에 맞으면 바로 보여줌
            }
        }
    } else {
        console.log("목록에 없는 완전히 새로운 방입니다! (페이지 새로고침 필요)");
    }
}