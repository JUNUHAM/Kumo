/* ==========================================
   FindPw.js - 비밀번호 찾기 (다국어 지원 및 통합 유효성 검사 완료)
   ========================================== */

/* ★ 다국어 메시지 가져오는 헬퍼 함수 */
function getMsg(id) {
    const el = document.getElementById(id);
    return el ? el.value : "";
}

// 1. 에러 메시지 표시/해제 공통 함수
function showError(fieldId, msg) {
    const errorEl = document.getElementById('err-' + fieldId);
    const inputEl = document.getElementById(fieldId);

    if(errorEl) {
        errorEl.style.display = 'block';
        if(msg) errorEl.innerText = msg;
    }

    if(inputEl) {
        inputEl.classList.add('input-error');
        inputEl.focus();
    }
}

function clearError(fieldId) {
    const errorEl = document.getElementById('err-' + fieldId);
    const inputEl = document.getElementById(fieldId);

    if(errorEl) errorEl.style.display = 'none';
    if(inputEl) inputEl.classList.remove('input-error');

    if(fieldId === 'email' || fieldId === 'authCode') {
         const verifyErr = document.getElementById('err-verify');
         if(verifyErr) verifyErr.style.display = 'none';
    }
}

// 2. 역할 선택
function selectRole(role) {
    const tabs = document.querySelectorAll('.tab-btn');
    tabs.forEach(btn => btn.classList.remove('active'));

    if(role === 'SEEKER') {
        tabs[0].classList.add('active');
    } else {
        tabs[1].classList.add('active');
    }

    document.getElementById('roleInput').value = role;
}

// 3. 연락처 자동 하이픈
function autoHyphen(target) {
    clearError('contact');
    target.value = target.value
        .replace(/[^0-9]/g, '')
        .replace(/^(\d{0,3})(\d{0,4})(\d{0,4})$/g, "$1-$2-$3")
        .replace(/(\-{1,2})$/g, "");
}

// 4. 인증메일 발송 요청 (통합 정보 확인 로직 추가)
function sendMail() {
    // 필드 값 가져오기
    const name = document.getElementById('name').value.trim();
    const contact = document.getElementById('contact').value.trim();
    const emailInput = document.getElementById('email');
    const email = emailInput.value.trim();
    const role = document.getElementById('roleInput').value;

    // ★ 1) 메일 전송 전 이름, 연락처, 이메일 빈 값 체크
    if(!name) {
        showError('name', getMsg('msg-name-empty'));
        return;
    } else {
        clearError('name');
    }

    if(!contact) {
        showError('contact', getMsg('msg-contact-empty'));
        return;
    } else {
        clearError('contact');
    }

    if(!email) {
        showError('email', getMsg('msg-email-empty'));
        return;
    }

    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailPattern.test(email)) {
        showError('email', getMsg('msg-email-invalid'));
        return;
    }

    // 버튼 비활성화 및 텍스트 변경
    const btn = document.getElementById('btn-email-send');
    btn.disabled = true;
    const originalText = btn.innerText;
    btn.innerText = getMsg('msg-btn-sending');

    // ★ 2) 서버로 모든 정보(이름, 연락처, 이메일, 역할)를 보냄
    const requestData = {
        name,
        contact,
        email,
        role
    };

    fetch('/api/mail/send', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestData)
    })
    .then(async res => {
        if (res.ok) {
            // 성공: 인증번호 입력창 노출
            document.getElementById('auth-box').style.display = 'flex';
        } else {
            // 실패: 서버가 보낸 에러 코드 확인
            const errorCode = await res.text();

            if (errorCode === "USER_NOT_FOUND") {
                // 다국어 메시지: "일치하는 회원 정보가 없습니다."
                showError('email', getMsg('msg-fail-default'));
            } else if (errorCode === "EMPTY_EMAIL") {
                showError('email', getMsg('msg-email-empty'));
            } else {
                showError('email', getMsg('msg-server-error'));
            }
        }
    })
        .catch(err => {
            console.error(err);
            showError('email', getMsg('msg-server-error'));
        })
    .finally(() => {
        btn.disabled = false;
        btn.innerText = originalText;
    });
}

// 5. 인증번호 확인 요청
function checkCode() {
    const inputCodeEl = document.getElementById('authCode');
    const inputCode = inputCodeEl.value.trim();

    if(!inputCode) {
        showError('authCode', getMsg('msg-auth-code-empty'));
        return;
    }

    fetch('/api/mail/check', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ code: inputCode })
    })
    .then(res => res.json())
    .then(isMatch => {
        if(isMatch == true) {
            document.getElementById('auth-box').style.display = 'none';
            clearError('authCode');

            document.getElementById('auth-success-msg').style.display = 'block';
            document.getElementById('isVerified').value = "true";

            const verifyErr = document.getElementById('err-verify');
            if(verifyErr) verifyErr.style.display = 'none';

            document.getElementById('email').readOnly = true;
            const sendBtn = document.getElementById('btn-email-send');
            sendBtn.disabled = true;
            sendBtn.innerText = getMsg('msg-btn-complete');
        } else {
            showError('authCode', getMsg('msg-auth-code-mismatch'));
        }
    })
    .catch(err => {
        console.error(err);
        showError('authCode', getMsg('msg-auth-error'));
    });
}

// 6. 비밀번호 변경 페이지로 이동
function goToChangePw() {
    const isVerified = document.getElementById('isVerified').value;
    const name = document.getElementById('name').value.trim();
    const contact = document.getElementById('contact').value.trim();

    if(!name) {
        showError('name', getMsg('msg-name-empty'));
        return;
    } else {
        clearError('name');
    }

    if(!contact) {
        showError('contact', getMsg('msg-contact-empty'));
        return;
    } else {
        clearError('contact');
    }

    if(isVerified !== "true") {
        const verifyErr = document.getElementById('err-verify');
        if(verifyErr) verifyErr.style.display = 'block';
        document.getElementById('email').focus();
        return;
    }

    const form = document.getElementById('findPwForm');
    form.action = "/changePw";
    form.method = "POST";
    form.submit();
}