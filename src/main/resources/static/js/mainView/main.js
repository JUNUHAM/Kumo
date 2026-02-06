// 지도 생성
let map;
let jobMarkers = []; // 직업 마커 보관 배열
let markerTimeouts = [] // 애니메이션을 위한 타이머들을 받아둘 배열

// 🌟 [추가] 자원 관리용 변수
let debounceTimer;        // 딜레이 타이머
let abortController = null; // 네트워크 요청 취소용 컨트롤러

// Jquery를 사용하면 오히려 번거롭기 때문에 Vanilla JS를 사용하여 map 구축
function initMap() {
    // [수정 후] 컨테이너 안에 있는 id="map" 요소를 찾음
    const mapElement = document.getElementById('map');

    if (!mapElement) return;

    const tokyo = { lat: 35.6804, lng: 139.7690 };

    map = new google.maps.Map(mapElement, {
        center: tokyo,
        zoom: 10,
        disableDefaultUI: true,
        style: [
            {"stylers" : [{"saturation":-20}]},
        ]
    });

    drawMasking();

    // 🌟 [수정] 이벤트 리스너: 디바운싱 적용
    map.addListener("idle", () => {
        // 1. 기존에 대기 중이던 타이머가 있다면 취소 (아직 유저가 지도 조작 중이라는 뜻)
        clearTimeout(debounceTimer);

        // 2. 0.5초(500ms) 뒤에 실행하도록 예약
        debounceTimer = setTimeout(() => {
            const bounds = map.getBounds();
            loadJobs(bounds);
        }, 500);
    });

    map.addListener("click", () => {
        closeJobCard();
    });
}

// HTML 문서가 다 로딩되면 실행 (jQuery의 ready 함수) <- Jquery 사용
$(function() {
    // 1. '.sheet-handle' 클래스를 가진 요소를 클릭하면
    $('.sheet-handle').on('click', function() {
        // 2. '#bottomSheet' 아이디를 가진 요소에 'active' 클래스를 줬다 뺏었다 함
        $('#bottomSheet').toggleClass('active');

        // 3. 만약 bottomSheet가 올라오면 플로팅 카드를 제거해주세용
        if($("#bottomSheet").hasClass('active')) {
            // 리스트 올라올시 카드 제거
            closeJobCard();
        }
    });
});

// 전역 등록
window.initMap = initMap;

function drawMasking() {
    // 1. 전세계를 덮는 거대한 회색 사각형 좌표 (외각선)
    const worldCoords = [
        { lat: 85, lng: -180 }, { lat: 85, lng: 0 }, { lat: 85, lng: 180 },
        { lat: -85, lng: 180 }, { lat: -85, lng: 0 }, { lat: -85, lng: -180 },
        { lat: 85, lng: -180 }
    ];

    // 2. HTML에서 로드한 GeoJSON 파일 변수가 있는지 확인하고 경로 추출
    // (파일이 없어도 에러 안 나게 빈 배열 처리)
    const tokyoPaths = typeof tokyoGeoJson !== 'undefined' ? getPathsFromGeoJson(tokyoGeoJson) : [];
    const osakaCityPaths = typeof osakaCityGeoJson !== 'undefined' ? getPathsFromGeoJson(osakaCityGeoJson) : [];
    const kansaiPaths = typeof osakaGeoJson !== 'undefined' ? getPathsFromGeoJson(osakaGeoJson, 1) : [];

    // 3. 폴리곤 그리기
    // paths의 첫 번째 배열은 '색칠할 영역(전세계)', 그 뒤의 배열들은 '구멍 뚫을 영역(도쿄,오사카)'이 됩니다.
    new google.maps.Polygon({
        paths: [worldCoords, ...tokyoPaths, ...osakaCityPaths, ...kansaiPaths],
        strokeColor: "#FF0000", // 경계선 색 (필요 없으면 투명하게)
        strokeOpacity: 0,
        strokeWeight: 0,
        fillColor: "#000000",   // 배경 색 (검정)
        fillOpacity: 0.6,       // 투명도 (0.6 정도가 적당)
        map: map,
        clickable: false        // 배경 클릭 안 되게
    });
}

// GeoJSON 데이터를 구글 맵 Path로 변환하는 헬퍼 함수
function getPathsFromGeoJson(json, specificIndex = -1) {
    const paths = [];
    if (!json) return paths;

    // FeatureCollection인지 단일 Feature인지 확인
    const features = (json.type === "FeatureCollection") ? json.features : [json];

    features.forEach(f => {
        if (!f.geometry) return;

        if (f.geometry.type === "MultiPolygon") {
            f.geometry.coordinates.forEach((polygon, index) => {
                if (specificIndex >= 0 && index !== specificIndex) return;
                // 구글 맵은 [Lng, Lat] 순서인 GeoJSON을 [Lat, Lng] 객체로 변환해야 함
                paths.push(polygon[0].map(c => ({ lat: c[1], lng: c[0] })));
            });
        } else if (f.geometry.type === "Polygon") {
            paths.push(f.geometry.coordinates[0].map(c => ({ lat: c[1], lng: c[0] })));
        }
    });
    return paths;
}

