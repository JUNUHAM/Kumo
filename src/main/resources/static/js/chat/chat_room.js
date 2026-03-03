var stompClient = null;
var roomId = document.getElementById("roomId").value;
var myId = document.getElementById("myId").value;
var msgArea = document.getElementById("msgArea");
var lastChatDate = null;

connect();

// ==========================================
// 1. 강철 멘탈 자동 재연결 기능이 추가된 connect()
// ==========================================
function connect() {
    var socket = new SockJS('/ws-stomp');
    stompClient = Stomp.over(socket);

    // 콘솔창에 STOMP 기본 로그가 너무 많이 찍히는 걸 방지 (선택 사항)
    // stompClient.debug = null; 

    stompClient.connect({}, function (frame) {
        console.log('✅ [LIVE] 웹소켓 방 입장 완료: ' + frame);

        // 메시지 수신 파이프 연결
        stompClient.subscribe('/sub/chat/room/' + roomId, function (messageOutput) {
            showMessage(JSON.parse(messageOutput.body));
        });

        scrollToBottom();

    }, function (error) {
        // ★ 핵심: 터널 통과 등 인터넷 끊김 시 자동 재연결 시도!
        console.error('❌ [LIVE] 웹소켓 연결 끊김! 3초 후 좀비처럼 재연결 시도...', error);
        setTimeout(connect, 3000);
    });
}

// ==========================================
// 3. 메모리 누수 방지용 수동 탈출 함수 (신규 추가)
// ==========================================
function disconnect() {
    if (stompClient !== null && stompClient.connected) {
        stompClient.disconnect(function () {
            console.log("🛑 [LIVE] 방 탈출 성공! 웹소켓 연결 안전하게 해제됨 (메모리 누수 방지)");
        });
    }
}

// 브라우저 탭 닫기, 새로고침 시 무조건 파이프 끊기
window.addEventListener('beforeunload', disconnect);

function sendMessage() {
    var msgInput = document.getElementById("msgInput");
    var messageContent = msgInput.value.trim();

    if (messageContent && stompClient) {
        var chatMessage = {
            roomId: roomId,
            senderId: myId,
            content: messageContent,
            messageType: 'TEXT'
        };
        stompClient.send("/pub/chat/message", {}, JSON.stringify(chatMessage));

        msgInput.value = '';
        msgInput.style.height = 'auto';
        msgInput.style.overflowY = 'hidden';
        msgInput.focus();
    }
}

function showMessage(message) {
    var today = new Date();
    var days = ['일요일', '월요일', '화요일', '수요일', '목요일', '금요일', '토요일'];
    var currentDate = today.getFullYear() + "년 " + (today.getMonth() + 1) + "월 " + today.getDate() + "일 " + days[today.getDay()];

    if (lastChatDate !== currentDate) {
        var dateDiv = document.createElement('div');
        dateDiv.className = "date-divider";
        dateDiv.innerHTML = `<span class="date-divider-text">${currentDate}</span>`;
        msgArea.appendChild(dateDiv);
        lastChatDate = currentDate;
    }
    var isMe = (message.senderId == myId);
    var div = document.createElement('div');
    var timeString = message.createdAt;

    var finalContentHtml = "";
    if (message.messageType === 'IMAGE') {
        finalContentHtml = `<img src="${message.content}" class="chat-image" 
                    style="max-width: 200px; border-radius: 10px; margin-top: 5px;"
                    onclick="openImageModal(this.src)">`;
    }
    else if (message.messageType === 'FILE') {
        const rawPath = message.content;
        const fileName = rawPath.includes('_') ? rawPath.split('_').pop() : rawPath;
        const ext = fileName.split('.').pop().toLowerCase();

        let iconClass = 'fa-file';
        let iconColor = '#95a5a6';

        if (ext === 'pdf') { iconClass = 'fa-file-pdf'; iconColor = '#ff6b6b'; }
        else if (ext === 'xlsx' || ext === 'xls') { iconClass = 'fa-file-excel'; iconColor = '#2ecc71'; }
        else if (ext === 'docx' || ext === 'doc') { iconClass = 'fa-file-word'; iconColor = '#4a90e2'; }
        else if (ext === 'txt') { iconClass = 'fa-file-lines'; iconColor = '#f1c40f'; }

        finalContentHtml = `
        <div class="file-bubble" data-url="${message.content}" onclick="window.open(this.getAttribute('data-url'))" style="cursor: pointer;">
            <div class="file-icon-box" style="color: ${iconColor};"><i class="fa-solid ${iconClass}"></i></div>
            <div class="file-info-box">
                <div class="file-display-name">${fileName}</div>
                <div class="file-display-sub">파일 열기</div>
            </div>
        </div>`;
    } else {
        finalContentHtml = `<div class="msg-bubble">${message.content}</div>`;
    }

    if (isMe) {
        div.className = "msg-row me";
        div.innerHTML = `<span class="msg-time">${timeString}</span><span class="unread-count">1</span>${finalContentHtml}`;
    } else {
        div.className = "msg-row other";
        div.innerHTML = `<img src="/images/dog_profile.jpg" class="profile-img">${finalContentHtml}<span class="msg-time">${timeString}</span>`;
    }

    msgArea.appendChild(div);
    scrollToBottom();
}

