// 지도 생성
let map;
let jobMarkers = [];

// Jquery를 사용하면 오히려 번거롭기 때문에 Vanilla JS를 사용하여 map 구축
function initMap() {
    const mapElement = document.querySelector('.map-container');
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

    map.addListener("idle", () => {
        const bounds = map.getBounds();
        loadJobs(bounds);
    })
}

// HTML 문서가 다 로딩되면 실행 (jQuery의 ready 함수) <- Jquery 사용
$(function() {
    // 1. '.sheet-handle' 클래스를 가진 요소를 클릭하면
    $('.sheet-handle').on('click', function() {

        // 2. '#bottomSheet' 아이디를 가진 요소에 'active' 클래스를 줬다 뺏었다 함
        $('#bottomSheet').toggleClass('active');

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

    // 2. API 호출
    fetch(`/map/api/jobs?${params.toString()}`)
        .then(res => res.json())
        .then(data => {
            // 🌟 [2] 데이터가 도착하면 기존 마커 지우고 -> 리스트 그리기 -> 새 마커 찍기
            clearMarkers();      // 1. 지도 청소
            renderList(data, currentLang); // 2. 바텀시트 리스트 갱신
            renderMarkers(data); // 3. 지도에 마커 꽂기 (NEW!)
        })
        .catch(err => {
            console.error(err);
            listBody.innerHTML = `<tr><td colspan="7" class="msg-box">데이터 로딩 실패</td></tr>`;
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
                    <button class="btn btn-view" onclick="window.open('/jobs/${job.id}')">상세</button>
                 </div>
            </td>
        </tr>
        `;
    });
    tbody.innerHTML = html;
}

// 🌟 [3] 마커 렌더링 함수 (새로 추가됨)
function renderMarkers(jobs) {
    if (!jobs || jobs.length === 0) return;

    jobs.forEach(job => {
        // DTO에 있는 lat, lng 확인 (null 체크)
        if (job.lat && job.lng) {

            const marker = new google.maps.Marker({
                position: { lat: job.lat, lng: job.lng },
                map: map,
                title: job.title, // 마우스 올리면 나오는 툴팁
                animation: google.maps.Animation.DROP // 툭 떨어지는 애니메이션
            });

            // 마커 클릭 이벤트 (선택사항)
            // 클릭하면 해당 공고 상세페이지를 새 창으로 띄움
            marker.addListener("click", () => {
                window.open(`/jobs/${job.id}`);
            });

            // 배열에 저장 (나중에 지우기 위해)
            jobMarkers.push(marker);
        }
    });
}

// 🌟 [4] 마커 삭제 함수 (새로 추가됨)
function clearMarkers() {
    // 지도에서 제거
    jobMarkers.forEach(marker => {
        marker.setMap(null);
    });
    // 배열 비우기
    jobMarkers = [];
}

// [추가] 헤더 언어 변경
function updateTableHeader(lang) {
    if (lang === 'jp') {
        const headers = document.querySelectorAll('#tableHeader th');
        const jpHeaders = ['タイトル', '会社名', '勤務地', '給与', '連絡先', '担当者', '管理'];
        headers.forEach((th, idx) => { if(jpHeaders[idx]) th.innerText = jpHeaders[idx]; });
    }
}