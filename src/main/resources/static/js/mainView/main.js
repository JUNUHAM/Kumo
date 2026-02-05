// 지도 생성
let map;

// Jquery를 사용하면 오히려 번거롭기 때문에 Vanilla JS를 사용하여 map 구축
function initMap() {
    const mapElement = document.querySelector('.map-container');
    if (!mapElement) return;

    const tokyo = { lat: 35.6804, lng: 139.7690 };

    map = new google.maps.Map(mapElement, {
        center: tokyo,
        zoom: 14,
    });

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
        .then(data => renderList(data, currentLang))
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

// [추가] 헤더 언어 변경
function updateTableHeader(lang) {
    if (lang === 'jp') {
        const headers = document.querySelectorAll('#tableHeader th');
        const jpHeaders = ['タイトル', '会社名', '勤務地', '給与', '連絡先', '担当者', '管理'];
        headers.forEach((th, idx) => { if(jpHeaders[idx]) th.innerText = jpHeaders[idx]; });
    }
}