function autoResize(textarea) {
    textarea.style.height = 'auto';
    var maxHeight = 120;
    if (textarea.scrollHeight > maxHeight) {
        textarea.style.height = maxHeight + 'px';
        textarea.style.overflowY = 'auto';
    } else {
        textarea.style.height = textarea.scrollHeight + 'px';
        textarea.style.overflowY = 'hidden';
    }
}

function handleEnter(e) {
    if (e.isComposing || e.keyCode === 229) return;
    if (e.key === 'Enter') {
        if (!e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    }
}

function uploadImage() {
    var fileInput = document.getElementById('fileInput');
    var file = fileInput.files[0];
    if (file) {
        var formData = new FormData();
        formData.append("file", file);
        fetch('/chat/upload', {
            method: 'POST',
            body: formData
        })
            .then(response => response.text())
            .then(imageUrl => {
                if (imageUrl.includes("실패")) {
                    alert("사진 업로드 실패");
                    return;
                }
                var chatMessage = {
                    roomId: roomId,
                    senderId: myId,
                    content: imageUrl,
                    messageType: 'IMAGE'
                };
                stompClient.send("/pub/chat/message", {}, JSON.stringify(chatMessage));
            });
        fileInput.value = '';
    }
}

function uploadFile() {
    var fileInput = document.getElementById('docFileInput');
    var file = fileInput.files[0];

    if (file) {
        var formData = new FormData();
        formData.append("file", file);

        fetch('/chat/upload', {
            method: 'POST',
            body: formData
        })
            .then(response => response.text())
            .then(fileUrl => {
                if (fileUrl.includes("실패")) {
                    alert("파일 업로드 실패");
                    return;
                }
                var chatMessage = {
                    roomId: roomId,
                    senderId: myId,
                    content: fileUrl,
                    messageType: 'FILE'
                };
                stompClient.send("/pub/chat/message", {}, JSON.stringify(chatMessage));
            })
            .catch(err => console.error("업로드 에러:", err));

        fileInput.value = '';
    }
}

function scrollToBottom() {
    setTimeout(function () {
        msgArea.scrollTop = msgArea.scrollHeight;
    }, 150);
    setTimeout(function () {
        msgArea.scrollTop = msgArea.scrollHeight;
    }, 500);
}

const modalImg = document.getElementById("imageModal");
const modalMain = document.getElementById("mainPlusMenu");
const modalTemp = document.getElementById("templateMenu");

function openImageModal(src) {
    document.getElementById("modalImage").src = src;
    modalImg.showModal();
}
function closeImageModal() { modalImg.close(); }

function openMainMenu() { modalMain.showModal(); }
function closeMainMenu() { modalMain.close(); }

function openTemplateMenu() { modalTemp.showModal(); }
function closeTemplateMenu() { modalTemp.close(); }

function openSubMenu(type) {
    closeMainMenu();
    if (type === 'template') openTemplateMenu();
}

function insertText(text) {
    const inputField = document.getElementById('msgInput');
    if (inputField) {
        inputField.value = text;
        inputField.focus();
        if (typeof autoResize === 'function') autoResize(inputField);
    }
    closeTemplateMenu();
}

[modalImg, modalMain, modalTemp].forEach(m => {
    m.addEventListener('click', (e) => {
        if (e.target.nodeName === 'DIALOG') m.close();
    });
});

async function translateAllMessages() {
    console.log("🚀 [DEBUG] 번역 프로세스 시작");

    const translateBtn = document.querySelector('.header-translate-btn');
    const bubbles = document.querySelectorAll('.msg-bubble');

    const textsToTranslate = [];
    const targetBubbles = [];

    bubbles.forEach(bubble => {
        if (!bubble.querySelector('.translated-text')) {
            const txt = bubble.innerText.trim();
            if (txt) {
                textsToTranslate.push(txt);
                targetBubbles.push(bubble);
            }
        }
    });

    if (textsToTranslate.length === 0) {
        console.log("⚠️ 번역할 메시지가 없습니다.");
        return;
    }

    try {
        const isKorean = /[ㄱ-ㅎ|ㅏ-ㅣ|가-힣]/.test(textsToTranslate[0]);
        const targetLang = isKorean ? 'JA' : 'KO';

        console.log(`📡 서버에 ${textsToTranslate.length}개 문장 번역 요청 중...`);

        const response = await fetch('/api/translate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                text: textsToTranslate,
                target_lang: targetLang
            })
        });

        if (!response.ok) throw new Error(`서버 응답 실패: ${response.status}`);

        const data = await response.json();
        console.log("✅ 서버 응답 수신:", data);

        if (data && data.translations) {
            data.translations.forEach((item, index) => {
                const bubble = targetBubbles[index];
                if (!bubble) return;

                const hr = document.createElement('hr');
                hr.style.margin = '5px 0';
                hr.style.border = '0.5px solid rgba(0,0,0,0.1)';

                const div = document.createElement('div');
                div.className = 'translated-text';
                div.style.fontSize = '0.85em';
                div.style.color = '#555';
                div.innerText = '🌐 ' + item.text;

                bubble.appendChild(hr);
                bubble.appendChild(div);
            });
            console.log("🎉 모든 번역이 완료되었습니다!");
        }
    } catch (err) {
        console.error("❌ 번역 중 에러 발생:", err);
        alert("번역 처리 중 문제가 발생했습니다. 콘솔을 확인하세요.");
    }
}

