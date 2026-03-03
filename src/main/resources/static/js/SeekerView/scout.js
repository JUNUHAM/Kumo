/* scout.js */
document.addEventListener('DOMContentLoaded', () => {
    // 삭제 버튼 이벤트 리스너 등록
    const deleteButtons = document.querySelectorAll('.btn-delete-scout');
    
    deleteButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            const scoutId = this.getAttribute('data-id');
            const card = this.closest('.scout-card');

            if (!confirm("이 제안을 목록에서 삭제하시겠습니까?")) return;

            // 🌟 Vanilla JS Fetch API 사용
            fetch('/Seeker/scout/delete', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `scoutId=${scoutId}`
            })
            .then(response => {
                if (response.ok) {
                    // 삭제 성공 시 애니메이션 후 제거
                    card.style.opacity = '0';
                    card.style.transform = 'scale(0.95)';
                    setTimeout(() => {
                        card.remove();
                        // 목록이 비었는지 확인
                        const remainingCards = document.querySelectorAll('.scout-card');
                        if (remainingCards.length === 0) {
                            const list = document.querySelector('.scout-list');
                            list.innerHTML = '<div class="text-center py-5"><p class="text-muted">받은 스카우트 제의가 없습니다.</p></div>';
                        }
                    }, 300);
                } else {
                    alert("삭제 중 오류가 발생했습니다.");
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert("서버와 통신 중 오류가 발생했습니다.");
            });
        });
    });
});
