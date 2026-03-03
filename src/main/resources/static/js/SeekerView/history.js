/* history.js */
document.addEventListener('DOMContentLoaded', () => {
    // 지원 취소 버튼 이벤트 리스너
    const cancelButtons = document.querySelectorAll('.btn-cancel-app');
    
    cancelButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            const appId = this.getAttribute('data-id');
            const row = this.closest('tr');

            if (!confirm("지원을 취소하시겠습니까?")) return;

            // 🌟 Vanilla JS Fetch API 사용
            fetch('/Seeker/history/cancel', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `appId=${appId}`
            })
            .then(response => response.text())
            .then(res => {
                if (res === "success") {
                    // 삭제 성공 시 행 제거 애니메이션
                    row.style.transition = 'opacity 0.3s, transform 0.3s';
                    row.style.opacity = '0';
                    row.style.transform = 'translateX(20px)';
                    
                    setTimeout(() => {
                        row.remove();
                        // 모든 행이 삭제되었는지 확인
                        const remainingRows = document.querySelectorAll('tbody tr');
                        if (remainingRows.length === 0) {
                            location.reload(); // 마지막 항목이면 새로고침하여 빈 상태 메시지 노출
                        }
                    }, 300);
                } else {
                    alert("취소 중 오류가 발생했습니다.");
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert("서버와 통신 중 오류가 발생했습니다.");
            });
        });
    });
});
