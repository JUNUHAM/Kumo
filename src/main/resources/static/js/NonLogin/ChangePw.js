/* ==========================================
   ChangePw.js - 비밀번호 변경 유효성 검사 (정규식 적용)
   ========================================== */

// ★ 정규식: 8~16자, 영문 + 숫자 + 특수문자[$@$!%*#?&] 포함
const pwRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[$@$!%*#?&])[A-Za-z\d$@$!%*#?&]{8,16}$/;

function checkMatch() {
    const pw1 = document.getElementById('newPassword').value;
    const pw2 = document.getElementById('confirmPassword').value;
    const errorEl = document.getElementById('pw-error-msg');

    // 둘 다 비어있으면 에러 숨김
    if(!pw1 && !pw2) {
        errorEl.style.display = 'none';
        return;
    }

    // 1. 일치 여부 확인
    if(pw1 !== pw2) {
        errorEl.innerText = "비밀번호가 일치하지 않습니다.";
        errorEl.style.color = "#EA4335"; // 빨강
        errorEl.style.display = 'block';
    } else {
        // 2. 일치하더라도 정규식 규칙을 지켰는지 확인
        if(pwRegex.test(pw1)) {
            // 규칙 통과
            errorEl.innerText = "비밀번호가 일치하며 사용 가능합니다.";
            errorEl.style.color = "#4285F4"; // 파랑/초록
            errorEl.style.display = 'block';
        } else {
            // 규칙 위반 (빨간불)
            errorEl.innerText = "비밀번호 규칙(8~16자, 영문+숫자+특수문자)을 확인해 주세요.";
            errorEl.style.color = "#EA4335"; // 빨강
            errorEl.style.display = 'block';
        }
    }
}

function submitChange() {
    const pw1 = document.getElementById('newPassword').value;
    const pw2 = document.getElementById('confirmPassword').value;
    const errorEl = document.getElementById('pw-error-msg');

    // 1. 빈 값 체크
    if(!pw1) {
        alert("새 비밀번호를 입력해 주세요.");
        return;
    }

    // 2. ★ 정규식 체크 (기존 길이 체크 대체)
    if(!pwRegex.test(pw1)) {
        // 에러 메시지 표시
        errorEl.innerText = "비밀번호는 8~16자 영문, 숫자, 특수문자를 사용해야 합니다.";
        errorEl.style.color = "#EA4335";
        errorEl.style.display = 'block';

        alert("비밀번호 규칙이 올바르지 않습니다.\n(8~16자, 영문+숫자+특수문자 포함)");
        return;
    }

    // 3. 불일치 체크
    if(pw1 !== pw2) {
        errorEl.innerText = "비밀번호가 일치하지 않습니다.";
        errorEl.style.color = "#EA4335";
        errorEl.style.display = 'block';
        alert("비밀번호 확인란을 다시 입력해 주세요.");
        return;
    }

    // 모든 검사 통과 -> 전송
    alert("비밀번호가 변경되었습니다. 다시 로그인해 주세요.");
    document.getElementById('changePwForm').submit();
}