/* ==========================================
   KUMO 구직자 회원가입 Logic (Updated)
   ========================================== */

const regexPatterns = {
    email: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
    password: /^(?=.*[A-Za-z])(?=.*\d)(?=.*[$@$!%*#?&])[A-Za-z\d$@$!%*#?&]{8,16}$/,
    name_kanji: /^[一-龥ぁ-んァ-ヶー々〆〤]+$/,
    name_kana: /^[ァ-ヶー]+$/,
    contact: /^0\d{1,4}-\d{1,4}-\d{4}$/
};

/* ==========================================
   이벤트 리스너 등록
   ========================================== */
document.addEventListener('DOMContentLoaded', () => {

    // 1. 인증 해제 리스너 (이메일, 닉네임 수정 시 다시 중복확인 하도록)
    const emailInput = document.getElementById('email');
    if(emailInput) {
        emailInput.addEventListener('input', () => {
            document.getElementById('emailChecked').value = "false";
            document.getElementById('error_email').style.display = 'none';
        });
    }

    const nicknameInput = document.getElementById('nickname');
    if(nicknameInput) {
        nicknameInput.addEventListener('input', () => {
            document.getElementById('nicknameChecked').value = "false";
            document.getElementById('error_nickname').style.display = 'none';
        });
    }

    // 2. 주소 재검색 리스너 (상세주소 입력 후 좌표 갱신)
    const addrDetailInput = document.getElementById('address_detail');
    if(addrDetailInput) {
        addrDetailInput.addEventListener('blur', function() {
            const mainAddr = document.getElementById('address_main').value;
            const detailAddr = this.value;
            if(mainAddr) getGeocode(mainAddr + " " + detailAddr);
        });
    }

    // 3. 약관 전체 동의
    const checkAll = document.getElementById('checkAll');
    if(checkAll) {
        checkAll.addEventListener('change', function() {
            const checkboxes = document.querySelectorAll('.terms-box input[type="checkbox"]');
            checkboxes.forEach(cb => cb.checked = this.checked);
        });
    }
});

/* ==========================================
   기능 함수들 (주소찾기, 중복확인 등)
   ========================================== */

function searchAddress() {
    const zipcode = document.getElementById('zipcode').value;
    const errorEl = document.getElementById('error_zipcode');

    if (!zipcode || zipcode.length < 7) {
        errorEl.innerText = errorMessages.zipcode_check;
        errorEl.style.display = 'block';
        return;
    }
    errorEl.style.display = 'none';

    fetch(`https://zipcloud.ibsnet.co.jp/api/search?zipcode=${zipcode}`)
        .then(response => response.json())
        .then(data => {
            if (data.status === 200 && data.results) {
                const result = data.results[0];
                document.getElementById('addr_prefecture').value = result.address1;
                document.getElementById('addr_city').value = result.address2;
                document.getElementById('addr_town').value = result.address3;

                const fullAddress = result.address1 + result.address2 + result.address3;
                document.getElementById('address_main').value = fullAddress;
                getGeocode(fullAddress);

                document.getElementById('address_detail').focus();
            } else {
                errorEl.innerText = errorMessages.search_fail;
                errorEl.style.display = 'block';
            }
        })
        .catch(() => {
            errorEl.innerText = errorMessages.search_fail;
            errorEl.style.display = 'block';
        });
}

function getGeocode(address) {
    if(!window.google || !window.google.maps) return; // 구글맵 로드 안됐으면 패스

    const geocoder = new google.maps.Geocoder();
    geocoder.geocode({ 'address': address }, function(results, status) {
        if (status === 'OK') {
            const loc = results[0].geometry.location;
            document.getElementById('latitude').value = loc.lat();
            document.getElementById('longitude').value = loc.lng();

            // 주소 구성 요소 파싱 및 설정 로직 추가
            const addressComponents = results[0].address_components;
            let prefecture = '';
            let city = '';
            let town = '';

            for (let i = 0; i < addressComponents.length; i++) {
                const component = addressComponents[i];
                const types = component.types;

                if (types.includes('administrative_area_level_1')) { // 도/현 (e.g., Tokyo-to)
                    prefecture = component.long_name;
                } else if (types.includes('locality')) { // 시/구 (e.g., Shinjuku-ku)
                    city = component.long_name;
                } else if (types.includes('sublocality_level_1')) { // 동/읍 (e.g., Nishishinjuku)
                    town = component.long_name;
                } else if (types.includes('political') && types.includes('sublocality')) { // 더 일반적인 동/읍
                    if (!town) town = component.long_name; // sublocality_level_1이 없으면 이것 사용
                }
            }

            document.getElementById('addr_prefecture').value = prefecture;
            document.getElementById('addr_city').value = city;
            document.getElementById('addr_town').value = town;

        } else {
            console.error('Geocode was not successful for the following reason: ' + status);
        }
    });
}

// 이메일 중복확인
function checkEmail() {
    const emailInput = document.getElementById('email');
    const email = emailInput.value.trim();

    // 1. 입력값 검증
    if (!email) {
        showError('email', 'error_email', errorMessages.email_empty);
        return;
    }
    if (!regexPatterns.email.test(email)) {
        showError('email', 'error_email', errorMessages.email_invalid);
        return;
    }

    // 2. 서버 통신 (POST)
    fetch('/api/check/email', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: email })
    })
    .then(response => {
        if (!response.ok) throw new Error('Network response was not ok');
        return response.json();
    })
    .then(isDuplicate => {
        const errorEl = document.getElementById('error_email');

        if (isDuplicate) {
            // ★ 수정됨: 다국어 변수 사용
            errorEl.innerText = errorMessages.email_duplicate;
            errorEl.style.color = '#EA4335';
            errorEl.style.display = 'block';
            document.getElementById('emailChecked').value = "false";
            emailInput.focus();
        } else {
            errorEl.innerText = errorMessages.success_email;
            errorEl.style.color = "#4285F4";
            errorEl.style.display = 'block';
            document.getElementById('emailChecked').value = "true";
        }
    })
    .catch(error => {
        console.error('Error:', error);
    });
}

