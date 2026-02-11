/* ==========================================
   FindPw.js - 비밀번호 찾기 (UX 개선판)
   ========================================== */

// ★ 1. 에러 메시지 표시/해제 공통 함수
function showError(fieldId, msg) {
    const errorEl = document.getElementById('err-' + fieldId);
    const inputEl = document.getElementById(fieldId);

    // 에러 메시지 보이기
    if(errorEl) {
        errorEl.style.display = 'block';
        if(msg) errorEl.innerText = msg; // 메시지 내용 변경
    }

    // 입력창에 빨간 테두리 추가
    if(inputEl) {
        inputEl.classList.add('input-error');
        inputEl.focus(); // 에러난 곳으로 포커스 이동
    }
}

function clearError(fieldId) {
    const errorEl = document.getElementById('err-' + fieldId);
    const inputEl = document.getElementById(fieldId);

    // 에러 메시지 숨기기
    if(errorEl) errorEl.style.display = 'none';

    // 빨간 테두리 제거
    if(inputEl) inputEl.classList.remove('input-error');

    // (특수 케이스) 인증 미완료 에러 메시지도 같이 숨김
    if(fieldId === 'email' || fieldId === 'authCode') {
         const verifyErr = document.getElementById('err-verify');
         if(verifyErr) verifyErr.style.display = 'none';
    }
}


// 2. 역할 선택 (구직자 / 구인자)
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


// 3. 연락처 자동 하이픈 + 에러 지우기
function autoHyphen(target) {
    // 입력할 때 에러가 떠있다면 지워줌
    clearError('contact');

    target.value = target.value
        .replace(/[^0-9]/g, '')
        .replace(/^(\d{0,3})(\d{0,4})(\d{0,4})$/g, "$1-$2-$3")
        .replace(/(\-{1,2})$/g, "");
}


// 4. 인증메일 발송 요청 (AJAX)
function sendMail() {
    const emailInput = document.getElementById('email');
    const email = emailInput.value.trim();

    // 1) 빈 값 체크
    if(!email) {
        showError('email', '이메일을 입력해 주세요.');
        return;
    }

    // 2) 이메일 형식 정규식 체크 (선택사항)
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailPattern.test(email)) {
        showError('email', '올바른 이메일 형식이 아닙니다.');
        return;
    }

    // 버튼 비활성화 (중복 클릭 방지)
    const btn = document.getElementById('btn-email-send');
    btn.disabled = true;
    const originalText = btn.innerText;
    btn.innerText = '전송중...';

    fetch('/api/mail/send', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: email })
    })
    .then(res => {
        if(res.ok) {
            // 성공 시: 에러 지우고 인증번호 입력칸 보여주기
            clearError('email');
            document.getElementById('auth-box').style.display = 'flex';
            document.getElementById('authCode').focus();
        } else {
            // 실패 시: 에러 메시지 출력
            showError('email', '메일 발송 실패. 이메일 주소를 확인해 주세요.');
        }
    })
    .catch(err => {
        console.error(err);
        showError('email', '서버 통신 중 오류가 발생했습니다.');
    })
    .finally(() => {
        // 버튼 복구
        btn.disabled = false;
        btn.innerText = originalText;
    });
}


// 5. 인증번호 확인 요청 (AJAX)
function checkCode() {
    const inputCodeEl = document.getElementById('authCode');
    const inputCode = inputCodeEl.value.trim();

    if(!inputCode) {
        showError('authCode', '인증번호를 입력해 주세요.');
        return;
    }

    fetch('/api/mail/check', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ code: inputCode })
    })
    .then(res => res.json())
    .then(isMatch => {
        if(isMatch) {
            // ★ 인증 성공 처리
            // 1. 입력창과 에러 메시지 숨김
            document.getElementById('auth-box').style.display = 'none';
            clearError('authCode');

            // 2. 성공 메시지 노출 및 상태 변경
            document.getElementById('auth-success-msg').style.display = 'block';
            document.getElementById('isVerified').value = "true";

            // 3. 하단 '인증 미완료' 에러가 있다면 숨김
            const verifyErr = document.getElementById('err-verify');
            if(verifyErr) verifyErr.style.display = 'none';

            // 4. 이메일 수정 방지 및 버튼 변경
            document.getElementById('email').readOnly = true;
            const sendBtn = document.getElementById('btn-email-send');
            sendBtn.disabled = true;
            sendBtn.innerText = "완료";
        } else {
            // 실패 시 에러 표시
            showError('authCode', '인증번호가 일치하지 않습니다.');
        }
    })
    .catch(err => {
        console.error(err);
        showError('authCode', '인증 확인 중 오류가 발생했습니다.');
    });
}


// 6. 비밀번호 변경 페이지로 이동 (최종 제출)
function goToChangePw() {
    const isVerified = document.getElementById('isVerified').value;
    const name = document.getElementById('name').value.trim();
    const contact = document.getElementById('contact').value.trim();

    // 1) 이름 유효성 검사
    if(!name) {
        showError('name', '이름을 입력해 주세요.');
        return;
    } else {
        clearError('name');
    }

    // 2) 연락처 유효성 검사
    if(!contact) {
        showError('contact', '연락처를 입력해 주세요.');
        return;
    } else {
        clearError('contact');
    }

    // 3) 인증 여부 검사
    if(isVerified !== "true") {
        // 하단에 에러 메시지 표시
        const verifyErr = document.getElementById('err-verify');
        if(verifyErr) verifyErr.style.display = 'block';

        // 이메일 입력창으로 포커스 이동 (사용자가 놓친 부분 알려줌)
        document.getElementById('email').focus();
        return;
    }

    // 4) 모든 검사 통과 -> alert 없이 즉시 이동
    const form = document.getElementById('findPwForm');
    form.action = "/changePw";
    form.method = "POST";
    form.submit();
}