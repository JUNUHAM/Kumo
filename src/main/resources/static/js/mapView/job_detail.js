/**
 * job_detail.js
 * - HTML에서 선언된 'isUserLoggedIn'과 'MESSAGES' 객체를 바로 사용합니다!
 * - 삼항 연산자와 URL 파라미터 파싱 로직은 완전히 삭제되었습니다.
 */

// =========================================
// [기능 1] 신고 모달 관련 로직
// =========================================
function openReportModal() {
    if (!isUserLoggedIn) {
        if (confirm(MESSAGES.loginRequired)) location.href = '/login';
        return;
    }
    document.getElementById('reportModal').style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

function closeReportModal() {
    document.getElementById('reportModal').style.display = 'none';
    document.body.style.overflow = 'auto';
    document.getElementById('reportType').value = "";
    document.getElementById('reportDetail').value = "";
}

function submitReport() {
    const type = document.getElementById('reportType').value;
    const detail = document.getElementById('reportDetail').value;

    // 리소스에서 가져온 메시지 사용
    if (!type) {
        alert(MESSAGES.selectReportType);
        return;
    }

    const applyBtn = document.querySelector('.btn-apply');
    const targetId = applyBtn ? applyBtn.getAttribute('data-id') : null;
    const targetSource = applyBtn ? applyBtn.getAttribute('data-source') : null;

    if (!targetId) {
        alert("Error: ID Not Found");
        return;
    }

    if (confirm(MESSAGES.confirmReport)) {
        const reportData = {
            targetPostId: targetId,
            targetSource: targetSource,
            reasonCategory: type,
            description: detail
        };

        fetch('/map/api/reports', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(reportData)
        })
            .then(response => {
                if (response.ok) {
                    alert(MESSAGES.reportSuccess);
                    closeReportModal();
                } else if (response.status === 401) {
                    if(confirm(MESSAGES.loginRequired)) location.href = '/login';
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
// [기능 2] 구인 신청 관련 로직
// =========================================
function applyForJob(btnElement) {
    if (!isUserLoggedIn) {
        if (confirm(MESSAGES.loginRequired)) location.href = '/login';
        return;
    }
    // 리소스에서 가져온 메시지 사용
    alert(MESSAGES.applyReady);
}

// ==========================================
// [기능 3] 즐겨찾기 (스크랩) 기능 구현
// ==========================================
function toggleScrap(btnElement) {
    if (!isUserLoggedIn) {
        if (confirm(MESSAGES.loginRequired)) location.href = '/login';
        return;
    }

    const $btn = $(btnElement);
    const jobId = $btn.data('id');
    const $svg = $btn.find('svg');

    $.ajax({
        url: '/api/scraps',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ targetPostId: jobId }),
        success: function(response) {
            if (response.isScraped) {
                $svg.attr('fill', '#4285F4').attr('stroke', '#4285F4');
            } else {
                $svg.attr('fill', 'none').attr('stroke', '#999');
            }
        },
        error: function(xhr) {
            if (xhr.status === 401) {
                if (confirm(MESSAGES.loginRequired)) location.href = '/login';
            } else {
                // 리소스에서 가져온 메시지 사용
                alert(MESSAGES.processError);
            }
        }
    });
}