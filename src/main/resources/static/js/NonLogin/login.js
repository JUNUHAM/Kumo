/* ==========================================
   KUMO 로그인 페이지 Logic (AJAX 통합)
   ========================================== */

/**
 * SNS 로그인 버튼 클릭 시 알림창
 */
function alertSns() {
    if (typeof loginMessages !== 'undefined' && loginMessages.sns_alert) {
        alert(loginMessages.sns_alert);
    } else {
        alert("서비스 준비 중입니다.");
    }
}

document.addEventListener("DOMContentLoaded", function() {

    const loginForm = document.querySelector('form');
    const errorMsgBox = document.querySelector('.login-error-msg');
    const inputs = document.querySelectorAll('.custom-input');
    // ★ 캡차 영역을 감싸는 div (HTML에 추가 필요)
    const captchaArea = document.getElementById('captchaArea');

    // 1. [핵심] 로그인 폼 제출 시 비동기(AJAX) 처리
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();

            const formData = new URLSearchParams(new FormData(loginForm));

            $.ajax({
                url: loginForm.getAttribute('action'),
                type: 'POST',
                data: formData.toString(),
                contentType: 'application/x-www-form-urlencoded',

                success: function(response) {
                    // 성공 시 메인으로 이동
                    window.location.href = '/';
                },

                error: function(xhr) {
                    const response = xhr.responseJSON;

                    if (errorMsgBox) {
                        const errorText = errorMsgBox.querySelector('span');
                        if (response && response.message && errorText) {
                            // 서버에서 보내준 다국어 메시지(KR/JP)를 그대로 출력
                            errorText.textContent = response.message;
                        }
                        errorMsgBox.style.display = 'flex';

                        // ★ 캡차 노출 로직: 5회 이상 실패 시
                        if (response && response.showCaptcha) {
                            if (captchaArea) {
                                // 숨겨져 있던 캡차 영역을 보여줌
                                captchaArea.style.display = 'block';
                                console.warn("보안 인증(CAPTCHA)이 활성화되었습니다.");
                            }
                        }
                    }

                    // 비밀번호 입력창 초기화 및 포커스
                    const pwInput = document.querySelector('input[name="password"]');
                    if (pwInput) {
                        pwInput.value = '';
                        pwInput.focus();
                    }
                }
            });
        });
    }

    // 2. 사용자가 입력을 시작하면 에러 메시지 즉시 숨기기
    if (errorMsgBox) {
        inputs.forEach(input => {
            input.addEventListener('input', function() {
                errorMsgBox.style.display = 'none';
            });
        });
    }

    // 3. URL 파라미터 정리
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('error')) {
        const newUrl = window.location.pathname;
        window.history.replaceState({}, document.title, newUrl);
    }
});