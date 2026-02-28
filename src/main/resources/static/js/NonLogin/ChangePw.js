/* ==========================================
   ChangePw.js - 비밀번호 변경 (에러 메시지 방식)
   ========================================== */

const pwRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[$@$!%*#?&])[A-Za-z\d$@$!%*#?&]{8,16}$/;

function getMsg(id) {
    const el = document.getElementById(id);
    return el ? el.value : "";
}

// 에러 표시 공통 함수
function showError(fieldId, msg) {
    const errorEl = document.getElementById('err-' + fieldId);
    const inputEl = document.getElementById(fieldId);
    if(errorEl) {
        errorEl.innerText = msg;
        errorEl.style.display = 'block';
    }
    if(inputEl) inputEl.classList.add('input-error');
}

// 에러 숨김 공통 함수
function clearError(fieldId) {
    const errorEl = document.getElementById('err-' + fieldId);
    const inputEl = document.getElementById(fieldId);
    if(errorEl) errorEl.style.display = 'none';
    if(inputEl) inputEl.classList.remove('input-error');
}

function checkMatch() {
    const pw1 = document.getElementById('newPassword').value;
    const pw2 = document.getElementById('confirmPassword').value;
    const matchMsg = document.getElementById('pw-match-msg'); // 하단 통합 메시지

    if(!pw1 && !pw2) {
        matchMsg.style.display = 'none';
        return;
    }

    if(pw1 !== pw2) {
        matchMsg.innerText = getMsg('msg-pw-mismatch');
        matchMsg.style.color = "#EA4335";
        matchMsg.style.display = 'block';
    } else {
        if(pwRegex.test(pw1)) {
            matchMsg.innerText = getMsg('msg-pw-available');
            matchMsg.style.color = "#4285F4";
            matchMsg.style.display = 'block';
        } else {
            matchMsg.innerText = getMsg('msg-pw-invalid');
            matchMsg.style.color = "#EA4335";
            matchMsg.style.display = 'block';
        }
    }
}

function submitChange() {
    const pw1 = document.getElementById('newPassword').value;
    const pw2 = document.getElementById('confirmPassword').value;

    // 초기화
    clearError('newPassword');
    clearError('confirmPassword');

    // 1. 빈 값 체크
    if(!pw1) {
        showError('newPassword', getMsg('msg-pw-empty'));
        return;
    }

    if(!pw2) {
        showError('confirmPassword', getMsg('msg-confirm-empty'));
        return;
    }

    // 2. 정규식 체크
    if(!pwRegex.test(pw1)) {
        showError('newPassword', getMsg('msg-pw-invalid'));
        return;
    }

    // 3. 불일치 체크
    if(pw1 !== pw2) {
        showError('confirmPassword', getMsg('msg-pw-mismatch'));
        return;
    }

    // 모든 검사 통과
    document.getElementById('changePwForm').submit();
}