// [추가] 공고 리스트 불러오기 함수
function loadJobs(bounds) {
    const listBody = document.getElementById('listBody');
    listBody.innerHTML = '<tr><td colspan="7" class="msg-box">데이터 로딩 중...</td></tr>';

    // 1. 현재 지도의 좌표 범위(bounds) 가져오기 (없으면 URL 파라미터 사용)
    const params = new URLSearchParams();

    if (bounds) {
        // 구글 맵에서 넘어온 좌표 범위
        const ne = bounds.getNorthEast();
        const sw = bounds.getSouthWest();
        params.append('minLat', sw.lat());
        params.append('maxLat', ne.lat());
        params.append('minLng', sw.lng());
        params.append('maxLng', ne.lng());
    } else {
        // 초기 로딩 시 (URL 파라미터 등)
        const urlParams = new URLSearchParams(window.location.search);
        params.append('minLat', urlParams.get('minLat') || 0);
        // ... 필요한 값 처리
    }

    // 언어 설정 확인
    const currentLang = new URLSearchParams(window.location.search).get('lang') === 'jp' ? 'jp' : 'kr';
    updateTableHeader(currentLang); // 헤더 언어 변경

    // 🌟 [추가] 이전 네트워크 요청이 아직 살아있다면 강제 취소!
    if (abortController) {
        abortController.abort();
    }
    // 새 컨트롤러 생성
    abortController = new AbortController();
    const signal = abortController.signal;

    // 2. API 호출
    fetch(`/map/api/jobs?${params.toString()}`,{ signal: signal })
        .then(res => res.json())
        .then(data => {
            // 🌟 [2] 데이터가 도착하면 기존 마커 지우고 -> 리스트 그리기 -> 새 마커 찍기
            clearMarkers();      // 1. 지도 청소
            renderList(data, currentLang); // 2. 바텀시트 리스트 갱신
            renderMarkers(data); // 3. 지도에 마커 꽂기 (NEW!)
        })
        .catch(err => {
            if (err.name === 'AbortError') {
                console.log('이전 요청 취소됨 (정상)'); // 에러 아님
            } else {
                console.error(err);
                listBody.innerHTML = `<tr><td colspan="7" class="msg-box">데이터 로딩 실패</td></tr>`;
            }
        });
}