// 닉네임 중복확인
function checkNickname() {
    const nicknameInput = document.getElementById('nickname');
    const nickname = nicknameInput.value.trim();

    if (!nickname) {
        showError('nickname', 'error_nickname', errorMessages.nickname);
        return;
    }

    fetch('/api/check/nickname', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nickname: nickname })
    })
    .then(response => {
        if (!response.ok) throw new Error('Network response was not ok');
        return response.json();
    })
    .then(isDuplicate => {
        const errorEl = document.getElementById('error_nickname');

        if (isDuplicate) {
            // ★ 수정됨: 다국어 변수 사용
            errorEl.innerText = errorMessages.nickname_duplicate;
            errorEl.style.color = '#EA4335';
            errorEl.style.display = 'block';
            document.getElementById('nicknameChecked').value = "false";
            nicknameInput.focus();
        } else {
            errorEl.innerText = errorMessages.success_nickname;
            errorEl.style.color = "#4285F4";
            errorEl.style.display = 'block';
            document.getElementById('nicknameChecked').value = "true";
        }
    })
    .catch(error => {
        console.error('Error:', error);
    });
}

// 에러 메시지 표시 및 포커스 이동
function showError(inputId, errorId, msg) {
    const errorEl = document.getElementById(errorId);
    if(errorEl) {
        errorEl.innerText = msg;
        errorEl.style.color = '#EA4335'; // 에러 빨간색
        errorEl.style.display = 'block';
    }
    if(inputId) {
        const input = document.getElementById(inputId);
        if(input) input.focus();
    }
}

// 필드 유효성 검사 함수 (값 존재 여부 & 정규식 체크)
function checkField(inputId, errorId, emptyMsg, regex = null, regexMsg = null) {
    const input = document.getElementById(inputId);
    if(!input) return false;

    const value = input.value.trim();

    if (!value) {
        showError(inputId, errorId, emptyMsg);
        return false;
    }

    if (regex && !regex.test(value)) {
        showError(inputId, errorId, regexMsg);
        return false;
    }

    return true; // 통과
}

/* ==========================================
   ★ 폼 제출 최종 검사 (순차 검증 로직 적용) ★
   ========================================== */
