/**
 * profileEdit.js
 * 회원정보 수정 페이지의 프론트엔드 비즈니스 로직을 담당합니다.
 * 닉네임 중복 확인, 일본 우편번호 API 연동 및 Google Maps 지오코딩 기능을 수행합니다.
 */

document.addEventListener('DOMContentLoaded', () => {
    // 1. 닉네임 변경 시 중복확인 초기화
    const nicknameInput = document.getElementById('nickname');
    if(nicknameInput) {
        nicknameInput.addEventListener('input', () => {
            document.getElementById('nicknameChecked').value = "false";
            const errorEl = document.getElementById('error_nickname');
            if(errorEl) {
                errorEl.style.display = 'none';
                errorEl.innerText = '';
            }
        });
    }

    // 2. 상세주소 입력 후 포커스 아웃 시 구글 좌표 갱신
    const addrDetailInput = document.getElementById('address_detail');
    if(addrDetailInput) {
        addrDetailInput.addEventListener('blur', function() {
            const mainAddr = document.getElementById('address_main').value;
            const detailAddr = this.value;
            if(mainAddr) {
                getGeocode(mainAddr + " " + detailAddr);
            }
        });
    }

    // 3. 폼 제출 전 중복확인 여부 최종 검사
    const editForm = document.getElementById('editForm');
    if(editForm) {
        editForm.addEventListener('submit', function(e) {
            const isChecked = document.getElementById('nicknameChecked').value;
            if (isChecked !== "true") {
                e.preventDefault();
                const errorEl = document.getElementById('error_nickname');
                if(errorEl) {
                    errorEl.innerText = msgs.check_dup;
                    errorEl.style.color = '#EA4335';
                    errorEl.style.display = 'block';
                }
                document.getElementById('nickname').focus();
            }
        });
    }
});

/**
 * 서버에 닉네임 중복 여부를 비동기로 확인합니다.
 */
function checkNickname() {
    const nicknameInput = document.getElementById('nickname');
    const nickname = nicknameInput.value.trim();
    const errorEl = document.getElementById('error_nickname');

    if (!nickname) {
        errorEl.innerText = msgs.nickname_empty;
        errorEl.style.color = '#EA4335';
        errorEl.style.display = 'block';
        return;
    }

    fetch('/api/check/nickname', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nickname: nickname })
    })
        .then(response => {
            if (!response.ok) throw new Error('Network error');
            return response.json();
        })
        .then(isDuplicate => {
            if (isDuplicate) {
                errorEl.innerText = msgs.nickname_duplicate;
                errorEl.style.color = '#EA4335';
                errorEl.style.display = 'block';
                document.getElementById('nicknameChecked').value = "false";
                nicknameInput.focus();
            } else {
                errorEl.innerText = msgs.nickname_ok;
                errorEl.style.color = "#4285F4";
                errorEl.style.display = 'block';
                document.getElementById('nicknameChecked').value = "true";
            }
        })
        .catch(error => {
            console.error('Error:', error);
        });
}

/**
 * zipcloud API를 이용해 일본 우편번호로 주소를 검색합니다.
 */
function searchAddress() {
    const zipcode = document.getElementById('zipcode').value.trim();

    if (!zipcode || zipcode.length < 7) {
        alert(msgs.zipcode_empty);
        document.getElementById('zipcode').focus();
        return;
    }

    fetch(`https://zipcloud.ibsnet.co.jp/api/search?zipcode=${zipcode}`)
        .then(response => response.json())
        .then(data => {
            if (data.status === 200 && data.results) {
                const result = data.results[0];
                const fullAddress = result.address1 + result.address2 + result.address3;

                // 기본 주소 입력
                document.getElementById('address_main').value = fullAddress;

                // 행정구역 히든 필드 주입
                document.getElementById('addr_prefecture').value = result.address1;
                document.getElementById('addr_city').value = result.address2;
                document.getElementById('addr_town').value = result.address3;

                // 구글맵 위도/경도 추출
                getGeocode(fullAddress);

                document.getElementById('address_detail').focus();
            } else {
                alert(msgs.search_fail);
            }
        })
        .catch(() => {
            alert(msgs.search_fail);
        });
}

/**
 * Google Maps Geocoder를 사용하여 텍스트 주소를 위도/경도 좌표로 변환합니다.
 * @param {string} address 변환할 주소 문자열
 */
function getGeocode(address) {
    if(!window.google || !window.google.maps) {
        console.warn("구글 맵 API가 로드되지 않았습니다.");
        return;
    }

    const geocoder = new google.maps.Geocoder();
    geocoder.geocode({
        'address': address,
        'language': window.CURRENT_LANG // 전역 변수에서 가져온 언어 설정 적용
    }, function(results, status) {
        if (status === 'OK') {
            const loc = results[0].geometry.location;
            document.getElementById('latitude').value = loc.lat();
            document.getElementById('longitude').value = loc.lng();
            console.log("좌표 추출 완료:", loc.lat(), loc.lng());
        } else {
            console.error('지오코딩 실패: ' + status);
        }
    });
}