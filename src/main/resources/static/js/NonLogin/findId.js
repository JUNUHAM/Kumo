/* ==========================================
   FindId.js - 아이디 찾기 (유효성 검사 & 다국어 지원 완료)
   ========================================== */

/* 전역 변수 */
let currentRole = 'SEEKER';

/* ★ 연락처 정규식 (010-0000-0000 형식) */
const contactRegex = /^01([0|1|6|7|8|9])-?([0-9]{3,4})-?([0-9]{4})$/;

/* 1. 역할 선택 */
function selectRole(role) {
    currentRole = role;

    const tabs = document.querySelectorAll('.tab-btn');
    tabs.forEach(btn => btn.classList.remove('active'));

    if(role === 'SEEKER') {
        tabs[0].classList.add('active');
    } else {
        tabs[1].classList.add('active');
    }

    // hidden input 값 업데이트
    const roleInput = document.getElementById('roleInput');
    if(roleInput) roleInput.value = role;

    // 화면 초기화
    resetDisplay();
}

/* 2. 화면 초기화 */
function resetDisplay() {
    // 결과창 및 에러창 숨김
    const resultBox = document.getElementById('result-box');
    const errorMsg = document.getElementById('error-msg');
    if(resultBox) resultBox.style.display = 'none';
    if(errorMsg) errorMsg.style.display = 'none';

    // 입력값 초기화
    document.getElementById('name').value = '';
    document.getElementById('contact').value = '';

    // ★ 빨간 에러 메시지 및 테두리 초기화
    clearError('name');
    clearError('contact');
}

/* 3. 자동 하이픈 + 에러 지우기 */
function autoHyphen(target) {
    // 입력 중에는 에러 지워줌 (UX 향상)
    clearError('contact');

    target.value = target.value
        .replace(/[^0-9]/g, '')
        .replace(/^(\d{0,3})(\d{0,4})(\d{0,4})$/g, "$1-$2-$3")
        .replace(/(\-{1,2})$/g, "");
}

/* ==============================
   ★ 유효성 검사 관련 함수
   ============================== */

/* 에러 메시지 숨기기 & 테두리 복구 */
function clearError(fieldId) {
    const inputEl = document.getElementById(fieldId);

    // 1. 빨간 테두리 제거
    if(inputEl) inputEl.classList.remove('input-error');

    // 2. 에러 메시지 숨기기 (연락처는 2개 관리)
    if (fieldId === 'contact') {
        const emptyErr = document.getElementById('err-contact-empty');
        const invalidErr = document.getElementById('err-contact-invalid');
        if(emptyErr) emptyErr.style.display = 'none';
        if(invalidErr) invalidErr.style.display = 'none';
    } else {
        // 일반 필드 (이름)
        const errorEl = document.getElementById('err-' + fieldId);
        if(errorEl) errorEl.style.display = 'none';
    }
}

/* 에러 메시지 보이기 & 테두리 빨갛게 */
function showError(fieldId, type = 'empty') {
    const inputEl = document.getElementById(fieldId);

    // 1. 빨간 테두리 추가 및 포커스
    if(inputEl) {
        inputEl.classList.add('input-error');
        inputEl.focus();
    }

    // 2. 상황에 맞는 에러 메시지 표시
    if (fieldId === 'contact') {
        if (type === 'empty') {
            document.getElementById('err-contact-empty').style.display = 'block';
            document.getElementById('err-contact-invalid').style.display = 'none';
        } else if (type === 'invalid') {
            document.getElementById('err-contact-empty').style.display = 'none';
            document.getElementById('err-contact-invalid').style.display = 'block';
        }
    } else {
        // 일반 필드 (이름)
        const errorEl = document.getElementById('err-' + fieldId);
        if(errorEl) errorEl.style.display = 'block';
    }
}


/* 4. 아이디 찾기 (AJAX) */
function findId() {
    const nameInput = document.getElementById('name');
    const contactInput = document.getElementById('contact');

    const name = nameInput.value.trim();
    const contact = contactInput.value.trim();

    // --- 유효성 검사 시작 ---

    // 1) 이름 검사
    if (!name) {
        showError('name');
        return;
    } else {
        clearError('name');
    }

    // 2) 연락처 빈 값 검사
    if (!contact) {
        showError('contact', 'empty');
        return;
    }
    // 3) 연락처 정규식(형식) 검사
    else if (!contactRegex.test(contact)) {
        showError('contact', 'invalid');
        return;
    }
    else {
        clearError('contact');
    }

    // --- 유효성 검사 통과 후 서버 요청 ---

    const requestData = {
        name: name,
        contact: contact,
        role: currentRole
    };

    fetch('/api/findId', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestData)
    })
    .then(response => {
        // 서버 오류(500 등) 체크
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(data => {
        const resultBox = document.getElementById('result-box');
        const errorMsg = document.getElementById('error-msg');

        if (data.status === 'success') {
            // [성공 시] 결과창 표시, 에러창 숨김
            if(errorMsg) errorMsg.style.display = 'none';
            if(resultBox) resultBox.style.display = 'block';

            // 이메일 데이터 표시
            document.getElementById('found-email').innerText = data.email;
        } else {
            // [실패 시] 결과창 숨김, 에러창 표시
            if(resultBox) resultBox.style.display = 'none';
            if(errorMsg) errorMsg.style.display = 'block';

            const errorText = document.getElementById('error-text-content');
            if(errorText) {
                // ★ 핵심 수정: HTML에 숨겨둔 다국어 메시지 가져오기
                const defaultMsgInput = document.getElementById('msg-fail-default');

                // 숨겨둔 메시지가 있으면 그걸 쓰고, 없으면 서버 메시지 사용
                const messageToShow = defaultMsgInput ? defaultMsgInput.value : (data.message || "일치하는 정보가 없습니다.");

                errorText.innerText = messageToShow;
            }
        }
    })
    .catch(error => {
        console.error('Error:', error);
        // 통신 에러 시 이름 필드 쪽에 에러 표시
        showError('name');
        const errEl = document.getElementById('err-name');
        if(errEl) errEl.innerText = "서버 통신 중 오류가 발생했습니다.";
    });
}