document.getElementById('joinForm').addEventListener('submit', function(e) {

    // 0. 기존 에러 메시지 모두 초기화 (깨끗한 상태에서 시작)
    document.querySelectorAll('.error-msg').forEach(el => {
        el.style.display = 'none';
        el.style.color = '#EA4335';
    });

    // ★ 헬퍼 함수 1: 검사 실패 시 에러 띄우고 전송 중단 (return false)
    function fail(inputId, errorId, msg) {
        showError(inputId, errorId, msg);
        e.preventDefault(); // 폼 전송 막기
        return true; // "에러 있음" 신호 리턴
    }

    // ★ 헬퍼 함수 2: checkField 실행 후 실패하면 바로 전송 중단
    function checkAndStop(inputId, errorId, emptyMsg, regex, regexMsg) {
        if (!checkField(inputId, errorId, emptyMsg, regex, regexMsg)) {
            e.preventDefault();
            return true; // "에러 있음(멈춰라)" 신호 리턴
        }
        return false; // 통과
    }

    // ----------------------------------------------------------------
    // [순차 검사 시작] 위에서부터 하나라도 걸리면 즉시 return 하여 멈춤
    // ----------------------------------------------------------------

    // 1. 이름 검사 (한자 성 -> 한자 이름 -> 가나 성 -> 가나 이름)
    if (checkAndStop('name_kanji_sei', 'error_name_kanji_sei', errorMessages.name_kanji_sei, regexPatterns.name_kanji, errorMessages.regex_kanji)) return;
    if (checkAndStop('name_kanji_mei', 'error_name_kanji_mei', errorMessages.name_kanji_mei, regexPatterns.name_kanji, errorMessages.regex_kanji)) return;
    if (checkAndStop('name_kana_sei', 'error_name_kana_sei', errorMessages.name_kana_sei, regexPatterns.name_kana, errorMessages.regex_kana)) return;
    if (checkAndStop('name_kana_mei', 'error_name_kana_mei', errorMessages.name_kana_mei, regexPatterns.name_kana, errorMessages.regex_kana)) return;

    // 2. 닉네임 검사
           if (checkAndStop('nickname', 'error_nickname', errorMessages.nickname)) return;
           if (document.getElementById('nicknameChecked').value !== "true") {
               // ★ 수정됨: 하드코딩 -> 변수 사용
               return fail('nickname', 'error_nickname', errorMessages.check_dup);
           }


    // 3. 생년월일 검사
    const y = document.getElementById('birth_year').value;
    const m = document.getElementById('birth_month').value;
    const d = document.getElementById('birth_day').value;
    if (!y || !m || !d) {
        return fail('birth_year', 'error_birth', errorMessages.birth);
    }

    // 4. 주소 검사
    if (checkAndStop('zipcode', 'error_zipcode', errorMessages.zipcode)) return;
    if (checkAndStop('address_detail', 'error_address_detail', errorMessages.address_detail)) return;

    // 5. 전화번호 검사
    if (checkAndStop('contact', 'error_contact', errorMessages.contact_empty, regexPatterns.contact, errorMessages.contact_invalid)) return;

     // 6. 이메일 검사
            if (checkAndStop('email', 'error_email', errorMessages.email_empty, regexPatterns.email, errorMessages.email_invalid)) return;
            if (document.getElementById('emailChecked').value !== "true") {
                // ★ 수정됨: 하드코딩 -> 변수 사용
                return fail('email', 'error_email', errorMessages.check_dup);
            }

    // 7. 비밀번호 검사
    if (checkAndStop('password', 'error_password', errorMessages.pw_empty, regexPatterns.password, errorMessages.pw_invalid)) return;

    // 비밀번호 확인 일치 검사
    const pw = document.getElementById('password').value;
    const pwConf = document.getElementById('password_confirm').value;
    if (pw !== pwConf) {
        return fail('password_confirm', 'error_password_confirm', errorMessages.pw_mismatch);
    }

    // 8. 가입경로 검사
    if (checkAndStop('join_path', 'error_join_path', errorMessages.joinpath)) return;

    // 9. 약관 동의 검사 (5가지 필수 항목 체크)
    const requiredTerms = [
        'term_age',      // 나이 (필수)
        'term_service',  // 서비스 이용약관 (필수)
        'term_privacy',  // 개인정보 (필수)
        'term_location', // 위치기반 (필수)
        'term_provision' // 제3자 제공 (필수)
    ];

    for (const id of requiredTerms) {
        const checkbox = document.getElementById(id);
        if (!checkbox || !checkbox.checked) {
            // 약관은 input 포커스가 애매하므로 null 전달, 에러 메시지만 표시
            return fail(null, 'error_terms', errorMessages.terms);
        }
    }

    // 여기까지 도달하면 모든 검사 통과! -> 자동으로 form submit 발생
});