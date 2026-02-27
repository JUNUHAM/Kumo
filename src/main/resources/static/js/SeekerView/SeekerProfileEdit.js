/**
 * SeekerProfileEdit.js
 * * [의존성]
 * 1. 이 파일은 HTML에서 정의된 전역 변수 `msgs` 객체에 의존합니다.
 * 2. Google Maps API가 로드되어 있어야 작동합니다.
 */

document.addEventListener('DOMContentLoaded', () => {
    initNicknameEvents();
    initAddressEvents();
    initFormSubmit();
});

// =========================================
// 1. 닉네임 관련 로직
// =========================================
function initNicknameEvents() {
    const nicknameInput = document.getElementById('nickname');
    const btnCheck = document.getElementById('btnCheckNickname');
    const errorEl = document.getElementById('error_nickname');
    const checkStatus = document.getElementById('nicknameChecked');

    // 입력 시 중복확인 상태 초기화
    if (nicknameInput) {
        nicknameInput.addEventListener('input', () => {
            checkStatus.value = "false";
            errorEl.style.display = 'none';
            errorEl.innerText = '';
        });
    }

    // 중복확인 버튼 클릭
    if (btnCheck) {
        btnCheck.addEventListener('click', () => {
            const nickname = nicknameInput.value.trim();

            if (!nickname) {
                showError(errorEl, msgs.nickname_empty, true);
                return;
            }

            fetch('/api/check/nickname', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ nickname })
            })
            .then(res => {
                if (!res.ok) throw new Error('Network response was not ok');
                return res.json();
            })
            .then(isDuplicate => {
                if (isDuplicate) {
                    showError(errorEl, msgs.nickname_duplicate, true);
                    checkStatus.value = "false";
                    nicknameInput.focus();
                } else {
                    showError(errorEl, msgs.nickname_ok, false); // false는 파란색(성공)
                    checkStatus.value = "true";
                }
            })
            .catch(err => {
                console.error('Error:', err);
                alert(msgs.network_error);
            });
        });
    }
}

// =========================================
// 2. 주소 및 좌표(Google Maps) 로직
// =========================================
function initAddressEvents() {
    const btnSearch = document.getElementById('btnSearchAddress');
    const detailInput = document.getElementById('address_detail');

    // 주소 검색 버튼 클릭
    if (btnSearch) {
        btnSearch.addEventListener('click', searchAddressFromZipcloud);
    }

    // 상세주소 입력 후 포커스 아웃 시 -> 전체 주소로 좌표 정밀 갱신
    if (detailInput) {
        detailInput.addEventListener('blur', function() {
            const mainAddr = document.getElementById('address_main').value;
            const detailAddr = this.value;
            if (mainAddr) {
                getGeocode(mainAddr + " " + detailAddr);
            }
        });
    }
}

// Zipcloud API 호출
function searchAddressFromZipcloud() {
    const zipcode = document.getElementById('zipcode').value.trim();

    if (!zipcode || zipcode.length < 7) {
        alert(msgs.zipcode_empty);
        document.getElementById('zipcode').focus();
        return;
    }

    fetch(`https://zipcloud.ibsnet.co.jp/api/search?zipcode=${zipcode}`)
        .then(res => res.json())
        .then(data => {
            if (data.status === 200 && data.results) {
                const result = data.results[0];
                const fullAddress = result.address1 + result.address2 + result.address3;

                // 1. 화면 표시
                document.getElementById('address_main').value = fullAddress;

                // 2. DB 저장용 분리 데이터
                document.getElementById('addr_prefecture').value = result.address1;
                document.getElementById('addr_city').value = result.address2;
                document.getElementById('addr_town').value = result.address3;

                // 3. 좌표 추출 (Google Maps)
                getGeocode(fullAddress);

                // 4. 상세주소로 포커스 이동
                document.getElementById('address_detail').focus();
            } else {
                alert(msgs.search_fail);
            }
        })
        .catch(err => {
            console.error(err);
            alert(msgs.search_fail);
        });
}

// Google Maps Geocoding API 호출
function getGeocode(address) {
    if (!window.google || !window.google.maps) {
        console.warn("Google Maps API is not loaded.");
        return;
    }

    const geocoder = new google.maps.Geocoder();
    geocoder.geocode({ 'address': address }, function(results, status) {
        if (status === 'OK' && results[0]) {
            const loc = results[0].geometry.location;

            document.getElementById('latitude').value = loc.lat();
            document.getElementById('longitude').value = loc.lng();

            console.log(`좌표 갱신 완료: ${loc.lat()}, ${loc.lng()}`);
        } else {
            console.error('Geocode failed: ' + status);
        }
    });
}

// =========================================
// 3. 폼 제출 검증
// =========================================
function initFormSubmit() {
    const form = document.getElementById('editForm');
    if (form) {
        form.addEventListener('submit', function(e) {
            const isChecked = document.getElementById('nicknameChecked').value;

            // 닉네임 중복확인 안 했으면 차단
            if (isChecked !== "true") {
                e.preventDefault();
                const errorEl = document.getElementById('error_nickname');
                showError(errorEl, msgs.check_dup, true);
                document.getElementById('nickname').focus();
            }
        });
    }
}

// 공통 에러 메시지 표시 함수
function showError(element, message, isError) {
    element.innerText = message;
    element.style.color = isError ? '#EA4335' : '#4285F4';
    element.style.display = 'block';
}