// [추가] 리스트 렌더링 (작성하신 코드 그대로 사용)
function renderList(jobs, lang) {
    const tbody = document.getElementById('listBody');
    if (!jobs || jobs.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="msg-box">조건에 맞는 공고가 없습니다.</td></tr>`;
        return;
    }

    let html = '';
    jobs.forEach(job => {
        // ... (작성하신 데이터 매핑 로직 그대로 복사) ...
        const title = (lang === 'jp' && job.titleJp) ? job.titleJp : job.title;
        const company = (lang === 'jp' && job.companyNameJp) ? job.companyNameJp : job.companyName;
        const wage = (lang === 'jp' && job.wageJp) ? job.wageJp : (job.wage || '협의');
        const address = job.address || '-';
        const thumb = job.thumbnailUrl || 'https://via.placeholder.com/40'; // 이미지 없으면 기본값
        const dateStr = job.writeTime || 'Recently';
        const contact = job.contactPhone || '-';

        html += `
        <tr>
            <td>
                <span class="title-text">${title}</span>
                <span class="badge bg-blue">${lang === 'jp' ? '募集中' : '구인중'}</span>
            </td>
            <td><a href="#" class="company-text">${company}</a></td>
            <td><span class="addr-text">${address}</span></td>
            <td><span class="wage-text">${wage}</span></td>
            <td style="color:#666; font-size:12px;">${contact}</td>
            <td>
                <div class="profile-wrap">
                    <img src="${thumb}" class="profile-img"
                        onerror="this.onerror=null; this.src='https://via.placeholder.com/40?text=No+Img';">
                    <div class="profile-info"><div>Admin</div><div>${dateStr}</div></div>
                </div>
            </td>
            <td>
                 <div class="btn-wrap">
                    <button class="btn btn-view" 
                        onclick="location.href='/map/jobs/detail?id=${job.id}&source=${job.source}&lang=${lang}'">
                        ${lang === 'jp' ? '詳細' : '상세'}
                    </button>
                 </div>
            </td>
        </tr>
        `;
    });
    
    tbody.innerHTML = html;
}

// 🌟 [3][수정] 마커 렌더링 함수 (방사형 애니메이션 적용)
function renderMarkers(jobs) {
    if (!jobs || jobs.length === 0) return;

    // 1. 현재 지도의 중심 좌표 가져오기
    const center = map.getCenter();
    const centerLat = center.lat();
    const centerLng = center.lng();

    // 2. 거리 계산 후 정렬 (가까운 순에서 -> 먼 순)
    const sortedJobs = jobs.map(job => {
        // 거리값 (dist) 임시 추가
        const distance = Math.pow(job.lat - centerLat, 2) + Math.pow(job.lng - centerLng, 2);
        return { ...job, _dist: distance };
    }).sort((a, b) => a._dist - b._dist); // 오름차순 정렬

    // 3. 순차적으로 마커 생성
    sortedJobs.forEach((job, index) => {
        // index가 커질수록(멀어질수록) 딜레이가 길어짐
        // 30ms 간격으로 하나씩 톡, 톡, 톡 떨어짐
        const timeoutId = setTimeout(() => {

            // DTO 유효성 체크
            if (job.lat && job.lng) {
                const marker = new google.maps.Marker({
                    position: { lat: job.lat, lng: job.lng },
                    map: map,
                    title: job.title,
                    // DROP 애니메이션을 쓰면 하늘에서 떨어지는 효과까지 더해짐
                    animation: google.maps.Animation.DROP
                });

                // 🌟 [핵심] 마커 클릭 시 '플로팅 카드' 열기
                marker.addListener("click", () => {
                    openJobCard(job); // job 데이터를 넘겨줌
                });

                jobMarkers.push(marker);
            }

        }, index * 30); // ⚡ 속도 조절: 이 숫자가 작을수록 빨리 퍼짐 (20~50 추천)

        // 타이머 ID 저장 (나중에 캔슬하기 위해)
        markerTimeouts.push(timeoutId);
    });
}

// 🌟 [수정] 마커 삭제 함수 (애니메이션 취소 기능 추가)
function clearMarkers() {
    // 1. 이미 찍힌 마커들 지도에서 제거
    jobMarkers.forEach(marker => {
        marker.setMap(null);
    });
    jobMarkers = [];

    // 2. 🌟 중요: 아직 실행 대기 중인(퍼지고 있는) 마커 생성 타이머들을 모두 취소!
    // 이걸 안 하면 지도를 휙휙 움직였을 때 이전 위치의 마커들이 계속 생겨남
    markerTimeouts.forEach(id => clearTimeout(id));
    markerTimeouts = [];
}

// [추가] 헤더 언어 변경
function updateTableHeader(lang) {
    if (lang === 'jp') {
        const headers = document.querySelectorAll('#tableHeader th');
        const jpHeaders = ['タイトル', '会社名', '勤務地', '給与', '連絡先', '担当者', '管理'];
        headers.forEach((th, idx) => { if(jpHeaders[idx]) th.innerText = jpHeaders[idx]; });
    }
}

// 🌟 [NEW] 카드 열기 함수 & 자세히 보기 이벤트 연결
function openJobCard(job) {
    const card = document.getElementById('jobDetailCard');

    // 1. 데이터 채워넣기 (기존 코드 유지)
    document.getElementById('card-company').innerText = job.companyName || '회사명 미정';
    document.getElementById('card-manager').innerText = job.manager || '담당자';

    // 이미지 에러 처리 포함
    const imgEl = document.getElementById('card-img');
    imgEl.src = job.thumbnailUrl || 'https://via.placeholder.com/300';
    imgEl.onerror = function() { this.src='https://via.placeholder.com/300?text=No+Image'; };

    document.getElementById('card-title').innerText = job.title;
    document.getElementById('card-address').innerText = job.address;
    document.getElementById('card-phone').innerText = job.contactPhone || '-';

    // 🌟 [핵심 수정] 자세히 보기 버튼 클릭 이벤트 연결
    const detailBtn = document.getElementById('btn-detail');

    detailBtn.onclick = function() {
        // 현재 언어 설정 가져오기 (없으면 'kr')
        const currentLang = new URLSearchParams(window.location.search).get('lang') || 'kr';

        // 컨트롤러에 맞는 URL 생성 (/map/jobs/detail?id=...&source=...&lang=...)
        // job.source가 DTO에 있으므로 반드시 넣어줘야 합니다!
        const targetUrl = `/map/jobs/detail?id=${job.id}&source=${job.source}&lang=${currentLang}`;

        // 페이지 이동 (새 창을 원하면 window.open(targetUrl) 사용)
        window.location.href = targetUrl;
    };

    // 2. 카드 보여주기 & 바텀 시트 내리기
    card.style.display = 'block';
    $('#bottomSheet').removeClass('active');
}

// 🌟 [NEW] 카드 닫기 함수
function closeJobCard() {
    document.getElementById('jobDetailCard').style.display = 'none';
}



/* ======================================================================= 
*                           좌표 관련 오류 발생시 로그 처리
* 
* 
* 
* // (26/2/6) 프로젝트 구조 변경으로 인한 마커 미출력 문제로 코드 검토중
            console.log("서버에서 받은 데이터: ",data);

            if (data.length > 0){
                console.log("첫번째 데이터 샘플:", data[0]);
                console.log("JS가 찾는 좌표:", data[0].lat, data[0].lng);
            } 
            * 위 내용을 fetch 내부에 삽입후 실행하면 데이터가 출력됨
* */