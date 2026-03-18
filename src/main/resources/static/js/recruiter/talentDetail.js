/**
 * talentDetail.js
 * 인재 상세 정보 조회 페이지의 프론트엔드 비즈니스 로직을 담당합니다.
 * 서버로부터 전달받은 성공/에러 메시지 출력 및 스카우트 제안 전 컨펌 창(SweetAlert2) 기능을 제어합니다.
 */

document.addEventListener('DOMContentLoaded', function () {
    // 1. 현재 테마(Dark/Light) 상태에 따른 SweetAlert2 공통 스타일 정의
    const isDark = document.body.classList.contains('dark-mode');
    const themeOptions = {
        background: isDark ? '#2a2b2e' : '#fff',
        color: isDark ? '#e3e5e8' : '#333'
    };

    // 2. 서버에서 전달된 성공 메시지 처리
    if (talentMsg.successMsg) {
        Swal.fire({
            icon: 'success',
            title: talentMsg.successMsg,
            confirmButtonColor: '#7db4e6',
            ...themeOptions
        });
    }

    // 3. 서버에서 전달된 에러 메시지 처리
    if (talentMsg.errorMsg) {
        Swal.fire({
            icon: 'error',
            title: talentMsg.errorMsg,
            confirmButtonColor: '#7db4e6',
            ...themeOptions
        });
    }
});

/**
 * 인재에게 스카우트 제안을 보내기 전 최종 확인 창을 띄웁니다.
 * 승인 시 해당 인재의 ID를 파라미터로 하여 제안 전송 주소로 이동합니다.
 * * @param {HTMLElement} btn 클릭된 스카우트 제안 버튼 요소
 */
function confirmOffer(btn) {
    const userId = btn.getAttribute('data-user-id');
    const isDark = document.body.classList.contains('dark-mode');
    const themeOptions = {
        background: isDark ? '#2a2b2e' : '#fff',
        color: isDark ? '#e3e5e8' : '#333'
    };

    Swal.fire({
        icon: 'question',
        title: talentMsg.offerConfirm,
        showCancelButton: true,
        confirmButtonColor: '#7db4e6',
        cancelButtonColor: '#aaa',
        confirmButtonText: talentMsg.offerConfirmBtn,
        width: '380px',
        ...themeOptions
    }).then((result) => {
        if (result.isConfirmed) {
            // 스카우트 제안 실행 페이지로 이동
            location.href = '/Recruiter/sendOffer?userId=' + userId;
        }
    });
}