document.addEventListener("DOMContentLoaded", function () {
    var dividers = document.querySelectorAll('.date-divider-text');
    if (dividers.length > 0) {
        lastChatDate = dividers[dividers.length - 1].innerText.trim();
    }

    const translateBtn = document.querySelector('.header-translate-btn');
    if (translateBtn) {
        translateBtn.onclick = translateAllMessages;
        console.log("✅ [DEBUG] 번역 버튼 이벤트 연결 완료!");
    } else {
        console.error("❌ [DEBUG] 번역 버튼(.header-translate-btn)을 찾을 수 없습니다.");
    }

    document.querySelectorAll('.file-bubble').forEach(bubble => {
        const rawNameDiv = bubble.querySelector('.raw-file-name');
        const displayNameDiv = bubble.querySelector('.file-display-name');
        const iconElement = bubble.querySelector('.file-icon-box i');
        const iconBox = bubble.querySelector('.file-icon-box');

        if (rawNameDiv && displayNameDiv && iconElement) {
            const rawPath = rawNameDiv.innerText;
            const fileName = rawPath.includes('_') ? rawPath.split('_').pop() : rawPath;
            displayNameDiv.innerText = fileName;

            const ext = fileName.split('.').pop().toLowerCase();
            iconElement.className = 'fa-solid';

            if (ext === 'pdf') {
                iconElement.classList.add('fa-file-pdf');
                iconBox.style.color = '#ff6b6b';
            } else if (ext === 'xlsx' || ext === 'xls') {
                iconElement.classList.add('fa-file-excel');
                iconBox.style.color = '#2ecc71';
            } else if (ext === 'docx' || ext === 'doc') {
                iconElement.classList.add('fa-file-word');
                iconBox.style.color = '#4a90e2';
            } else if (ext === 'txt') {
                iconElement.classList.add('fa-file-lines');
                iconBox.style.color = '#f1c40f';
            } else {
                iconElement.classList.add('fa-file');
                iconBox.style.color = '#95a5a6';
            }
        }
    }); // <-- file-bubble forEach 끝나는 곳

    // ==========================================
    // ★ 바로 이곳! 뒤로가기 버튼 누를 때 웹소켓 안전하게 끊기 ★
    // ==========================================
    const backBtn = document.querySelector('.header-back-btn');
    if (backBtn) {
        backBtn.addEventListener('click', function () {
            if (typeof disconnect === 'function') {
                disconnect(); // 웹소켓 탈출!
            }
        });
    }
}); // <-- DOMContentLoaded 완전히 끝나는 곳