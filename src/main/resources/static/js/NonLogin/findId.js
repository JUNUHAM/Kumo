/* 전역 변수 */
let currentRole = 'SEEKER';

/* 1. 역할 선택 */
function selectRole(role) {
    currentRole = role;

    const tabs = document.querySelectorAll('.tab-btn');
    tabs.forEach(btn => btn.classList.remove('active'));

    if(role === 'SEEKER') {
        tabs[0].classList.add('active');
    } else {
        tabs[1].classList.add('active');
    }

    document.getElementById('roleInput').value = role;
    resetDisplay();
}

/* 2. 화면 초기화 */
function resetDisplay() {
    document.getElementById('result-box').style.display = 'none';
    document.getElementById('error-msg').style.display = 'none';
    document.getElementById('name').value = '';
    document.getElementById('contact').value = '';
}

/* 3. 자동 하이픈 */
function autoHyphen(target) {
    target.value = target.value
        .replace(/[^0-9]/g, '')
        .replace(/^(\d{0,3})(\d{0,4})(\d{0,4})$/g, "$1-$2-$3")
        .replace(/(\-{1,2})$/g, "");
}

/* 4. 아이디 찾기 (AJAX) */
function findId() {
    const name = document.getElementById('name').value.trim();
    const contact = document.getElementById('contact').value.trim();

    if (!name || !contact) {
        alert('이름과 연락처를 모두 입력해주세요.');
        return;
    }

    const requestData = {
        name: name,
        contact: contact,
        role: currentRole
    };

    fetch('/api/findId', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestData)
    })
    .then(response => response.json())
    .then(data => {
        const resultBox = document.getElementById('result-box');
        const errorMsg = document.getElementById('error-msg');

        if (data.status === 'success') {
            errorMsg.style.display = 'none';
            resultBox.style.display = 'block';

            // 데이터 뿌리기
            document.getElementById('found-email').innerText = data.email;
        } else {
            resultBox.style.display = 'none';
            errorMsg.style.display = 'block';
            document.getElementById('error-text-content').innerText = data.message || "일치하는 정보가 없습니다.";
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('서버 통신 중 오류가 발생했습니다.');
    });
}

