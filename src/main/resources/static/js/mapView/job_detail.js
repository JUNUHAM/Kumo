/**
 * job_detail.js
 * - HTML에서 선언된 'isUserLoggedIn' 변수를 사용합니다.
 */

// 1. 현재 언어 설정 가져오기 (URL 파라미터 기준)
const urlParams = new URLSearchParams(window.location.search);
const currentLang = urlParams.get('lang') === 'ja' ? 'ja' : 'ko';

// 2. 메시지 상수 정의
const MESSAGES = {
    loginRequired: currentLang === 'jp' ? 'ログインが必要です。\nログインページに移動しますか？' : '로그인이 필요한 서비스입니다.\n로그인 페이지로 이동하시겠습니까?',
    reportSuccess: currentLang === 'jp' ? '申告が正常に受け付けられました。' : '신고가 정상적으로 접수되었습니다.',
    reportError: currentLang === 'jp' ? 'エラーが発生しました。' : '오류가 발생했습니다.',
    confirmReport: currentLang === 'jp' ? '本当にこの投稿を申告しますか？' : '정말로 이 게시글을 신고하시겠습니까?\n허위 신고 시 불이익을 받을 수 있습니다.'
};

// =========================================
// 🌟 [기능 0] 페이지 로드 시 다크모드 상태 동기화 (jQuery 버전)
// =========================================
$(function() {
    // 테마가 'dark'면 true, 아니면 false
    const isDark = localStorage.getItem('theme') === 'dark';

    // true면 클래스 추가, false면 클래스 제거를 알아서 해줌!
    $('body').toggleClass('dark-mode', isDark);
});

// =========================================
// [기능 1] 신고 모달 관련 로직
// =========================================

/**
 * 모달 열기
 * - 버튼 클릭 시 '가장 먼저' 실행됨
 * - 로그인 안 되어 있으면 모달 안 띄우고 로그인 페이지 이동 유도
 */
function openReportModal() {
    // HTML(Thymeleaf)에서 넘겨준 전역 변수 확인
    if (!isUserLoggedIn) {
        if (confirm(MESSAGES.loginRequired)) {
            // 로그인 컨트롤러 경로
            location.href = '/login';
        }
        return; // 함수 종료 (모달 안 뜸)
    }

    // 로그인 된 상태라면 모달 표시
    document.getElementById('reportModal').style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

function closeReportModal() {
    document.getElementById('reportModal').style.display = 'none';
    document.body.style.overflow = 'auto';

    // 입력값 초기화
    document.getElementById('reportType').value = "";
    document.getElementById('reportDetail').value = "";
}

function submitReport() {
    const typeSelect = document.getElementById('reportType');
    const detailInput = document.getElementById('reportDetail');
    const type = typeSelect.value;
    const detail = detailInput.value;

    // 1. 유효성 검사
    if (!type) {
        alert(currentLang === 'jp' ? "申告の種類を選択してください。" : "신고 종류를 선택해주세요.");
        return;
    }

    // 2. 공고 ID와 Source 가져오기 (지원하기 버튼의 dataset 활용)
    const applyBtn = document.querySelector('.btn-apply');
    const targetId = applyBtn ? applyBtn.getAttribute('data-id') : null;
    const targetSource = applyBtn ? applyBtn.getAttribute('data-source') : null;

    if (!targetId) {
        alert("Error: ID Not Found");
        return;
    }

    // 3. 전송
    if (confirm(MESSAGES.confirmReport)) {

        const reportData = {
            targetPostId: targetId,
            targetSource: targetSource,
            reasonCategory: type,
            description: detail
        };

        // ★ Controller의 @PostMapping("/api/reports") 호출
        // MapController 클래스 매핑("map") + 메소드 매핑("/api/reports") = "/map/api/reports"
        fetch('/map/api/reports', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(reportData)
        })
            .then(response => {
                if (response.ok) {
                    alert(MESSAGES.reportSuccess);
                    closeReportModal();
                } else if (response.status === 401) {
                    // 세션 만료 등의 이유로 401이 뜬 경우
                    if(confirm(MESSAGES.loginRequired)) {
                        location.href = '/login';
                    }
                } else {
                    return response.text().then(text => { throw new Error(text) });
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert(MESSAGES.reportError);
            });
    }
}

// =========================================
// [기능 2] 구인 신청 관련 로직 (기존 유지)
// =========================================

function applyForJob(btnElement) {
    // 1. 여기서도 로그인 체크를 한 번 더 할 수 있음
    if (!isUserLoggedIn) {
        if (confirm(MESSAGES.loginRequired)) {
            location.href = '/login';
        }
        return;
    }

    // ... (기존 지원하기 로직 구현) ...
    alert("지원 기능은 준비 중입니다.");
}

// ==========================================
// [기능 3] 즐겨찾기 (스크랩) 기능 구현
// ==========================================

function toggleScrap(btnElement) {
    // 1. 로그인 체크
    if (!isUserLoggedIn) {
        if (confirm(MESSAGES.loginRequired)) {
            location.href = '/login';
        }
        return;
    }

    // 2. jQuery 객체로 변환하여 데이터 및 SVG 태그 찾기
    const $btn = $(btnElement);
    const jobId = $btn.data('id');
    const $svg = $btn.find('svg');

    // 3. AJAX 통신
    $.ajax({
        url: '/api/scraps',            // 백엔드 API 주소 (필요시 /map/api/scraps 로 변경)
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ targetPostId: jobId }),
        success: function(response) {
            // 성공 시 백엔드에서 내려주는 결과(true/false)에 따라 즉시 색상 변경
            if (response.isScraped) {
                // 스크랩 됨 -> 파란색으로 칠하기
                $svg.attr('fill', '#4285F4').attr('stroke', '#4285F4');
            } else {
                // 스크랩 취소됨 -> 원래 회색/투명으로 되돌리기
                $svg.attr('fill', 'none').attr('stroke', '#999');
            }
        },
        error: function(xhr) {
            if (xhr.status === 401) {
                // 세션 만료 등의 이유로 비로그인 취급 시
                if (confirm(MESSAGES.loginRequired)) {
                    location.href = '/login';
                }
            } else {
                alert(currentLang === 'ja' ? '処理中にエラーが発生しました。' : '처리 중 오류가 발생했습니다.');
            }
        }
    });
}