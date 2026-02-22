document.addEventListener("DOMContentLoaded", function() {
    const modal = document.getElementById('profileModal');
    const btnOpenModal = document.getElementById('btnOpenModal');
    const btnCloseX = document.querySelector('.close-btn');
    const btnCancel = document.querySelector('.btn-cancel');
    const btnSave = document.getElementById('btnSaveImage');

    const fileInput = document.getElementById('fileInput');
    const modalPreview = document.getElementById('modalPreview');
    const currentProfileImg = document.getElementById('currentProfileImg');
    const fileNameSpan = document.getElementById('fileName');

    // [1] 모달 열기 & 초기화
    if(btnOpenModal) {
        btnOpenModal.addEventListener('click', function() {
            modal.classList.add('show');
            if(currentProfileImg && modalPreview) modalPreview.src = currentProfileImg.src;
            fileInput.value = '';
            if(fileNameSpan) {
                fileNameSpan.innerText = msg.fileNone; // "ファイルが選択されていません"
                fileNameSpan.style.color = "#888";
            }
        });
    }

    const closeModal = () => modal.classList.remove('show');
    if(btnCloseX) btnCloseX.addEventListener('click', closeModal);
    if(btnCancel) btnCancel.addEventListener('click', closeModal);

    // [3] 파일 선택 시 (미리보기 & 파일명 표시)
    if(fileInput) {
        fileInput.addEventListener('change', function(e) {
            const file = e.target.files[0];
            if (file) {
                if(fileNameSpan) {
                    fileNameSpan.innerText = file.name;
                    fileNameSpan.style.color = "#333";
                }
                const reader = new FileReader();
                reader.onload = function(evt) {
                    if(modalPreview) modalPreview.src = evt.target.result;
                };
                reader.readAsDataURL(file);
            }
        });
    }

    // [4] 서버 전송 (저장 버튼)
    if(btnSave) {
        btnSave.addEventListener('click', function() {
            if (!fileInput.files[0]) {
                alert(msg.selectPhoto); // "変更する写真を選択してください。"
                return;
            }

            const formData = new FormData();
            formData.append("profileImage", fileInput.files[0]);

            fetch('/api/profileImage', {
                method: 'POST',
                body: formData
            })
            .then(response => {
                if (response.ok) return response.text();
                throw new Error('FAILED');
            })
            .then(newImageUrl => {
                alert(msg.uploadSuccess); // "プロフィール写真が変更されました。"
                if(newImageUrl && currentProfileImg) currentProfileImg.src = newImageUrl;
                closeModal();
            })
            .catch(err => alert(msg.error + err.message)); // "エラーが発生しました: ..."
        });
    }

    // [5] 소셜 연동 알림 (LINE, Google)
    document.querySelectorAll('.switch').forEach(toggle => {
        toggle.addEventListener('click', (e) => {
            e.preventDefault();
            alert(msg.notService); // "現在準備中のサービスです。"
        });
    });
});